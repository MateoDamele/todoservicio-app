package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Hvac
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.PriceCheck
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import android.graphics.Bitmap
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OperationalRepository
import com.example.model.ClientProfile
import com.example.util.PdfReportGenerator
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.ModalBlurScrim
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

/**
 * Modelo de archivo multimedia adjunto.
 */
data class AttachedEvidenceFile(
    val id: String,
    val name: String,
    val sizeText: String,
    val isVideo: Boolean = false,
    val uriString: String? = null,
    val bitmap: Bitmap? = null
)

/**
 * Tipo de Petición del Cliente.
 */
enum class RequestFormType(val title: String, val subtitle: String) {
    PLAN_MENSUAL(
        title = "Usar Visita de mi Plan Mensual",
        subtitle = "Sin costo extra • Deducible de su cuota mensual activa."
    ),
    NUEVO_PRESUPUESTO(
        title = "Solicitar Presupuesto para Trabajo Nuevo",
        subtitle = "Para cotizaciones mayores, reformas o intervenciones fuera de abono."
    )
}

/**
 * Estado formal de una cotización.
 */
enum class QuoteState(val label: String, val colorHex: Long, val bgHex: Long) {
    PENDIENTE("Esperando tu respuesta", 0xFFE67E22, 0xFFFDF2E9),
    APROBADO("Aprobado / Programado", 0xFF1B8755, 0xFFE8F5E9),
    EN_REVISION("En Revisión Técnica", 0xFF1D4E89, 0xFFE3F0FC)
}

/**
 * Detalle completo de una cotización corporativa de Todoservicio.
 */
data class FullQuoteItem(
    val id: String,
    val quoteCode: String,
    val title: String,
    val category: String,
    val amountUsd: Double,
    val issueDate: String,
    val validUntil: String,
    val state: QuoteState,
    val scopeSummary: String,
    val lineItems: List<Pair<String, Double>>,
    val engineerNotes: String,
    val revisionFeedback: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestWorkScreen(
    clientProfile: ClientProfile,
    onEmergencyCall: () -> Unit,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Índice de pestaña activa (0: Solicitar Servicio / Presupuesto, 1: Mis Cotizaciones y Aprobaciones)
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }

    // Cotizaciones emitidas desde el repositorio central filtradas por empresa cliente
    val repoQuotes by OperationalRepository.quotes.collectAsStateWithLifecycle()
    val quotesList = remember(repoQuotes, clientProfile.companyName) {
        val companyLower = clientProfile.companyName.trim().lowercase()
        repoQuotes.filter { q ->
            companyLower.isNotBlank() && q.clientCompanyName.trim().lowercase() == companyLower
        }.map { q ->
            FullQuoteItem(
                id = q.id,
                quoteCode = q.quoteCode,
                title = q.title,
                category = q.category,
                amountUsd = q.amountUsd,
                issueDate = q.issueDate,
                validUntil = q.validUntil,
                state = when {
                    q.isApproved -> QuoteState.APROBADO
                    !q.revisionFeedback.isNullOrBlank() -> QuoteState.EN_REVISION
                    else -> QuoteState.PENDIENTE
                },
                scopeSummary = q.scopeDescription,
                lineItems = listOf(q.title to q.amountUsd),
                engineerNotes = q.attachedFileName?.let { "Documento adjunto: $it" }.orEmpty(),
                revisionFeedback = q.revisionFeedback
            )
        }
    }

    val pendingCount = quotesList.count { it.state == QuoteState.PENDIENTE }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
    ) {
        // ====================================================================
        // 1. ESTRUCTURA CON PESTAÑAS (TABBAR CORPORATIVO)
        // ====================================================================
        Surface(
            color = AzulMarca,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = AzulMarca,
                    contentColor = Blanco,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = CelesteAcento,
                                height = 3.5.dp
                            )
                        }
                    },
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTabIndex = 0
                        },
                        text = {
                            Text(
                                text = "Solicitar Servicio",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == 0) Blanco else Blanco.copy(alpha = 0.75f),
                                    fontSize = 13.5.sp
                                )
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Handyman,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 0) CelesteAcento else Blanco.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tab_solicitar_servicio")
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTabIndex = 1
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Mis Cotizaciones",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == 1) Blanco else Blanco.copy(alpha = 0.75f),
                                        fontSize = 13.5.sp
                                    )
                                )
                                if (pendingCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = NaranjaAlerta
                                    ) {
                                        Text(
                                            text = "$pendingCount",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Blanco,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.PriceCheck,
                                contentDescription = null,
                                tint = if (selectedTabIndex == 1) CelesteAcento else Blanco.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tab_mis_cotizaciones")
                    )
                }
            }
        }

        // Contenido según pestaña
        Crossfade(
            targetState = selectedTabIndex,
            animationSpec = tween(durationMillis = 200),
            modifier = Modifier.fillMaxSize(),
            label = "tabContentCrossfade"
        ) { tabIndex ->
            if (tabIndex == 0) {
                // ============================================================
                // PESTAÑA 1: FORMULARIO DE SOLICITUD EXPRESS
                // ============================================================
                RequestServiceExpressTab(
                    clientProfile = clientProfile,
                    onEmergencyCall = onEmergencyCall
                )
            } else {
                // ============================================================
                // PESTAÑA 2: GESTIÓN Y APROBACIÓN DE COTIZACIONES EN 1 CLIC
                // ============================================================
                QuotesManagementTab(
                    clientProfile = clientProfile,
                    quotes = quotesList,
                    onApproveQuote = { quote ->
                        OperationalRepository.approveQuote(quote.id)
                    },
                    onRejectOrReviseQuote = { quote, reason ->
                        OperationalRepository.requestQuoteRevision(quote.id, reason)
                    }
                )
            }
        }
    }
}

