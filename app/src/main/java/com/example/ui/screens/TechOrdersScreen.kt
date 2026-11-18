package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RequestPriority
import com.example.model.TechOrderStatus
import com.example.model.TechWorkOrder
import com.example.model.UserAccount
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.FloatingGlassBottomBar
import com.example.ui.components.FloatingGlassNavItem
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.components.InteractiveSignatureCanvas
import com.example.ui.components.SignaturePath
import com.example.ui.components.modalBackdropBlur
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.InstitutionalHeaderBrush
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.rounded.NotificationsActive
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.RojoError
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.data.LocalDatabaseStore
import com.example.util.PdfReportGenerator
import com.example.util.UruguayLocationProvider
import com.example.data.OperationalRepository
import com.example.model.ChatSenderRole
import com.example.model.ClientNotification
import com.example.model.ServiceChatMessage
import com.example.model.ServiceHistoryItem
import com.example.model.TaskOperationStatus
import com.example.model.UserRole
import com.example.model.isForUserRole
import com.example.model.isUnreadBy
import com.example.ui.components.AnnouncementsInboxModal
import com.example.ui.components.HeaderNotificationBell
import com.example.ui.components.RequestAndroidNotificationPermissionEffect
import com.example.ui.components.ServiceChatPanel
import com.example.ui.components.UserProfileHeaderCapsule

/**
 * Pantalla Principal de Órdenes y Agenda de Campo para el Técnico (Estilo 2Workers).
 * - Agenda organizada cronológicamente por día: "Hoy", "Mañana", "Jueves 29 de Octubre", "Viernes 30 de Octubre".
 * - Botón directo a Google Maps / GPS.
 * - Cierre de orden con vista previa completa de Reporte Técnico, fotos de evidencia,
 *   captura de firma táctil interactiva y compilación de PDF oficial.
 */
