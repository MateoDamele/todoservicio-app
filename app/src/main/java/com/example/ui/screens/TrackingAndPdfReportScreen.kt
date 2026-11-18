package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.util.RouteEtaResult
import com.example.util.UruguayLocationProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ActiveTechnicianTracking
import com.example.model.ClientProfile
import com.example.model.OperationalTask
import com.example.model.ServiceHistoryItem
import com.example.model.TaskOperationStatus
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.components.InteractiveSignatureCanvas
import com.example.ui.components.MapGeocenceItem
import com.example.ui.components.MapMarkerItem
import com.example.ui.components.MapsGroundingAssistantCard
import com.example.ui.components.RealStreetInteractiveMap
import com.example.ui.components.SignaturePath
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.util.PdfReportGenerator
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class SignableReportTarget(
    val orderCode: String,
    val title: String,
    val category: String,
    val executionDate: String,
    val checkInTime: String,
    val technicianName: String,
    val address: String,
    val summary: String,
    val evidencePhotos: List<String>,
    val isTechSigned: Boolean
)

/**
 * Seguimiento GPS, Firma de Conformidad, Reportes PDF y Encuesta NPS.
 * Conectado estrictamente al estado multi-tenant real en OperationalRepository.
 */
@Composable
fun TrackingAndPdfReportScreen(
    clientProfile: ClientProfile,
    modifier: Modifier = Modifier,
    initialTab: Int = 0,
    onViewReport: ((ServiceHistoryItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    var clientSignaturePaths by remember { mutableStateOf<List<SignaturePath>>(emptyList()) }
    var hasClientSigned by remember { mutableStateOf(false) }

    var lastGeneratedPdfFile by remember { mutableStateOf<File?>(null) }
    var showPdfSuccessDialog by remember { mutableStateOf(false) }

    var showNpsDialog by remember { mutableStateOf(false) }
    var npsRatedOrder by remember { mutableStateOf<String?>(null) }
    var npsTechnicianName by remember { mutableStateOf("") }

    val allTasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()
    val allReports by OperationalRepository.serviceReports.collectAsStateWithLifecycle()
    val activeTracking by OperationalRepository.activeTracking.collectAsStateWithLifecycle()
    val npsRatings by OperationalRepository.npsRatings.collectAsStateWithLifecycle()

    val tenantTasks = remember(allTasks, clientProfile) {
        val companyTrim = clientProfile.companyName.trim()
        allTasks.filter { task ->
            (clientProfile.clientId.isNotBlank() && task.clientId == clientProfile.clientId) ||
                (companyTrim.isNotBlank() && task.clientName.trim().equals(companyTrim, ignoreCase = true))
        }
    }

    val completedOrders = remember(allReports, clientProfile) {
        val companyTrim = clientProfile.companyName.trim()
        allReports.filter { report ->
            (clientProfile.clientId.isNotBlank() && report.clientId == clientProfile.clientId) ||
                (companyTrim.isNotBlank() && report.companyName.trim().equals(companyTrim, ignoreCase = true))
        }
    }

    val activeTask = remember(tenantTasks) {
        tenantTasks.firstOrNull { it.status == TaskOperationStatus.INICIO_EJECUCION }
            ?: tenantTasks.firstOrNull { it.status == TaskOperationStatus.ABIERTA && it.isEnCamino }
            ?: tenantTasks.firstOrNull { it.status == TaskOperationStatus.ABIERTA }
    }

    val signableTarget = remember(activeTask, completedOrders, clientProfile) {
        when {
            activeTask != null -> {
                val matchingReport = completedOrders.firstOrNull { it.serviceCode == activeTask.code }
                SignableReportTarget(
                    orderCode = activeTask.code,
                    title = activeTask.title,
                    category = activeTask.category,
                    executionDate = activeTask.scheduledDate,
                    checkInTime = activeTask.checkInTime ?: activeTask.timeSlot,
                    technicianName = activeTask.technicianName.ifBlank { "Técnico Asignado" },
                    address = activeTask.address.ifBlank { clientProfile.address },
                    summary = matchingReport?.summary ?: activeTask.comments.ifBlank { activeTask.description },
                    evidencePhotos = matchingReport?.evidencePhotos ?: emptyList(),
                    isTechSigned = matchingReport != null
                )
            }
            completedOrders.isNotEmpty() -> {
                val latest = completedOrders.first()
                SignableReportTarget(
                    orderCode = latest.serviceCode,
                    title = latest.title,
                    category = latest.category,
                    executionDate = latest.executionDate,
                    checkInTime = latest.executionTime,
                    technicianName = latest.leadTechnician.ifBlank { "Técnico Asignado" },
                    address = latest.address.ifBlank { clientProfile.address },
                    summary = latest.summary,
                    evidencePhotos = latest.evidencePhotos,
                    isTechSigned = true
                )
            }
            else -> null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
    ) {
        Surface(
            color = AzulMarca,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AzulMarca,
                contentColor = Blanco,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CelesteAcento,
                            height = 3.5.dp
                        )
                    }
                },
                divider = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "Seguimiento GPS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 0) Blanco else Blanco.copy(alpha = 0.75f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "Firma y PDF",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 1) Blanco else Blanco.copy(alpha = 0.75f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = 2
                    },
                    text = {
                        Text(
                            text = "Historial y NPS",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == 2) Blanco else Blanco.copy(alpha = 0.75f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }
        }

        when (selectedTab) {
            0 -> LiveGpsTrackingTab(
                clientProfile = clientProfile,
                activeTask = activeTask,
                activeTracking = activeTracking,
                onGoToSignTab = { selectedTab = 1 }
            )

            1 -> DualSignatureAndPdfTab(
                clientProfile = clientProfile,
                target = signableTarget,
                hasClientSigned = hasClientSigned,
                onSignatureChanged = { signed, paths ->
                    hasClientSigned = signed
                    clientSignaturePaths = paths
                },
                onGeneratePdf = { target ->
                    val today = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "UY")).format(Date())
                    val reportData = PdfReportGenerator.ReportData(
                        orderCode = target.orderCode,
                        serviceTitle = target.title,
                        category = target.category,
                        executionDate = target.executionDate.ifBlank { today },
                        checkInTime = target.checkInTime,
                        leadTechnician = target.technicianName,
                        clientName = clientProfile.clientName,
                        companyName = clientProfile.companyName,
                        clientRut = clientProfile.rut,
                        clientAddress = target.address.ifBlank { clientProfile.address },
                        findingsSummary = target.summary,
                        clientSignature = if (hasClientSigned) clientSignaturePaths else null
                    )
                    val pdfFile = PdfReportGenerator.generatePdfReport(context, reportData)
                    if (pdfFile != null) {
                        lastGeneratedPdfFile = pdfFile
                        showPdfSuccessDialog = true
                    } else {
                        Toast.makeText(context, "Error al compilar el documento PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenNpsSurvey = { target ->
                    npsRatedOrder = target.orderCode
                    npsTechnicianName = target.technicianName
                    showNpsDialog = true
                }
            )

            2 -> HistoryAndNpsPortalTab(
                orders = completedOrders,
                npsRatings = npsRatings,
                onDownloadPdf = { order ->
                    val reportData = PdfReportGenerator.ReportData(
                        orderCode = order.serviceCode.replace("#", ""),
                        serviceTitle = order.title,
                        category = order.category,
                        executionDate = order.executionDate,
                        checkInTime = order.checkInTime,
                        checkOutTime = order.checkOutTime.ifBlank { order.executionTime },
                        leadTechnician = order.leadTechnician,
                        clientName = clientProfile.clientName,
                        companyName = clientProfile.companyName,
                        clientRut = clientProfile.rut,
                        clientAddress = order.address.ifBlank { clientProfile.address },
                        findingsSummary = order.summary,
                        npsRating = npsRatings[order.serviceCode] ?: order.clientRatingStars,
                        npsComment = order.clientRatingComment
                    )
                    val pdf = PdfReportGenerator.generatePdfReport(context, reportData)
                    if (pdf != null) {
                        lastGeneratedPdfFile = pdf
                        PdfReportGenerator.openPdf(context, pdf)
                    } else {
                        Toast.makeText(context, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                onOpenNps = { order ->
                    npsRatedOrder = order.serviceCode
                    npsTechnicianName = order.leadTechnician
                    showNpsDialog = true
                },
                onViewReport = onViewReport
            )
        }
    }

    if (showNpsDialog && npsRatedOrder != null) {
        NpsRatingDialog(
            orderCode = npsRatedOrder!!,
            technicianName = npsTechnicianName.ifBlank { "Equipo Técnico" },
            onDismiss = { showNpsDialog = false },
            onSubmitRating = { stars, comment ->
                OperationalRepository.submitNpsRating(npsRatedOrder!!, stars, comment)
                showNpsDialog = false
                Toast.makeText(
                    context,
                    "Calificación de $stars estrellas registrada.",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    if (showPdfSuccessDialog && lastGeneratedPdfFile != null) {
        PdfSuccessDialog(
            file = lastGeneratedPdfFile!!,
            onDismiss = { showPdfSuccessDialog = false },
            onOpenPdf = {
                PdfReportGenerator.openPdf(context, lastGeneratedPdfFile!!)
                showPdfSuccessDialog = false
            },
            onSharePdf = {
                PdfReportGenerator.sharePdf(context, lastGeneratedPdfFile!!)
            },
            onRateService = {
                showPdfSuccessDialog = false
                val targetCode = signableTarget?.orderCode ?: completedOrders.firstOrNull()?.serviceCode
                if (!targetCode.isNullOrBlank()) {
                    npsRatedOrder = targetCode
                    npsTechnicianName = signableTarget?.technicianName ?: ""
                    showNpsDialog = true
                }
            }
        )
    }
}

@Composable
private fun LiveGpsTrackingTab(
    clientProfile: ClientProfile,
    activeTask: OperationalTask?,
    activeTracking: ActiveTechnicianTracking,
    onGoToSignTab: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeTask == null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(CelesteAcentoClaro, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = AzulCorporativo,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Sin técnico en camino o en sitio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Cuando se asigne e inicie un servicio para ${clientProfile.companyName.ifBlank { "tu sede" }}, verás aquí la ubicación y el estado en vivo.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        val matchesTracking = activeTracking.serviceCode.equals(activeTask.code, ignoreCase = true)
        val stageLabel = when {
            activeTask.status == TaskOperationStatus.INICIO_EJECUCION -> "TRABAJO EN EJECUCIÓN"
            activeTask.isEnCamino -> "TÉCNICO EN CAMINO"
            else -> "VISITA PROGRAMADA"
        }
        val techName = activeTask.technicianName.ifBlank { "Técnico por asignar" }
        val resolvedAddress = activeTask.address.ifBlank { clientProfile.address }
        val phone = if (matchesTracking) activeTracking.technicianPhone else ""
        val whatsapp = if (matchesTracking) activeTracking.technicianWhatsapp else ""

        val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
        val matchedBranch = remember(corporateClients, activeTask) {
            val client = corporateClients.firstOrNull {
                it.id == activeTask.clientId || it.name.equals(activeTask.clientName, ignoreCase = true)
            }
            client?.branches?.firstOrNull {
                it.name.equals(activeTask.sedeName, ignoreCase = true) ||
                    it.address.equals(activeTask.address, ignoreCase = true)
            } ?: client?.branches?.firstOrNull()
        }
        val destLat = if (matchesTracking) {
            activeTracking.destinationLatitude
        } else {
            matchedBranch?.lat ?: com.example.util.URUGUAY_DEFAULT_LAT
        }
        val destLng = if (matchesTracking) {
            activeTracking.destinationLongitude
        } else {
            matchedBranch?.lng ?: com.example.util.URUGUAY_DEFAULT_LNG
        }
        val techLat = if (matchesTracking && activeTask.isEnCamino) activeTracking.currentLatitude else destLat
        val techLng = if (matchesTracking && activeTask.isEnCamino) activeTracking.currentLongitude else destLng

        var liveEtaResult by remember(activeTask.code) {
            mutableStateOf<RouteEtaResult?>(null)
        }
        LaunchedEffect(activeTask.code, activeTask.isEnCamino, techLat, techLng, destLat, destLng) {
            if (activeTask.isEnCamino) {
                liveEtaResult = UruguayLocationProvider.calculateRealRouteEta(
                    originLat = techLat,
                    originLng = techLng,
                    destLat = destLat,
                    destLng = destLng
                )
            } else {
                liveEtaResult = null
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AzulMarca),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("floating_header_tech_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CelesteAcento
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Blanco, CircleShape)
                            )
                            Text(
                                text = stageLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Text(
                        text = activeTask.code,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcentoClaro
                        )
                    )
                }

                Text(
                    text = activeTask.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Blanco,
                        fontSize = 17.sp
                    )
                )

                Text(
                    text = "Técnico: $techName • ${activeTask.category}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = Blanco.copy(alpha = 0.9f),
                        fontSize = 12.5.sp
                    )
                )

                if (resolvedAddress.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = resolvedAddress,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = Blanco.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.2.dp, CelesteAcento),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = when {
                            activeTask.status == TaskOperationStatus.INICIO_EJECUCION -> "Intervención en curso"
                            activeTask.isEnCamino && liveEtaResult != null -> liveEtaResult!!.toFormattedLabel()
                            activeTask.isEnCamino && matchesTracking && activeTracking.etaMinutes > 0 ->
                                "Tiempo estimado de llegada: ${activeTracking.etaMinutes} min (${activeTracking.distanceKm} km)"
                            activeTask.isEnCamino -> "Móvil en traslado"
                            else -> "Agenda: ${activeTask.scheduledDate} • ${activeTask.timeSlot}"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = activeTask.checkInTime?.let { "Check-in: $it" }
                            ?: if (matchesTracking && activeTracking.statusMessage.isNotBlank()) {
                                activeTracking.statusMessage
                            } else {
                                "Orden ${activeTask.code} activa"
                            },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = AzulCorporativo,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(CelesteAcentoClaro, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            activeTask.status == TaskOperationStatus.INICIO_EJECUCION -> Icons.Rounded.Engineering
                            activeTask.isEnCamino -> Icons.Rounded.DirectionsCar
                            else -> Icons.Rounded.Schedule
                        },
                        contentDescription = null,
                        tint = CelesteAcento,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        val liveGeofences = remember(activeTask.id, clientProfile.companyName, resolvedAddress, destLat, destLng) {
            listOf(
                MapGeocenceItem(
                    id = "sede-${activeTask.id}",
                    name = clientProfile.companyName.ifBlank { activeTask.clientName },
                    address = resolvedAddress.ifBlank { "Sede Cliente" },
                    lat = destLat,
                    lng = destLng,
                    radiusMeters = 90
                )
            )
        }
        val initials = remember(techName) {
            techName.split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifBlank { "TS" }
        }
        val liveMarkers = remember(activeTask.id, activeTask.isEnCamino, techName, initials, destLat, destLng, techLat, techLng) {
            listOf(
                MapMarkerItem(
                    id = "tech-${activeTask.id}",
                    title = techName,
                    subtitle = if (activeTask.isEnCamino) "En camino" else "Sede del cliente",
                    lat = techLat,
                    lng = techLng,
                    type = "TECH",
                    badgeText = initials,
                    timeText = if (activeTask.isEnCamino) "En ruta" else "Asignado"
                ),
                MapMarkerItem(
                    id = "dest-${activeTask.id}",
                    title = clientProfile.companyName.ifBlank { activeTask.clientName },
                    subtitle = resolvedAddress,
                    lat = destLat,
                    lng = destLng,
                    type = "CLIENT",
                    badgeText = "S",
                    timeText = "Destino"
                )
            )
        }
        val liveRoute = remember(activeTask.isEnCamino, techLat, techLng, destLat, destLng) {
            if (activeTask.isEnCamino && (techLat != destLat || techLng != destLng)) {
                listOf(techLat to techLng, destLat to destLng)
            } else {
                emptyList()
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EEF5)),
            border = BorderStroke(1.dp, GrisBorde),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                RealStreetInteractiveMap(
                    centerLat = destLat,
                    centerLng = destLng,
                    zoomLevel = 15,
                    geofences = liveGeofences,
                    markers = liveMarkers,
                    routePolyline = liveRoute,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Blanco.copy(alpha = 0.92f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (activeTask.isEnCamino) "Google Maps • Técnico en Camino" else "Google Maps • Sede Asignada",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulCorporativo,
                            fontSize = 10.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (resolvedAddress.isNotBlank()) {
            MapsGroundingAssistantCard(
                defaultQuery = resolvedAddress,
                lat = destLat,
                lng = destLng,
                title = "Referencias de Llegada"
            )
        }

        if (phone.isNotBlank() || whatsapp.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                        border = BorderStroke(1.5.dp, AzulCorporativo),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_call_technician")
                    ) {
                        Icon(imageVector = Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Llamar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                if (whatsapp.isNotBlank()) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val cleanNumber = whatsapp.filter { it.isDigit() }
                            val msg = Uri.encode("Hola $techName, consulta sobre la orden ${activeTask.code}.")
                            val whatsappUrl = "https://api.whatsapp.com/send?phone=$cleanNumber&text=$msg"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Blanco
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_whatsapp_technician")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Button(
            onClick = onGoToSignTab,
            colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = Blanco)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ir a Firma y Reporte PDF",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = Blanco
            )
        }

        Spacer(modifier = Modifier.height(108.dp))
    }
}

@Composable
private fun DualSignatureAndPdfTab(
    clientProfile: ClientProfile,
    target: SignableReportTarget?,
    hasClientSigned: Boolean,
    onSignatureChanged: (Boolean, List<SignaturePath>) -> Unit,
    onGeneratePdf: (SignableReportTarget) -> Unit,
    onOpenNpsSurvey: (SignableReportTarget) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (target == null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(CelesteAcentoClaro, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Assignment,
                            contentDescription = null,
                            tint = AzulCorporativo,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Sin órdenes activas para firma",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Cuando tengas una orden en curso o finalizada, podrás firmar conformidad y descargar el PDF aquí.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.2.dp, GrisBorde),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
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
                        shape = RoundedCornerShape(8.dp),
                        color = CelesteAcentoClaro
                    ) {
                        Text(
                            text = target.orderCode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (target.category.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VerdeExitoFondo
                        ) {
                            Text(
                                text = target.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeExito,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = target.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 16.sp
                    )
                )

                val detailLines = buildList {
                    if (target.executionDate.isNotBlank()) add("Fecha: ${target.executionDate}")
                    if (target.checkInTime.isNotBlank()) add("Horario: ${target.checkInTime}")
                    if (target.address.isNotBlank()) add("Ubicación: ${target.address}")
                }.joinToString("\n")

                if (detailLines.isNotBlank()) {
                    Text(
                        text = detailLines,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    )
                }

                if (target.summary.isNotBlank()) {
                    Text(
                        text = target.summary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = AzulCorporativo,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisBorde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Evidencia Fotográfica",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                if (target.evidencePhotos.isEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = null,
                            tint = GrisTextoSecundario,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sin fotografías adjuntas aún.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                    }
                } else {
                    target.evidencePhotos.forEach { photo ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GrisNeutro,
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoLibrary,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = photo,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulMarca,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisBorde),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Responsable Técnico",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (target.isTechSigned) VerdeExitoFondo else CelesteAcentoClaro
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (target.isTechSigned) Icons.Rounded.Verified else Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = if (target.isTechSigned) VerdeExito else AzulCorporativo,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (target.isTechSigned) "Firmado" else "Pendiente",
                                color = if (target.isTechSigned) VerdeExito else AzulCorporativo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = target.technicianName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulCorporativo,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        InteractiveSignatureCanvas(
            title = "Firma Digital de Conformidad del Cliente",
            signerName = clientProfile.clientName.ifBlank { "Cliente" },
            signerRole = clientProfile.companyName.ifBlank { "Empresa" },
            onSignatureChanged = onSignatureChanged,
            testTag = "canvas_client_signature"
        )

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGeneratePdf(target)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CelesteAcento,
                contentColor = Blanco
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_generate_download_pdf")
        ) {
            Icon(imageVector = Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generar y Descargar PDF",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Blanco
                )
            )
        }

        OutlinedButton(
            onClick = { onOpenNpsSurvey(target) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Star, contentDescription = null, tint = CelesteAcento)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Calificar Atención",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(108.dp))
    }
}

@Composable
private fun HistoryAndNpsPortalTab(
    orders: List<ServiceHistoryItem>,
    npsRatings: Map<String, Int>,
    onDownloadPdf: (ServiceHistoryItem) -> Unit,
    onOpenNps: (ServiceHistoryItem) -> Unit,
    onViewReport: ((ServiceHistoryItem) -> Unit)?
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AzulCorporativo),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Reportes y Certificados",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )
                    Text(
                        text = "Historial técnico con firma digital disponible para descarga.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = Blanco.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null,
                            tint = GrisTextoSecundario,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "Sin reportes aún",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Los reportes cerrados aparecerán aquí para descarga y calificación.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                val ratingStars = npsRatings[order.serviceCode]

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { mod ->
                            if (onViewReport != null) mod.clickable { onViewReport(order) } else mod
                        }
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
                                    text = order.serviceCode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = order.executionDate,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Text(
                            text = order.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        Text(
                            text = "Técnico: ${order.leadTechnician.ifBlank { "Equipo Técnico" }} • ${order.category}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulCorporativo,
                                fontSize = 12.sp
                            )
                        )

                        if (order.summary.isNotBlank()) {
                            Text(
                                text = order.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (ratingStars != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VerdeExitoFondo
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = VerdeExito,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Calificado $ratingStars★",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VerdeExito
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onOpenNps(order) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(imageVector = Icons.Rounded.StarBorder, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Calificar",
                                        fontSize = 12.sp,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDownloadPdf(order)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CelesteAcento,
                                    contentColor = Blanco
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("btn_download_pdf_${order.serviceCode}")
                            ) {
                                Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Descargar PDF",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
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

@Composable
fun NpsRatingDialog(
    orderCode: String,
    technicianName: String,
    onDismiss: () -> Unit,
    onSubmitRating: (stars: Int, comment: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedStars by remember { mutableIntStateOf(5) }
    var commentText by remember { mutableStateOf("") }
    val tags = listOf("Puntualidad", "Limpieza", "Claridad técnica")
    val selectedTags = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Encuesta de Calidad",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = "¿Cómo calificas la atención recibida en este trabajo?",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo,
                        fontSize = 15.sp
                    )
                )

                Text(
                    text = if (technicianName.isNotBlank()) "Orden $orderCode • $technicianName" else "Orden $orderCode",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedStars = i
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = "$i estrellas",
                                tint = if (i <= selectedStars) CelesteAcento else Color(0xFFD0D7DE),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = when (selectedStars) {
                        5 -> "5 estrellas • Excelente"
                        4 -> "4 estrellas • Muy bueno"
                        3 -> "3 estrellas • Bueno"
                        2 -> "2 estrellas • Regular"
                        else -> "1 estrella • Mejorable"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedStars >= 4) VerdeExito else AzulMarca
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Aspectos destacados",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulCorporativo
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) CelesteAcento else GrisNeutro,
                            border = BorderStroke(1.dp, if (isSelected) CelesteAcento else GrisBorde),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = if (isSelected) Blanco else AzulCorporativo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = {
                        Text(
                            text = "Comentario breve (opcional)",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteAcento,
                        unfocusedBorderColor = GrisBorde
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val fullComment = buildList {
                            if (selectedTags.isNotEmpty()) add(selectedTags.joinToString(", "))
                            if (commentText.isNotBlank()) add(commentText.trim())
                        }.joinToString(". ")
                        onSubmitRating(selectedStars, fullComment)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulMarca,
                        contentColor = Blanco
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_submit_nps_rating")
                ) {
                    Icon(imageVector = Icons.Rounded.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Valoración",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun PdfSuccessDialog(
    file: File,
    onDismiss: () -> Unit,
    onOpenPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onRateService: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(VerdeExitoFondo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = VerdeExito,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Reporte PDF Generado",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Text(
                    text = "El archivo ${file.name} fue generado correctamente.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        fontSize = 12.5.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenPdf,
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Abrir PDF", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }

                    OutlinedButton(
                        onClick = onSharePdf,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(onClick = onRateService) {
                    Text(
                        text = "Calificar atención recibida",
                        color = CelesteAcento,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }
    }
}