/**
 * Pestaña 1: Formulario de Solicitud Express con selector visual, multimedia y turnos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestServiceExpressTab(
    clientProfile: ClientProfile,
    onEmergencyCall: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Selector de Tipo de Petición
    var selectedRequestType by remember { mutableStateOf(RequestFormType.PLAN_MENSUAL) }

    // Selector de Rubro
    var selectedCategory by remember { mutableStateOf("Sistemas Eléctricos") }

    // Descripción libre
    var descriptionText by remember { mutableStateOf("") }
    var locationSector by remember { mutableStateOf("") }

    // Preferencia Horaria y Fecha
    var selectedDatePreference by remember { mutableStateOf("Lo antes posible (Hoy / Mañana)") }
    var selectedShift by remember { mutableStateOf("Mañana (08:30 - 12:30 hs)") }

    // Evidencia Multimedia limpia (sin archivos maquetados por defecto)
    val attachedFiles = remember { mutableStateListOf<AttachedEvidenceFile>() }
    var showMediaPickerSheet by remember { mutableStateOf(false) }

    // Launcher para Cámara real del teléfono
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val newId = "att-" + System.currentTimeMillis()
            attachedFiles.add(
                AttachedEvidenceFile(
                    id = newId,
                    name = "foto_camara_" + (attachedFiles.size + 1) + ".jpg",
                    sizeText = "1.8 MB",
                    isVideo = false,
                    bitmap = bitmap
                )
            )
            Toast.makeText(context, "Foto capturada con la cámara y adjuntada", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para Galería real del dispositivo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val newId = "att-" + System.currentTimeMillis()
            attachedFiles.add(
                AttachedEvidenceFile(
                    id = newId,
                    name = "evidencia_galeria_" + (attachedFiles.size + 1) + ".jpg",
                    sizeText = "2.4 MB",
                    isVideo = false,
                    uriString = uri.toString()
                )
            )
            Toast.makeText(context, "Imagen de galería seleccionada y adjuntada", Toast.LENGTH_SHORT).show()
        }
    }

    var showSuccessDialogWithTicket by remember { mutableStateOf<String?>(null) }

    val rubrosList = listOf(
        Pair("Sistemas Eléctricos", Icons.Rounded.ElectricBolt),
        Pair("Climatización (HVAC)", Icons.Rounded.Hvac),
        Pair("Sanitaria y Desagües", Icons.Rounded.WaterDrop),
        Pair("Albañilería / Pintura", Icons.Rounded.FormatPaint),
        Pair("Cerrajería / Cristales", Icons.Rounded.Key),
        Pair("Otros / Infraestructura", Icons.Rounded.Construction)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // ====================================================================
        // A. SELECTOR DE TIPO DE PETICIÓN (CHIPS GRANDES Y ACCESIBLES)
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Tipo de Solicitud",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    RequestFormType.values().forEach { type ->
                        val isSelected = selectedRequestType == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CelesteAcentoClaro else GrisNeutro,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CelesteAcento else GrisBorde
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    selectedRequestType = type
                                }
                                .testTag("request_type_${type.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(2.dp, if (isSelected) CelesteAcento else GrisBorde, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(CelesteAcento, CircleShape)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AzulMarca else AzulCorporativo,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                    Text(
                                        text = type.subtitle,
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
        }

        // ====================================================================
        // B. SELECTOR DE RUBRO (CUADRÍCULA CON ICONOS GRANDES)
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Especialidad / Rubro",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    rubrosList.chunked(2).forEach { rowPair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowPair.forEach { (name, icon) ->
                                val isSelected = selectedCategory == name
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CelesteAcentoClaro else Blanco,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CelesteAcento else GrisBorde
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            selectedCategory = name
                                        }
                                        .testTag("rubro_button_$name")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    if (isSelected) CelesteAcento else CelesteAcentoClaro,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) Blanco else AzulCorporativo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) AzulMarca else AzulCorporativo,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================
        // C. DESCRIPCIÓN Y UBICACIÓN
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Ubicación y Detalle de la Falla",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    OutlinedTextField(
                        value = locationSector,
                        onValueChange = { locationSector = it },
                        label = { Text("Sector / Piso / Ubicación", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("Ej: Piso 3 - Baño de damas o Sala de Bombas", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_form_location")
                    )

                    OutlinedTextField(
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Descripción de la Necesidad", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("Describe brevemente la falla o trabajo que necesitas...", fontFamily = PoppinsFontFamily) },
                        minLines = 4,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_form_description")
                    )
                }
            }
        }

        // ====================================================================
        // D. MÓDULO DE ADJUNTAR EVIDENCIA MULTIMEDIA (CÁMARA / GALERÍA)
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "4. Evidencia Fotográfica o Video Corto",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    Text(
                        text = "Adjuntar fotos o un video breve acelera el diagnóstico y permite al técnico llevar los repuestos exactos.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        )
                    )

                    // Botón destacado para abrir selector nativo de Cámara o Galería
                    Button(
                        onClick = {
                            showMediaPickerSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CelesteAcentoClaro,
                            contentColor = AzulMarca
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CelesteAcento),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_capture_media")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tomar Foto / Grabar (Cámara o Galería)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 13.5.sp
                            )
                        )
                    }

                    // Miniaturas de archivos adjuntos con vista previa real y botón eliminar
                    if (attachedFiles.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            attachedFiles.forEach { file ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GrisNeutro,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (file.bitmap != null) {
                                                Image(
                                                    bitmap = file.bitmap.asImageBitmap(),
                                                    contentDescription = "Miniatura",
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(if (file.isVideo) AzulCorporativo else CelesteAcento, RoundedCornerShape(8.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = if (file.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Image,
                                                        contentDescription = null,
                                                        tint = Blanco,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = file.name,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AzulMarca
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${file.sizeText} • Evidencia real adjunta",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = PoppinsFontFamily,
                                                        color = GrisTextoSecundario,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                attachedFiles.remove(file)
                                                Toast.makeText(context, "Archivo eliminado", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Eliminar archivo",
                                                tint = RojoUrgencia,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GrisNeutro.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Sin evidencia adjunta. Presione el botón superior para capturar en el momento con la cámara o seleccionar de su galería.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
        // ====================================================================
        // E. PREFERENCIA HORARIA Y FECHA TENTATIVA
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "5. Preferencia de Fecha y Turno",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )

                    // Selector de Fecha
                    val dates = listOf("Lo antes posible (Hoy / Mañana)", "Esta Semana", "Próxima Semana")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dates.forEach { dateOpt ->
                            val isSelected = selectedDatePreference == dateOpt
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CelesteAcento else GrisNeutro,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedDatePreference = dateOpt }
                            ) {
                                Text(
                                    text = dateOpt,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Blanco else AzulMarca
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Selector de Turno (Mañana vs Tarde)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val shifts = listOf("Mañana (08:30 - 12:30 hs)", "Tarde (13:30 - 18:00 hs)")
                        shifts.forEach { shift ->
                            val isSelected = selectedShift == shift
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CelesteAcentoClaro else Blanco,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) CelesteAcento else GrisBorde
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedShift = shift }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = if (isSelected) CelesteAcento else GrisTextoSecundario,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = shift,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = AzulMarca,
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

        // ====================================================================
        // F. BOTÓN DE ENVÍO ("Enviar Solicitud a Todoservicio")
        // ====================================================================
        item {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (descriptionText.isBlank() && locationSector.isBlank()) {
                        Toast.makeText(context, "Ingrese sector o descripción de la falla", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val taskTitle = if (selectedRequestType == RequestFormType.PLAN_MENSUAL) {
                        "Visita de Plan: $selectedCategory"
                    } else {
                        "Solicitud: $selectedCategory"
                    }
                    val created = OperationalRepository.submitClientWorkRequest(
                        clientProfile = clientProfile,
                        title = taskTitle,
                        category = selectedCategory,
                        location = locationSector.ifBlank { clientProfile.address },
                        description = descriptionText.ifBlank { taskTitle },
                        isPlanVisit = selectedRequestType == RequestFormType.PLAN_MENSUAL
                    )
                    showSuccessDialogWithTicket = created.code
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulMarca,
                    contentColor = Blanco
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_submit_express_request")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Enviar Solicitud a Todoservicio",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Blanco
                    )
                )
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }

    // Diálogo Pop-up de Confirmación con Número de Ticket
    showSuccessDialogWithTicket?.let { ticketCode ->
        RequestSuccessDialog(
            ticketCode = ticketCode,
            category = selectedCategory,
            date = selectedDatePreference,
            shift = selectedShift,
            onDismiss = {
                showSuccessDialogWithTicket = null
                descriptionText = ""
                locationSector = ""
            }
        )
    }

    // Modal Inferior Nativo (BottomSheet) para Cámara y Galería
    if (showMediaPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaPickerSheet = false },
            containerColor = Blanco,
            scrimColor = ModalBlurScrim,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Adjuntar Evidencia Fotográfica o Video",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )
                    IconButton(onClick = { showMediaPickerSheet = false }) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = "Seleccione el origen del archivo multimedia:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                // Opción 1: Usar Cámara
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CelesteAcentoClaro,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CelesteAcento.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMediaPickerSheet = false
                            cameraLauncher.launch(null)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CelesteAcento,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = Blanco,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Opción 1: Usar Cámara",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "Abre la cámara del teléfono para capturar una foto o video corto en el momento",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                // Opción 2: Abrir Galería
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VerdeExitoFondo,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VerdeExito.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMediaPickerSheet = false
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = VerdeExito,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = null,
                                    tint = Blanco,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Opción 2: Abrir Galería",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "Abre el almacenamiento/galería de fotos para seleccionar un archivo ya guardado",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

/**
 * Pestaña 2: Gestión y Aprobación de Cotizaciones en 1 Clic.
 */
