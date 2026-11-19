package com.example.data

import com.example.BuildConfig
import com.example.model.AnnouncementItem
import com.example.model.AnnouncementPriority
import com.example.model.AnnouncementTargetRole
import com.example.model.CleaningAttendanceMark
import com.example.model.CleaningBuilding
import com.example.model.ClientBranch
import com.example.model.CorporateClientAccount
import com.example.model.MasterServiceCatalogItem
import com.example.model.OperationalTask
import com.example.model.ServiceHistoryItem
import com.example.model.StaffCollaborator
import com.example.model.TaskOperationStatus
import com.example.model.UserAccount
import com.example.model.UserRole
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Sincronización bidireccional directa entre la app Android y las tablas relacionales
 * de Supabase compartidas con el panel web en Vercel (clients, sedes, work_orders,
 * profiles, service_catalog, announcements, cleaning_logs).
 */
object SupabaseCloudSync {

    private const val TABLE_SYNC = "todoservicio_sync_state"
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private val syncScheduler = Executors.newSingleThreadScheduledExecutor()
    private var liveSyncTask: ScheduledFuture<*>? = null

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authAdapter = moshi.adapter(PersistedAuthSnapshot::class.java)
    private val operationalAdapter = moshi.adapter(PersistedOperationalSnapshot::class.java)
    private val cleaningAdapter = moshi.adapter(PersistedCleaningSnapshot::class.java)