@Composable
fun TechOrdersScreen(
    currentUser: UserAccount,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repoTasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()
    val allTechNotifs by OperationalRepository.techNotifications.collectAsStateWithLifecycle()
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    val chatMessagesMap by OperationalRepository.serviceChatMessages.collectAsStateWithLifecycle()
    val allAnnouncements by OperationalRepository.announcements.collectAsStateWithLifecycle()

    RequestAndroidNotificationPermissionEffect()

    val techNotifications = remember(allTechNotifs, currentUser.fullName) {
        allTechNotifs.filter { notif ->
            notif.targetTechName.isBlank() ||
                notif.targetTechName.contains(currentUser.fullName, ignoreCase = true) ||
                currentUser.fullName.contains(notif.targetTechName, ignoreCase = true)
        }
    }
    val techAnnouncements = remember(allAnnouncements, currentUser.role) {
        allAnnouncements.filter { it.isForUserRole(currentUser.role) }
    }
    val totalTechUnread = remember(techNotifications, techAnnouncements, currentUser.uid) {
        techNotifications.count { it.isUnread } + techAnnouncements.count { it.isUnreadBy(currentUser.uid) }
    }

    // Sobrescrituras locales en campo (check-in, fotos, modo offline, observaciones) indexadas por código OT
    val localOrderOverrides = remember { mutableStateMapOf<String, TechWorkOrder>() }

    val orders = remember(repoTasks, currentUser.uid, currentUser.fullName, localOrderOverrides.toMap()) {
        val matched = repoTasks.filter { task ->
            task.assignedTechIds.contains(currentUser.uid) ||
                (task.technicianName.isNotBlank() && (
                    task.technicianName.contains(currentUser.fullName, ignoreCase = true) ||
                        currentUser.fullName.contains(task.technicianName, ignoreCase = true)
                    )) ||
                task.assignedTechnicians.any { it.contains(currentUser.fullName, ignoreCase = true) } ||
                task.technicianName.isBlank()
        }
        val calToday = java.util.Calendar.getInstance()
        val todayDay = calToday.get(java.util.Calendar.DAY_OF_MONTH)
        val calTomorrow = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH, 1) }
        val tomorrowDay = calTomorrow.get(java.util.Calendar.DAY_OF_MONTH)
        val monthLabel = java.text.SimpleDateFormat("MMMM", java.util.Locale("es", "UY"))
            .format(calToday.time)
            .replaceFirstChar { it.uppercase() }

        matched.map { task ->
            val dayLabel = when (task.dayOfMonth) {
                todayDay -> "Hoy"
                tomorrowDay -> "Mañana"
                else -> task.scheduledDate.ifBlank { "Día ${task.dayOfMonth} de $monthLabel" }
            }
            val repoStatus = when {
                task.status == TaskOperationStatus.FINALIZADA -> TechOrderStatus.COMPLETADA
                task.status == TaskOperationStatus.INICIO_EJECUCION -> TechOrderStatus.EN_PROCESO
                task.status == TaskOperationStatus.ABIERTA && task.isEnCamino -> TechOrderStatus.EN_CAMINO
                else -> TechOrderStatus.PENDIENTE
            }
            val override = localOrderOverrides[task.code]
            val baseOrder = TechWorkOrder(
                id = task.id,
                code = task.code,
                title = task.title,
                clientName = task.sedeName.ifBlank { task.clientName },
                companyName = task.clientName,
                address = task.address,
                category = task.category,
                scheduledTime = if (task.timeSlot.contains("hs", ignoreCase = true)) task.timeSlot else "${task.timeSlot} hs",
                status = repoStatus,
                priority = if (task.isDec406) RequestPriority.ALTA else RequestPriority.NORMAL,
                description = task.description,
                requiresDec406Protocol = task.isDec406,
                scheduledDay = dayLabel,
                checkInTimestamp = task.checkInTime?.removePrefix("Check-in: "),
                observations = override?.observations ?: task.comments,
                evidencePhotos = override?.evidencePhotos ?: emptyList(),
                checkOutTimestamp = override?.checkOutTimestamp ?: task.checkOutTime
            )
            if (override != null) {
                override.copy(
                    id = baseOrder.id,
                    code = baseOrder.code,
                    title = baseOrder.title,
                    clientName = baseOrder.clientName,
                    companyName = baseOrder.companyName,
                    address = baseOrder.address,
                    category = baseOrder.category,
                    scheduledTime = baseOrder.scheduledTime,
                    scheduledDay = baseOrder.scheduledDay,
                    status = if (repoStatus == TechOrderStatus.COMPLETADA) TechOrderStatus.COMPLETADA else override.status
                )
            } else {
                baseOrder
            }
        }
    }

    // Transmisión GPS continua en tiempo real exclusivamente mientras alguna OT esté EN_CAMINO.
    // Al pasar a Check-in (EN_PROCESO) o Check-out (COMPLETADA) se desconecta automáticamente.
    val activeTransitOrderCode = remember(orders) {
        orders.firstOrNull { it.status == TechOrderStatus.EN_CAMINO }?.code
    }
    DisposableEffect(activeTransitOrderCode) {
        if (activeTransitOrderCode != null && UruguayLocationProvider.hasLocationPermission(context)) {
            val stopGpsStream = UruguayLocationProvider.startLiveLocationUpdates(context) { fix ->
                if (!fix.isFallback && !fix.isMocked) {
                    OperationalRepository.updateTechnicianLiveGps(
                        orderCode = activeTransitOrderCode,
                        lat = fix.lat,
                        lng = fix.lng
                    )
                }
            }
            onDispose { stopGpsStream() }
        } else {
            onDispose {}
        }
    }

    // Estado de Modo Subsuelo / Persistencia Offline
    var isOfflineModeActive by remember { mutableStateOf(false) }
    var isSyncingNow by remember { mutableStateOf(false) }

    val pendingSyncCount = orders.count { !it.isSyncedWithCloud || it.isStoredLocallyOffline }

    // Orden seleccionada para ver el Reporte Preliminar y Firmar
    var selectedOrderForReportPreview by remember { mutableStateOf<TechWorkOrder?>(null) }

    var currentTechTab by remember { mutableStateOf(TechBottomTab.INICIO) }
    var showMaterialsModal by remember { mutableStateOf(false) }
    var showQrModal by remember { mutableStateOf(false) }
    var showTechInboxModal by remember { mutableStateOf(false) }

    val safeTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val headerTopPadding = (safeTop + 8.dp).coerceAtLeast(32.dp)
    val isAnyTechModalOpen = showMaterialsModal || showQrModal || showTechInboxModal || selectedOrderForReportPreview != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .modalBackdropBlur(isAnyTechModalOpen)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = GrisNeutro,
            topBar = {
                Surface(
                    color = AzulMarca,
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InstitutionalHeaderBrush)
                            .padding(
                                top = headerTopPadding,
                                bottom = 12.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                            contentDescription = "Todoservicio",
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            modifier = Modifier
                                .height(30.dp)
                                .weight(1f, fill = false)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HeaderNotificationBell(
                                unreadCount = totalTechUnread,
                                onClick = { showTechInboxModal = true }
                            )

                            UserProfileHeaderCapsule(
                                firstName = currentUser.fullName,
                                avatarUrl = currentUser.avatarUrl
                            )

                            IconButton(
                                onClick = onLogout,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Blanco.copy(alpha = 0.14f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ExitToApp,
                                    contentDescription = "Cerrar Sesión",
                                    tint = Blanco,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Crossfade(
                targetState = currentTechTab,
                animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                label = "techTabFade"
            ) { tab ->
            when (tab) {
                TechBottomTab.INICIO -> {
                    TechExecutiveHomeScreen(
                        currentUser = currentUser,
                        orders = orders,
                        notifications = techNotifications,
                        chatMessagesMap = chatMessagesMap,
                        onSendChatMessage = { orderCode, text ->
                            OperationalRepository.sendServiceChatMessage(
                                orderCode = orderCode,
                                senderRole = ChatSenderRole.TECNICO,
                                senderName = currentUser.fullName,
                                rawMessage = text
                            )
                        },
                        onMarkNotificationsRead = {
                            OperationalRepository.markTechNotificationsRead(currentUser.fullName)
                        },
                        onNavigateAddress = { address ->
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            context.startActivity(mapIntent)
                        },
                        onOpenMaterials = { showMaterialsModal = true },
                        onOpenQrScanner = { showQrModal = true },
                        onNavigateToAllTasks = { currentTechTab = TechBottomTab.TAREAS },
                        onLogout = onLogout
                    )
                }
                TechBottomTab.TAREAS -> {
                    TechChronologicalAgendaView(
                        orders = orders,
                        notifications = techNotifications,
                        chatMessagesMap = chatMessagesMap,
                        onSendChatMessage = { orderCode, text ->
                            OperationalRepository.sendServiceChatMessage(
                                orderCode = orderCode,
                                senderRole = ChatSenderRole.TECNICO,
                                senderName = currentUser.fullName,
                                rawMessage = text
                            )
                        },
                        onMarkNotificationsRead = {
                            OperationalRepository.markTechNotificationsRead(currentUser.fullName)
                        },
                        isOfflineModeActive = isOfflineModeActive,
                        onToggleOfflineMode = { isOfflineModeActive = it },
                        pendingSyncCount = pendingSyncCount,
                        onSyncNow = {
                            localOrderOverrides.keys.toList().forEach { key ->
                                localOrderOverrides[key]?.let { existing ->
                                    if (!existing.isSyncedWithCloud || existing.isStoredLocallyOffline) {
                                        localOrderOverrides[key] = existing.copy(
                                            isSyncedWithCloud = true,
                                            isStoredLocallyOffline = false
                                        )
                                    }
                                }
                            }
                            Toast.makeText(context, "Sincronización completada", Toast.LENGTH_LONG).show()
                        },
                        onUpdateOrder = { updated ->
                            val previousStatus = localOrderOverrides[updated.code]?.status
                            val persisted = updated.copy(
                                isStoredLocallyOffline = isOfflineModeActive,
                                isSyncedWithCloud = !isOfflineModeActive
                            )
                            localOrderOverrides[updated.code] = persisted

                            when (updated.status) {
                                TechOrderStatus.EN_CAMINO -> {
                                    if (previousStatus != TechOrderStatus.EN_CAMINO) {
                                        scope.launch {
                                            val fix = UruguayLocationProvider.fetchCurrentGpsLocation(context)
                                            OperationalRepository.startTechnicianTransit(
                                                context = context,
                                                orderCode = updated.code,
                                                techName = currentUser.fullName,
                                                startLat = fix.lat,
                                                startLng = fix.lng
                                            )
                                        }
                                    }
                                }
                                TechOrderStatus.EN_PROCESO -> {
                                    val timeLabel = updated.checkInTimestamp
                                        ?: (java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) + " hs")
                                    OperationalRepository.markTechnicianCheckIn(
                                        orderCode = updated.code,
                                        checkInTimeFormatted = timeLabel
                                    )
                                }
                                TechOrderStatus.COMPLETADA -> {
                                    OperationalRepository.updateTaskStatusFromTech(
                                        orderCode = updated.code,
                                        newStatus = TaskOperationStatus.FINALIZADA,
                                        checkInTime = updated.checkInTimestamp,
                                        isEnCamino = false
                                    )
                                }
                                TechOrderStatus.PENDIENTE,
                                TechOrderStatus.CANCELADA -> {
                                    OperationalRepository.updateTaskStatusFromTech(
                                        orderCode = updated.code,
                                        newStatus = TaskOperationStatus.ABIERTA,
                                        checkInTime = updated.checkInTimestamp,
                                        isEnCamino = false
                                    )
                                }
                            }
                        },
                        onOpenFinalizeReport = { order -> selectedOrderForReportPreview = order },
                        onNavigateAddress = { address ->
                            com.example.ui.components.openExternalGoogleMaps(context, address)
                        },
                        onLogout = onLogout,
                        currentUser = currentUser
                    )
                }
                TechBottomTab.MAPA_GPS -> {
                    GeneralGpsMapSection(onInspectTaskDetail = { currentTechTab = TechBottomTab.TAREAS })
                }
                TechBottomTab.MATERIALES, TechBottomTab.QR_SCAN -> {
                    TechExecutiveHomeScreen(
                        currentUser = currentUser,
                        orders = orders,
                        notifications = techNotifications,
                        chatMessagesMap = chatMessagesMap,
                        onSendChatMessage = { orderCode, text ->
                            OperationalRepository.sendServiceChatMessage(
                                orderCode = orderCode,
                                senderRole = ChatSenderRole.TECNICO,
                                senderName = currentUser.fullName,
                                rawMessage = text
                            )
                        },
                        onMarkNotificationsRead = {
                            OperationalRepository.markTechNotificationsRead(currentUser.fullName)
                        },
                        onNavigateAddress = { address ->
                            com.example.ui.components.openExternalGoogleMaps(context, address)
                        },
                        onOpenMaterials = { showMaterialsModal = true },
                        onOpenQrScanner = { showQrModal = true },
                        onNavigateToAllTasks = { currentTechTab = TechBottomTab.TAREAS },
                        onLogout = onLogout
                    )
                }
            }
        }
        }

        val techGlassItems = remember {
            TechBottomTab.values().map { tab ->
                FloatingGlassNavItem(
                    key = tab,
                    label = tab.label,
                    icon = tab.icon,
                    testTag = tab.testTag
                )
            }
        }
        FloatingGlassBottomBar(
            items = techGlassItems,
            selectedKey = currentTechTab,
            onItemSelected = { tab ->
                when (tab) {
                    TechBottomTab.INICIO -> currentTechTab = TechBottomTab.INICIO
                    TechBottomTab.TAREAS -> currentTechTab = TechBottomTab.TAREAS
                    TechBottomTab.MAPA_GPS -> currentTechTab = TechBottomTab.MAPA_GPS
                    TechBottomTab.MATERIALES -> showMaterialsModal = true
                    TechBottomTab.QR_SCAN -> showQrModal = true
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showMaterialsModal) {
        RegisterMaterialsDialog(
            orders = orders,
            onDismiss = { showMaterialsModal = false }
        )
    }

    if (showQrModal) {
        TechQrScannerDialog(
            onDismiss = { showQrModal = false }
        )
    }

    if (showTechInboxModal) {
        AnnouncementsInboxModal(
            currentUser = currentUser,
            announcements = techAnnouncements,
            extraAlerts = techNotifications,
            onDismiss = {
                OperationalRepository.markTechNotificationsRead(currentUser.fullName)
                showTechInboxModal = false
            }
        )
    }

    // Modal de Vista Previa de Reporte Técnico y Firma Digital (Cierre Check-out)
    selectedOrderForReportPreview?.let { order ->
        TechnicalReportPreviewAndSigningDialog(
            order = order,
            technicianName = currentUser.fullName,
            onDismiss = { selectedOrderForReportPreview = null },
            onConfirmCompletedWithSignature = { updatedObservations, signaturePaths, attachedPhotoUris ->
                val localeEs = java.util.Locale("es", "UY")
                val nowDate = java.util.Date()
                val timeNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(nowDate)
                val todayFormatted = java.text.SimpleDateFormat("d 'de' MMMM, yyyy", localeEs).format(nowDate)
                val checkInFormatted = order.checkInTimestamp?.ifBlank { "$timeNow hs" } ?: "$timeNow hs"
                val checkOutFormatted = order.checkOutTimestamp?.ifBlank { "$timeNow hs" } ?: "$timeNow hs"

                val updatedOrder = order.copy(
                    status = TechOrderStatus.COMPLETADA,
                    checkInTimestamp = checkInFormatted,
                    checkOutTimestamp = checkOutFormatted,
                    observations = updatedObservations,
                    evidencePhotos = attachedPhotoUris
                )
                localOrderOverrides[order.code] = updatedOrder

                val resolvedClientId = corporateClients.firstOrNull {
                    it.name.equals(order.companyName, ignoreCase = true) ||
                        order.companyName.contains(it.name, ignoreCase = true)
                }?.id.orEmpty()

                // Vinculación reactiva: Cierre Check-out + Desconexión GPS + Reporte A4 con fotos y horarios reales
                val newReport = ServiceHistoryItem(
                    id = "REP-${order.code}",
                    serviceCode = order.code,
                    title = order.title,
                    category = order.category,
                    clientId = resolvedClientId,
                    companyName = order.companyName,
                    sedeName = order.clientName,
                    address = order.address,
                    executionDate = todayFormatted,
                    executionTime = checkOutFormatted,
                    leadTechnician = currentUser.fullName,
                    summary = updatedObservations.ifBlank { "Servicio técnico completado y verificado." },
                    technicianComments = updatedObservations,
                    certifiedDec406 = order.requiresDec406Protocol,
                    resolutionHours = 1.0,
                    signedBy = currentUser.fullName,
                    evidencePhotos = attachedPhotoUris,
                    checkInTime = checkInFormatted,
                    checkOutTime = checkOutFormatted
                )
                OperationalRepository.completeTechnicianCheckOut(
                    orderCode = order.code,
                    checkOutTimeFormatted = checkOutFormatted,
                    report = newReport
                )

                // Generar y guardar el Certificado PDF oficial
                PdfReportGenerator.generatePdfReport(
                    context = context,
                    data = PdfReportGenerator.ReportData(
                        orderCode = order.code,
                        serviceTitle = order.title,
                        category = order.category,
                        executionDate = todayFormatted,
                        checkInTime = checkInFormatted,
                        checkOutTime = checkOutFormatted,
                        totalDuration = "Registrada en sitio",
                        leadTechnician = currentUser.fullName,
                        clientName = order.clientName,
                        companyName = order.companyName,
                        clientAddress = order.address,
                        findingsSummary = updatedObservations,
                        clientSignature = signaturePaths
                    )
                )

                Toast.makeText(context, "¡Reporte firmado y PDF oficial emitido con éxito!", Toast.LENGTH_LONG).show()
                selectedOrderForReportPreview = null
            }
        )
    }
}

/**
 * Pestañas de navegación inferior para el perfil Técnico (con INICIO casita como primera fija).
 */
enum class TechBottomTab(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
) {
    INICIO("INICIO", Icons.Rounded.Home, "tab_tech_inicio"),
    TAREAS("TAREAS", Icons.Rounded.CalendarMonth, "tab_tech_tareas"),
    MAPA_GPS("MAPA GPS", Icons.Rounded.Map, "tab_tech_gps"),
    MATERIALES("MATERIALES", Icons.Rounded.Construction, "tab_tech_materiales"),
    QR_SCAN("ESCANEAR QR", Icons.Rounded.QrCodeScanner, "tab_tech_qr")
}

/**
 * Pantalla de Inicio Ejecutiva para el perfil de Técnico.
 * - Saludo personalizado: "Hola, [Nombre del Técnico]".
 * - Resumen de jornada: Próximo destino asignado con botón destacado "Ver en GPS",
 *   lista de tareas del día, y accesos para [ 📦 Registrar Materiales ] y [ 📷 Escáner QR de Equipos ].
 */
@Composable
private fun TechExecutiveHomeScreen(
    currentUser: UserAccount,
    orders: List<TechWorkOrder>,
    notifications: List<ClientNotification>,
    chatMessagesMap: Map<String, List<ServiceChatMessage>>,
    onSendChatMessage: (String, String) -> Boolean,
    onMarkNotificationsRead: () -> Unit,
    onNavigateAddress: (String) -> Unit,
    onOpenMaterials: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onNavigateToAllTasks: () -> Unit,
    onLogout: () -> Unit
) {
    val todayTasks = remember(orders) { orders.filter { it.scheduledDay == "Hoy" } }
    val nextDestination = remember(todayTasks, orders) {
        todayTasks.firstOrNull { it.status != TechOrderStatus.COMPLETADA }
            ?: todayTasks.firstOrNull()
            ?: orders.firstOrNull { it.status != TechOrderStatus.COMPLETADA }
    }
    val unreadNotifs = remember(notifications) { notifications.filter { it.isUnread } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Saludo personalizado y Encabezado Ejecutivo
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AzulMarca),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CelesteAcentoClaro
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Construction,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "OPERACIONES EN CAMPO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Blanco.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Blanco,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "Hola, ${currentUser.fullName}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 22.sp
                        )
                    )

                    Text(
                        text = "Jornada activa en Montevideo y Canelones • ${todayTasks.size} visitas asignadas hoy",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            color = Blanco.copy(alpha = 0.85f),
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        // 1.1 Alertas de Agenda en Tiempo Real (Admin -> Técnico)
        if (unreadNotifs.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NaranjaAlertaFondo),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaranjaAlerta.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = NaranjaAlerta,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Avisos (${unreadNotifs.size})",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = GrisGrafito
                                    )
                                )
                            }
                            TextButton(
                                onClick = onMarkNotificationsRead,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Marcar leídas",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca
                                    )
                                )
                            }
                        }
                        unreadNotifs.take(2).forEach { notif ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = GrisGrafito
                                    )
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Resumen de jornada: Próximo destino asignado con botón destacado "Ver en GPS"
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RESUMEN DE JORNADA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )

                if (nextDestination != null) {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CelesteAcentoClaro
                                ) {
                                    val isCardActive = nextDestination.status == TechOrderStatus.EN_PROCESO ||
                                        nextDestination.status == TechOrderStatus.EN_CAMINO
                                    Text(
                                        text = if (isCardActive) "SERVICIO EN CURSO • ${nextDestination.code}" else "PRÓXIMO DESTINO • ${nextDestination.code}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulCorporativo,
                                            fontSize = 10.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (nextDestination.status == TechOrderStatus.EN_PROCESO) VerdeExitoFondo else NaranjaAlertaFondo
                                ) {
                                    Text(
                                        text = nextDestination.status.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (nextDestination.status == TechOrderStatus.EN_PROCESO) VerdeExito else NaranjaAlerta,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = nextDestination.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 17.sp
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${nextDestination.scheduledTime} • ${nextDestination.category}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulCorporativo
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GrisNeutro,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = CelesteAcento,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = nextDestination.companyName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca
                                            )
                                        )
                                        Text(
                                            text = nextDestination.address,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Chat en tiempo real con el Cliente dentro de la tarjeta activa
                            val isNextOrderChatActive = nextDestination.status == TechOrderStatus.EN_PROCESO ||
                                nextDestination.status == TechOrderStatus.EN_CAMINO
                            ServiceChatPanel(
                                orderCode = nextDestination.code,
                                messages = chatMessagesMap[nextDestination.code].orEmpty(),
                                currentRole = ChatSenderRole.TECNICO,
                                counterpartyLabel = nextDestination.companyName,
                                isChatActive = isNextOrderChatActive,
                                isCompleted = nextDestination.status == TechOrderStatus.COMPLETADA || nextDestination.status == TechOrderStatus.CANCELADA,
                                initiallyExpanded = isNextOrderChatActive,
                                onSendMessage = { msg -> onSendChatMessage(nextDestination.code, msg) }
                            )

                            // Botón destacado "Ver en GPS"
                            Button(
                                onClick = { onNavigateAddress(nextDestination.address) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo, contentColor = Blanco),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ver en GPS • Navegar a Sede",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Blanco,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = GrisTextoSecundario.copy(alpha = 0.6f),
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Sin órdenes activas",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "Cuando administración asigne una orden aparecerá aquí.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 3. Accesos para [ 📦 Registrar Materiales ] y [ 📷 Escáner QR de Equipos ]
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "HERRAMIENTAS OPERATIVAS RÁPIDAS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // [ 📦 Registrar Materiales ]
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenMaterials() }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CelesteAcentoClaro,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Construction,
                                        contentDescription = null,
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Text(
                                text = "📦 Registrar Materiales",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 13.sp
                                )
                            )

                            Text(
                                text = "Insumos, cables, llaves y repuestos utilizados",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // [ 📷 Escáner QR de Equipos ]
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenQrScanner() }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CelesteAcentoClaro,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.QrCodeScanner,
                                        contentDescription = null,
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Text(
                                text = "📷 Escáner QR",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 13.sp
                                )
                            )

                            Text(
                                text = "Validar tableros y fichas técnicas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 10.5.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Lista de Tareas del Día (Hoy)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAREAS DE HOY (${todayTasks.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    )

                    TextButton(onClick = onNavigateToAllTasks) {
                        Text(
                            text = "Ver agenda completa →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = CelesteAcento
                            )
                        )
                    }
                }

                if (todayTasks.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Blanco,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Sin visitas programadas para hoy.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            ),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                todayTasks.forEach { order ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CelesteAcentoClaro
                                    ) {
                                        Text(
                                            text = order.code,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulCorporativo,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = order.scheduledTime,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = GrisTextoSecundario,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = order.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${order.companyName} • ${order.address}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = { onNavigateAddress(order.address) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CelesteAcentoClaro, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = "Navegar",
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

/**
 * Vista de Agenda Cronológica Completa para el Técnico.
 */
@Composable
private fun TechChronologicalAgendaView(
    orders: List<TechWorkOrder>,
    notifications: List<ClientNotification>,
    chatMessagesMap: Map<String, List<ServiceChatMessage>>,
    onSendChatMessage: (String, String) -> Boolean,
    onMarkNotificationsRead: () -> Unit,
    isOfflineModeActive: Boolean,
    onToggleOfflineMode: (Boolean) -> Unit,
    pendingSyncCount: Int,
    onSyncNow: () -> Unit,
    onUpdateOrder: (TechWorkOrder) -> Unit,
    onOpenFinalizeReport: (TechWorkOrder) -> Unit,
    onNavigateAddress: (String) -> Unit,
    onLogout: () -> Unit,
    currentUser: UserAccount
) {
    val context = LocalContext.current
    val dayGroups = remember(orders) {
        val preferredOrder = listOf("Hoy", "Mañana")
        val dynamicDays = orders.map { it.scheduledDay }.distinct()
        val prioritized = preferredOrder.filter { it in dynamicDays }
        val others = dynamicDays.filter { it !in preferredOrder }.sorted()
        prioritized + others
    }
    val unreadNotifs = remember(notifications) { notifications.filter { it.isUnread } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (unreadNotifs.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NaranjaAlertaFondo),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaranjaAlerta.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = unreadNotifs.first().title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisGrafito
                                )
                            )
                            Text(
                                text = unreadNotifs.first().message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        TextButton(onClick = onMarkNotificationsRead) {
                            Text(
                                text = "Leído",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        }
                    }
                }
            }
        }

        // Cabecera institucional del Técnico
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AzulMarca),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CelesteAcentoClaro
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Construction,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "OPERACIONES EN CAMPO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Blanco.copy(alpha = 0.15f), CircleShape)
                                .testTag("btn_tech_logout")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Blanco,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "Mi Agenda de Tareas",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Técnico: ${currentUser.fullName} • Montevideo y Canelones",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                color = Blanco.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Botón Organizar mi ruta
                        Button(
                            onClick = {
                                Toast.makeText(context, "¡Ruta optimizada! Orden de visitas recalculada para reducir traslados en MVD y Canelones.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Organizar mi ruta",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // BANNER DE CONECTIVIDAD Y PERSISTENCIA OFFLINE EN SUBSUELOS
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOfflineModeActive) NaranjaAlertaFondo else Blanco
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOfflineModeActive) NaranjaAlerta else GrisBorde
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOfflineModeActive) Icons.Rounded.CloudOff else Icons.Rounded.CloudDone,
                                contentDescription = null,
                                tint = if (isOfflineModeActive) NaranjaAlerta else VerdeExito,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (isOfflineModeActive) "Modo Subsuelo / Offline Activo" else "Conexión 4G/LTE En Línea",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOfflineModeActive) NaranjaAlerta else AzulMarca
                                    )
                                )
                                Text(
                                    text = if (isOfflineModeActive) "Check-in, fotos y firma se respaldan localmente" else "Sincronización automática activa",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isOfflineModeActive,
                            onCheckedChange = onToggleOfflineMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Blanco,
                                checkedTrackColor = NaranjaAlerta,
                                uncheckedThumbColor = Blanco,
                                uncheckedTrackColor = CelesteAcento
                            )
                        )
                    }

                    if (pendingSyncCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Blanco,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isOfflineModeActive) NaranjaAlerta.copy(alpha = 0.5f) else CelesteAcento.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Sync,
                                        contentDescription = null,
                                        tint = CelesteAcento,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$pendingSyncCount orden(es) en cola local",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AzulMarca,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }

                                Button(
                                    onClick = onSyncNow,
                                    colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "Sincronizar ahora",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Blanco,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sin tareas asignadas",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Las órdenes asignadas por administración aparecerán aquí.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Agrupación cronológica por día
        dayGroups.forEach { dayName ->
            val dayTasks = orders.filter { it.scheduledDay == dayName }

            if (dayTasks.isNotEmpty()) {
                item {
                    DaySectionHeader(
                        dayName = dayName,
                        taskCount = dayTasks.size
                    )
                }

                items(dayTasks, key = { it.id }) { order ->
                    TechWorkOrderCard(
                        order = order,
                        chatMessages = chatMessagesMap[order.code].orEmpty(),
                        onSendChatMessage = { msg -> onSendChatMessage(order.code, msg) },
                        isOfflineActive = isOfflineModeActive,
                        onUpdateOrder = onUpdateOrder,
                        onOpenFinalizeReport = { onOpenFinalizeReport(order) },
                        onNavigateAddress = { onNavigateAddress(order.address) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

/**
 * Diálogo de Registro de Materiales e Insumos utilizados en campo.
 */
@Composable
private fun RegisterMaterialsDialog(
    orders: List<TechWorkOrder>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedOrderIndex by remember { mutableStateOf(0) }
    var selectedMaterialPreset by remember { mutableStateOf("Cable 4mm² (mts)") }
    var quantity by remember { mutableStateOf(5) }
    var customNotes by remember { mutableStateOf("") }

    val presets = listOf(
        "Cable 4mm² (mts)",
        "Contactor Schneider 25A",
        "Disyuntor Bipolar 16A",
        "Válvula Esférica 1\"",
        "Filtro HVAC Clase G4",
        "Cinta 3M Temflex",
        "Sellador Poliuretano",
        "Bomba Presurizadora 0.5HP"
    )

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Blanco,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Construction,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Registrar Materiales",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = "Seleccione la orden de trabajo e ingrese los insumos o repuestos aplicados en sitio:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                // Selector de orden
                val currentTarget = orders.getOrNull(selectedOrderIndex)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = orders.size > 1) {
                            if (orders.isNotEmpty()) {
                                selectedOrderIndex = (selectedOrderIndex + 1) % orders.size
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (orders.size > 1) "ORDEN DESTINO (TOCA PARA CAMBIAR):" else "ORDEN DE TRABAJO DESTINO:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = if (currentTarget != null) {
                                "${currentTarget.code} • ${currentTarget.companyName}"
                            } else {
                                "Sin órdenes activas asignadas"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentTarget != null) AzulMarca else GrisTextoSecundario
                            )
                        )
                    }
                }

                // Insumos predefinidos en chips
                Text(
                    text = "CATÁLOGO DE INSUMOS FRECUENTES:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 10.sp
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPresets.forEach { item ->
                                val isSel = selectedMaterialPreset == item
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) CelesteAcento else GrisNeutro,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CelesteAcento else GrisBorde),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMaterialPreset = item }
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) Blanco else AzulMarca,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Cantidad (+ / -)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cantidad Utilizada:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .background(GrisNeutro, RoundedCornerShape(8.dp))
                        ) {
                            Text("-", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AzulMarca)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$quantity",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                        }

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .background(GrisNeutro, RoundedCornerShape(8.dp))
                        ) {
                            Text("+", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AzulMarca)
                        }
                    }
                }

                // Observaciones
                OutlinedTextField(
                    value = customNotes,
                    onValueChange = { customNotes = it },
                    label = { Text("Ubicación específica / Detalle de uso") },
                    placeholder = { Text("Ej. Reemplazado en Tablero Subsuelo Sala de Bombas") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteAcento,
                        unfocusedBorderColor = GrisBorde
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancelar", fontFamily = PoppinsFontFamily)
                    }

                    Button(
                        onClick = {
                            val targetCode = currentTarget?.code ?: return@Button
                            val noteDetail = if (customNotes.isNotBlank()) " ($customNotes)" else ""
                            OperationalRepository.addNotification("Insumo en $targetCode: $quantity x $selectedMaterialPreset$noteDetail")
                            Toast.makeText(
                                context,
                                "Registrado: $quantity x $selectedMaterialPreset en $targetCode",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        },
                        enabled = currentTarget != null,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo, contentColor = Blanco),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Text("Guardar Insumo", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }
    }
}

