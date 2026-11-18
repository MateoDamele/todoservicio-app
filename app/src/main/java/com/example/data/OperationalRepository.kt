package com.example.data

import android.content.Context
import com.example.model.ActiveTechnicianTracking
import com.example.model.AnnouncementItem
import com.example.model.AnnouncementPriority
import com.example.model.AnnouncementTargetRole
import com.example.model.ChatSenderRole
import com.example.model.ClientBranch
import com.example.model.ClientEquipmentAsset
import com.example.model.ClientNotification
import com.example.model.ClientProfile
import com.example.model.CorporateClientAccount
import com.example.model.MasterServiceCatalogItem
import com.example.model.MetricCheckInRecord
import com.example.model.OperationalTask
import com.example.model.PendingQuote
import com.example.model.RescheduleNotice
import com.example.model.ServiceChatMessage
import com.example.model.ServiceHistoryItem
import com.example.model.ServiceRatingRecord
import com.example.model.TaskOperationStatus
import com.example.model.UserRole
import com.example.model.isForUserRole
import com.example.util.LocalNotificationDispatcher
import com.example.util.UruguayLocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
- Repositorio Central de Operaciones, Clientes, Cotizaciones y Reportes de Todoservicio.
- Respaldado por persistencia SQLite cifrada (LocalDatabaseStore) sincronizada en vivo con Supabase.
 */
object OperationalRepository {

    // ========================================================================
    // CORRELATIVO GLOBAL DE ÓRDENES DE TRABAJO (OT-0001 AL INFINITO)
    // ========================================================================
    private var _globalOrderCounter = 0

    @Synchronized
    fun nextOrderCode(): String {
        ++_globalOrderCounter
        val code = "OT-%04d".format(_globalOrderCounter)
        persistOperational()
        return code
    }

    fun currentMaxOrderCode(): String = "OT-%04d".format(_globalOrderCounter)

    fun peekNextOrderCode(): String = "OT-%04d".format(_globalOrderCounter + 1)

    // ========================================================================
    // 0. EMPRESAS CORPORATIVAS Y SEDES MULTI-LOCAL (CENTRALIZADAS)
    // ========================================================================
    private val _corporateClients = MutableStateFlow<List<CorporateClientAccount>>(emptyList())
    val corporateClients: StateFlow<List<CorporateClientAccount>> = _corporateClients.asStateFlow()

    fun addCorporateClient(company: CorporateClientAccount) {
        _corporateClients.value = listOf(company) + _corporateClients.value
        persistOperational()
    }

    fun addClientBranch(clientId: String, branch: ClientBranch) {
        _corporateClients.value = _corporateClients.value.map { client ->
            if (client.id == clientId) client.copy(branches = client.branches + branch) else client
        }
        persistOperational()
    }

    fun updateCorporateClientLogo(clientId: String, logoUri: String?) {
        _corporateClients.value = _corporateClients.value.map { client ->
            if (client.id == clientId) client.copy(logoUri = logoUri) else client
        }
        persistOperational()
    }

    // ========================================================================
    // 1. GESTIÓN REACTIVA DE COTIZACIONES Y DOCUMENTOS (ADMIN & CLIENTE)
    // ========================================================================
    private val _quotes = MutableStateFlow<List<PendingQuote>>(emptyList())
    val quotes: StateFlow<List<PendingQuote>> = _quotes.asStateFlow()

    fun sendQuote(quote: PendingQuote) {
        val updated = _quotes.value.toMutableList()
        updated.add(0, quote)
        _quotes.value = updated
        addNotification("Nueva Cotización Emitida: ${quote.quoteCode} para ${quote.clientCompanyName} ($${quote.amountUsd} USD)")
        persistOperational()
    }

