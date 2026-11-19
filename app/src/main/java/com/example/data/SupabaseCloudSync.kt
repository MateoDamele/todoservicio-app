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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject
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

    private fun mapStatusFromWeb(raw: String): TaskOperationStatus {
        val norm = raw.trim().lowercase(Locale.ROOT)
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

    private fun mapRoleFromWeb(raw: String): UserRole {
        val norm = raw.trim().lowercase(Locale.ROOT)
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

    private fun mapPriorityFromWeb(raw: String): AnnouncementPriority {
        val norm = raw.trim().lowercase(Locale.ROOT)
        return when {
            norm.contains("import") || norm.contains("alta") || norm.contains("high") ->
                AnnouncementPriority.IMPORTANTE
            norm.contains("baj") || norm.contains("low") ->
                AnnouncementPriority.BAJO
            else -> AnnouncementPriority.MEDIO
        }
    }

    private fun JSONObject.optCleanString(key: String, fallback: String = ""): String {
        if (!has(key) || isNull(key)) return fallback
        val v = optString(key, fallback)
        return if (v == "null") fallback else v
    }

    private fun JSONObject.optStringList(key: String): List<String> {
        val arr = optJSONArray(key) ?: return emptyList()
        val result = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val item = arr.optString(i, "")
            if (item.isNotBlank() && item != "null") result.add(item)
        }
        return result
    }

    private fun httpGetArray(pathWithQuery: String): JSONArray? {
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
            JSONArray(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun httpUpsertArray(table: String, rows: JSONArray, conflictCol: String = "id"): Boolean {
        if (!isConfigured() || rows.length() == 0) return false
        return try {
            val payload = rows.toString()
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
        val arr = httpGetArray("$TABLE_SYNC?store_key=eq.$storeKey&select=payload&limit=1") ?: return null
        if (arr.length() == 0) return null
        val row = arr.optJSONObject(0) ?: return null
        val payloadObj = row.opt("payload") ?: return null
        return when (payloadObj) {
            is JSONObject -> payloadObj.toString()
            is String -> payloadObj
            else -> null
        }
    }

    private fun pushSyncStatePayload(storeKey: String, jsonState: String) {
        try {
            val parsedObj = JSONObject(jsonState)
            val row = JSONObject().apply {
                put("store_key", storeKey)
                put("payload", parsedObj)
                put("updated_at", isoNow())
            }
            val arr = JSONArray().apply { put(row) }
            httpUpsertArray(TABLE_SYNC, arr, "store_key")
        } catch (_: Exception) {
        }
    }

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

        val webClients = httpGetArray("clients?select=*&order=created_at.asc") ?: JSONArray()
        val webSedes = httpGetArray("sedes?select=*") ?: JSONArray()
        val webOrders = httpGetArray("work_orders?select=*&order=created_at.desc") ?: JSONArray()
        val webCatalog = httpGetArray("service_catalog?select=*") ?: JSONArray()
        val webAnnouncements = httpGetArray("announcements?select=*&order=created_at.desc") ?: JSONArray()
        val webProfiles = httpGetArray("profiles?select=id,full_name,username") ?: JSONArray()

        if (webClients.length() == 0 && webOrders.length() == 0 &&
            webCatalog.length() == 0 && webAnnouncements.length() == 0
        ) {
            return fallbackJson
        }

        val profileNameById = HashMap<String, String>()
        for (i in 0 until webProfiles.length()) {
            val row = webProfiles.optJSONObject(i) ?: continue
            val id = row.optCleanString("id")
            val name = row.optCleanString("full_name").ifBlank { row.optCleanString("username") }
            if (id.isNotBlank()) profileNameById[id] = name
        }

        val sedesByClient = HashMap<String, MutableList<ClientBranch>>()
        val sedeById = HashMap<String, JSONObject>()
        for (i in 0 until webSedes.length()) {
            val s = webSedes.optJSONObject(i) ?: continue
            val sid = s.optCleanString("id")
            val cid = s.optCleanString("client_id")
            if (sid.isNotBlank()) sedeById[sid] = s
            val branch = ClientBranch(
                id = sid,
                name = s.optCleanString("name"),
                address = s.optCleanString("address"),
                contactPerson = s.optCleanString("contact_name"),
                phone = s.optCleanString("contact_phone"),
                lat = s.optDouble("lat", -34.9011),
                lng = s.optDouble("lng", -56.1645),
                geofenceRadiusMeters = s.optInt("geofence_radius_meters", 100)
            )
            sedesByClient.getOrPut(cid) { mutableListOf() }.add(branch)
        }

        val mappedClients = if (webClients.length() > 0) {
            val list = ArrayList<CorporateClientAccount>(webClients.length())
            for (i in 0 until webClients.length()) {
                val row = webClients.optJSONObject(i) ?: continue
                val cid = row.optCleanString("id")
                val cname = row.optCleanString("name")
                val visits = row.optInt("plan_visits", 8)
                val logo = row.optCleanString("logo_url").ifBlank { null }
                list.add(
                    CorporateClientAccount(
                        id = cid,
                        name = cname,
                        rut = "",
                        planName = "Plan Corporativo ($visits Visitas)",
                        clientColorHex = 0xFF1D4E89,
                        branches = sedesByClient[cid].orEmpty(),
                        logoUri = logo
                    )
                )
            }
            list
        } else {
            baseSnapshot?.corporateClients.orEmpty()
        }

        val clientNameById = mappedClients.associate { it.id to it.name }
        val mappedTasks = mutableListOf<OperationalTask>()
        val mappedHistory = mutableListOf<ServiceHistoryItem>()

        for (idx in 0 until webOrders.length()) {
            val row = webOrders.optJSONObject(idx) ?: continue
            val oid = row.optCleanString("id")
            val otCode = row.optCleanString("ot_number").ifBlank { "OT-${1000 + idx}" }
            val cid = row.optCleanString("client_id")
            val sid = row.optCleanString("sede_id")
            val sedeRow = sedeById[sid]
            val clientName = clientNameById[cid] ?: "Cliente Corporativo"
            val sedeName = sedeRow?.optCleanString("name").orEmpty()
            val address = sedeRow?.optCleanString("address").orEmpty()
            val serviceType = row.optCleanString("service_type").ifBlank { "Mantenimiento General" }
            val schedDate = row.optCleanString("scheduled_date")
            val schedTime = row.optCleanString("scheduled_time").ifBlank { "09:00 hs" }
            val status = mapStatusFromWeb(row.optCleanString("status"))
            val adminComments = row.optCleanString("admin_comments")
            val techComments = row.optCleanString("tech_comments")
            val checkIn = row.optCleanString("check_in_at").ifBlank { null }
            val checkOut = row.optCleanString("check_out_at").ifBlank { null }
            val rating = if (row.has("client_rating") && !row.isNull("client_rating")) row.optInt("client_rating") else null
            val feedback = row.optCleanString("client_feedback")

            val techIds = row.optStringList("assigned_tech_ids")
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
                val beforePhotos = row.optStringList("photos_before")
                val afterPhotos = row.optStringList("photos_after")
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

        val finalTasks = if (webOrders.length() > 0) mappedTasks else baseSnapshot?.operationalTasks.orEmpty()
        val finalHistory = if (mappedHistory.isNotEmpty()) mappedHistory else baseSnapshot?.serviceReports.orEmpty()

        val mappedCatalog = if (webCatalog.length() > 0) {
            val list = ArrayList<MasterServiceCatalogItem>(webCatalog.length())
            for (idx in 0 until webCatalog.length()) {
                val row = webCatalog.optJSONObject(idx) ?: continue
                val sid = row.optCleanString("id")
                val title = row.optCleanString("title")
                list.add(
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
                )
            }
            list
        } else {
            baseSnapshot?.masterServices.orEmpty()
        }

        val mappedAnnouncements = if (webAnnouncements.length() > 0) {
            val list = ArrayList<AnnouncementItem>(webAnnouncements.length())
            for (i in 0 until webAnnouncements.length()) {
                val row = webAnnouncements.optJSONObject(i) ?: continue
                val rawRoles = row.optStringList("target_roles").map { it.lowercase(Locale.ROOT) }
                val targetRoles = mutableListOf<AnnouncementTargetRole>()
                if (rawRoles.any { it.contains("limp") }) targetRoles.add(AnnouncementTargetRole.LIMPIEZA)
                if (rawRoles.any { it.contains("tecn") }) targetRoles.add(AnnouncementTargetRole.TECNICOS)
                if (rawRoles.any { it.contains("client") }) targetRoles.add(AnnouncementTargetRole.CLIENTES)
                if (targetRoles.isEmpty()) {
                    targetRoles.addAll(listOf(AnnouncementTargetRole.TECNICOS, AnnouncementTargetRole.CLIENTES))
                }
                list.add(
                    AnnouncementItem(
                        id = row.optCleanString("id"),
                        title = row.optCleanString("title"),
                        body = row.optCleanString("body"),
                        priority = mapPriorityFromWeb(row.optCleanString("priority")),
                        targetRoles = targetRoles,
                        imageUri = row.optCleanString("image_url").ifBlank { null },
                        createdAtLabel = row.optCleanString("created_at")
                    )
                )
            }
            list
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

        val webProfiles = httpGetArray("profiles?select=*&order=created_at.asc") ?: return fallbackJson
        if (webProfiles.length() == 0) return fallbackJson

        val existingUsersById = baseAuth?.users.orEmpty().associateBy { it.uid }
        val existingUsersByUsername = baseAuth?.users.orEmpty().associateBy { it.username.lowercase(Locale.ROOT) }

        val mergedUsers = ArrayList<UserAccount>(webProfiles.length())
        for (i in 0 until webProfiles.length()) {
            val row = webProfiles.optJSONObject(i) ?: continue
            val id = row.optCleanString("id")
            val username = row.optCleanString("username")
            val fullName = row.optCleanString("full_name")
            val role = mapRoleFromWeb(row.optCleanString("role"))
            val clientId = row.optCleanString("client_id").ifBlank { null }
            val avatar = row.optCleanString("avatar_url").ifBlank { null }
            val phone = row.optCleanString("phone")
            val mustChange = row.optBoolean("must_change_password", false)
            val isActive = row.optBoolean("is_active", true)

            val existing = existingUsersById[id] ?: existingUsersByUsername[username.lowercase(Locale.ROOT)]
            mergedUsers.add(
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

        val webSedes = httpGetArray("sedes?select=*") ?: JSONArray()
        val webLogs = httpGetArray("cleaning_logs?select=*&order=created_at.desc&limit=100") ?: JSONArray()
        val webProfiles = httpGetArray("profiles?select=id,full_name") ?: JSONArray()

        if (webSedes.length() == 0 && webLogs.length() == 0) return fallbackJson

        val profileNames = HashMap<String, String>()
        for (i in 0 until webProfiles.length()) {
            val row = webProfiles.optJSONObject(i) ?: continue
            profileNames[row.optCleanString("id")] = row.optCleanString("full_name")
        }

        val sedeMap = HashMap<String, JSONObject>()
        val buildings = if (webSedes.length() > 0) {
            val list = ArrayList<CleaningBuilding>(webSedes.length())
            for (i in 0 until webSedes.length()) {
                val s = webSedes.optJSONObject(i) ?: continue
                val sid = s.optCleanString("id")
                if (sid.isNotBlank()) sedeMap[sid] = s
                list.add(
                    CleaningBuilding(
                        id = sid,
                        name = s.optCleanString("name"),
                        address = s.optCleanString("address"),
                        lat = s.optDouble("lat", -34.9011),
                        lng = s.optDouble("lng", -56.1645),
                        geofenceRadiusMeters = s.optInt("geofence_radius_meters", 100)
                    )
                )
            }
            list
        } else {
            baseCleaning?.buildingsList.orEmpty()
        }

        val marks = if (webLogs.length() > 0) {
            val list = ArrayList<CleaningAttendanceMark>(webLogs.length())
            for (i in 0 until webLogs.length()) {
                val log = webLogs.optJSONObject(i) ?: continue
                val uid = log.optCleanString("user_id")
                val sid = log.optCleanString("sede_id")
                val sede = sedeMap[sid]
                val evt = log.optCleanString("event_type").uppercase(Locale.ROOT)
                val markType = if (evt.startsWith("S") || evt.contains("OUT") || evt.contains("SAL")) "S" else "E"
                val createdAt = log.optCleanString("created_at")
                val datePart = createdAt.substringBefore("T", "")
                val timePart = createdAt.substringAfter("T", "").take(5)

                list.add(
                    CleaningAttendanceMark(
                        id = log.optCleanString("id"),
                        staffId = uid,
                        staffName = profileNames[uid] ?: "Personal Limpieza",
                        buildingName = sede?.optCleanString("name") ?: "Sede Asignada",
                        buildingAddress = sede?.optCleanString("address").orEmpty(),
                        date = datePart,
                        markTime = timePart,
                        markType = markType,
                        lat = log.optDouble("lat", -34.9011),
                        lng = log.optDouble("lng", -56.1645),
                        isInsideGeofence = !log.optBoolean("out_of_range", false),
                        distanceToBuildingMeters = log.optDouble("distance_meters", 0.0).toInt(),
                        comment = log.optCleanString("comment"),
                        isManualEntry = log.optBoolean("is_manual", false)
                    )
                )
            }
            list
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

        val clientArr = JSONArray()
        val sedeArr = JSONArray()

        snap.corporateClients.forEach { client ->
            val clientUuid = ensureUuid(client.id)
            clientArr.put(
                JSONObject().apply {
                    put("id", clientUuid)
                    put("name", client.name)
                    put("logo_url", client.logoUri ?: JSONObject.NULL)
                    put("plan_visits", 8)
                    put("is_active", true)
                }
            )
            client.branches.forEach { branch ->
                sedeArr.put(
                    JSONObject().apply {
                        put("id", ensureUuid(branch.id))
                        put("client_id", clientUuid)
                        put("name", branch.name)
                        put("address", branch.address)
                        put("lat", branch.lat)
                        put("lng", branch.lng)
                        put("geofence_radius_meters", branch.geofenceRadiusMeters)
                        put("contact_name", branch.contactPerson)
                        put("contact_phone", branch.phone)
                    }
                )
            }
        }
        httpUpsertArray("clients", clientArr)
        httpUpsertArray("sedes", sedeArr)

        val orderArr = JSONArray()
        snap.operationalTasks.forEach { task ->
            val techArr = JSONArray()
            task.assignedTechIds.forEach { techArr.put(ensureUuid(it)) }
            orderArr.put(
                JSONObject().apply {
                    put("id", ensureUuid(task.id))
                    put("ot_number", task.code)
                    put("client_id", if (task.clientId.isNotBlank()) ensureUuid(task.clientId) else JSONObject.NULL)
                    put("sede_id", if (task.sedeId.isNotBlank()) ensureUuid(task.sedeId) else JSONObject.NULL)
                    put("assigned_tech_ids", techArr)
                    put("service_type", task.title)
                    put("scheduled_time", task.timeSlot)
                    put("status", mapStatusToWeb(task.status))
                    put("admin_comments", task.description)
                    put("tech_comments", task.comments)
                    put("client_rating", task.clientRatingStars ?: JSONObject.NULL)
                    put("client_feedback", task.clientRatingComment)
                }
            )
        }
        httpUpsertArray("work_orders", orderArr)

        val catalogArr = JSONArray()
        snap.masterServices.forEach { srv ->
            catalogArr.put(
                JSONObject().apply {
                    put("id", ensureUuid(srv.id))
                    put("title", srv.name)
                    put("is_active", true)
                }
            )
        }
        httpUpsertArray("service_catalog", catalogArr)

        val annArr = JSONArray()
        snap.announcements.forEach { ann ->
            val rolesArr = JSONArray()
            ann.targetRoles.forEach {
                rolesArr.put(
                    when (it) {
                        AnnouncementTargetRole.LIMPIEZA -> "limpieza"
                        AnnouncementTargetRole.TECNICOS -> "tecnico"
                        AnnouncementTargetRole.CLIENTES -> "cliente"
                    }
                )
            }
            annArr.put(
                JSONObject().apply {
                    put("id", ensureUuid(ann.id))
                    put("title", ann.title)
                    put("body", ann.body)
                    put("image_url", ann.imageUri ?: JSONObject.NULL)
                    put("priority", mapPriorityToWeb(ann.priority))
                    put("target_roles", rolesArr)
                }
            )
        }
        httpUpsertArray("announcements", annArr)
    }

    private fun pushAuthToWebTables(jsonState: String) {
        val snap = try { authAdapter.fromJson(jsonState) } catch (_: Exception) { null } ?: return
        val profileArr = JSONArray()
        snap.users.forEach { user ->
            profileArr.put(
                JSONObject().apply {
                    put("id", ensureUuid(user.uid))
                    put("username", user.username)
                    put("full_name", user.fullName)
                    put("role", mapRoleToWeb(user.role))
                    put("client_id", user.clientId?.takeIf { it.isNotBlank() }?.let { ensureUuid(it) } ?: JSONObject.NULL)
                    put("avatar_url", user.avatarUrl ?: JSONObject.NULL)
                    put("phone", user.phone)
                    put("must_change_password", user.isTemporaryPassword)
                    put("is_active", user.isActive)
                }
            )
        }
        httpUpsertArray("profiles", profileArr)
    }

    private fun pushCleaningToWebTables(jsonState: String) {
        val snap = try { cleaningAdapter.fromJson(jsonState) } catch (_: Exception) { null } ?: return
        val logArr = JSONArray()
        snap.marksList.forEach { mark ->
            logArr.put(
                JSONObject().apply {
                    put("id", ensureUuid(mark.id))
                    put("user_id", if (mark.staffId.isNotBlank()) ensureUuid(mark.staffId) else JSONObject.NULL)
                    put("event_type", if (mark.markType == "S") "salida" else "entrada")
                    put("is_manual", mark.isManualEntry)
                    put("lat", mark.lat)
                    put("lng", mark.lng)
                    put("distance_meters", mark.distanceToBuildingMeters.toDouble())
                    put("out_of_range", !mark.isInsideGeofence)
                    put("comment", mark.comment)
                }
            )
        }
        httpUpsertArray("cleaning_logs", logArr)
    }

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