    private val mapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        Any::class.java
    )
    private val mapAdapter = moshi.adapter>(mapType)

    private val listMapType = Types.newParameterizedType(
        List::class.java,
        mapType
    )
    private val listMapAdapter = moshi.adapter<List>>(listMapType)

    fun isConfigured(): Boolean {
        val url = BuildConfig.SUPABASE_URL.trim()
        val key = BuildConfig.SUPABASE_ANON_KEY.trim()
        return url.startsWith("https://") && key.length > 20
    }

    private fun baseUrl(): String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    private fun apiKey(): String = BuildConfig.SUPABASE_ANON_KEY.trim()

    private fun isoNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun ensureUuid(rawId: String): String {
        return try {
            UUID.fromString(rawId).toString()
        } catch (_: Exception) {
            UUID.nameUUIDFromBytes(rawId.toByteArray(Charsets.UTF_8)).toString()
        }
    }

    private fun mapStatusToWeb(status: TaskOperationStatus): String = when (status) {
        TaskOperationStatus.ABIERTA -> "programada"
        TaskOperationStatus.INICIO_EJECUCION -> "en_curso"
        TaskOperationStatus.FINALIZADA -> "finalizada"
    }

    private fun mapStatusFromWeb(raw: String?): TaskOperationStatus {
        val norm = raw.orEmpty().trim().lowercase(Locale.ROOT)
        return when {
            norm.contains("final") || norm.contains("complet") || norm.contains("cerrad") ->
                TaskOperationStatus.FINALIZADA
            norm.contains("curso") || norm.contains("ejecu") || norm.contains("progres") || norm.contains("camino") ->
                TaskOperationStatus.INICIO_EJECUCION
            else -> TaskOperationStatus.ABIERTA
        }
    }

    private fun mapRoleToWeb(role: UserRole): String = when (role) {
        UserRole.SUPERADMIN -> "superadmin"
        UserRole.ADMIN -> "admin"
        UserRole.TECNICO -> "tecnico"
        UserRole.LIMPIEZA -> "limpieza"
        UserRole.CLIENTE -> "cliente"
    }

    private fun mapRoleFromWeb(raw: String?): UserRole {
        val norm = raw.orEmpty().trim().lowercase(Locale.ROOT)
        return when {
            norm.contains("super") -> UserRole.SUPERADMIN
            norm.contains("admin") -> UserRole.ADMIN
            norm.contains("tecn") || norm.contains("tech") -> UserRole.TECNICO
            norm.contains("limp") || norm.contains("clean") -> UserRole.LIMPIEZA
            else -> UserRole.CLIENTE
        }
    }

    private fun mapPriorityToWeb(priority: AnnouncementPriority): String = when (priority) {
        AnnouncementPriority.IMPORTANTE -> "importante"
        AnnouncementPriority.MEDIO -> "medio"
        AnnouncementPriority.BAJO -> "bajo"
    }

    private fun mapPriorityFromWeb(raw: String?): AnnouncementPriority {
        val norm = raw.orEmpty().trim().lowercase(Locale.ROOT)
        return when {
            norm.contains("import") || norm.contains("alta") || norm.contains("high") ->
                AnnouncementPriority.IMPORTANTE
            norm.contains("baj") || norm.contains("low") ->
                AnnouncementPriority.BAJO
            else -> AnnouncementPriority.MEDIO
        }
    }

    private fun httpGetList(pathWithQuery: String): List>? {
        if (!isConfigured()) return null
        return try {
            val url = URL("${baseUrl()}/rest/v1/$pathWithQuery")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("apikey", apiKey())
                setRequestProperty("Authorization", "Bearer ${apiKey()}")
                setRequestProperty("Accept", "application/json")
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream?.use { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText() }.orEmpty()
            conn.disconnect()
            if (code !in 200..299 || body.isBlank()) return null
            listMapAdapter.fromJson(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun httpUpsertRows(table: String, rows: List>, conflictCol: String = "id"): Boolean {
        if (!isConfigured() || rows.isEmpty()) return false
        return try {
            val payload = listMapAdapter.toJson(rows)
            val url = URL("${baseUrl()}/rest/v1/$table?on_conflict=$conflictCol")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("apikey", apiKey())
                setRequestProperty("Authorization", "Bearer ${apiKey()}")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Prefer", "resolution=merge-duplicates,return=minimal")
            }
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload)
                writer.flush()
            }
            val code = conn.responseCode
            conn.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }

    private fun pullSyncStatePayload(storeKey: String): String? {
        val rows = httpGetList("$TABLE_SYNC?store_key=eq.$storeKey&select=payload&limit=1") ?: return null
        val payloadObj = rows.firstOrNull()?.get("payload") ?: return null
        return when (payloadObj) {
            is String -> payloadObj
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                mapAdapter.toJson(payloadObj as Map<String, Any?>)
            }
            else -> null
        }
    }

    private fun pushSyncStatePayload(storeKey: String, jsonState: String) {
        val parsedMap = try {
            mapAdapter.fromJson(jsonState)
        } catch (_: Exception) {
            null
        } ?: return

        httpUpsertRows(
            table = TABLE_SYNC,
            rows = listOf(
                mapOf(
                    "store_key" to storeKey,
                    "payload" to parsedMap,
                    "updated_at" to isoNow()
                )
            ),
            conflictCol = "store_key"
        )
    }

    /**
     * Descarga el estado desde las tablas reales de la web en Supabase (clients, sedes,
     * work_orders, profiles, service_catalog, announcements, cleaning_logs) y lo fusiona
     * con los datos locales para la app Android.
     */
    fun pullStateJson(storeKey: String): String? {
        if (!isConfigured()) return null
        val future = ioExecutor.submit(Callable<String?> {
            when (storeKey) {
                "operational_snapshot" -> buildOperationalFromWebTables()
                "auth_snapshot" -> buildAuthFromWebTables()
                "cleaning_snapshot" -> buildCleaningFromWebTables()
                else -> pullSyncStatePayload(storeKey)
            }
        })
        return try {
            future.get(7, TimeUnit.SECONDS)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Publica los cambios locales tanto hacia las tablas relacionales de la web en Vercel
     * como hacia todoservicio_sync_state.
     */
    fun pushStateJson(storeKey: String, jsonState: String) {
        if (!isConfigured() || jsonState.isBlank()) return
        ioExecutor.execute {
            try {
                pushSyncStatePayload(storeKey, jsonState)
                when (storeKey) {
                    "operational_snapshot" -> pushOperationalToWebTables(jsonState)
                    "auth_snapshot" -> pushAuthToWebTables(jsonState)
                    "cleaning_snapshot" -> pushCleaningToWebTables(jsonState)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun buildOperationalFromWebTables(): String? {
        val fallbackJson = pullSyncStatePayload("operational_snapshot")
        val baseSnapshot = if (!fallbackJson.isNullOrBlank()) {
            try { operationalAdapter.fromJson(fallbackJson) } catch (_: Exception) { null }
        } else null

        val webClients = httpGetList("clients?select=*&order=created_at.asc") ?: emptyList()
        val webSedes = httpGetList("sedes?select=*") ?: emptyList()
        val webOrders = httpGetList("work_orders?select=*&order=created_at.desc") ?: emptyList()
        val webCatalog = httpGetList("service_catalog?select=*") ?: emptyList()
        val webAnnouncements = httpGetList("announcements?select=*&order=created_at.desc") ?: emptyList()
        val webProfiles = httpGetList("profiles?select=id,full_name,username") ?: emptyList()

        if (webClients.isEmpty() && webOrders.isEmpty() && webCatalog.isEmpty() && webAnnouncements.isEmpty()) {
            return fallbackJson
        }

        val profileNameById = webProfiles.associate { row ->
            val id = row["id"]?.toString().orEmpty()
            val name = row["full_name"]?.toString()?.takeIf { it.isNotBlank() }
                ?: row["username"]?.toString().orEmpty()
            id to name
        }

        val sedesByClient = webSedes.groupBy { it["client_id"]?.toString().orEmpty() }
        val sedeById = webSedes.associateBy { it["id"]?.toString().orEmpty() }

        val mappedClients = if (webClients.isNotEmpty()) {
            webClients.map { row ->
                val cid = row["id"]?.toString().orEmpty()
                val cname = row["name"]?.toString().orEmpty()
                val visits = (row["plan_visits"] as? Number)?.toInt() ?: 8
                val logo = row["logo_url"]?.toString()
                val branches = sedesByClient[cid].orEmpty().map { s ->
                    ClientBranch(
                        id = s["id"]?.toString().orEmpty(),
                        name = s["name"]?.toString().orEmpty(),
                        address = s["address"]?.toString().orEmpty(),
                        contactPerson = s["contact_name"]?.toString().orEmpty(),
                        phone = s["contact_phone"]?.toString().orEmpty(),
                        lat = (s["lat"] as? Number)?.toDouble() ?: -34.9011,
                        lng = (s["lng"] as? Number)?.toDouble() ?: -56.1645,
                        geofenceRadiusMeters = (s["geofence_radius_meters"] as? Number)?.toInt() ?: 100
                    )
                }
                CorporateClientAccount(
                    id = cid,
                    name = cname,
                    rut = "",
                    planName = "Plan Corporativo ($visits Visitas)",
                    clientColorHex = 0xFF1D4E89,
                    branches = branches,
                    logoUri = logo
                )
            }
        } else {
            baseSnapshot?.corporateClients.orEmpty()
        }

        val clientNameById = mappedClients.associate { it.id to it.name }

        val mappedTasks = mutableListOf<OperationalTask>()
        val mappedHistory = mutableListOf<ServiceHistoryItem>()

        webOrders.forEachIndexed { idx, row ->
            val oid = row["id"]?.toString().orEmpty()
            val otCode = row["ot_number"]?.toString()?.takeIf { it.isNotBlank() }
                ?: "OT-${(1000 + idx)}"
            val cid = row["client_id"]?.toString().orEmpty()
            val sid = row["sede_id"]?.toString().orEmpty()
            val sedeRow = sedeById[sid]
            val clientName = clientNameById[cid] ?: "Cliente Corporativo"
            val sedeName = sedeRow?.get("name")?.toString().orEmpty()
            val address = sedeRow?.get("address")?.toString().orEmpty()
            val serviceType = row["service_type"]?.toString()?.takeIf { it.isNotBlank() }
                ?: "Mantenimiento General"
            val schedDate = row["scheduled_date"]?.toString().orEmpty()
            val schedTime = row["scheduled_time"]?.toString()?.takeIf { it.isNotBlank() }
                ?: "09:00 hs"
            val status = mapStatusFromWeb(row["status"]?.toString())
            val adminComments = row["admin_comments"]?.toString().orEmpty()
            val techComments = row["tech_comments"]?.toString().orEmpty()
            val checkIn = row["check_in_at"]?.toString()
            val checkOut = row["check_out_at"]?.toString()
            val rating = (row["client_rating"] as? Number)?.toInt()
            val feedback = row["client_feedback"]?.toString().orEmpty()

            @Suppress("UNCHECKED_CAST")
            val techIds = (row["assigned_tech_ids"] as? List<Any?>)?.mapNotNull { it?.toString() } ?: emptyList()
            val techNames = techIds.mapNotNull { profileNameById[it]?.takeIf { n -> n.isNotBlank() } }
            val mainTechName = techNames.firstOrNull() ?: "Equipo Técnico"

            val dayNum = schedDate.split("-").lastOrNull()?.toIntOrNull() ?: 1

            mappedTasks.add(
                OperationalTask(
                    id = oid,
                    code = otCode,
                    title = serviceType,
                    clientId = cid,
                    clientName = clientName,
                    sedeId = sid,
                    sedeName = sedeName,
                    address = address,
                    technicianName = mainTechName,
                    assignedTechIds = techIds,
                    scheduledDate = schedDate,
                    date = schedDate,
                    dayOfMonth = dayNum,
                    timeSlot = schedTime,
                    time = schedTime,
                    status = status,
                    checkInTime = checkIn,
                    checkOutTime = checkOut,
                    category = serviceType,
                    description = adminComments,
                    comments = techComments,
                    assignedTechnicians = techNames,
                    clientRatingStars = rating,
                    clientRatingComment = feedback
                )
            )

            if (status == TaskOperationStatus.FINALIZADA) {
                @Suppress("UNCHECKED_CAST")
                val beforePhotos = (row["photos_before"] as? List<Any?>)?.mapNotNull { it?.toString() } ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val afterPhotos = (row["photos_after"] as? List<Any?>)?.mapNotNull { it?.toString() } ?: emptyList()
                mappedHistory.add(
                    ServiceHistoryItem(
                        id = oid,
                        serviceCode = otCode,
                        title = serviceType,
                        category = serviceType,
                        clientId = cid,
                        companyName = clientName,
                        sedeName = sedeName,
                        address = address,
                        executionDate = schedDate,
                        executionTime = schedTime,
                        leadTechnician = mainTechName,
                        summary = adminComments.ifBlank { serviceType },
                        technicianComments = techComments,
                        evidencePhotos = beforePhotos + afterPhotos,
                        checkInTime = checkIn.orEmpty(),
                        checkOutTime = checkOut.orEmpty(),
                        clientRatingStars = rating,
                        clientRatingComment = feedback
                    )
                )
            }
        }

        val finalTasks = if (webOrders.isNotEmpty()) mappedTasks else baseSnapshot?.operationalTasks.orEmpty()
        val finalHistory = if (mappedHistory.isNotEmpty()) mappedHistory else baseSnapshot?.serviceReports.orEmpty()

        val mappedCatalog = if (webCatalog.isNotEmpty()) {
            webCatalog.mapIndexed { idx, row ->
                val sid = row["id"]?.toString().orEmpty()
                val title = row["title"]?.toString().orEmpty()
                MasterServiceCatalogItem(
                    id = sid,
                    code = "SRV-${(idx + 1).toString().padStart(2, '0')}",
                    name = title,
                    category = "Mantenimiento Integral",
                    description = title,
                    estimatedDurationHours = 2.0,
                    dec406Compliant = true,
                    basePriceUsd = 0.0
                )
            }
        } else {
            baseSnapshot?.masterServices.orEmpty()
        }

        val mappedAnnouncements = if (webAnnouncements.isNotEmpty()) {
            webAnnouncements.map { row ->
                @Suppress("UNCHECKED_CAST")
                val rawRoles = (row["target_roles"] as? List<Any?>)?.mapNotNull { it?.toString()?.lowercase(Locale.ROOT) } ?: emptyList()
                val targetRoles = mutableListOf<AnnouncementTargetRole>()
                if (rawRoles.any { it.contains("limp") }) targetRoles.add(AnnouncementTargetRole.LIMPIEZA)
                if (rawRoles.any { it.contains("tecn") }) targetRoles.add(AnnouncementTargetRole.TECNICOS)
                if (rawRoles.any { it.contains("client") }) targetRoles.add(AnnouncementTargetRole.CLIENTES)
                if (targetRoles.isEmpty()) {
                    targetRoles.addAll(listOf(AnnouncementTargetRole.TECNICOS, AnnouncementTargetRole.CLIENTES))
                }
                AnnouncementItem(
                    id = row["id"]?.toString().orEmpty(),
                    title = row["title"]?.toString().orEmpty(),
                    body = row["body"]?.toString().orEmpty(),
                    priority = mapPriorityFromWeb(row["priority"]?.toString()),
                    targetRoles = targetRoles,
                    imageUri = row["image_url"]?.toString(),
                    createdAtLabel = row["created_at"]?.toString().orEmpty()
                )
            }
        } else {
            baseSnapshot?.announcements.orEmpty()
        }

        val merged = (baseSnapshot ?: PersistedOperationalSnapshot()).copy(
            globalOrderCounter = maxOf(baseSnapshot?.globalOrderCounter ?: 0, finalTasks.size),
            corporateClients = mappedClients,
            operationalTasks = finalTasks,
            serviceReports = finalHistory,
            masterServices = mappedCatalog,
            masterCatalogInitialized = mappedCatalog.isNotEmpty() || (baseSnapshot?.masterCatalogInitialized == true),
            announcements = mappedAnnouncements
        )
        return operationalAdapter.toJson(merged)
    }

    private fun buildAuthFromWebTables(): String? {
        val fallbackJson = pullSyncStatePayload("auth_snapshot")
        val baseAuth = if (!fallbackJson.isNullOrBlank()) {
            try { authAdapter.fromJson(fallbackJson) } catch (_: Exception) { null }
        } else null

        val webProfiles = httpGetList("profiles?select=*&order=created_at.asc") ?: return fallbackJson
        if (webProfiles.isEmpty()) return fallbackJson

        val existingUsersById = baseAuth?.users.orEmpty().associateBy { it.uid }
        val existingUsersByUsername = baseAuth?.users.orEmpty().associateBy { it.username.lowercase(Locale.ROOT) }

        val mergedUsers = webProfiles.map { row ->
            val id = row["id"]?.toString().orEmpty()
            val username = row["username"]?.toString().orEmpty()
            val fullName = row["full_name"]?.toString().orEmpty()
            val role = mapRoleFromWeb(row["role"]?.toString())
            val clientId = row["client_id"]?.toString()
            val avatar = row["avatar_url"]?.toString()
            val phone = row["phone"]?.toString().orEmpty()
            val mustChange = (row["must_change_password"] as? Boolean) ?: false
            val isActive = (row["is_active"] as? Boolean) ?: true

            val existing = existingUsersById[id] ?: existingUsersByUsername[username.lowercase(Locale.ROOT)]
            UserAccount(
                uid = existing?.uid ?: id,
                username = username.ifBlank { existing?.username.orEmpty() },
                email = existing?.email ?: if (username.contains("@")) username else "$username@todoservicio.com.uy",
                fullName = fullName.ifBlank { existing?.fullName.orEmpty() },
                phone = phone.ifBlank { existing?.phone.orEmpty() },
                companyName = existing?.companyName.orEmpty(),
                role = role,
                clientId = clientId ?: existing?.clientId,
                monthlyPlanId = existing?.monthlyPlanId,
                isTemporaryPassword = mustChange,
                isActive = isActive,
                avatarUrl = avatar ?: existing?.avatarUrl
            )
        }

        val staffList = mergedUsers
            .filter { it.role == UserRole.TECNICO || it.role == UserRole.LIMPIEZA }
            .map { u ->
                StaffCollaborator(
                    id = u.uid,
                    fullName = u.fullName,
                    phone = u.phone,
                    email = u.email,
                    jobTitle = if (u.role == UserRole.LIMPIEZA) "Personal de Limpieza" else "Técnico Especializado",
                    userType = "Empleado",
                    lastCheckIn = "Sincronizado",
                    isActive = u.isActive,
                    roleCategory = if (u.role == UserRole.LIMPIEZA) "Limpieza" else "Técnico",
                    photoUri = u.avatarUrl
                )
            }

        val finalSnapshot = PersistedAuthSnapshot(
            users = if (mergedUsers.isNotEmpty()) mergedUsers else baseAuth?.users.orEmpty(),
            credentials = baseAuth?.credentials.orEmpty(),
            staffCollaborators = if (staffList.isNotEmpty()) staffList else baseAuth?.staffCollaborators.orEmpty()
        )
        return authAdapter.toJson(finalSnapshot)
    }

    private fun buildCleaningFromWebTables(): String? {
        val fallbackJson = pullSyncStatePayload("cleaning_snapshot")
        val baseCleaning = if (!fallbackJson.isNullOrBlank()) {
            try { cleaningAdapter.fromJson(fallbackJson) } catch (_: Exception) { null }
        } else null

        val webSedes = httpGetList("sedes?select=*") ?: emptyList()
        val webLogs = httpGetList("cleaning_logs?select=*&order=created_at.desc&limit=100") ?: emptyList()
        val webProfiles = httpGetList("profiles?select=id,full_name") ?: emptyList()

        if (webSedes.isEmpty() && webLogs.isEmpty()) return fallbackJson

        val profileNames = webProfiles.associate {
            it["id"]?.toString().orEmpty() to it["full_name"]?.toString().orEmpty()
        }
        val sedeMap = webSedes.associateBy { it["id"]?.toString().orEmpty() }

        val buildings = if (webSedes.isNotEmpty()) {
            webSedes.map { s ->
                CleaningBuilding(
                    id = s["id"]?.toString().orEmpty(),
                    name = s["name"]?.toString().orEmpty(),
                    address = s["address"]?.toString().orEmpty(),
                    lat = (s["lat"] as? Number)?.toDouble() ?: -34.9011,
                    lng = (s["lng"] as? Number)?.toDouble() ?: -56.1645,
                    geofenceRadiusMeters = (s["geofence_radius_meters"] as? Number)?.toInt() ?: 100
                )
            }
        } else {
            baseCleaning?.buildingsList.orEmpty()
        }

        val marks = if (webLogs.isNotEmpty()) {
            webLogs.map { log ->
                val uid = log["user_id"]?.toString().orEmpty()
                val sid = log["sede_id"]?.toString().orEmpty()
                val sede = sedeMap[sid]
                val evt = log["event_type"]?.toString().orEmpty().uppercase(Locale.ROOT)
                val markType = if (evt.startsWith("S") || evt.contains("OUT") || evt.contains("SAL")) "S" else "E"
                val createdAt = log["created_at"]?.toString().orEmpty()
                val datePart = createdAt.substringBefore("T", "")
                val timePart = createdAt.substringAfter("T", "").take(5)

                CleaningAttendanceMark(
                    id = log["id"]?.toString().orEmpty(),
                    staffId = uid,
                    staffName = profileNames[uid] ?: "Personal Limpieza",
                    buildingName = sede?.get("name")?.toString() ?: "Sede Asignada",
                    buildingAddress = sede?.get("address")?.toString().orEmpty(),
                    date = datePart,
                    markTime = timePart,
                    markType = markType,
                    lat = (log["lat"] as? Number)?.toDouble() ?: -34.9011,
                    lng = (log["lng"] as? Number)?.toDouble() ?: -56.1645,
                    isInsideGeofence = !((log["out_of_range"] as? Boolean) ?: false),
                    distanceToBuildingMeters = (log["distance_meters"] as? Number)?.toInt() ?: 0,
                    comment = log["comment"]?.toString().orEmpty(),
                    isManualEntry = (log["is_manual"] as? Boolean) ?: false
                )
            }
        } else {
            baseCleaning?.marksList.orEmpty()
        }

        val finalCleaning = (baseCleaning ?: PersistedCleaningSnapshot()).copy(
            buildingsList = buildings,
            marksList = marks
        )
        return cleaningAdapter.toJson(finalCleaning)
    }

    private fun pushOperationalToWebTables(jsonState: String) {
        val snap = try { operationalAdapter.fromJson(jsonState) } catch (_: Exception) { null } ?: return

        val clientRows = mutableListOf>()
        val sedeRows = mutableListOf>()

        snap.corporateClients.forEach { client ->
            val clientUuid = ensureUuid(client.id)
            clientRows.add(
                mapOf(
                    "id" to clientUuid,
                    "name" to client.name,
                    "logo_url" to client.logoUri,
                    "plan_visits" to 8,
                    "is_active" to true
                )
            )
            client.branches.forEach { branch ->
                sedeRows.add(
                    mapOf(
                        "id" to ensureUuid(branch.id),
                        "client_id" to clientUuid,
                        "name" to branch.name,
                        "address" to branch.address,
                        "lat" to branch.lat,
                        "lng" to branch.lng,
                        "geofence_radius_meters" to branch.geofenceRadiusMeters,
                        "contact_name" to branch.contactPerson,
                        "contact_phone" to branch.phone
                    )
                )
            }
        }
        httpUpsertRows("clients", clientRows)
        httpUpsertRows("sedes", sedeRows)

        val orderRows = snap.operationalTasks.map { task ->
            val clientUuid = if (task.clientId.isNotBlank()) ensureUuid(task.clientId) else null
            val sedeUuid = if (task.sedeId.isNotBlank()) ensureUuid(task.sedeId) else null
            val techUuids = task.assignedTechIds.map { ensureUuid(it) }
            mapOf(
                "id" to ensureUuid(task.id),
                "ot_number" to task.code,
                "client_id" to clientUuid,
                "sede_id" to sedeUuid,
                "assigned_tech_ids" to techUuids,
                "service_type" to task.title,
                "scheduled_time" to task.timeSlot,
                "status" to mapStatusToWeb(task.status),
                "admin_comments" to task.description,
                "tech_comments" to task.comments,
                "client_rating" to task.clientRatingStars,
                "client_feedback" to task.clientRatingComment
            )
        }
        httpUpsertRows("work_orders", orderRows)

        val catalogRows = snap.masterServices.map { srv ->
            mapOf(
                "id" to ensureUuid(srv.id),
                "title" to srv.name,
                "is_active" to true
            )
        }
        httpUpsertRows("service_catalog", catalogRows)

        val announcementRows = snap.announcements.map { ann ->
            val roles = ann.targetRoles.map {
                when (it) {
                    AnnouncementTargetRole.LIMPIEZA -> "limpieza"
                    AnnouncementTargetRole.TECNICOS -> "tecnico"
                    AnnouncementTargetRole.CLIENTES -> "cliente"
                }
            }
            mapOf(
                "id" to ensureUuid(ann.id),
                "title" to ann.title,
                "body" to ann.body,
                "image_url" to ann.imageUri,
                "priority" to mapPriorityToWeb(ann.priority),
                "target_roles" to roles
            )
        }
        httpUpsertRows("announcements", announcementRows)
    }

    private fun pushAuthToWebTables(jsonState: String) {
        val snap = try { authAdapter.fromJson(jsonState) } catch (_: Exception) { null } ?: return
        val profileRows = snap.users.map { user ->
            mapOf(
                "id" to ensureUuid(user.uid),
                "username" to user.username,
                "full_name" to user.fullName,
                "role" to mapRoleToWeb(user.role),
                "client_id" to user.clientId?.takeIf { it.isNotBlank() }?.let { ensureUuid(it) },
                "avatar_url" to user.avatarUrl,
                "phone" to user.phone,
                "must_change_password" to user.isTemporaryPassword,
                "is_active" to user.isActive
            )
        }
        httpUpsertRows("profiles", profileRows)
    }

    private fun pushCleaningToWebTables(jsonState: String) {
        val snap = try { cleaningAdapter.fromJson(jsonState) } catch (_: Exception) { null } ?: return
        val logRows = snap.marksList.map { mark ->
            mapOf(
                "id" to ensureUuid(mark.id),
                "user_id" to if (mark.staffId.isNotBlank()) ensureUuid(mark.staffId) else null,
                "event_type" to if (mark.markType == "S") "salida" else "entrada",
                "is_manual" to mark.isManualEntry,
                "lat" to mark.lat,
                "lng" to mark.lng,
                "distance_meters" to mark.distanceToBuildingMeters.toDouble(),
                "out_of_range" to !mark.isInsideGeofence,
                "comment" to mark.comment
            )
        }
        httpUpsertRows("cleaning_logs", logRows)
    }

    /**
     * Sondea Supabase cada 5 segundos para reflejar en vivo en el celular cualquier
     * cliente, sede, orden, perfil o aviso creado desde el panel web en Vercel.
     */
    fun startLiveSync(
        onOperationalUpdated: () -> Unit,
        onAuthUpdated: () -> Unit,
        onCleaningUpdated: () -> Unit
    ) {
        if (!isConfigured()) return
        liveSyncTask?.cancel(false)
        var lastOpHash = ""
        var lastAuthHash = ""
        var lastCleanHash = ""

        liveSyncTask = syncScheduler.scheduleWithFixedDelay({
            try {
                val opJson = buildOperationalFromWebTables()
                if (!opJson.isNullOrBlank()) {
                    val h = opJson.hashCode().toString()
                    if (h != lastOpHash) {
                        lastOpHash = h
                        onOperationalUpdated()
                    }
                }

                val authJson = buildAuthFromWebTables()
                if (!authJson.isNullOrBlank()) {
                    val h = authJson.hashCode().toString()
                    if (h != lastAuthHash) {
                        lastAuthHash = h
                        onAuthUpdated()
                    }
                }

                val cleanJson = buildCleaningFromWebTables()
                if (!cleanJson.isNullOrBlank()) {
                    val h = cleanJson.hashCode().toString()
                    if (h != lastCleanHash) {
                        lastCleanHash = h
                        onCleaningUpdated()
                    }
                }
            } catch (_: Exception) {
            }
        }, 2, 5, TimeUnit.SECONDS)
    }
}