@Composable
private fun QuotesManagementTab(
    clientProfile: ClientProfile,
    quotes: List<FullQuoteItem>,
    onApproveQuote: (FullQuoteItem) -> Unit,
    onRejectOrReviseQuote: (FullQuoteItem, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedQuoteForPdf by remember { mutableStateOf<FullQuoteItem?>(null) }
    var selectedQuoteForApprove by remember { mutableStateOf<FullQuoteItem?>(null) }
    var selectedQuoteForRevise by remember { mutableStateOf<FullQuoteItem?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRESUPUESTOS Y COTIZACIONES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcento,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Aprobación y Seguimiento en 1 Clic",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )
                    Text(
                        text = "Revise el desglose técnico de cada propuesta emitida por nuestro equipo y apruebe con validez legal inmediata para coordinar el trabajo.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        )
                    )
                }
            }
        }

        if (quotes.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
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
                            imageVector = Icons.Rounded.PriceCheck,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.6f),
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Sin cotizaciones aún",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Cuando administración envíe un presupuesto aparecerá aquí.",
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
            items(quotes, key = { it.id }) { quote ->
                QuoteCardItem(
                    quote = quote,
                    onViewPdf = { selectedQuoteForPdf = quote },
                    onApprove = { selectedQuoteForApprove = quote },
                    onRevise = { selectedQuoteForRevise = quote }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }

    // Modal de Aprobación Inmediata en 1 Clic
    selectedQuoteForApprove?.let { quote ->
        ConfirmApproveQuoteModal(
            quote = quote,
            onDismiss = { selectedQuoteForApprove = null },
            onConfirmApprove = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onApproveQuote(quote)
                selectedQuoteForApprove = null
            }
        )
    }

    // Modal de Rechazo / Solicitud de Revisión con nota
    selectedQuoteForRevise?.let { quote ->
        RequestRevisionModal(
            quote = quote,
            onDismiss = { selectedQuoteForRevise = null },
            onSubmitRevision = { reason ->
                onRejectOrReviseQuote(quote, reason)
                selectedQuoteForRevise = null
            }
        )
    }

    // Modal de Desglose Formal PDF
    selectedQuoteForPdf?.let { quote ->
        QuotePdfBreakdownModal(
            quote = quote,
            clientProfile = clientProfile,
            onDismiss = { selectedQuoteForPdf = null }
        )
    }
}