    fun approveQuote(quoteId: String) {
        val today = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "UY")).format(Date())
        _quotes.value = _quotes.value.map {
            if (it.id == quoteId) it.copy(isApproved = true, approvedDate = today) else it
        }
        val approved = _quotes.value.find { it.id == quoteId }
        addNotification("¡Cotización ${approved?.quoteCode ?: ""} APROBADA por el cliente!")
        persistOperational()
    }

    fun requestQuoteRevision(quoteId: String, reason: String) {
        _quotes.value = _quotes.value.map {
            if (it.id == quoteId) it.copy(revisionFeedback = reason.ifBlank { "Revisión técnica solicitada" }) else it
        }
        val quote = _quotes.value.find { it.id == quoteId }
        addNotification("Revisión solicitada sobre ${quote?.quoteCode ?: "cotización"}: $reason")
        persistOperational()
    }

    // ========================================================================
    // 2. CALENDARIO OPERATIVO Y REPROGRAMACIÓN
    // ========================================================================
    private val _operationalTasks = MutableStateFlow<List<OperationalTask>>(emptyList())
    val operationalTasks: StateFlow<List<OperationalTask>> = _operationalTasks.asStateFlow()

    private val _serviceReports = MutableStateFlow<List<ServiceHistoryItem>>(emptyList())
    val serviceReports: StateFlow<List<ServiceHistoryItem>> = _serviceReports.asStateFlow()

    fun addCompletedReport(report: ServiceHistoryItem) {
        val existingRating = _ratingDetails.value[report.serviceCode]
        val finalReport = if (existingRating != null && report.clientRatingStars == null) {
            report.copy(
                clientRatingStars = existingRating.stars,
                clientRatingComment = existingRating.comment
            )
        } else report

        val updated = _serviceReports.value.toMutableList()
        val existingIndex = updated.indexOfFirst { it.serviceCode == finalReport.serviceCode }
        if (existingIndex != -1) {
            updated[existingIndex] = finalReport
        } else {
            updated.add(0, finalReport)
        }
        _serviceReports.value = updated

        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.code == finalReport.serviceCode) {
                task.copy(
                    status = TaskOperationStatus.FINALIZADA,
                    isEnCamino = false,
                    checkInTime = task.checkInTime ?: finalReport.checkInTime.takeIf { it.isNotBlank() },
                    checkOutTime = finalReport.checkOutTime.takeIf { it.isNotBlank() } ?: task.checkOutTime
                )
            } else task
        }

        addNotification("Orden ${finalReport.serviceCode} cerrada con reporte y firma digital.")
        persistOperational()
    }

    fun rescheduleTask(
        taskId: String,
        newDate: String,
        reason: String,
        newDay: Int? = null,
        newTimeSlot: String? = null
    ) {
        val now = SimpleDateFormat("dd MMM, HH:mm 'hs'", Locale("es", "UY")).format(Date())
        var updatedTaskRef: OperationalTask? = null
        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.id == taskId) {
                val resolvedDay = newDay ?: task.dayOfMonth
                val resolvedTime = newTimeSlot ?: task.timeSlot
                val updated = task.copy(
                    dayOfMonth = resolvedDay,
                    timeSlot = resolvedTime,
                    scheduledDate = newDate,
                    rescheduleNotice = RescheduleNotice(
                        isRescheduled = true,
                        originalDate = task.scheduledDate,
                        newDate = "$newDate ($resolvedTime)",
                        reason = reason,
                        rescheduledBy = "Administración Central Todoservicio",
                        timestamp = now
                    )
                )
                updatedTaskRef = updated
                updated
            } else task
        }

        updatedTaskRef?.let { task ->
            addNotification("Visita ${task.code} reprogramada para el $newDate (${task.timeSlot}).")
            dispatchCrossNotificationsForTask(task = task, isReschedule = true, reason = reason)
        }
        persistOperational()
    }

    fun addTask(task: OperationalTask): OperationalTask {
        val finalCode = if (task.code.isBlank() || task.code.startsWith("OT-48") || task.code.startsWith("OT-2026")) {
            nextOrderCode()
        } else {
            task.code
        }
        val resolvedClientId = task.clientId.ifBlank {
            _corporateClients.value.firstOrNull {
                it.name.equals(task.clientName, ignoreCase = true) ||
                    task.clientName.contains(it.name, ignoreCase = true)
            }?.id ?: "client-${task.clientName.lowercase().replace(" ", "-")}"
        }
        val taskWithGlobalCode = task.copy(
            code = finalCode,
            clientId = resolvedClientId
        )
        _operationalTasks.value = listOf(taskWithGlobalCode) + _operationalTasks.value
        addNotification("Nueva tarea programada: ${taskWithGlobalCode.code} (${taskWithGlobalCode.clientName})")
        dispatchCrossNotificationsForTask(task = taskWithGlobalCode, isReschedule = false, reason = null)
        persistOperational()
        return taskWithGlobalCode
    }

    fun updateTaskStatusFromTech(
        orderCode: String,
        newStatus: TaskOperationStatus,
        checkInTime: String? = null,
        isEnCamino: Boolean = false
    ) {
        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.code == orderCode) {
                task.copy(
                    status = newStatus,
                    checkInTime = checkInTime ?: task.checkInTime,
                    isEnCamino = if (newStatus == TaskOperationStatus.FINALIZADA) false else isEnCamino
                )
            } else task
        }
        persistOperational()
    }

    // ========================================================================
    // 2.1 CHAT EN TIEMPO REAL POR ORDEN DE TRABAJO ACTIVA (CLIENTE <-> TÉCNICO)
    // ========================================================================
    private val _serviceChatMessages = MutableStateFlow>>(emptyMap())
    val serviceChatMessages: StateFlow>> = _serviceChatMessages.asStateFlow()

    fun isTaskChatActive(task: OperationalTask?): Boolean {
        if (task == null) return false
        return task.status == TaskOperationStatus.INICIO_EJECUCION ||
            (task.status == TaskOperationStatus.ABIERTA && task.isEnCamino)
    }

    fun sendServiceChatMessage(
        orderCode: String,
        senderRole: ChatSenderRole,
        senderName: String,
        rawMessage: String
    ): Boolean {
        val trimmed = rawMessage.trim().take(140)
        if (trimmed.isEmpty()) return false

        val task = _operationalTasks.value.firstOrNull { it.code == orderCode }
        if (!isTaskChatActive(task)) {
            return false
        }

        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msg = ServiceChatMessage(
            id = "chat-${System.currentTimeMillis()}-$orderCode",
            orderCode = orderCode,
            senderRole = senderRole,
            senderName = senderName,
            message = trimmed,
            timestamp = "$timeNow hs"
        )

        val currentMap = _serviceChatMessages.value.toMutableMap()
        val existingList = currentMap[orderCode].orEmpty()
        currentMap[orderCode] = existingList + msg
        _serviceChatMessages.value = currentMap

        if (task != null) {
            if (senderRole == ChatSenderRole.TECNICO) {
                val clientNotif = ClientNotification(
                    id = "cnotif-chat-${System.currentTimeMillis()}",
                    title = "Mensaje del Técnico (${task.code})",
                    message = "$senderName: $trimmed",
                    timeAgo = "Ahora",
                    isUnread = true,
                    type = "service",
                    targetClientId = task.clientId,
                    targetClientName = task.clientName
                )
                _clientNotifications.value = listOf(clientNotif) + _clientNotifications.value
            } else {
                val techNotif = ClientNotification(
                    id = "tnotif-chat-${System.currentTimeMillis()}",
                    title = "Mensaje de ${task.clientName} (${task.code})",
                    message = trimmed,
                    timeAgo = "Ahora",
                    isUnread = true,
                    type = "service",
                    targetClientId = task.clientId,
                    targetClientName = task.clientName,
                    targetTechName = task.technicianName
                )
                _techNotifications.value = listOf(techNotif) + _techNotifications.value
            }
        }
        persistOperational()
        return true
    }

    // ========================================================================
    // 3. SEGUIMIENTO GPS EN TIEMPO REAL DEL TÉCNICO
    // ========================================================================
    private val _activeTracking = MutableStateFlow(
        ActiveTechnicianTracking(
            id = "",
            serviceCode = "",
            serviceTitle = "Sin servicio activo",
            scheduledTime = "",
            location = "",
            technicianName = "",
            technicianRole = "",
            technicianPhone = "",
            technicianWhatsapp = "",
            vehiclePlate = "",
            vehicleModel = "",
            statusMessage = "Sin móvil activo",
            etaMinutes = 0,
            distanceKm = 0.0,
            isEnCamino = false,
            currentLatitude = -34.9011,
            currentLongitude = -56.1645,
            destinationLatitude = -34.9011,
            destinationLongitude = -56.1645,
            destinationAddress = "",
            trackingStage = "Inactivo"
        )
    )
    val activeTracking: StateFlow<ActiveTechnicianTracking> = _activeTracking.asStateFlow()

    fun updateTechnicianTripStatus(stage: String, etaMinutes: Int, isEnCamino: Boolean) {
        val statusMsg = when (stage) {
            "En Trayecto" -> "En Camino (A $etaMinutes min de su edificio)"
            "En Sitio (Check-in)" -> "Técnico en Sitio - Iniciando Trabajo"
            "Completada", "Servicio Finalizado" -> "Servicio Finalizado"
            else -> "Sin móvil activo"
        }
        _activeTracking.value = _activeTracking.value.copy(
            trackingStage = stage,
            etaMinutes = etaMinutes,
            isEnCamino = isEnCamino,
            statusMessage = statusMsg
        )
        persistOperational()
    }

    fun startTechnicianTransit(
        context: Context?,
        orderCode: String,
        techName: String,
        techPhone: String = "",
        startLat: Double,
        startLng: Double
    ) {
        val task = _operationalTasks.value.firstOrNull { it.code == orderCode }
        val resolvedClientId = task?.clientId.orEmpty()
        val resolvedClientName = task?.clientName.orEmpty()

        val matchedClient = _corporateClients.value.firstOrNull { client ->
            (resolvedClientId.isNotBlank() && client.id == resolvedClientId) ||
                (resolvedClientName.isNotBlank() && client.name.equals(resolvedClientName, ignoreCase = true))
        }
        val matchedBranch = matchedClient?.branches?.firstOrNull { branch ->
            task?.sedeName.isNullOrBlank() || branch.name.equals(task?.sedeName, ignoreCase = true)
        } ?: matchedClient?.branches?.firstOrNull()

        val destLat = matchedBranch?.lat ?: -34.9011
        val destLng = matchedBranch?.lng ?: -56.1645
        val initialEta = UruguayLocationProvider.computeRoadGeodesicEta(startLat, startLng, destLat, destLng)

        _operationalTasks.value = _operationalTasks.value.map { t ->
            if (t.code == orderCode) {
                t.copy(status = TaskOperationStatus.ABIERTA, isEnCamino = true)
            } else {
                t
            }
        }

        _activeTracking.value = ActiveTechnicianTracking(
            id = "track-$orderCode",
            serviceCode = orderCode,
            serviceTitle = task?.title ?: "Visita Técnica $orderCode",
            scheduledTime = task?.timeSlot ?: "En curso",
            location = task?.sedeName?.ifBlank { task.address } ?: resolvedClientName,
            technicianName = techName.ifBlank { task?.technicianName ?: "Técnico Todoservicio" },
            technicianRole = "Técnico Especialista",
            technicianPhone = techPhone.ifBlank { "+598 99 000 111" },
            technicianWhatsapp = techPhone,
            vehiclePlate = "SUT-4821",
            vehicleModel = "Móvil Todoservicio",
            statusMessage = "En Camino (${initialEta.toFormattedLabel()})",
            etaMinutes = initialEta.etaMinutes,
            distanceKm = initialEta.distanceKm,
            isEnCamino = true,
            currentLatitude = startLat,
            currentLongitude = startLng,
            destinationLatitude = destLat,
            destinationLongitude = destLng,
            destinationAddress = task?.address ?: matchedBranch?.address.orEmpty(),
            trackingStage = "En Trayecto",
            clientId = resolvedClientId,
            clientName = resolvedClientName,
            lastUpdatedMillis = System.currentTimeMillis(),
            etaSource = initialEta.sourceLabel
        )

        val transitMsg = "El técnico ${techName.ifBlank { "asignado" }} ya va en camino a su sede"
        val targetedNotif = ClientNotification(
            id = "cnotif-transit-${System.currentTimeMillis()}-$orderCode",
            title = "Técnico en Camino ($orderCode)",
            message = transitMsg,
            timeAgo = "Ahora",
            isUnread = true,
            type = "service",
            targetClientId = resolvedClientId,
            targetClientName = resolvedClientName
        )
        _clientNotifications.value = listOf(targetedNotif) + _clientNotifications.value
        addNotification("$transitMsg ($orderCode - $resolvedClientName)")

        if (context != null) {
            LocalNotificationDispatcher.dispatchServiceTransitPush(
                context = context,
                orderCode = orderCode,
                title = "Técnico en camino • $orderCode",
                message = transitMsg
            )
        }
        persistOperational()
    }

    fun updateTechnicianLiveGps(
        orderCode: String,
        lat: Double,
        lng: Double,
        etaMinutes: Int? = null,
        distanceKm: Double? = null,
        etaSource: String = ""
    ) {
        val current = _activeTracking.value
        if (current.serviceCode != orderCode || !current.isEnCamino) return

        val computed = if (etaMinutes != null && distanceKm != null) {
            com.example.util.RouteEtaResult(
                etaMinutes = etaMinutes,
                distanceKm = distanceKm,
                sourceLabel = etaSource.ifBlank { current.etaSource }
            )
        } else {
            UruguayLocationProvider.computeRoadGeodesicEta(
                originLat = lat,
                originLng = lng,
                destLat = current.destinationLatitude,
                destLng = current.destinationLongitude
            )
        }

        _activeTracking.value = current.copy(
            currentLatitude = lat,
            currentLongitude = lng,
            etaMinutes = computed.etaMinutes,
            distanceKm = computed.distanceKm,
            etaSource = computed.sourceLabel,
            statusMessage = computed.toFormattedLabel(),
            lastUpdatedMillis = System.currentTimeMillis()
        )
    }

    fun markTechnicianCheckIn(orderCode: String, checkInTimeFormatted: String) {
        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.code == orderCode) {
                task.copy(
                    status = TaskOperationStatus.INICIO_EJECUCION,
                    checkInTime = checkInTimeFormatted,
                    isEnCamino = false
                )
            } else task
        }

        if (_activeTracking.value.serviceCode == orderCode) {
            _activeTracking.value = _activeTracking.value.copy(
                isEnCamino = false,
                trackingStage = "En Sitio (Check-in)",
                etaMinutes = 0,
                statusMessage = "Técnico en Sitio • Entrada: $checkInTimeFormatted",
                lastUpdatedMillis = System.currentTimeMillis()
            )
        }

        addNotification("Check-in en sitio registrado para $orderCode ($checkInTimeFormatted).")
        persistOperational()
    }

    fun completeTechnicianCheckOut(
        orderCode: String,
        checkOutTimeFormatted: String,
        report: ServiceHistoryItem
    ) {
        val taskBefore = _operationalTasks.value.firstOrNull { it.code == orderCode }
        val resolvedCheckIn = report.checkInTime.ifBlank {
            taskBefore?.checkInTime?.removePrefix("Check-in:")?.trim().orEmpty()
        }

        val finalReport = report.copy(
            checkInTime = resolvedCheckIn,
            checkOutTime = checkOutTimeFormatted
        )

        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.code == orderCode) {
                task.copy(
                    status = TaskOperationStatus.FINALIZADA,
                    isEnCamino = false,
                    checkInTime = resolvedCheckIn.takeIf { it.isNotBlank() } ?: task.checkInTime,
                    checkOutTime = checkOutTimeFormatted
                )
            } else task
        }

        if (_activeTracking.value.serviceCode == orderCode || _activeTracking.value.isEnCamino) {
            _activeTracking.value = _activeTracking.value.copy(
                serviceCode = orderCode,
                isEnCamino = false,
                trackingStage = "Servicio Finalizado",
                statusMessage = "Servicio Finalizado",
                etaMinutes = 0,
                distanceKm = 0.0,
                lastUpdatedMillis = System.currentTimeMillis()
            )
        }

        addCompletedReport(finalReport)
    }

    // ========================================================================
    // 4. NOTIFICACIONES PUSH INTERNAS Y CRUZADAS (ADMIN -> CLIENTE & TÉCNICO)
    // ========================================================================
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    private val _clientNotifications = MutableStateFlow<List<ClientNotification>>(emptyList())
    val clientNotifications: StateFlow<List<ClientNotification>> = _clientNotifications.asStateFlow()

    private val _techNotifications = MutableStateFlow<List<ClientNotification>>(emptyList())
    val techNotifications: StateFlow<List<ClientNotification>> = _techNotifications.asStateFlow()

    private fun dispatchCrossNotificationsForTask(
        task: OperationalTask,
        isReschedule: Boolean,
        reason: String?
    ) {
        val sedeLabel = task.sedeName.ifBlank { task.address.substringBefore("•").trim() }
        val clientTitle = if (isReschedule) "Visita Reprogramada (${task.code})" else "Nueva Visita Programada (${task.code})"
        val clientMessage = if (isReschedule) {
            "Visita reprogramada para el ${task.scheduledDate} a las ${task.timeSlot}. ${reason ?: ""}".trim()
        } else {
            "Nueva visita programada para el ${task.scheduledDate} a las ${task.timeSlot} en $sedeLabel."
        }

        val newClientNotif = ClientNotification(
            id = "cnotif-${System.currentTimeMillis()}-${task.code}",
            title = clientTitle,
            message = clientMessage,
            timeAgo = "Ahora",
            isUnread = true,
            type = "service",
            targetClientId = task.clientId,
            targetClientName = task.clientName
        )
        _clientNotifications.value = listOf(newClientNotif) + _clientNotifications.value

        val techTitle = if (isReschedule) "Orden Reprogramada (${task.code})" else "Nueva Orden Asignada (${task.code})"
        val techMessage = if (isReschedule) {
            "Orden reprogramada: ${task.clientName} - $sedeLabel el ${task.scheduledDate} (${task.timeSlot})."
        } else {
            "Nueva orden asignada: ${task.clientName} - $sedeLabel el ${task.scheduledDate} (${task.timeSlot})."
        }
        val newTechNotif = ClientNotification(
            id = "tnotif-${System.currentTimeMillis()}-${task.code}",
            title = techTitle,
            message = techMessage,
            timeAgo = "Ahora",
            isUnread = true,
            type = "service",
            targetClientId = task.clientId,
            targetClientName = task.clientName,
            targetTechName = task.technicianName
        )
        _techNotifications.value = listOf(newTechNotif) + _techNotifications.value
    }

    fun markClientNotificationsRead(clientId: String, companyName: String) {
        _clientNotifications.value = _clientNotifications.value.map { notif ->
            val matches = notif.targetClientId == clientId ||
                notif.targetClientName.equals(companyName, ignoreCase = true) ||
                notif.targetClientId.isBlank()
            if (matches) notif.copy(isUnread = false) else notif
        }
        persistOperational()
    }

    fun markTechNotificationsRead(techName: String) {
        _techNotifications.value = _techNotifications.value.map { notif ->
            val matches = notif.targetTechName.isBlank() ||
                notif.targetTechName.contains(techName, ignoreCase = true) ||
                techName.contains(notif.targetTechName, ignoreCase = true)
            if (matches) notif.copy(isUnread = false) else notif
        }
        persistOperational()
    }

    fun addNotification(message: String) {
        val list = _notifications.value.toMutableList()
        list.add(0, message)
        _notifications.value = list
        persistOperational()
    }

    // ========================================================================
    // 5. CATÁLOGO DE EQUIPOS / ACTIVOS POR CLIENTE
    // ========================================================================
    private val _equipmentAssets = MutableStateFlow<List<ClientEquipmentAsset>>(emptyList())
    val equipmentAssets: StateFlow<List<ClientEquipmentAsset>> = _equipmentAssets.asStateFlow()

    fun addEquipmentAsset(asset: ClientEquipmentAsset) {
        val list = _equipmentAssets.value.toMutableList()
        list.add(0, asset)
        _equipmentAssets.value = list
        addNotification("Nuevo equipo registrado: ${asset.name} (${asset.assetCode}) en ${asset.clientCompanyName}")
        persistOperational()
    }

    // ========================================================================
    // 6. CATÁLOGO MAESTRO DE TAREAS Y SERVICIOS (SOLO ADMINISTRACIÓN)
    // ========================================================================
    private var _masterCatalogInitialized = false

    private val _masterServices = MutableStateFlow<List<MasterServiceCatalogItem>>(defaultMasterServices())
    val masterServices: StateFlow<List<MasterServiceCatalogItem>> = _masterServices.asStateFlow()

    fun addMasterService(service: MasterServiceCatalogItem) {
        val list = _masterServices.value.toMutableList()
        list.add(0, service)
        _masterServices.value = list
        _masterCatalogInitialized = true
        addNotification("Nueva tarea añadida al catálogo: ${service.name} (${service.code})")
        persistOperational()
    }

    fun updateMasterService(updated: MasterServiceCatalogItem) {
        _masterServices.value = _masterServices.value.map { item ->
            if (item.id == updated.id) updated else item
        }
        _masterCatalogInitialized = true
        addNotification("Tarea actualizada en catálogo: ${updated.name}")
        persistOperational()
    }

    fun deleteMasterService(serviceId: String) {
        val target = _masterServices.value.firstOrNull { it.id == serviceId }
        _masterServices.value = _masterServices.value.filterNot { it.id == serviceId }
        _masterCatalogInitialized = true
        if (target != null) {
            addNotification("Tarea eliminada del catálogo: ${target.name}")
        }
        persistOperational()
    }

    private fun defaultMasterServices(): List<MasterServiceCatalogItem> = listOf(
        MasterServiceCatalogItem("cat-01", "TAR-01", "Limpieza de brasero", "Limpieza Especializada", "Limpieza profunda, deshollinado y retiro de cenizas y residuos.", 1.5, true, 0.0),
        MasterServiceCatalogItem("cat-02", "TAR-02", "Limpieza de filtro de aire acondicionado", "Climatización (HVAC)", "Lavado, sanitización de filtros y chequeo de drenaje.", 1.0, false, 0.0),
        MasterServiceCatalogItem("cat-03", "TAR-03", "Mantenimiento integral de aire acondicionado", "Climatización (HVAC)", "Control de presiones, carga térmica, limpieza de serpentín y tablero.", 2.5, true, 0.0),
        MasterServiceCatalogItem("cat-04", "TAR-04", "Cerrajería y herrajes", "Mantenimiento Edilicio", "Ajuste, lubricación o recambio de cerraduras, cierrapuertas y bisagras.", 1.5, false, 0.0),
        MasterServiceCatalogItem("cat-05", "TAR-05", "Limpieza de vidrios en altura / fachada", "Seguridad en Altura (Dec. 406/88)", "Lavado técnico de ventanales y muros cortina con arnés certificado.", 3.5, true, 0.0),
        MasterServiceCatalogItem("cat-06", "TAR-06", "Barrido y limpieza de azotea / desagües", "Cubiertas y Pluviales", "Desobstrucción de rejillas, canalones y limpieza preventiva de azotea.", 2.0, true, 0.0),
        MasterServiceCatalogItem("cat-07", "TAR-07", "Cambio de luminaria y tableros eléctricos", "Electricidad", "Sustitución de luminarias LED, revisión térmica y protecciones diferenciales.", 1.5, true, 0.0),
        MasterServiceCatalogItem("cat-08", "TAR-08", "Sanitaria y bombas de agua", "Sanitaria y Bombas", "Purgado, control de presostato, sellos y desobstrucción sanitaria.", 2.0, true, 0.0),
        MasterServiceCatalogItem("cat-09", "TAR-09", "Pintura y albañilería menor", "Albañilería y Pintura", "Resanes, sellado de fisuras, enduido y retoques de pintura interior/exterior.", 3.0, false, 0.0)
    )

    // ========================================================================
    // 7. MÉTRICAS Y AUDITORÍA DE CHECK-IN EN SITIO
    // ========================================================================
    private val _metricCheckIns = MutableStateFlow<List<MetricCheckInRecord>>(emptyList())
    val metricCheckIns: StateFlow<List<MetricCheckInRecord>> = _metricCheckIns.asStateFlow()

    // ========================================================================
    // 8. SOLICITUDES DE CLIENTE Y CALIFICACIONES 5 ESTRELLAS
    // ========================================================================
    private val _npsRatings = MutableStateFlow>(emptyMap())
    val npsRatings: StateFlow> = _npsRatings.asStateFlow()

    private val _ratingDetails = MutableStateFlow>(emptyMap())
    val ratingDetails: StateFlow> = _ratingDetails.asStateFlow()

    fun submitNpsRating(orderCode: String, stars: Int, comment: String) {
        val cleanStars = stars.coerceIn(1, 5)
        val cleanComment = comment.trim()
        val ratedAt = SimpleDateFormat("dd MMM yyyy, HH:mm 'hs'", Locale("es", "UY")).format(Date())

        val current = _npsRatings.value.toMutableMap()
        current[orderCode] = cleanStars
        _npsRatings.value = current

        val detailsMap = _ratingDetails.value.toMutableMap()
        detailsMap[orderCode] = ServiceRatingRecord(
            orderCode = orderCode,
            stars = cleanStars,
            comment = cleanComment,
            ratedAtLabel = ratedAt
        )
        _ratingDetails.value = detailsMap

        _operationalTasks.value = _operationalTasks.value.map { task ->
            if (task.code == orderCode) {
                task.copy(clientRatingStars = cleanStars, clientRatingComment = cleanComment)
            } else task
        }

        _serviceReports.value = _serviceReports.value.map { report ->
            if (report.serviceCode == orderCode) {
                report.copy(clientRatingStars = cleanStars, clientRatingComment = cleanComment)
            } else report
        }

        val detail = if (cleanComment.isNotBlank()) " — \"$cleanComment\"" else ""
        addNotification("Calificación del cliente en $orderCode: $cleanStars★$detail")
        persistOperational()
    }

    fun submitClientWorkRequest(
        clientProfile: ClientProfile,
        title: String,
        category: String,
        location: String,
        description: String,
        scheduledTimeSlot: String = "09:00",
        isPlanVisit: Boolean = false
    ): OperationalTask {
        val nowCal = java.util.Calendar.getInstance()
        val todayDay = nowCal.get(java.util.Calendar.DAY_OF_MONTH)
        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale("es", "UY")).format(nowCal.time)
        val clientCompany = clientProfile.companyName.ifBlank { clientProfile.clientName.ifBlank { "Cliente" } }
        val newTask = OperationalTask(
            id = "req-${System.currentTimeMillis()}",
            code = nextOrderCode(),
            title = title,
            clientId = clientProfile.clientId,
            clientName = clientCompany,
            sedeName = location.ifBlank { clientCompany },
            address = clientProfile.address.ifBlank { location },
            category = category,
            dayOfMonth = todayDay,
            scheduledDate = formattedDate,
            timeSlot = scheduledTimeSlot,
            technicianName = "",
            status = TaskOperationStatus.ABIERTA,
            description = if (isPlanVisit) "[Visita de Plan] $description" else description,
            comments = description
        )
        return addTask(newTask)
    }

    // ========================================================================
    // 9. PORTAL DE AVISOS Y COMUNICADOS SEGMENTADOS (ADMIN -> ROLES)
    // ========================================================================
    private val _announcements = MutableStateFlow<List<AnnouncementItem>>(emptyList())
    val announcements: StateFlow<List<AnnouncementItem>> = _announcements.asStateFlow()

    fun publishAnnouncement(
        context: Context?,
        title: String,
        body: String,
        priority: AnnouncementPriority,
        targetRoles: List<AnnouncementTargetRole>,
        imageUri: String? = null,
        authorName: String = "Administración Central"
    ): AnnouncementItem {
        val now = System.currentTimeMillis()
        val formattedTime = SimpleDateFormat("dd MMM, HH:mm 'hs'", Locale("es", "UY")).format(Date(now))
        val item = AnnouncementItem(
            id = "adv-$now-${_announcements.value.size + 1}",
            title = title.trim(),
            body = body.trim(),
            priority = priority,
            targetRoles = targetRoles.distinct(),
            imageUri = imageUri,
            createdAtMillis = now,
            createdAtLabel = formattedTime,
            authorName = authorName.ifBlank { "Administración Central" },
            readByUserIds = emptyList()
        )
        _announcements.value = listOf(item) + _announcements.value
        addNotification("Comunicado [${priority.label}]: ${item.title}")
        if (context != null) {
            LocalNotificationDispatcher.dispatchAnnouncementPush(context, item)
        }
        persistOperational()
        return item
    }

    fun markAnnouncementsAsRead(userId: String, role: UserRole) {
        if (userId.isBlank()) return
        var changed = false
        val updated = _announcements.value.map { item ->
            if (item.isForUserRole(role) && !item.readByUserIds.contains(userId)) {
                changed = true
                item.copy(readByUserIds = item.readByUserIds + userId)
            } else {
                item
            }
        }
        if (changed) {
            _announcements.value = updated
            persistOperational()
        }
    }

    /**
- Inicializa y restaura todo el estado operativo y contador global desde SQLite y Supabase.
     */
    fun init(context: Context) {
        val appCtx = context.applicationContext
        LocalDatabaseStore.init(appCtx)
        reloadOperationalFromStore()
        SupabaseCloudSync.startLiveSync(
            onOperationalUpdated = { reloadOperationalFromStore() },
            onAuthUpdated = { AuthRepository.reloadFromStore() },
            onCleaningUpdated = { CleaningRepository.init(appCtx) }
        )
    }

    @Synchronized
    fun reloadOperationalFromStore() {
        val persisted = LocalDatabaseStore.loadOperationalSnapshot()
        if (persisted != null) {
            _globalOrderCounter = maxOf(_globalOrderCounter, persisted.globalOrderCounter)
            _corporateClients.value = persisted.corporateClients
            _quotes.value = persisted.quotes
            _operationalTasks.value = persisted.operationalTasks
            _serviceReports.value = persisted.serviceReports
            _serviceChatMessages.value = persisted.serviceChatMessages
            _activeTracking.value = persisted.activeTracking
            _notifications.value = persisted.notifications
            _clientNotifications.value = persisted.clientNotifications
            _techNotifications.value = persisted.techNotifications
            _equipmentAssets.value = persisted.equipmentAssets
            _masterCatalogInitialized = persisted.masterCatalogInitialized
            _masterServices.value = if (persisted.masterCatalogInitialized) {
                persisted.masterServices
            } else {
                defaultMasterServices()
            }
            _metricCheckIns.value = persisted.metricCheckIns
            _npsRatings.value = persisted.npsRatings
            _ratingDetails.value = persisted.ratingDetails
            _announcements.value = persisted.announcements
        } else if (!_masterCatalogInitialized) {
            _masterServices.value = defaultMasterServices()
            _masterCatalogInitialized = true
        }
    }

    @Synchronized
    private fun persistOperational() {
        val snapshot = PersistedOperationalSnapshot(
            globalOrderCounter = _globalOrderCounter,
            corporateClients = _corporateClients.value,
            quotes = _quotes.value,
            operationalTasks = _operationalTasks.value,
            serviceReports = _serviceReports.value,
            serviceChatMessages = _serviceChatMessages.value,
            activeTracking = _activeTracking.value,
            notifications = _notifications.value,
            clientNotifications = _clientNotifications.value,
            techNotifications = _techNotifications.value,
            equipmentAssets = _equipmentAssets.value,
            masterServices = _masterServices.value,
            masterCatalogInitialized = _masterCatalogInitialized,
            metricCheckIns = _metricCheckIns.value,
            npsRatings = _npsRatings.value,
            ratingDetails = _ratingDetails.value,
            announcements = _announcements.value
        )
        LocalDatabaseStore.saveOperationalSnapshot(snapshot)
    }
}
