package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddBusiness
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Videocam
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ActiveTechnicianTracking
import com.example.model.AppDestination
import com.example.model.ChatSenderRole
import com.example.model.ClientProfile
import com.example.model.PendingQuote
import com.example.model.ServiceHistoryItem
import com.example.model.TaskOperationStatus
import com.example.ui.components.ServiceChatPanel
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.BouncingTouchSurface
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.components.LiveStatusBadge
import com.example.ui.components.PulsingLiveIndicator
import com.example.ui.components.modalBackdropBlur
import com.example.ui.theme.GrisPerlaFrio
import com.example.ui.theme.VerdeEsmeralda
import com.example.ui.theme.AmbarCalido
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.util.PdfReportGenerator

/**
 * ClientHomeScreen (Portal del Cliente de Todoservicio).
 * Diseñado con criterios UX de máxima legibilidad, botones amplios,
 * estado de servicio en tiempo real estilo Uber y gestión de cotizaciones/documentos.
 */
@Composable
fun HomeScreen(
    clientProfile: ClientProfile,
    onNavigate: (AppDestination) -> Unit,
    onOpenIdentityInfo: () -> Unit,
    onViewReport: ((ServiceHistoryItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Estado del Servicio en Tiempo Real conectado al Repositorio Operativo
    val repoTasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()
    val repoActiveTracking by OperationalRepository.activeTracking.collectAsStateWithLifecycle()
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    val chatMessagesMap by OperationalRepository.serviceChatMessages.collectAsStateWithLifecycle()

    val repoQuotes by OperationalRepository.quotes.collectAsStateWithLifecycle()
    val repoReports by OperationalRepository.serviceReports.collectAsStateWithLifecycle()

    val tenantTasks = remember(repoTasks, clientProfile) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        repoTasks.filter { task ->
            (clientProfile.clientId.isNotBlank() && task.clientId == clientProfile.clientId) ||
                (companyLower.isNotBlank() && task.clientName.trim().lowercase() == companyLower)
        }
    }
    val activeTask = remember(tenantTasks) {
        tenantTasks.firstOrNull { OperationalRepository.isTaskChatActive(it) }
    }
    val lastClosedOrPendingTask = remember(tenantTasks, chatMessagesMap) {
        tenantTasks.firstOrNull {
            it.status == TaskOperationStatus.FINALIZADA && !(chatMessagesMap[it.code].isNullOrEmpty())
        } ?: tenantTasks.firstOrNull { it.status == TaskOperationStatus.ABIERTA }
    }

    val isTechnicianEnCamino = activeTask != null
    val baseLiveTracking = remember(activeTask, repoActiveTracking, corporateClients, clientProfile) {
        activeTask?.let { task ->
            val matchedClient = corporateClients.firstOrNull { client ->
                (task.clientId.isNotBlank() && client.id == task.clientId) ||
                    client.name.equals(task.clientName, ignoreCase = true)
            }
            val matchedBranch = matchedClient?.branches?.firstOrNull { branch ->
                task.sedeName.isBlank() || branch.name.equals(task.sedeName, ignoreCase = true)
            } ?: matchedClient?.branches?.firstOrNull()

            val destLat = if (repoActiveTracking.serviceCode == task.code) {
                repoActiveTracking.destinationLatitude
            } else {
                matchedBranch?.lat ?: com.example.util.URUGUAY_DEFAULT_LAT
            }
            val destLng = if (repoActiveTracking.serviceCode == task.code) {
                repoActiveTracking.destinationLongitude
            } else {
                matchedBranch?.lng ?: com.example.util.URUGUAY_DEFAULT_LNG
            }
            val matchesRepoLive = repoActiveTracking.serviceCode == task.code &&
                (repoActiveTracking.clientId.isBlank() ||
                    repoActiveTracking.clientId == clientProfile.clientId ||
                    repoActiveTracking.clientName.equals(clientProfile.companyName, ignoreCase = true))

            val curLat = if (matchesRepoLive) repoActiveTracking.currentLatitude else com.example.util.URUGUAY_DEFAULT_LAT
            val curLng = if (matchesRepoLive) repoActiveTracking.currentLongitude else com.example.util.URUGUAY_DEFAULT_LNG
            val initialEta = com.example.util.UruguayLocationProvider.computeRoadGeodesicEta(curLat, curLng, destLat, destLng)

            ActiveTechnicianTracking(
                id = "TRK-${task.code}",
                serviceCode = task.code,
                serviceTitle = task.title,
                scheduledTime = "${task.timeSlot} hs",
                location = task.address.ifBlank { clientProfile.address },
                technicianName = task.technicianName.ifBlank { "Técnico Asignado" },
                technicianRole = "Especialista • ${task.category}",
                technicianPhone = if (matchesRepoLive) repoActiveTracking.technicianPhone else "",
                vehiclePlate = if (matchesRepoLive) repoActiveTracking.vehiclePlate else "",
                vehicleModel = if (matchesRepoLive && repoActiveTracking.vehicleModel.isNotBlank()) repoActiveTracking.vehicleModel else "Unidad Móvil",
                statusMessage = if (task.status == TaskOperationStatus.INICIO_EJECUCION) {
                    "Técnico en sitio (${task.checkInTime ?: "En curso"})"
                } else {
                    initialEta.toFormattedLabel()
                },
                etaMinutes = if (task.status == TaskOperationStatus.INICIO_EJECUCION) 0 else {
                    if (matchesRepoLive && repoActiveTracking.etaMinutes > 0) repoActiveTracking.etaMinutes else initialEta.etaMinutes
                },
                distanceKm = if (task.status == TaskOperationStatus.INICIO_EJECUCION) 0.0 else {
                    if (matchesRepoLive && repoActiveTracking.distanceKm > 0.0) repoActiveTracking.distanceKm else initialEta.distanceKm
                },
                isEnCamino = task.isEnCamino,
                currentLatitude = curLat,
                currentLongitude = curLng,
                destinationLatitude = destLat,
                destinationLongitude = destLng,
                destinationAddress = task.address.ifBlank { clientProfile.address },
                trackingStage = if (task.status == TaskOperationStatus.INICIO_EJECUCION) "En Sitio (Check-in)" else "En Trayecto",
                clientId = task.clientId,
                clientName = task.clientName
            )
        }
    }

    var liveEtaOverride by androidx.compose.runtime.remember(baseLiveTracking?.serviceCode) {
        mutableStateOf<com.example.util.RouteEtaResult?>(null)
    }
    androidx.compose.runtime.LaunchedEffect(
        baseLiveTracking?.serviceCode,
        baseLiveTracking?.currentLatitude,
        baseLiveTracking?.currentLongitude,
        baseLiveTracking?.destinationLatitude,
        baseLiveTracking?.destinationLongitude,
        baseLiveTracking?.isEnCamino
    ) {
        val trk = baseLiveTracking
        if (trk != null && trk.isEnCamino) {
            liveEtaOverride = com.example.util.UruguayLocationProvider.calculateRealRouteEta(
                originLat = trk.currentLatitude,
                originLng = trk.currentLongitude,
                destLat = trk.destinationLatitude,
                destLng = trk.destinationLongitude
            )
        } else {
            liveEtaOverride = null
        }
    }

    val liveActiveTracking = remember(baseLiveTracking, liveEtaOverride) {
        val trk = baseLiveTracking ?: return@remember null
        val eta = liveEtaOverride
        if (eta != null && trk.isEnCamino) {
            trk.copy(
                etaMinutes = eta.etaMinutes,
                distanceKm = eta.distanceKm,
                statusMessage = eta.toFormattedLabel(),
                etaSource = eta.sourceLabel
            )
        } else trk
    }
    // Estado del Tutorial Interactivo de Bienvenida (Onboarding)
    var showWelcomeTutorial by remember { mutableStateOf(false) }

    val tenantQuotes = remember(repoQuotes, clientProfile.companyName) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        repoQuotes.filter { quote ->
            companyLower.isNotBlank() && quote.clientCompanyName.trim().lowercase() == companyLower
        }
    }
    val pendingQuote = remember(tenantQuotes) {
        tenantQuotes.firstOrNull { !it.isApproved } ?: tenantQuotes.firstOrNull()
    }

    val recentReport = remember(repoReports, clientProfile) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        repoReports.firstOrNull { report ->
            (clientProfile.clientId.isNotBlank() && report.clientId == clientProfile.clientId) ||
                (companyLower.isNotBlank() && report.companyName.trim().lowercase() == companyLower)
        }
    }

    // Modales interactivos
    var showLiveTrackingModal by remember { mutableStateOf(false) }
    var showExpressConsultModal by remember { mutableStateOf(false) }
    var showQuoteApprovalModal by remember { mutableStateOf(false) }

    // Cierre automático inmediato del mapa en vivo cuando el técnico hace Check-in / Check-out
    androidx.compose.runtime.LaunchedEffect(activeTask?.code, activeTask?.isEnCamino) {
        if (activeTask == null || (!activeTask.isEnCamino && activeTask.status == TaskOperationStatus.FINALIZADA)) {
            showLiveTrackingModal = false
        }
    }

    val loadedClientLogoBitmap = remember(clientProfile.logoUri) {
        if (clientProfile.logoUri != null) {
            try {
                val uri = Uri.parse(clientProfile.logoUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else null
    }

    var conciergePrompt by remember { mutableStateOf("") }
    var conciergeAiResponse by remember { mutableStateOf<String?>(null) }
    var categorizedCategory by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .modalBackdropBlur(
                showLiveTrackingModal ||
                    showWelcomeTutorial ||
                    showExpressConsultModal ||
                    showQuoteApprovalModal
            )
            .background(GrisPerlaFrio)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // ====================================================================
        // 2. SECCIÓN SUPERIOR: SALUDO Y RESUMEN DE EMPRESA
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(listOf(Blanco, Color(0xFFE2E8F0)))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x0F102C54),
                        spotColor = Color(0x18102C54)
                    )
                    .testTag("client_welcome_card")
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
                            shape = RoundedCornerShape(20.dp),
                            color = if (clientProfile.planType != null) VerdeExitoFondo else NaranjaAlertaFondo
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (clientProfile.planType != null) VerdeExito else NaranjaAlerta, CircleShape)
                                )
                                Text(
                                    text = if (clientProfile.planType != null) "CUENTA CORPORATIVA ACTIVA" else "SIN PLAN ASIGNADO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (clientProfile.planType != null) VerdeExito else NaranjaAlerta,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        // Botón para volver a ver la Guía de Bienvenida / Tutorial
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier.clickable { showWelcomeTutorial = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Guía App",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = CelesteAcento,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Banner con Logotipo de la Empresa y Saludo Formal
                    val corporateGreetingName = clientProfile.companyName.ifBlank { clientProfile.clientName.ifBlank { "Cliente" } }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(54.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            if (loadedClientLogoBitmap != null) {
                                Image(
                                    bitmap = loadedClientLogoBitmap,
                                    contentDescription = "Logotipo de $corporateGreetingName",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    val initials = corporateGreetingName.split(" ")
                                        .filter { it.isNotBlank() }
                                        .take(2)
                                        .mapNotNull { it.firstOrNull()?.uppercase() }
                                        .joinToString("")
                                        .ifBlank { "TS" }
                                    Text(
                                        text = initials,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bienvenido a su portal, $corporateGreetingName",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 18.sp,
                                    lineHeight = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Sede: ${clientProfile.address}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    // Subtítulo con estado del plan y visitas
                    Text(
                        text = if (clientProfile.planType != null) {
                            "Plan Activo: ${clientProfile.planType} • ${clientProfile.coverage}"
                        } else {
                            "Sin plan activo asignado • Cobertura técnica integral"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = if (clientProfile.planType != null) GrisTextoSecundario else NaranjaAlerta,
                            fontWeight = if (clientProfile.planType != null) FontWeight.Normal else FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )

                    // Métricas del abono mensual con indicador de visitas
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GrisNeutro, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                        text = "ABONO MENSUAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulCorporativo,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${clientProfile.visitsThisMonth} de ${clientProfile.totalPlanVisits} visitas realizadas",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            Text(
                                text = "${clientProfile.totalPlanVisits - clientProfile.visitsThisMonth} disponibles",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = VerdeExito,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Barra de progreso de visitas del abono
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(clientProfile.visitsThisMonth.toFloat() / clientProfile.totalPlanVisits.toFloat().coerceAtLeast(1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CelesteAcento)
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // ASISTENTE VIRTUAL INTELIGENTE (IA CONCIERGE)
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.5.dp, CelesteAcento),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x154A90D9),
                        spotColor = Color(0x25102C54)
                    )
                    .testTag("ai_concierge_card")
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
                            shape = RoundedCornerShape(20.dp),
                            color = CelesteAcentoClaro
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "IA CONCIERGE TÉCNICO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = CelesteAcento,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Orientación 24/7",
                            fontSize = 11.sp,
                            color = GrisTextoSecundario,
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Text(
                        text = "¿En qué podemos ayudarte hoy?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 17.sp
                        )
                    )

                    Text(
                        text = "Describe tu incidencia técnica o consulta operativa. El asistente orienta la categorización de fallas y guía el despacho inmediato.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    )

                    // Campo de entrada conversacional
                    OutlinedTextField(
                        value = conciergePrompt,
                        onValueChange = { conciergePrompt = it },
                        placeholder = {
                            Text(
                                "Describe tu incidencia o consulta...",
                                fontSize = 12.sp,
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (conciergePrompt.isNotBlank()) {
                                        val promptLower = conciergePrompt.lowercase()
                                        when {
                                            promptLower.contains("fuga") || promptLower.contains("agua") || promptLower.contains("bomba") || promptLower.contains("grasa") -> {
                                                categorizedCategory = "Sanitaria"
                                                conciergeAiResponse = "Detectamos una incidencia en categoría Sanitaria / Bombas. Contamos con cuadrilla técnica equipada para presurización y saneamiento en Montevideo."
                                            }
                                            promptLower.contains("luz") || promptLower.contains("corte") || promptLower.contains("fase") || promptLower.contains("tablero") || promptLower.contains("disyuntor") -> {
                                                categorizedCategory = "Electricidad"
                                                conciergeAiResponse = "Clasificado como Electricidad & Seguridad. Equipo técnico eléctrico disponible para inspección y control de tableros."
                                            }
                                            promptLower.contains("aire") || promptLower.contains("hvac") || promptLower.contains("clima") || promptLower.contains("calor") || promptLower.contains("frio") -> {
                                                categorizedCategory = "Climatización"
                                                conciergeAiResponse = "Incidencia de Climatización / HVAC. Cuadrilla especialista en filtros y serpentinas lista para despacho."
                                            }
                                            promptLower.contains("seguridad") || promptLower.contains("certificacion") || promptLower.contains("bombero") || promptLower.contains("habilitacion") -> {
                                                categorizedCategory = "Seguridad Técnica"
                                                conciergeAiResponse = "Gestión técnica y emisión de informe oficial firmado por especialista."
                                            }
                                            else -> {
                                                categorizedCategory = "Mantenimiento General"
                                                conciergeAiResponse = "Consulta procesada. Recomendamos solicitar una visita de inspección general para evaluar la sede."
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CelesteAcento, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Send,
                                    contentDescription = "Enviar",
                                    tint = Blanco,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde,
                            focusedContainerColor = GrisNeutro.copy(alpha = 0.5f),
                            unfocusedContainerColor = GrisNeutro.copy(alpha = 0.5f)
                        )
                    )

                    // Pastillas interactivas de orientación rápida
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Orientación rápida en un toque:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        conciergePrompt = "Fuga de agua en sala de bombas"
                                        categorizedCategory = "Sanitaria"
                                        conciergeAiResponse = "Incidencia clasificada como Sanitaria. Cuadrilla de urgencia disponible en Montevideo con tiempo de respuesta estimado: 40 min."
                                    }
                            ) {
                                Text(
                                    text = "💧 Fuga de agua\n-> Sanitaria",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier.padding(8.dp),
                                    lineHeight = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        conciergePrompt = "Corte de luz parcial en local"
                                        categorizedCategory = "Electricidad"
                                        conciergeAiResponse = "Incidencia clasificada como Electricidad. Protocolo de contingencia y chequeo de fases activado."
                                    }
                            ) {
                                Text(
                                    text = "⚡ Corte de luz\n-> Electricidad",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier.padding(8.dp),
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        conciergePrompt = "Falla de climatización en salón"
                                        categorizedCategory = "Climatización"
                                        conciergeAiResponse = "Incidencia clasificada como Climatización (HVAC). Cuadrilla con termografía y filtros disponible."
                                    }
                            ) {
                                Text(
                                    text = "❄️ Falla de aire\n-> Climatización",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier.padding(8.dp),
                                    lineHeight = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        conciergePrompt = "Certificación de Seguridad Técnica"
                                        categorizedCategory = "Seguridad Técnica"
                                        conciergeAiResponse = "Protocolo de Seguridad Integral. Emisión de informe técnico oficial firmado."
                                    }
                            ) {
                                Text(
                                    text = "🔥 Seguridad Técnica\n-> Certificación",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier.padding(8.dp),
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // Respuesta y diagnóstico inteligente del Asistente
                    conciergeAiResponse?.let { responseText ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF102C54),
                            border = BorderStroke(1.dp, CelesteAcento),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = CelesteAcento,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Diagnóstico IA: ${categorizedCategory ?: "Asistencia Técnica"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }

                                Text(
                                    text = responseText,
                                    fontSize = 11.5.sp,
                                    color = Blanco.copy(alpha = 0.9f),
                                    fontFamily = PoppinsFontFamily,
                                    lineHeight = 15.sp
                                )

                                Button(
                                    onClick = {
                                        onNavigate(AppDestination.SOLICITAR_TRABAJO)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Build,
                                        contentDescription = null,
                                        tint = Blanco,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pedir Asistencia para esta Incidencia",
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontSize = 12.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 4. SECCIÓN DE ESTADO DE SERVICIOS EN TIEMPO REAL (CONDICIONAL ESTRICTO)
        // ====================================================================
        item {
            val hasActiveService = activeTask != null || (isTechnicianEnCamino && liveActiveTracking != null)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (hasActiveService) AzulMarca else GrisNeutro
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (hasActiveService) CelesteAcento else GrisBorde
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (hasActiveService) 12.dp else 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x15102C54),
                        spotColor = Color(0x22102C54)
                    )
                    .testTag("real_time_service_card")
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
                        if (hasActiveService && liveActiveTracking != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = VerdeEsmeralda.copy(alpha = 0.22f),
                                border = BorderStroke(1.dp, VerdeEsmeralda.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PulsingLiveIndicator(dotSize = 7.dp, pulseColor = VerdeEsmeralda)
                                    Text(
                                        text = if (activeTask?.status == TaskOperationStatus.INICIO_EJECUCION) {
                                            "SERVICIO EN CURSO • ${liveActiveTracking.serviceCode}"
                                        } else {
                                            "TÉCNICO EN CAMINO • ${liveActiveTracking.serviceCode}"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco,
                                            letterSpacing = 0.4.sp
                                        )
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GrisBorde.copy(alpha = 0.8f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GrisTextoSecundario, CircleShape)
                                    )
                                    Text(
                                        text = "SIN SERVICIOS EN CURSO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = GrisTextoSecundario,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (hasActiveService && liveActiveTracking != null) liveActiveTracking.scheduledTime else "Inactivo",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (hasActiveService) CelesteAcentoClaro else GrisTextoSecundario
                            )
                        )
                    }

                    if (hasActiveService && liveActiveTracking != null) {
                        val tracking = liveActiveTracking
                        val isChatOpen = if (activeTask != null) {
                            OperationalRepository.isTaskChatActive(activeTask)
                        } else {
                            true
                        }
                        Text(
                            text = tracking.serviceTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco,
                                fontSize = 17.sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = CelesteAcentoClaro,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tracking.location,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = Blanco.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Blanco.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Blanco.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Engineering,
                                    contentDescription = null,
                                    tint = Blanco,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tracking.technicianName} (${tracking.vehicleModel})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                )
                                Text(
                                    text = if (!tracking.isEnCamino || tracking.etaMinutes == 0) {
                                        tracking.statusMessage
                                    } else {
                                        "Tiempo estimado de llegada: ${tracking.etaMinutes} min (${tracking.distanceKm} km)"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = CelesteAcentoClaro,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        // Chat en tiempo real integrado dentro de la tarjeta 'Servicio en Curso'
                        ServiceChatPanel(
                            orderCode = tracking.serviceCode,
                            messages = chatMessagesMap[tracking.serviceCode].orEmpty(),
                            currentRole = ChatSenderRole.CLIENTE,
                            counterpartyLabel = tracking.technicianName,
                            isChatActive = isChatOpen,
                            isCompleted = activeTask?.status == TaskOperationStatus.FINALIZADA,
                            initiallyExpanded = true,
                            onSendMessage = { message ->
                                val sent = OperationalRepository.sendServiceChatMessage(
                                    orderCode = tracking.serviceCode,
                                    senderRole = ChatSenderRole.CLIENTE,
                                    senderName = clientProfile.companyName.ifBlank { clientProfile.clientName },
                                    rawMessage = message
                                )
                                if (!sent) {
                                    Toast.makeText(
                                        context,
                                        "El chat solo está disponible mientras la OT está activa.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                sent
                            }
                        )

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showLiveTrackingModal = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Blanco.copy(alpha = 0.18f),
                                contentColor = Blanco
                            ),
                            border = BorderStroke(1.dp, Blanco.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_track_technician_live")
                        ) {
                            Icon(imageVector = Icons.Rounded.NearMe, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ver Ubicación en Vivo",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco
                                )
                            )
                        }
                    } else {
                        // Estado Vacío Inactivo Estricto
                        Text(
                            text = "Sin intervenciones activas en este momento",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 15.sp
                            )
                        )

                        Text(
                            text = "El chat con el técnico se habilita automáticamente cuando la OT pasa a estado En Camino o En Curso.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario,
                                fontSize = 12.sp
                            )
                        )

                        if (lastClosedOrPendingTask != null) {
                            ServiceChatPanel(
                                orderCode = lastClosedOrPendingTask.code,
                                messages = chatMessagesMap[lastClosedOrPendingTask.code].orEmpty(),
                                currentRole = ChatSenderRole.CLIENTE,
                                counterpartyLabel = lastClosedOrPendingTask.technicianName,
                                isChatActive = false,
                                isCompleted = lastClosedOrPendingTask.status == TaskOperationStatus.FINALIZADA,
                                initiallyExpanded = !(chatMessagesMap[lastClosedOrPendingTask.code].isNullOrEmpty()),
                                onSendMessage = { false }
                            )
                        }

                        // Indicador estático (sin botón muerto)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisBorde,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsCar,
                                    contentDescription = null,
                                    tint = GrisTextoSecundario,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sin móvil activo",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = GrisTextoSecundario
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        // 3. MÓDULO DE ACCESOS RÁPIDOS Y ACCIONES PRINCIPALES (4 BOTONES GRANDES)
        // ====================================================================
        item {
            Text(
                text = "Accesos y Acciones Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Fila 1: Botón 1 y Botón 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BigActionCard(
                        title = "Pedir Asistencia",
                        subtitle = "Correctivos y urgencias",
                        icon = Icons.Rounded.AddTask,
                        iconTint = CelesteAcento,
                        borderColor = CelesteAcento,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigate(AppDestination.SOLICITAR_TRABAJO)
                        },
                        testTag = "btn_quick_pedir_asistencia",
                        modifier = Modifier.weight(1f)
                    )

                    BigActionCard(
                        title = "Técnico en Camino",
                        subtitle = "Monitoreo GPS en vivo",
                        icon = Icons.Rounded.Map,
                        iconTint = AzulCorporativo,
                        borderColor = GrisBorde,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigate(AppDestination.MAPA_GPS)
                        },
                        testTag = "btn_quick_tecnico_camino",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Botón 3 y Botón 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BigActionCard(
                        title = "Ver Mis Reportes",
                        subtitle = "Hojas de servicio y PDFs",
                        icon = Icons.Rounded.Description,
                        iconTint = AzulCorporativo,
                        borderColor = GrisBorde,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigate(AppDestination.HISTORIAL_SERVICIOS)
                        },
                        testTag = "btn_quick_ver_reportes",
                        modifier = Modifier.weight(1f)
                    )

                    BigActionCard(
                        title = "Firmar Trabajo",
                        subtitle = "Conformidad y doble firma",
                        icon = Icons.Rounded.HistoryEdu,
                        iconTint = CelesteAcento,
                        borderColor = CelesteAcento,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigate(AppDestination.DOBLE_FIRMA)
                        },
                        testTag = "btn_quick_firmar_trabajo",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ====================================================================
        // 5. SECCIÓN INFERIOR: ÚLTIMOS DOCUMENTOS Y COTIZACIONES PENDIENTES
        // ====================================================================
        item {
            Text(
                text = "Cotizaciones y Documentos Recientes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )
        }

        // Tarjeta de Cotización Recibida ("Esperando Aprobación" con botón directo)
        if (pendingQuote != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (pendingQuote.isApproved) VerdeExito.copy(alpha = 0.5f) else NaranjaAlerta.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x10102C54),
                            spotColor = Color(0x18102C54)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (pendingQuote.isApproved) VerdeExitoFondo else NaranjaAlertaFondo
                            ) {
                                Text(
                                    text = if (pendingQuote.isApproved) "Aprobada por Cliente" else "Esperando Aprobación",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pendingQuote.isApproved) VerdeExito else NaranjaAlerta
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = pendingQuote.quoteCode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                        }

                        Text(
                            text = pendingQuote.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        Text(
                            text = "Monto: USD ${pendingQuote.amountUsd} • Válida hasta: ${pendingQuote.validUntil}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (pendingQuote.isApproved) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VerdeExitoFondo
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(16.dp))
                                        Text("Orden Coordinada", fontFamily = PoppinsFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdeExito)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { showQuoteApprovalModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .testTag("btn_view_approve_quote")
                                ) {
                                    Icon(imageVector = Icons.Rounded.PriceCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ver y Aprobar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tarjeta de Reporte Técnico Reciente con Botón de Descarga PDF
        if (recentReport != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x10102C54),
                            spotColor = Color(0x18102C54)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                    text = recentReport.serviceCode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = recentReport.executionDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = recentReport.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        Text(
                            text = "Firma: ${recentReport.signedBy}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulCorporativo,
                                fontSize = 11.5.sp
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val pdfFile = PdfReportGenerator.generatePdfReport(
                                        context,
                                        PdfReportGenerator.ReportData(
                                            orderCode = recentReport.serviceCode.replace("#", ""),
                                            serviceTitle = recentReport.title,
                                            category = recentReport.category,
                                            executionDate = recentReport.executionDate,
                                            checkInTime = recentReport.checkInTime,
                                            checkOutTime = recentReport.checkOutTime.ifBlank { recentReport.executionTime },
                                            leadTechnician = recentReport.leadTechnician,
                                            clientName = clientProfile.clientName,
                                            companyName = clientProfile.companyName,
                                            clientRut = clientProfile.rut,
                                            clientAddress = recentReport.address.ifBlank { clientProfile.address },
                                            findingsSummary = recentReport.summary,
                                            npsRating = recentReport.clientRatingStars,
                                            npsComment = recentReport.clientRatingComment
                                        )
                                    )
                                    if (pdfFile != null) {
                                        PdfReportGenerator.openPdf(context, pdfFile)
                                    } else {
                                        Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("btn_download_pdf_report")
                            ) {
                                Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Descargar Reporte PDF", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = AzulCorporativo)
                            }
                        }
                    }
                }
            }
        }

        if (pendingQuote == null && recentReport == null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.6f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Sin documentos aún",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Tus cotizaciones y reportes aparecerán aquí.",
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

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }

    // Modal de Seguimiento en Vivo
    if (showLiveTrackingModal && liveActiveTracking != null) {
        LiveTrackingModal(
            tracking = liveActiveTracking,
            clientSenderName = clientProfile.companyName.ifBlank { clientProfile.clientName },
            isChatActive = activeTask == null || OperationalRepository.isTaskChatActive(activeTask),
            onDismiss = { showLiveTrackingModal = false }
        )
    }

    // Modal de Tutorial de Bienvenida (Onboarding)
    if (showWelcomeTutorial) {
        ClientWelcomeOnboardingModal(
            onDismiss = { showWelcomeTutorial = false }
        )
    }

    // Modal de Consulta Exprés
    if (showExpressConsultModal) {
        ExpressConsultationModal(
            onDismiss = { showExpressConsultModal = false },
            onSubmit = {
                Toast.makeText(context, "Consulta enviada. Guardia notificada.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal de Aprobación de Cotización
    if (showQuoteApprovalModal && pendingQuote != null) {
        QuoteApprovalModal(
            quote = pendingQuote,
            onDismiss = { showQuoteApprovalModal = false },
            onApprove = {
                OperationalRepository.approveQuote(pendingQuote.id)
                showQuoteApprovalModal = false
                Toast.makeText(context, "Cotización aprobada.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

/**
 * Tarjeta de Acción Grande para la cuadrícula 2x2 accesible.
 * Estilizada con radio de curvatura de 20dp, sombra volumétrica 3D y
 * micro-interacción táctil press-down al 97% con EaseOutCubic.
 */
@Composable
private fun BigActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(durationMillis = 150, easing = EaseOutCubic),
        label = "pressDownScale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Blanco,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0x12102C54),
                spotColor = Color(0x20102C54)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(CelesteAcentoClaro, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontSize = 13.5.sp
                ),
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily,
                    color = GrisTextoSecundario,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}


/**
 * Asistente Guiado de Bienvenida (Onboarding) de 3 pasos para clientes nuevos.
 */
@Composable
fun ClientWelcomeOnboardingModal(
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Triple(
            "Solicita Asistencia en Segundos",
            "Reporta fallas y solicita visitas técnicas adjuntando fotos en vivo desde tu cámara o seleccionándolas de tu galería.",
            Icons.Rounded.CameraAlt
        ),
        Triple(
            "Seguimiento en Tiempo Real",
            "Sigue el desplazamiento del móvil técnico en tiempo real sobre el mapa interactivo con hora estimada de arribo.",
            Icons.Rounded.NearMe
        ),
        Triple(
            "Presupuestos e Informes Oficiales",
            "Gestiona presupuestos, aprueba cotizaciones y descarga reportes técnicos certificados con firma digital.",
            Icons.Rounded.Description
        )
    )

    Dialog(
        onDismissRequest = onDismiss
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Blanco,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Indicador de Pasos (dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (currentStep == index) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (currentStep == index) CelesteAcento else GrisBorde)
                        )
                    }
                }

                // Ícono central destacado
                Surface(
                    shape = CircleShape,
                    color = CelesteAcentoClaro,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = steps[currentStep].third,
                            contentDescription = null,
                            tint = AzulMarca,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Text(
                    text = steps[currentStep].first,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = steps[currentStep].second,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Controles inferiores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(onClick = { currentStep-- }) {
                            Text("Atrás", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Omitir", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento, contentColor = Blanco),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (currentStep == steps.size - 1) "Comenzar" else "Siguiente",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        }
    }
}