/**
 * Tarjeta de Cotización Individual con borde según estado y botones de respuesta rápida.
 */
@Composable
private fun QuoteCardItem(
    quote: FullQuoteItem,
    onViewPdf: () -> Unit,
    onApprove: () -> Unit,
    onRevise: () -> Unit
) {
    val borderColor = when (quote.state) {
        QuoteState.PENDIENTE -> CelesteAcento
        QuoteState.APROBADO -> VerdeExito
        QuoteState.EN_REVISION -> AzulCorporativo
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: Código, Fecha y Badge de Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(quote.state.bgHex)
                ) {
                    Text(
                        text = quote.state.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(quote.state.colorHex)
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "${quote.quoteCode} • ${quote.issueDate}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario
                    )
                )
            }

            // Título y Rubro
            Column {
                Text(
                    text = quote.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )
                Text(
                    text = quote.category,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulCorporativo,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Monto Total Destacado en Poppins Bold (#102C54)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GrisNeutro,
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monto Total Presupuestado:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                        Text(
                            text = "USD ${quote.amountUsd}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 22.sp
                            )
                        )
                    }

                    // Botón para abrir/descargar desglose PDF
                    OutlinedButton(
                        onClick = onViewPdf,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Desglose PDF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            )
                        )
                    }
                }
            }

            Text(
                text = quote.scopeSummary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    color = GrisTextoSecundario
                )
            )

            if (quote.revisionFeedback != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CelesteAcentoClaro,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota de revisión enviada: \"${quote.revisionFeedback}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = AzulCorporativo,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            androidx.compose.material3.HorizontalDivider(color = GrisBorde.copy(alpha = 0.6f))

            // ================================================================
            // MÓDULO DE RESPUESTA RÁPIDA (BOTONES DENTRO DE LA TARJETA)
            // ================================================================
            when (quote.state) {
                QuoteState.PENDIENTE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Botón A: "Aprobar Cotización" (Color Celeste de Acento #4A90D9, ancho completo)
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CelesteAcento,
                                contentColor = Blanco
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_approve_quote_${quote.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aprobar Cotización",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Blanco
                                )
                            )
                        }

                        // Botón B: "Rechazar o Pedir Revisión" (Texto sutil en gris/rojo)
                        TextButton(
                            onClick = onRevise,
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Text(
                                text = "Rechazar o Pedir Revisión Técnica",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrisTextoSecundario
                                )
                            )
                        }
                    }
                }
                QuoteState.APROBADO -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VerdeExitoFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cotización Aprobada por la Administración",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeExito
                                )
                            )
                        }
                    }
                }
                QuoteState.EN_REVISION -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CelesteAcentoClaro,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.PendingActions, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "En Ajuste Técnico con Ingeniería",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo Pop-up de Éxito al Enviar Solicitud.
 */
