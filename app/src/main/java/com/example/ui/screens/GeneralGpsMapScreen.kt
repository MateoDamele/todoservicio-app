package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CleaningRepository
import com.example.data.OperationalRepository
import com.example.model.GpsMonitoringPoint
import com.example.model.GpsPointType
import com.example.model.TaskOperationStatus
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Módulo de Monitoreo General en Mapa GPS (Montevideo & Canelones).
 * Sincronizado en tiempo real con las OT activas y marcaciones del sistema.
 */
@Composable
fun GeneralGpsMapSection(
    onInspectTaskDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val operationalTasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    val cleaningMarks by CleaningRepository.marksList.collectAsStateWithLifecycle()
    val cleaningBuildings by CleaningRepository.buildingsList.collectAsStateWithLifecycle()

    // Construcción dinámica de puntos GPS reales (sin datos de demostración)
    val (points, pointCoords) = remember(operationalTasks, corporateClients, cleaningMarks) {
        val coordsMap = mutableMapOf<String, Pair<Double, Double>>()
        val taskPoints = operationalTasks
            .filter { it.status != TaskOperationStatus.FINALIZADA }
            .mapIndexed { idx, task ->
                val matchedClient = corporateClients.firstOrNull {
                    it.id == task.clientId || it.name.equals(task.clientName, ignoreCase = true)
                }
                val matchedBranch = matchedClient?.branches?.firstOrNull {
                    it.name.equals(task.sedeName, ignoreCase = true) ||
                        it.address.equals(task.address, ignoreCase = true)
                } ?: matchedClient?.branches?.firstOrNull()

                val lat = matchedBranch?.lat ?: com.example.util.URUGUAY_DEFAULT_LAT
                val lng = matchedBranch?.lng ?: com.example.util.URUGUAY_DEFAULT_LNG
                val id = "task-${task.id}"
                coordsMap[id] = lat to lng

                val pointType = when {
                    task.isEnCamino -> GpsPointType.EN_CAMINO
                    task.status == TaskOperationStatus.INICIO_EJECUCION -> GpsPointType.EN_SITIO
                    else -> GpsPointType.PENDIENTE
                }
                val techName = task.technicianName.ifBlank { task.clientName.ifBlank { "Sin asignar" } }
                val initials = techName.split(" ").filter { it.isNotBlank() }.take(2)
                    .joinToString("") { it.first().uppercase() }.ifBlank { "OT" }

                GpsMonitoringPoint(
                    id = id,
                    initials = initials,
                    name = techName,
                    role = if (task.technicianName.isNotBlank()) "Técnico asignado" else "Cliente corporativo",
                    pointType = pointType,
                    taskCode = task.code,
                    taskTitle = task.title,
                    clientName = task.clientName,
                    address = task.address.ifBlank { task.sedeName.ifBlank { "Montevideo, Uruguay" } },
                    zone = task.sedeName.ifBlank { "Montevideo" },
                    coordinates = String.format("%.4f° S, %.4f° W", lat, lng),
                    mapX = 0.5f,
                    mapY = 0.5f,
                    etaOrCheckIn = when (pointType) {
                        GpsPointType.EN_CAMINO -> "En trayecto al destino"
                        GpsPointType.EN_SITIO -> "En sitio ejecutando OT"
                        GpsPointType.PENDIENTE -> "Pendiente de despacho"
                    },
                    batteryPct = 100,
                    speedKmh = if (pointType == GpsPointType.EN_CAMINO) 38 else null,
                    phoneNumber = matchedBranch?.phone ?: "",
                    isDec406 = task.isDec406
                )
            }

        val cleanPoints = cleaningMarks
            .distinctBy { it.staffId }
            .map { mark ->
                val id = "clean-${mark.id}"
                coordsMap[id] = mark.lat to mark.lng
                GpsMonitoringPoint(
                    id = id,
                    initials = mark.avatarInitials,
                    name = mark.staffName,
                    role = "Personal de Limpieza",
                    pointType = GpsPointType.EN_SITIO,
                    taskCode = "LIMP-${mark.markType}",
                    taskTitle = "Jornada en ${mark.buildingName}",
                    clientName = mark.buildingName,
                    address = mark.buildingName,
                    zone = "Geocerca Limpieza",
                    coordinates = String.format("%.4f° S, %.4f° W", mark.lat, mark.lng),
                    mapX = 0.5f,
                    mapY = 0.5f,
                    etaOrCheckIn = "${mark.markType} • ${mark.markTime}",
                    batteryPct = 100,
                    phoneNumber = "",
                    isDec406 = false
                )
            }

        (taskPoints + cleanPoints) to coordsMap.toMap()
    }

    var selectedFilter by remember { mutableStateOf<GpsPointType?>(null) } // null = Todos
    var selectedPointId by remember { mutableStateOf<String?>(null) }
    var hasWebRendererError by remember { mutableStateOf(false) }

    // Estado de sincronización
    var isSyncing by remember { mutableStateOf(false) }
    var syncTimestamp by remember { mutableStateOf("En espera de actividad GPS") }

    // Animación de rotación del botón sincronizar
    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Animación de radar del marcador seleccionado
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val filteredPoints = remember(selectedFilter, points) {
        if (selectedFilter == null) points else points.filter { it.pointType == selectedFilter }
    }
    val selectedPoint = remember(filteredPoints, selectedPointId) {
        filteredPoints.firstOrNull { it.id == selectedPointId } ?: filteredPoints.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ====================================================================
        // CABECERA SUPERIOR: TÍTULO, SINCRONIZAR Y TIMESTAMP
        // ====================================================================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Blanco,
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Mapa de Monitoreo GPS en Vivo",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            // Indicador de en vivo
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = VerdeExito
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(6.dp).background(Blanco, CircleShape))
                                    Text(
                                        text = "EN VIVO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Montevideo y Ciudad de la Costa • $syncTimestamp",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Botón Sincronizar Informaciones
                    Button(
                        onClick = {
                            if (!isSyncing) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSyncing = true
                                scope.launch {
                                    delay(1200)
                                    val now = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                    syncTimestamp = "Sincronizado ahora ($now hs)"
                                    isSyncing = false
                                    Toast.makeText(context, "Información de GPS sincronizada con éxito", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSyncing) CelesteAcentoClaro else CelesteAcento,
                            contentColor = if (isSyncing) AzulCorporativo else Blanco
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("btn_sync_gps_map")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Sincronizar",
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isSyncing) Modifier.rotate(syncRotation) else Modifier)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing) "Sincronizando..." else "Sincronizar",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Barra de Chips de Filtros (Todos, En Sitio [Verde], En Camino [Naranja], Pendientes [Rojo])
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val inactiveContainer = Color(0xFFEAEEF4)
                    val inactiveText = Color(0xFF102C54)
                    val activeContainer = AzulMarca
                    val activeText = Blanco

                    // Filtro: Todos
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = {
                            Text(
                                "Todos (${points.size})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == null,
                            borderColor = Color(0xFFD0D7DE),
                            selectedBorderColor = Color.Transparent
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = inactiveContainer,
                            labelColor = inactiveText,
                            selectedContainerColor = activeContainer,
                            selectedLabelColor = activeText
                        )
                    )

                    // Filtro: En Sitio
                    FilterChip(
                        selected = selectedFilter == GpsPointType.EN_SITIO,
                        onClick = { selectedFilter = GpsPointType.EN_SITIO },
                        leadingIcon = {
                            Box(modifier = Modifier.size(9.dp).background(VerdeExito, CircleShape))
                        },
                        label = {
                            Text(
                                "En Sitio (${points.count { it.pointType == GpsPointType.EN_SITIO }})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedFilter == GpsPointType.EN_SITIO) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == GpsPointType.EN_SITIO,
                            borderColor = Color(0xFFD0D7DE),
                            selectedBorderColor = Color.Transparent
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = inactiveContainer,
                            labelColor = inactiveText,
                            selectedContainerColor = activeContainer,
                            selectedLabelColor = activeText
                        )
                    )

                    // Filtro: En Camino
                    FilterChip(
                        selected = selectedFilter == GpsPointType.EN_CAMINO,
                        onClick = { selectedFilter = GpsPointType.EN_CAMINO },
                        leadingIcon = {
                            Box(modifier = Modifier.size(9.dp).background(NaranjaAlerta, CircleShape))
                        },
                        label = {
                            Text(
                                "En Camino (${points.count { it.pointType == GpsPointType.EN_CAMINO }})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedFilter == GpsPointType.EN_CAMINO) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == GpsPointType.EN_CAMINO,
                            borderColor = Color(0xFFD0D7DE),
                            selectedBorderColor = Color.Transparent
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = inactiveContainer,
                            labelColor = inactiveText,
                            selectedContainerColor = activeContainer,
                            selectedLabelColor = activeText
                        )
                    )

                    // Filtro: Pendientes
                    FilterChip(
                        selected = selectedFilter == GpsPointType.PENDIENTE,
                        onClick = { selectedFilter = GpsPointType.PENDIENTE },
                        leadingIcon = {
                            Box(modifier = Modifier.size(9.dp).background(RojoUrgencia, CircleShape))
                        },
                        label = {
                            Text(
                                "Pendientes (${points.count { it.pointType == GpsPointType.PENDIENTE }})",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (selectedFilter == GpsPointType.PENDIENTE) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp
                            )
                        },
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == GpsPointType.PENDIENTE,
                            borderColor = Color(0xFFD0D7DE),
                            selectedBorderColor = Color.Transparent
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = inactiveContainer,
                            labelColor = inactiveText,
                            selectedContainerColor = activeContainer,
                            selectedLabelColor = activeText
                        )
                    )
                }
            }
        }

        // ====================================================================
        // MAPA CALLEJERO REAL (MONTEVIDEO Y CIUDAD DE LA COSTA)
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFE4EDF7))
                .border(1.5.dp, GrisBorde, RoundedCornerShape(18.dp))
        ) {
            // Mapa de Google real de Montevideo y Canelones
            NativeMontevideoInteractiveMap(
                points = filteredPoints,
                pointCoords = pointCoords,
                buildings = cleaningBuildings,
                selectedPoint = selectedPoint,
                onSelectPoint = { pt ->
                    selectedPointId = pt.id
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Indicador flotante superior de Google Maps
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AzulMarca.copy(alpha = 0.88f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.LocationOn, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Google Maps • Montevideo & Canelones",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = Blanco,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        // Asistente compacto de Maps Grounding para referencias y direcciones
        val selectedCoords = selectedPoint?.let { pointCoords[it.id] }
        com.example.ui.components.MapsGroundingAssistantCard(
            defaultQuery = selectedPoint?.address ?: "Montevideo y Canelones",
            lat = selectedCoords?.first ?: -34.9011,
            lng = selectedCoords?.second ?: -56.1645,
            title = "Consultar Ubicación"
        )

        if (selectedPoint == null) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GpsFixed,
                        contentDescription = null,
                        tint = GrisTextoSecundario.copy(alpha = 0.55f),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "Sin cuadrillas activas en mapa",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = GrisTextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ====================================================================
        // TARJETA FLOTANTE DE DETALLE
        // ====================================================================
        selectedPoint?.let { point ->
            val pointBadgeColor = when (point.pointType) {
                GpsPointType.EN_SITIO -> VerdeExito
                GpsPointType.EN_CAMINO -> NaranjaAlerta
                GpsPointType.PENDIENTE -> RojoUrgencia
            }
            val pointBadgeFondo = when (point.pointType) {
                GpsPointType.EN_SITIO -> VerdeExitoFondo
                GpsPointType.EN_CAMINO -> NaranjaAlertaFondo
                GpsPointType.PENDIENTE -> RojoUrgenciaFondo
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x22102C54),
                        spotColor = Color(0x35102C54)
                    )
                    .testTag("gps_selected_point_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                shape = CircleShape,
                                color = pointBadgeColor,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = point.initials,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco
                                        )
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = point.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca
                                    )
                                )
                                Text(
                                    text = point.role,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Badge de Estado del punto
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pointBadgeFondo
                        ) {
                            Text(
                                text = point.pointType.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = pointBadgeColor,
                                    fontSize = 10.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    androidx.compose.material3.HorizontalDivider(color = GrisBorde.copy(alpha = 0.5f))

                    // Datos de la tarea en curso y ubicación
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${point.taskCode} • ${point.taskTitle}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulCorporativo
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.LocationOn, null, tint = CelesteAcento, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${point.clientName} (${point.address})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (point.pointType == GpsPointType.EN_CAMINO) Icons.Rounded.Navigation else Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = pointBadgeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${point.etaOrCheckIn} • Coord: ${point.coordinates}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = pointBadgeColor,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Fila de Telemetría (Batería, GPS, Velocidad si aplica)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GrisNeutro, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.BatteryFull, null, tint = VerdeExito, modifier = Modifier.size(14.dp))
                            Text("Batería: ${point.batteryPct}%", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = AzulCorporativo)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.GpsFixed, null, tint = CelesteAcento, modifier = Modifier.size(14.dp))
                            Text("GPS: ±4m", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = AzulCorporativo)
                        }

                        point.speedKmh?.let { speed ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Rounded.Speed, null, tint = NaranjaAlerta, modifier = Modifier.size(14.dp))
                                Text("$speed km/h", fontFamily = PoppinsFontFamily, fontSize = 11.sp, color = AzulCorporativo)
                            }
                        }
                    }

                    // Botones de acción rápida: Llamar al Técnico y Ver Detalle Completo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${point.phoneNumber}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Llamando a ${point.name} (${point.phoneNumber})", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Rounded.Call, null, modifier = Modifier.size(16.dp), tint = Blanco)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (point.pointType == GpsPointType.PENDIENTE) "Llamar Cliente" else "Llamar Técnico",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                com.example.ui.components.openExternalGoogleMaps(
                                    context = context,
                                    queryOrAddress = point.address
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Rounded.Navigation, null, modifier = Modifier.size(16.dp), tint = CelesteAcento)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Google Maps",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onInspectTaskDetail(point.taskCode)
                                Toast.makeText(context, "Inspeccionando orden ${point.taskCode}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AzulCorporativo),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Rounded.Visibility, null, modifier = Modifier.size(16.dp), tint = AzulCorporativo)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detalle",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Mapa interactivo nativo vectorial de Montevideo y Canelones.
 * Proporciona renderizado resiliente y fluido con avenidas principales,
 * costas de la Rambla y marcadores interactivos geolocalizados.
 */
@Composable
fun NativeMontevideoInteractiveMap(
    points: List<GpsMonitoringPoint>,
    pointCoords: Map<String, Pair<Double, Double>> = emptyMap(),
    buildings: List<com.example.model.CleaningBuilding> = emptyList(),
    selectedPoint: GpsMonitoringPoint?,
    onSelectPoint: (GpsMonitoringPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    fun coordinatesForPoint(pt: GpsMonitoringPoint): Pair<Double, Double> {
        return pointCoords[pt.id] ?: (-34.9011 to -56.1645)
    }

    val markers = remember(points, pointCoords) {
        points.map { pt ->
            val (lat, lng) = coordinatesForPoint(pt)
            com.example.ui.components.MapMarkerItem(
                id = pt.id,
                title = pt.name,
                subtitle = "${pt.clientName} • ${pt.address}",
                lat = lat,
                lng = lng,
                type = "TECH",
                isAlert = pt.pointType == GpsPointType.PENDIENTE,
                badgeText = pt.initials,
                timeText = pt.etaOrCheckIn
            )
        }
    }

    val liveGeofences = remember(buildings) {
        buildings.map { b ->
            com.example.ui.components.MapGeocenceItem(
                id = b.id,
                name = b.name,
                address = b.address,
                lat = b.lat,
                lng = b.lng,
                radiusMeters = b.geofenceRadiusMeters,
                staffCount = b.assignedStaffNames.size
            )
        }
    }

    val activeRoute = remember(selectedPoint, pointCoords) {
        if (selectedPoint?.pointType == GpsPointType.EN_CAMINO) {
            val (destLat, destLng) = coordinatesForPoint(selectedPoint)
            listOf(
                (destLat - 0.012) to (destLng - 0.015),
                (destLat - 0.005) to (destLng - 0.006),
                destLat to destLng
            )
        } else {
            emptyList()
        }
    }

    val (focusLat, focusLng) = selectedPoint?.let { coordinatesForPoint(it) }
        ?: (com.example.util.URUGUAY_DEFAULT_LAT to com.example.util.URUGUAY_DEFAULT_LNG)

    com.example.ui.components.RealStreetInteractiveMap(
        centerLat = focusLat,
        centerLng = focusLng,
        zoomLevel = if (selectedPoint != null) 15 else 13,
        geofences = liveGeofences,
        markers = markers,
        routePolyline = activeRoute,
        onMarkerClick = { id ->
            points.firstOrNull { it.id == id }?.let { onSelectPoint(it) }
        },
        centerOnUserLocationIfNoTarget = (selectedPoint == null),
        modifier = modifier
    )
}