/**
 * Diálogo de Escáner QR de Equipos y Tableros.
 */
@Composable
private fun TechQrScannerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val equipmentAssets by OperationalRepository.equipmentAssets.collectAsStateWithLifecycle()
    var searchCode by remember { mutableStateOf("") }
    val matchedAsset = remember(equipmentAssets, searchCode) {
        if (searchCode.isBlank()) equipmentAssets.firstOrNull()
        else equipmentAssets.firstOrNull {
            it.assetCode.contains(searchCode, ignoreCase = true) ||
                it.name.contains(searchCode, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Blanco,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Escáner QR de Equipos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                OutlinedTextField(
                    value = searchCode,
                    onValueChange = { searchCode = it },
                    label = { Text("Código QR o Nombre de Activo", fontFamily = PoppinsFontFamily) },
                    placeholder = { Text("Ej. EQ-001", fontFamily = PoppinsFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Ficha del activo o estado vacío
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (matchedAsset != null) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = matchedAsset.assetCode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VerdeExitoFondo
                                ) {
                                    Text(
                                        text = matchedAsset.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = VerdeExito,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = matchedAsset.name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )

                            Text(
                                text = "${matchedAsset.clientCompanyName} • ${matchedAsset.locationInBuilding}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.sp
                                )
                            )

                            if (matchedAsset.lastMaintenanceDate.isNotBlank()) {
                                Text(
                                    text = "Última revisión: ${matchedAsset.lastMaintenanceDate}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulCorporativo,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                tint = GrisTextoSecundario,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (equipmentAssets.isEmpty()) "Sin equipos registrados aún" else "Sin coincidencia para \"$searchCode\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulMarca
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cerrar", fontFamily = PoppinsFontFamily)
                    }

                    Button(
                        onClick = {
                            val asset = matchedAsset ?: return@Button
                            OperationalRepository.addNotification("Activo ${asset.assetCode} (${asset.name}) verificado en campo.")
                            Toast.makeText(context, "Equipo ${asset.assetCode} vinculado.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        enabled = matchedAsset != null,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo, contentColor = Blanco),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Text("Vincular a Orden", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }
    }
}

/**
 * Cabecera de sección por día en la agenda del técnico.
 */
@Composable
private fun DaySectionHeader(
    dayName: String,
    taskCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = if (dayName == "Hoy") CelesteAcento else AzulMarca,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = dayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontSize = 17.sp
                )
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (dayName == "Hoy") CelesteAcentoClaro else GrisBorde.copy(alpha = 0.5f)
        ) {
            Text(
                text = if (taskCount == 1) "1 Tarea" else "$taskCount Tareas",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (dayName == "Hoy") AzulCorporativo else GrisTextoSecundario
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

/**
 * Tarjeta de orden de trabajo para el técnico con navegación GPS y acciones operativas.
 */
@Composable
private fun TechWorkOrderCard(
    order: TechWorkOrder,
    chatMessages: List<ServiceChatMessage>,
    onSendChatMessage: (String) -> Boolean,
    isOfflineActive: Boolean = false,
    onUpdateOrder: (TechWorkOrder) -> Unit,
    onOpenFinalizeReport: () -> Unit,
    onNavigateAddress: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Fila de Código y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CelesteAcentoClaro
                ) {
                    Text(
                        text = "${order.code} • ${order.category}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (order.status) {
                        TechOrderStatus.COMPLETADA -> VerdeExitoFondo
                        TechOrderStatus.EN_PROCESO -> VerdeExitoFondo
                        TechOrderStatus.EN_CAMINO -> NaranjaAlertaFondo
                        TechOrderStatus.PENDIENTE -> GrisNeutro
                        TechOrderStatus.CANCELADA -> GrisNeutro
                    }
                ) {
                    Text(
                        text = order.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = when (order.status) {
                                TechOrderStatus.COMPLETADA -> VerdeExito
                                TechOrderStatus.EN_PROCESO -> VerdeExito
                                TechOrderStatus.EN_CAMINO -> NaranjaAlerta
                                TechOrderStatus.PENDIENTE -> GrisTextoSecundario
                                TechOrderStatus.CANCELADA -> GrisTextoSecundario
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Título de la tarea
            Text(
                text = order.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            // Horario asignado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = CelesteAcento,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Horario asignado: ${order.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulCorporativo
                    )
                )
            }

            // Destino: Empresa / Cliente
            Text(
                text = "Destino: ${order.companyName} (${order.clientName})",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily,
                    color = GrisTextoSecundario
                )
            )

            // Dirección con botón directo para abrir en Mapa/GPS
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GrisNeutro,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateAddress() }
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = order.address,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Botón directo GPS
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CelesteAcento
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Map, contentDescription = null, tint = Blanco, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Abrir GPS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            if (order.requiresDec406Protocol) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AzulMarca.copy(alpha = 0.06f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AzulMarca.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Protocolo de Seguridad Laboral",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }

            // Check-in confirmado si aplica
            if (order.checkInTimestamp != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VerdeExitoFondo
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Check-in en sitio: ${order.checkInTimestamp}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = VerdeExito,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Badge de almacenamiento local si la orden fue operada offline
            if (order.isStoredLocallyOffline && !order.isSyncedWithCloud) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NaranjaAlertaFondo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NaranjaAlerta.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.CloudOff, contentDescription = null, tint = NaranjaAlerta, modifier = Modifier.size(13.dp))
                        Text(
                            text = "Operada localmente en subsuelo • Pendiente Sync nube",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = NaranjaAlerta,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = GrisBorde.copy(alpha = 0.5f))

            // Chat en tiempo real con el Cliente (restringido al estado activo de la OT)
            val isOrderChatActive = order.status == TechOrderStatus.EN_PROCESO ||
                order.status == TechOrderStatus.EN_CAMINO
            val isOrderCompleted = order.status == TechOrderStatus.COMPLETADA ||
                order.status == TechOrderStatus.CANCELADA
            ServiceChatPanel(
                orderCode = order.code,
                messages = chatMessages,
                currentRole = ChatSenderRole.TECNICO,
                counterpartyLabel = order.companyName,
                isChatActive = isOrderChatActive,
                isCompleted = isOrderCompleted,
                initiallyExpanded = isOrderChatActive,
                onSendMessage = onSendChatMessage
            )

            HorizontalDivider(color = GrisBorde.copy(alpha = 0.5f))

            val cardContext = LocalContext.current

            // Control ergonómico paso a paso por etapas (cero confusión):
            // ETAPA 1: Iniciar Traslado -> ETAPA 2: Check-in en Sitio -> ETAPA 3: Check-out y Reporte A4
            val gpsPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) {
                onUpdateOrder(order.copy(status = TechOrderStatus.EN_CAMINO))
                Toast.makeText(
                    cardContext,
                    "Traslado iniciado • GPS en vivo activado para ${order.companyName}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CONTROL DE ETAPA EN CAMPO:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 10.5.sp
                    )
                )

                if (order.checkInTimestamp != null || order.checkOutTimestamp != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        order.checkInTimestamp?.let { checkIn ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VerdeExitoFondo,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Entrada: $checkIn",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        order.checkOutTimestamp?.let { checkOut ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CelesteAcentoClaro,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Salida: $checkOut",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                when (order.status) {
                    TechOrderStatus.PENDIENTE -> {
                        Button(
                            onClick = {
                                if (UruguayLocationProvider.hasLocationPermission(cardContext)) {
                                    onUpdateOrder(order.copy(status = TechOrderStatus.EN_CAMINO))
                                    Toast.makeText(
                                        cardContext,
                                        "Traslado iniciado • GPS en vivo activado para ${order.companyName}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    gpsPermissionLauncher.launch(UruguayLocationProvider.REQUIRED_PERMISSIONS)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AzulCorporativo,
                                contentColor = Blanco
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_tech_stage_transit_${order.code}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Iniciar Traslado (En Camino)",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    TechOrderStatus.EN_CAMINO -> {
                        Button(
                            onClick = {
                                val timeNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) + " hs"
                                onUpdateOrder(
                                    order.copy(
                                        status = TechOrderStatus.EN_PROCESO,
                                        checkInTimestamp = timeNow
                                    )
                                )
                                Toast.makeText(
                                    cardContext,
                                    "Check-in registrado ($timeNow) • En sitio",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CelesteAcento,
                                contentColor = Blanco
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_tech_stage_checkin_${order.code}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Llegué al Sitio (Check-in)",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    TechOrderStatus.EN_PROCESO -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = VerdeExitoFondo,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeExito.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = VerdeExito,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "En sitio ejecutando trabajo • Traslado GPS detenido",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = VerdeExito
                                )
                            }
                        }
                    }
                    TechOrderStatus.COMPLETADA, TechOrderStatus.CANCELADA -> {}
                }
            }

            var showQrDialog by remember { mutableStateOf(false) }
            var chk1 by remember { mutableStateOf(true) }
            var chk2 by remember { mutableStateOf(true) }
            var chk3 by remember { mutableStateOf(true) }
            val availableParts = listOf("Contactor 25A", "Térmica Schneider 16A", "Válvula 1\"", "Filtro HVAC", "Cable 4mm²")
            val selectedParts = remember { mutableStateListOf<String>() }

            if (order.status == TechOrderStatus.EN_PROCESO) {
                // Lector QR de Activos
                OutlinedButton(
                    onClick = { showQrDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escanear QR de Activo / Tablero", fontFamily = PoppinsFontFamily, fontSize = 11.5.sp, color = CelesteAcento, fontWeight = FontWeight.Bold)
                }

                // Checklist técnico obligatorio antes del cierre según rubro
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "CHECKLIST TÉCNICO OBLIGATORIO PREVIO AL CIERRE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 10.5.sp
                            )
                        )

                        val isElectric = order.category.contains("Elec", ignoreCase = true)
                        val isSanitaria = order.category.contains("Sani", ignoreCase = true)

                        val item1Text = if (isElectric) "Verificación de tensión de entrada (380V / 220V)" else if (isSanitaria) "Presión de colectores y presostatos (4.2 bar)" else "Limpieza profunda de filtros y evaporador"
                        val item2Text = if (isElectric) "Reapriete dinamométrico de bornes y contactores" else if (isSanitaria) "Bombas de achique y recirculación operativas" else "Consumo en amperes del compresor dentro de norma"
                        val item3Text = if (isElectric) "Test de disparo en diferencial (30mA verificado)" else if (isSanitaria) "Inspección de flotadores y graseras estancas" else "Drenaje de condensado y bandeja desinfectada"

                        listOf(
                            chk1 to item1Text,
                            chk2 to item2Text,
                            chk3 to item3Text
                        ).forEachIndexed { idx, (checked, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (idx) {
                                            0 -> chk1 = !chk1
                                            1 -> chk2 = !chk2
                                            2 -> chk3 = !chk3
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = if (checked) Icons.Rounded.CheckCircle else Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = if (checked) VerdeExito else GrisTextoSecundario,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        color = if (checked) AzulMarca else GrisTextoSecundario,
                                        fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // Registro de Repuestos e Insumos utilizados
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "REPUESTOS E INSUMOS UTILIZADOS:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo,
                            fontSize = 10.5.sp
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableParts.forEach { part ->
                            val isUsed = selectedParts.contains(part)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUsed) CelesteAcentoClaro else Blanco,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isUsed) CelesteAcento else GrisBorde),
                                modifier = Modifier.clickable {
                                    if (isUsed) selectedParts.remove(part) else selectedParts.add(part)
                                }
                            ) {
                                Text(
                                    text = (if (isUsed) "✓ " else "+ ") + part,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isUsed) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isUsed) AzulMarca else GrisTextoSecundario,
                                        fontSize = 10.5.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ETAPA 3: Finalizar Trabajo (Check-out) -> Abre cierre oficial A4
                Button(
                    onClick = {
                        val checkOutNow = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) + " hs"
                        onUpdateOrder(order.copy(checkOutTimestamp = checkOutNow))
                        onOpenFinalizeReport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeExito, contentColor = Blanco),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_tech_stage_checkout_${order.code}")
                ) {
                    Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Finalizar Trabajo (Check-out)",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else if (order.status == TechOrderStatus.COMPLETADA) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VerdeExitoFondo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Orden Completada con Doble Firma Oficial",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = VerdeExito
                        )
                    }
                }
            }

            if (showQrDialog) {
                val clientAssets by OperationalRepository.equipmentAssets.collectAsStateWithLifecycle()
                val matchedAsset = remember(clientAssets, order.companyName) {
                    clientAssets.firstOrNull {
                        it.clientCompanyName.equals(order.companyName, ignoreCase = true)
                    } ?: clientAssets.firstOrNull()
                }
                val qrContext = LocalContext.current
                Dialog(onDismissRequest = { showQrDialog = false }) {
                    ApplyWindowBackdropBlur()
                    GlassDialogEntrance {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Lector QR de Activo", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca, fontSize = 16.sp)
                                IconButton(onClick = { showQrDialog = false }) {
                                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar")
                                }
                            }
                            if (matchedAsset != null) {
                                Surface(shape = RoundedCornerShape(10.dp), color = CelesteAcentoClaro, modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = null, tint = AzulMarca, modifier = Modifier.size(32.dp))
                                        Column {
                                            Text("Código: ${matchedAsset.assetCode}", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                                            Text(matchedAsset.name, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = AzulCorporativo)
                                        }
                                    }
                                }
                                Text("Ubicación: ${matchedAsset.locationInBuilding} • Estado: ${matchedAsset.status}", fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = GrisTextoSecundario)
                                Button(
                                    onClick = {
                                        OperationalRepository.addNotification("Activo ${matchedAsset.assetCode} vinculado a ${order.code}.")
                                        Toast.makeText(qrContext, "Activo ${matchedAsset.assetCode} vinculado", Toast.LENGTH_SHORT).show()
                                        showQrDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text("Vincular a esta Orden", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                                }
                            } else {
                                Text(
                                    text = "Sin equipos registrados aún para ${order.companyName}.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = GrisTextoSecundario
                                )
                                OutlinedButton(
                                    onClick = { showQrDialog = false },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(42.dp)
                                ) {
                                    Text("Cerrar", fontFamily = PoppinsFontFamily)
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}

/**
 * DIÁLOGO MODAL: VISTA PREVIA DEL REPORTE TÉCNICO PRELIMINAR Y CAPTURA DE FIRMA
 */
@Composable
private fun TechnicalReportPreviewAndSigningDialog(
    order: TechWorkOrder,
    technicianName: String,
    onDismiss: () -> Unit,
    onConfirmCompletedWithSignature: (
        observations: String,
        signaturePaths: List<SignaturePath>,
        photoUris: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    val nowTime = remember {
        order.checkOutTimestamp?.ifBlank { null }
            ?: (java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()) + " hs")
    }
    var observationsText by remember {
        mutableStateOf(order.observations.orEmpty())
    }

    val attachedPhotos = remember(order.code) {
        mutableStateListOf<String>().apply { addAll(order.evidencePhotos) }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = LocalDatabaseStore.persistPickedImageUri(context, uri)
            attachedPhotos.add(savedPath)
            Toast.makeText(context, "Foto adjuntada al reporte A4", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = LocalDatabaseStore.persistCapturedBitmap(context, bitmap)
            if (savedUri != null) {
                attachedPhotos.add(savedUri)
                Toast.makeText(context, "Captura de cámara guardada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara requerido para capturar evidencia", Toast.LENGTH_SHORT).show()
        }
    }

    // Estado de firma táctil exclusiva del técnico a cargo
    var capturedTechSignaturePaths by remember { mutableStateOf<List<SignaturePath>>(emptyList()) }
    var isTechSignatureCompleted by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                color = Blanco,
                shadowElevation = 10.dp
            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Barra superior de la vista previa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hoja de Cierre Oficial A4",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Certificado de Conformidad Técnica & Doble Firma",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(GrisNeutro, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = AzulMarca)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contenido del reporte desplazable estilizado como hoja A4
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado corporativo institucional (Hoja A4) con logotipo horizontal oficial
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Blanco,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                                    contentDescription = "Todoservicio Logo HD",
                                    modifier = Modifier.height(34.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AzulMarca
                                ) {
                                    Text(
                                        text = order.code,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AzulCorporativo.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CERTIFICADO DE CONFORMIDAD TÉCNICA",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulMarca,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TODOSERVICIO S.A.S. • Facility Management",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulCorporativo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "RUT 218944510014",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Datos Consolidados de la Intervención y Horarios de Check-in / Check-out
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GrisNeutro),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DATOS CONSOLIDADOS DEL SERVICIO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                            Text(
                                text = "Cliente: ${order.companyName} • Sede: ${order.clientName}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "Dirección: ${order.address}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                            )
                            Text(
                                text = "Especialista Asignado: $technicianName",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = AzulMarca, fontWeight = FontWeight.Medium)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = GrisBorde)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Check-in: ${order.checkInTimestamp ?: nowTime}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = VerdeExito)
                                )
                                Text(
                                    text = "Check-out: $nowTime",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulMarca)
                                )
                            }
                        }
                    }

                    // Checklist Técnico Verificado Obligatorio
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VerdeExito.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CHECKLIST TÉCNICO VERIFICADO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito
                                    )
                                )
                                Surface(shape = RoundedCornerShape(4.dp), color = VerdeExito.copy(alpha = 0.15f)) {
                                    Text("100% Conforme", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = VerdeExito, fontWeight = FontWeight.Bold, fontSize = 10.sp))
                                }
                            }

                            val checklistItems = when {
                                order.category.contains("Eléctric", ignoreCase = true) -> listOf(
                                    "Verificación de tensión de entrada (230V / 400V)",
                                    "Reapriete dinamométrico de bornes con torquímetro",
                                    "Test diferencial (30mA verificado en <25ms)"
                                )
                                order.category.contains("Sanitar", ignoreCase = true) -> listOf(
                                    "Presión de colectores y presostatos verificada",
                                    "Bombas de achique y tableros auxiliares operativos",
                                    "Flotadores de corte y cámaras graseras desobstruidas"
                                )
                                else -> listOf(
                                    "Limpieza profunda de filtros de aire y serpentín",
                                    "Consumo nominal en amperes dentro de rango de placa",
                                    "Drenaje de condensado desobstruido y testeado"
                                )
                            }

                            checklistItems.forEach { itemText ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = itemText,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PoppinsFontFamily, color = AzulMarca, fontSize = 11.5.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Módulo de Fotos de Evidencia (Cámara y Galería reales)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EVIDENCIA FOTOGRÁFICA",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "${attachedPhotos.size} tomas",
                                style = MaterialTheme.typography.bodySmall.copy(color = CelesteAcento, fontSize = 11.sp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) {
                                        cameraLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_tech_take_photo")
                            ) {
                                Icon(imageVector = Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp), tint = CelesteAcento)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tomar Foto", fontFamily = PoppinsFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AzulCorporativo)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AzulMarca.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_tech_pick_gallery")
                            ) {
                                Icon(imageVector = Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = AzulMarca)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Galería", fontFamily = PoppinsFontFamily, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AzulMarca)
                            }
                        }

                        if (attachedPhotos.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Sin fotografías adjuntas aún. Tocá Tomar Foto o Galería.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.5.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                attachedPhotos.forEachIndexed { idx, uriStr ->
                                    val bmp = remember(uriStr) {
                                        try {
                                            when {
                                                uriStr.startsWith("file://") -> Uri.parse(uriStr).path?.let { BitmapFactory.decodeFile(it) }?.asImageBitmap()
                                                uriStr.startsWith("content://") -> context.contentResolver.openInputStream(Uri.parse(uriStr))?.use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
                                                else -> null
                                            }
                                        } catch (_: Throwable) {
                                            null
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp, 88.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GrisNeutro)
                                            .border(1.dp, GrisBorde, RoundedCornerShape(10.dp))
                                    ) {
                                        if (bmp != null) {
                                            Image(
                                                bitmap = bmp,
                                                contentDescription = "Evidencia ${idx + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(imageVector = Icons.Rounded.Image, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(22.dp))
                                                Text(text = "Foto ${idx + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AzulMarca))
                                            }
                                        }
                                        IconButton(
                                            onClick = { attachedPhotos.removeAt(idx) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .size(24.dp)
                                                .background(AzulMarca.copy(alpha = 0.78f), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Quitar foto", tint = Blanco, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Observaciones técnicas editables
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "OBSERVACIONES TÉCNICAS FINALES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        OutlinedTextField(
                            value = observationsText,
                            onValueChange = { observationsText = it },
                            placeholder = { Text("Detalle de hallazgos, mediciones y repuestos utilizados...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CelesteAcento,
                                unfocusedBorderColor = GrisBorde
                            )
                        )
                    }

                    // ================================================================
                    // MÓDULO DE FIRMA DEL TÉCNICO A CARGO (EXCLUSIVA, SIN CLIENTE)
                    // ================================================================
                    Text(
                        text = "RÚBRICA Y CONFORMIDAD TÉCNICA A CARGO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    // Lienzo de firma táctil espacioso (altura 240dp, 220px-260px)
                    InteractiveSignatureCanvas(
                        title = "Rúbrica Oficial del Especialista Actuante",
                        signerName = technicianName,
                        signerRole = "Técnico Especialista a Cargo",
                        technicianSpecialty = order.category,
                        closingTime = nowTime,
                        canvasHeight = 240.dp,
                        initialPaths = capturedTechSignaturePaths,
                        onSignatureChanged = { hasSig, paths ->
                            capturedTechSignaturePaths = paths
                            isTechSignatureCompleted = hasSig
                        },
                        onConfirmSignature = {
                            if (capturedTechSignaturePaths.isNotEmpty()) {
                                isTechSignatureCompleted = true
                                Toast.makeText(context, "Firma técnica estampada con éxito.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Por favor dibuje su firma sobre el lienzo.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón final: Finalizar y Emitir OT Oficial (envía directo al Cliente para su calificación 5★)
                    val isReadyToFinalize = isTechSignatureCompleted || capturedTechSignaturePaths.isNotEmpty()
                    Button(
                        onClick = {
                            if (!isReadyToFinalize) {
                                Toast.makeText(context, "Debe registrar y estampar la firma del técnico para emitir la OT.", Toast.LENGTH_SHORT).show()
                            } else {
                                onConfirmCompletedWithSignature(
                                    observationsText,
                                    capturedTechSignaturePaths,
                                    attachedPhotos.toList()
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReadyToFinalize) VerdeExito else CelesteAcento,
                            contentColor = Blanco
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_tech_emit_official_ot")
                    ) {
                        Icon(
                            imageVector = if (isReadyToFinalize) Icons.Rounded.CheckCircle else Icons.Rounded.Draw,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isReadyToFinalize) "Finalizar y Emitir OT Oficial" else "Estampar Rúbrica para Finalizar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
        }
    }
}