@Composable
private fun RequestSuccessDialog(
    ticketCode: String,
    category: String,
    date: String,
    shift: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(VerdeExitoFondo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = VerdeExito,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "¡Solicitud Recibida con Éxito!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = "Nuestro equipo técnico la revisará de inmediato para coordinar la cuadrilla especializada.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        textAlign = TextAlign.Center
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Código de Ticket: $ticketCode",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = CelesteAcento
                            )
                        )
                        Text(
                            text = "Especialidad: $category",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Preferencia: $date ($shift)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Entendido y Cerrar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                }
            }
        }
        }
    }
}

/**
 * Modal de Confirmación para Aprobar Cotización en 1 Clic.
 */
@Composable
private fun ConfirmApproveQuoteModal(
    quote: FullQuoteItem,
    onDismiss: () -> Unit,
    onConfirmApprove: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confirmar Aprobación",
                        style = MaterialTheme.typography.titleLarge.copy(
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
                    text = "¿Confirma la aprobación del presupuesto ${quote.quoteCode} por un monto de USD ${quote.amountUsd} para ${quote.title}?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulMarca,
                        lineHeight = 22.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = VerdeExitoFondo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Al aprobar, se asigna automáticamente la fecha de inicio y se emite la orden de trabajo para el equipo de Todoservicio.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = VerdeExito,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", fontFamily = PoppinsFontFamily)
                    }

                    Button(
                        onClick = onConfirmApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Text("Sí, Aprobar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
        }
    }
}

/**
 * Modal para Rechazar o Pedir Revisión con nota explicativa.
 */
@Composable
private fun RequestRevisionModal(
    quote: FullQuoteItem,
    onDismiss: () -> Unit,
    onSubmitRevision: (String) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Pedir Revisión de Cotización",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Text(
                    text = "Indique al ingeniero a cargo qué modificaciones de alcance, marcas de repuestos o cronograma necesita ajustar:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    placeholder = { Text("Ej: Solicito cotizar alternativa con repuesto nacional o dividir la obra en dos etapas...", fontFamily = PoppinsFontFamily) },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancelar", fontFamily = PoppinsFontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmitRevision(reasonText.ifBlank { "Revisión técnica solicitada por el cliente" }) },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Enviar a Revisión", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                    }
                }
            }
        }
        }
    }
}

/**
 * Modal de Desglose Formal PDF de la Cotización.
 */
@Composable
private fun QuotePdfBreakdownModal(
    quote: FullQuoteItem,
    clientProfile: ClientProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            text = quote.quoteCode,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario)
                    }
                }

                Text(
                    text = quote.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Text(
                    text = "Validez hasta: ${quote.validUntil} • Emitido por Todoservicio Uruguay",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "DESGLOSE DE RUBROS Y MATERIALES:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            )
                        )

                        quote.lineItems.forEach { (concept, price) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = concept,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulMarca
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "USD $price",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                            }
                        }

                        androidx.compose.material3.HorizontalDivider(color = GrisBorde)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL ESTIMADO:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Text(
                                text = "USD ${quote.amountUsd}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = CelesteAcento,
                                    fontSize = 18.sp
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val pdfFile = PdfReportGenerator.generatePdfReport(
                            context = context,
                            data = PdfReportGenerator.ReportData(
                                orderCode = quote.quoteCode.removePrefix("#"),
                                serviceTitle = quote.title,
                                category = quote.category,
                                executionDate = quote.issueDate,
                                leadTechnician = "Ingeniería Todoservicio",
                                clientName = clientProfile.clientName,
                                companyName = clientProfile.companyName,
                                clientRut = clientProfile.rut,
                                clientAddress = clientProfile.address,
                                findingsSummary = "${quote.scopeSummary} | Total: USD ${quote.amountUsd}"
                            )
                        )
                        if (pdfFile != null) {
                            PdfReportGenerator.openPdf(context, pdfFile)
                        } else {
                            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Descargar PDF Oficial", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
                }
            }
        }
        }
    }
}


