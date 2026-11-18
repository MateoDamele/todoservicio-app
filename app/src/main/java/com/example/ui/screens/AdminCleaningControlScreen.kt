package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import com.example.util.PdfReportGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthRepository
import com.example.data.CleaningRepository
import com.example.model.CleaningAttendanceMark
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.model.UserRole
import com.example.ui.components.BouncingTouchSurface
import com.example.ui.components.modalBackdropBlur
import com.example.ui.theme.AmbarCalido
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisPerlaFrio
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.GrisTextoTenue
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.VerdeEsmeralda
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

/**
 * Panel de Administración de Limpieza y Control de Asistencia Georreferenciada.
 * Referencia visual directa a Lenox HR (image_2957ff.jpg):
 * 1. Mapa interactivo con capas simultáneas: Geocercas circulares (50m-150m), marcas Entrada (E) y Salida (S).
 * 2. Carrusel/Panel de Lugares / Geocercas manuales con modal "+ Agregar Edificio / Geocerca".
 * 3. Tabla auditora detallada de marcaciones del día con alerta de rango fuera de geocerca (⚠️).
 * 4. Botones de exportación: [PDF] • [Excel] • [CSV].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCleaningControlScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val staffList by CleaningRepository.staffList.collectAsStateWithLifecycle()
    val allAccounts by AuthRepository.accountsState.collectAsStateWithLifecycle()
    val marksList by CleaningRepository.marksList.collectAsStateWithLifecycle()
    val buildingsList by CleaningRepository.buildingsList.collectAsStateWithLifecycle()

    // Combinación de personal de limpieza registrado en legajo + perfiles RBAC LIMPIEZA
    val availableCleaningStaff = remember(staffList, allAccounts) {
        val accountStaff = allAccounts
            .filter { it.role == UserRole.LIMPIEZA && it.isActive }
            .map { acc ->
                CleaningStaffMember(
                    id = acc.uid,
                    fullName = acc.fullName,
                    ci = "",
                    birthDate = "",
                    address = "",
                    phone = acc.phone,
                    assignedBuildings = acc.headquarters,
                    username = acc.username
                )
            }
        val combined = mutableListOf<CleaningStaffMember>()
        combined.addAll(staffList)
        accountStaff.forEach { accStaff ->
            val exists = combined.any {
                it.id == accStaff.id ||
                    it.username.equals(accStaff.username, ignoreCase = true) ||
                    it.fullName.equals(accStaff.fullName, ignoreCase = true)
            }
            if (!exists) combined.add(accStaff)
        }
        combined
    }

    // Buscadores
    var geocercaSearchQuery by remember { mutableStateOf("") }
    var staffSearchQuery by remember { mutableStateOf("") }

    // Estados de selección interactiva en el mapa
    var selectedMarkOnMap by remember { mutableStateOf<CleaningAttendanceMark?>(null) }
    var selectedBuildingOnMap by remember { mutableStateOf<CleaningBuilding?>(null) }

    // Modal de Alerta "Fuera de Rango"
    var outOfRangeAlertMark by remember { mutableStateOf<CleaningAttendanceMark?>(null) }

    // Modal "+ Agregar Edificio / Geocerca"
    var showAddBuildingModal by remember { mutableStateOf(false) }

    // Modal "+ Marca Manual" (sin GPS)
    var showManualMarkModal by remember { mutableStateOf(false) }

    // Modal de edición de novedad de marca
    var markToEdit by remember { mutableStateOf<CleaningAttendanceMark?>(null) }

    // Transformación del mapa (pan y zoom táctil)
    var mapZoom by remember { mutableFloatStateOf(1.0f) }
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }

    val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .modalBackdropBlur(
                outOfRangeAlertMark != null ||
                    showAddBuildingModal ||
                    showManualMarkModal ||
                    markToEdit != null
            )
            .background(GrisPerlaFrio)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================================================================
            // 1. PASTILLA DE MÉTRICAS COMPACTA Y DINÁMICA (ENFOQUE MINIMALISTA)
            // ================================================================
            item {
                val totalStaff = availableCleaningStaff.size
                val activeStaff = marksList.filter { it.markType == "E" }.distinctBy { it.staffId }.size
                val outOfRangeCount = marksList.count { !it.isManualEntry && !it.isInsideGeofence }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Blanco,
                    border = BorderStroke(1.dp, GrisBorde),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Pastilla [ 👥 5/6 activas en turno ]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VerdeExitoFondo
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Groups,
                                    contentDescription = null,
                                    tint = VerdeExito,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$activeStaff/$totalStaff activas en turno",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Text("•", color = GrisTextoSecundario.copy(alpha = 0.5f), fontSize = 14.sp)

                        // Pastilla [ ⚠️ 1 fuera de rango ]
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NaranjaAlertaFondo
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = NaranjaAlerta,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "$outOfRangeCount fuera de rango",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = NaranjaAlerta,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 2. MAPA CENTRAL DE GEOCERCAS Y PUNTOS DE MARCAJE (CANVAS NATIVO)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Icon(
                                imageVector = Icons.Rounded.Navigation,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Mapa de Geocercas y Puntos de Marcaje",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        // Leyenda de capas (E: Entrada verde, S: Salida morada, Círculo: Geocerca)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(VerdeEsmeralda, CircleShape))
                                Text("Entrada (E)", fontSize = 9.5.sp, color = GrisTextoSecundario, fontFamily = PoppinsFontFamily)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(AzulCorporativo, CircleShape))
                                Text("Salida (S)", fontSize = 9.5.sp, color = GrisTextoSecundario, fontFamily = PoppinsFontFamily)
                            }
                        }
                    }

                    // Mapeo a items de Geocerca y Marcadores para el Mapa Callejero Real
                    val mapGeofences = remember(buildingsList) {
                        buildingsList.map { b ->
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
                    val mapMarkers = remember(marksList) {
                        marksList
                            .filter { !it.isManualEntry }
                            .map { m ->
                                com.example.ui.components.MapMarkerItem(
                                    id = m.id,
                                    title = m.staffName,
                                    subtitle = m.buildingName,
                                    lat = m.lat,
                                    lng = m.lng,
                                    type = m.markType,
                                    isAlert = !m.isInsideGeofence,
                                    badgeText = m.markType,
                                    timeText = m.markTime
                                )
                            }
                    }

                    // Visor HD de calles de Uruguay con Geocercas y Marcas de Asistencia
                    val focusLat = selectedMarkOnMap?.lat ?: selectedBuildingOnMap?.lat ?: com.example.util.URUGUAY_DEFAULT_LAT
                    val focusLng = selectedMarkOnMap?.lng ?: selectedBuildingOnMap?.lng ?: com.example.util.URUGUAY_DEFAULT_LNG
                    val focusZoom = if (selectedMarkOnMap != null || selectedBuildingOnMap != null) 16 else 13

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Blanco, Color(0xFFE2E8F0)))),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x0F102C54))
                    ) {
                        com.example.ui.components.RealStreetInteractiveMap(
                            centerLat = focusLat,
                            centerLng = focusLng,
                            zoomLevel = focusZoom,
                            geofences = mapGeofences,
                            markers = mapMarkers,
                            onMarkerClick = { markerId ->
                                selectedMarkOnMap = marksList.firstOrNull { it.id == markerId }
                            },
                            centerOnUserLocationIfNoTarget = (selectedMarkOnMap == null && selectedBuildingOnMap == null),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    com.example.ui.components.MapsGroundingAssistantCard(
                        defaultQuery = selectedBuildingOnMap?.address
                            ?: selectedMarkOnMap?.buildingName
                            ?: "Montevideo y Canelones",
                        lat = focusLat,
                        lng = focusLng,
                        title = "Verificar Edificio / Geocerca"
                    )

                    // Tarjeta Flotante con el Detalle Exacto de la Marca Seleccionada
                    selectedMarkOnMap?.let { mark ->
                        val isMarkEntrada = mark.markType.equals("E", ignoreCase = true)
                        val pinAccent = if (isMarkEntrada) VerdeEsmeralda else AzulCorporativo
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            border = BorderStroke(1.2.dp, if (mark.isInsideGeofence) pinAccent else Color(0xFFF59E0B)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = pinAccent.copy(alpha = 0.16f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = mark.markType,
                                                fontWeight = FontWeight.Bold,
                                                color = pinAccent,
                                                fontSize = 16.sp,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${mark.staffName} • ${mark.markTime} (${if (isMarkEntrada) "Entrada" else "Salida"})",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${mark.buildingName} • ${mark.buildingAddress}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 11.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (mark.isInsideGeofence) {
                                                "En geocerca (${mark.distanceToBuildingMeters} m)"
                                            } else {
                                                "⚠️ Fuera de rango: a ${mark.distanceToBuildingMeters} metros"
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = if (mark.isInsideGeofence) VerdeExito else Color(0xFFD97706),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { selectedMarkOnMap = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 3. PANEL DE LUGARES Y ASIGNACIÓN (GEOCERCAS MANUALES)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edificios y Geocercas (${buildingsList.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 14.sp
                            )
                        )

                        // Botón [ + Agregar Edificio / Geocerca ]
                        BouncingTouchSurface(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showAddBuildingModal = true
                            }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CelesteAcento,
                                modifier = Modifier.testTag("btn_add_geocerca")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.AddLocationAlt, contentDescription = null, tint = Blanco, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "+ Agregar Edificio",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Buscador de Geocercas
                    OutlinedTextField(
                        value = geocercaSearchQuery,
                        onValueChange = { geocercaSearchQuery = it },
                        placeholder = { Text("Buscar geocerca o edificio...", fontSize = 11.5.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco,
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        )
                    )

                    // Carrusel Horizontal de Tarjetas Compactas de Edificios
                    val filteredBuildings = buildingsList.filter {
                        geocercaSearchQuery.isBlank() || it.name.contains(geocercaSearchQuery, ignoreCase = true)
                    }

                    if (filteredBuildings.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            border = BorderStroke(1.dp, GrisBorde),
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
                                    imageVector = Icons.Rounded.Apartment,
                                    contentDescription = null,
                                    tint = GrisTextoSecundario.copy(alpha = 0.55f),
                                    modifier = Modifier.size(30.dp)
                                )
                                Text(
                                    text = "Sin registros aún. Toca + Agregar Edificio para comenzar",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.5.sp,
                                    color = GrisTextoSecundario,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredBuildings, key = { it.id }) { building ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Blanco),
                                    border = BorderStroke(1.dp, Brush.linearGradient(listOf(Blanco, Color(0xFFE2E8F0)))),
                                    modifier = Modifier
                                        .width(200.dp)
                                        .shadow(4.dp, RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedBuildingOnMap = building
                                            Toast.makeText(context, "Geocerca ${building.name}: ${building.geofenceRadiusMeters}m", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF00897B).copy(alpha = 0.12f),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Rounded.Apartment, contentDescription = null, tint = Color(0xFF00897B), modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        Text(
                                            text = building.name,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca,
                                                fontSize = 12.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = building.address,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario,
                                            fontSize = 10.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Indicador de Radio y Personal asignado
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
                                                text = "${building.geofenceRadiusMeters} m radio",
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
                                            text = "${building.assignedStaffNames.size} empleada${if (building.assignedStaffNames.size > 1) "s" else ""}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

            // ================================================================
            // 4. TABLA AUDITORA DE MARCAS CON BOTONES DE EXPORTACIÓN (LENOX HR)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cabecera con Botón [+ Marca Manual] y Botones de Exportación [PDF] • [Excel] • [CSV]
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auditoría de Marcas del Día",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AzulMarca,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showManualMarkModal = true
                                    }
                                    .testTag("btn_open_manual_mark_modal")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EditNote,
                                        contentDescription = null,
                                        tint = Blanco,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "+ Marca Manual",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Botones de Exportación
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // PDF
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Blanco,
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val rows = marksList.map {
                                                val tag = if (it.isManualEntry) "Carga Manual" else "${it.distanceToBuildingMeters}m"
                                                "${it.date} ${it.markTime} [${it.markType}] • ${it.staffName} • ${it.buildingName} ($tag) - ${it.comment}"
                                            }
                                            val pdf = PdfReportGenerator.generatePdfReport(
                                                context = context,
                                                data = PdfReportGenerator.ReportData(
                                                    orderCode = "ASISTENCIA-LIMPIEZA",
                                                    serviceTitle = "Planilla de Asistencias Georreferenciadas",
                                                    category = "Control de Limpieza",
                                                    companyName = "Todoservicio S.A.S.",
                                                    tasksExecuted = if (rows.isEmpty()) listOf("Sin marcaciones registradas") else rows,
                                                    findingsSummary = "Total marcaciones: ${marksList.size}. Fuera de rango: ${marksList.count { !it.isManualEntry && !it.isInsideGeofence }}."
                                                )
                                            )
                                            if (pdf != null) PdfReportGenerator.openPdf(context, pdf)
                                            else Toast.makeText(context, "Error generando PDF", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = "PDF", tint = RojoUrgencia, modifier = Modifier.size(13.dp))
                                        Text("PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AzulMarca, fontFamily = PoppinsFontFamily)
                                    }
                                }

                                // Excel / CSV share
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Blanco,
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val csvContent = buildString {
                                                appendLine("Fecha,Hora,Tipo,Colaboradora,Edificio,Modo,DistanciaM,EnRango,Comentario")
                                                marksList.forEach {
                                                    val mode = if (it.isManualEntry) "Carga Manual" else "GPS"
                                                    appendLine("${it.date},${it.markTime},${it.markType},${it.staffName},${it.buildingName},$mode,${it.distanceToBuildingMeters},${it.isInsideGeofence},${it.comment}")
                                                }
                                            }
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_SUBJECT, "Asistencia Limpieza Todoservicio")
                                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Exportar Planilla Excel/CSV"))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Rounded.TableChart, contentDescription = "Excel", tint = VerdeExito, modifier = Modifier.size(13.dp))
                                        Text("Excel", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AzulMarca, fontFamily = PoppinsFontFamily)
                                    }
                                }

                                // CSV
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Blanco,
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val csvContent = buildString {
                                                appendLine("Fecha,Hora,Tipo,Colaboradora,Edificio,Modo,DistanciaM,EnRango,Comentario")
                                                marksList.forEach {
                                                    val mode = if (it.isManualEntry) "Carga Manual" else "GPS"
                                                    appendLine("${it.date},${it.markTime},${it.markType},${it.staffName},${it.buildingName},$mode,${it.distanceToBuildingMeters},${it.isInsideGeofence},${it.comment}")
                                                }
                                            }
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "Dataset Asistencia CSV")
                                                putExtra(Intent.EXTRA_TEXT, csvContent)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Exportar CSV"))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Rounded.FileDownload, contentDescription = "CSV", tint = CelesteAcento, modifier = Modifier.size(13.dp))
                                        Text("CSV", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AzulMarca, fontFamily = PoppinsFontFamily)
                                    }
                                }
                            }
                        }
                    }

                    // Buscador de colaboradoras
                    OutlinedTextField(
                        value = staffSearchQuery,
                        onValueChange = { staffSearchQuery = it },
                        placeholder = { Text("Buscar por empleada...", fontSize = 11.5.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CelesteAcento, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco,
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        )
                    )
                }
            }

            // LISTA DE FILAS DE MARCAS (FORMATO AUDITOR LENOX HR)
            val filteredMarks = marksList.filter {
                staffSearchQuery.isBlank() || it.staffName.contains(staffSearchQuery, ignoreCase = true)
            }

            if (filteredMarks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CleaningServices,
                                contentDescription = null,
                                tint = GrisTextoSecundario.copy(alpha = 0.55f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "Sin registros aún",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                color = GrisTextoSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(filteredMarks, key = { it.id }) { mark ->
                val isEntrada = mark.markType.equals("E", ignoreCase = true)
                val markAccent = if (isEntrada) VerdeEsmeralda else AzulCorporativo
                val showOutOfRangeBorder = !mark.isManualEntry && !mark.isInsideGeofence

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = if (showOutOfRangeBorder) BorderStroke(1.5.dp, Color(0xFFF59E0B)) else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            if (mark.isManualEntry) {
                                Toast.makeText(context, "Carga manual administrativa (sin coordenadas GPS)", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedMarkOnMap = mark
                                Toast.makeText(context, "Centrando mapa en ${mark.staffName}", Toast.LENGTH_SHORT).show()
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Empleada + Fecha + Marcación
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
                                // Avatar con Iniciales
                                Surface(
                                    shape = CircleShape,
                                    color = markAccent.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = mark.avatarInitials,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = markAccent,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mark.staffName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulMarca,
                                            fontSize = 13.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${mark.date} • ${mark.buildingName}",
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

                            // Hora e Indicador E / S
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = markAccent
                            ) {
                                Text(
                                    text = "${mark.markTime} ${mark.markType}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // 2. Validación de Geocerca / Carga Manual y Comentario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (mark.isManualEntry) {
                                    // Distintivo sobrio gris [ Carga Manual ]
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF1F5F9),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(Icons.Rounded.EditNote, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(12.dp))
                                            Text(
                                                text = "Carga Manual",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475569),
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }
                                } else if (mark.isInsideGeofence) {
                                    // Marca dentro del rango
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = VerdeExitoFondo
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = null, tint = VerdeExito, modifier = Modifier.size(11.dp))
                                            Text(
                                                text = "En rango (${mark.distanceToBuildingMeters}m)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VerdeExito,
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }
                                } else {
                                    // MARCA FUERA DE RANGO: Alerta triangular ámbar destacada con metros exactos
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NaranjaAlertaFondo,
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                        modifier = Modifier.clickable {
                                            outOfRangeAlertMark = mark
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Rounded.Warning, contentDescription = "Alerta", tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                            Text(
                                                text = "⚠️ Fuera de rango: a ${mark.distanceToBuildingMeters}m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706),
                                                fontFamily = PoppinsFontFamily
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "• ${mark.comment}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = if (mark.isInsideGeofence || mark.isManualEntry) GrisTextoSecundario else RojoUrgencia,
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }

                            // Acciones Rápidas: Ojo para centrar (solo si tiene GPS) y editar
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!mark.isManualEntry) {
                                    IconButton(
                                        onClick = {
                                            selectedMarkOnMap = mark
                                            Toast.makeText(context, "Mostrando ${mark.staffName} en el mapa GPS", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Rounded.Visibility, contentDescription = "Ver en mapa", tint = CelesteAcento, modifier = Modifier.size(16.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        markToEdit = mark
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Editar", tint = GrisTextoSecundario, modifier = Modifier.size(15.dp))
                                }
                            }
                        }

                        if (mark.durationMinutes != null && mark.durationMinutes > 0) {
                            val hrs = mark.durationMinutes / 60
                            val mins = mark.durationMinutes % 60
                            Text(
                                text = "Horas computadas del turno: ${hrs}h ${mins}m",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulCorporativo
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(108.dp)) }
        }
    }

    // ========================================================================
    // MODAL ADVERTENCIA: MARCA FUERA DE RANGO
    // ========================================================================
    outOfRangeAlertMark?.let { alertMark ->
        AlertDialog(
            onDismissRequest = { outOfRangeAlertMark = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                    Text(
                        text = "Alerta: Marca Fuera de Rango",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "La colaboradora ${alertMark.staffName} registró su marcación a ${alertMark.distanceToBuildingMeters} metros del perímetro oficial del edificio ${alertMark.buildingName}.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        color = AzulMarca
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NaranjaAlertaFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Radio permitido: 100m • Desviación detectada: +${alertMark.distanceToBuildingMeters}m.\nCoordenada GPS: (${String.format("%.4f", alertMark.lat)}, ${String.format("%.4f", alertMark.lng)})",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFD97706),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Text(
                        text = "¿Desea justificar esta excepción o contactar a la colaboradora?",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.5.sp,
                        color = GrisTextoSecundario
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Excepción validada por supervisor", Toast.LENGTH_SHORT).show()
                        outOfRangeAlertMark = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Justificar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { outOfRangeAlertMark = null }) {
                    Text("Cerrar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ========================================================================
    // MODAL: + AGREGAR EDIFICIO / GEOCERCA POR ENLACE DE GOOGLE MAPS
    // ========================================================================
    if (showAddBuildingModal) {
        var buildingNameInput by remember { mutableStateOf("") }
        var buildingAddressInput by remember { mutableStateOf("") }
        var mapsLinkInput by remember { mutableStateOf("") }
        var resolvedLat by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LAT) }
        var resolvedLng by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LNG) }
        var hasResolvedCoords by remember { mutableStateOf(false) }
        var selectedRadiusMeters by remember { mutableIntStateOf(100) }
        val assignedStaffSelection = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showAddBuildingModal = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.AddLocationAlt, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Agregar Edificio / Geocerca",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = buildingNameInput,
                        onValueChange = { buildingNameInput = it },
                        label = { Text("Nombre del Lugar / Edificio") },
                        placeholder = { Text("ej. Edificio 21 de Septiembre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = buildingAddressInput,
                        onValueChange = { buildingAddressInput = it },
                        label = { Text("Dirección Física (Uruguay)") },
                        placeholder = { Text("ej. 21 de Septiembre 2840, Montevideo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    com.example.ui.components.GoogleMapsLinkPickerField(
                        linkValue = mapsLinkInput,
                        onLinkValueChange = { mapsLinkInput = it },
                        previewName = buildingNameInput,
                        previewAddress = buildingAddressInput,
                        resolvedLat = resolvedLat,
                        resolvedLng = resolvedLng,
                        hasResolvedCoordinates = hasResolvedCoords,
                        geofenceRadiusMeters = selectedRadiusMeters,
                        onRadiusSelected = { selectedRadiusMeters = it },
                        onCoordinatesResolved = { lat, lng, placeLabel, _ ->
                            resolvedLat = lat
                            resolvedLng = lng
                            hasResolvedCoords = true
                            if (buildingNameInput.isBlank() && !placeLabel.isNullOrBlank()) {
                                buildingNameInput = placeLabel
                            }
                        }
                    )

                    // Asignación de Personal
                    Text(
                        text = "Asignar Funcionarias de Limpieza:",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = AzulMarca
                    )

                    if (staffList.isEmpty()) {
                        Text(
                            text = "Podés asignar funcionarias luego desde Control de Accesos.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                    } else {
                        staffList.forEach { staff ->
                            val isAssigned = assignedStaffSelection.contains(staff.fullName)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isAssigned) assignedStaffSelection.remove(staff.fullName)
                                        else assignedStaffSelection.add(staff.fullName)
                                    }
                            ) {
                                Checkbox(
                                    checked = isAssigned,
                                    onCheckedChange = { checked ->
                                        if (checked) assignedStaffSelection.add(staff.fullName)
                                        else assignedStaffSelection.remove(staff.fullName)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AzulCorporativo)
                                )
                                Text(
                                    text = staff.fullName,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = AzulMarca
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (buildingNameInput.isBlank()) {
                            Toast.makeText(context, "Ingresá el nombre del edificio", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!hasResolvedCoords) {
                            Toast.makeText(context, "Pegá un enlace de Google Maps válido para fijar la geocerca", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val newBuilding = CleaningBuilding(
                            id = "bld-${System.currentTimeMillis()}",
                            name = buildingNameInput.trim(),
                            address = buildingAddressInput.ifBlank { "Montevideo, Uruguay" },
                            lat = resolvedLat,
                            lng = resolvedLng,
                            geofenceRadiusMeters = selectedRadiusMeters,
                            assignedStaffNames = assignedStaffSelection.toList(),
                            googleMapsUrl = mapsLinkInput.trim()
                        )
                        CleaningRepository.addBuilding(newBuilding)
                        selectedBuildingOnMap = newBuilding
                        Toast.makeText(context, "Geocerca '${newBuilding.name}' guardada con ubicación real", Toast.LENGTH_LONG).show()
                        showAddBuildingModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar Geocerca", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBuildingModal = false }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(20.dp)
        )
    }

    markToEdit?.let { editingMark ->
        var editedComment by remember(editingMark) { mutableStateOf(editingMark.comment) }
        AlertDialog(
            onDismissRequest = { markToEdit = null },
            title = {
                Text(
                    text = "Editar Novedad",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontSize = 16.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = editedComment,
                    onValueChange = { editedComment = it },
                    label = { Text("Observación de asistencia") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        CleaningRepository.updateMarkComment(
                            markId = editingMark.id,
                            newComment = editedComment.ifBlank { "Sin novedad" }
                        )
                        Toast.makeText(context, "Observación actualizada", Toast.LENGTH_SHORT).show()
                        markToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { markToEdit = null }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(18.dp)
        )
    }

    if (showManualMarkModal) {
        ManualAttendanceMarkDialog(
            staffOptions = availableCleaningStaff,
            buildingOptions = buildingsList,
            onDismiss = { showManualMarkModal = false },
            onSaveManualMark = { staff, buildingName, buildingAddress, markType, dateFormatted, timeFormatted, comment ->
                val created = CleaningRepository.registerManualMark(
                    staffId = staff.id,
                    staffName = staff.fullName,
                    buildingName = buildingName,
                    buildingAddress = buildingAddress,
                    markType = markType,
                    dateFormatted = dateFormatted,
                    timeFormatted = timeFormatted,
                    comment = comment
                )
                val hoursMsg = created.durationMinutes?.let { mins ->
                    " (+${mins / 60}h ${mins % 60}m sumadas)"
                }.orEmpty()
                Toast.makeText(
                    context,
                    "Marca manual registrada: ${created.staffName} [$markType]$hoursMsg",
                    Toast.LENGTH_LONG
                ).show()
                showManualMarkModal = false
            }
        )
    }
}

/**
 * Modal rápido de Administración para registrar marcas manuales SIN GPS (ej. olvido o falta de señal en subsuelo).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualAttendanceMarkDialog(
    staffOptions: List<CleaningStaffMember>,
    buildingOptions: List<CleaningBuilding>,
    onDismiss: () -> Unit,
    onSaveManualMark: (
        staff: CleaningStaffMember,
        buildingName: String,
        buildingAddress: String,
        markType: String,
        dateFormatted: String,
        timeFormatted: String,
        comment: String
    ) -> Unit
) {
    val context = LocalContext.current
    var selectedStaff by remember(staffOptions) { mutableStateOf(staffOptions.firstOrNull()) }
    var staffMenuExpanded by remember { mutableStateOf(false) }

    var selectedBuilding by remember(buildingOptions) { mutableStateOf(buildingOptions.firstOrNull()) }
    var customBuildingName by remember { mutableStateOf("") }
    var buildingMenuExpanded by remember { mutableStateOf(false) }

    var selectedMarkType by remember { mutableStateOf("E") }
    var adminComment by remember { mutableStateOf("") }

    val nowCal = remember { java.util.Calendar.getInstance() }
    var selectedDateFormatted by remember {
        mutableStateOf(
            String.format(
                java.util.Locale("es", "UY"),
                "%02d/%02d/%04d",
                nowCal.get(java.util.Calendar.DAY_OF_MONTH),
                nowCal.get(java.util.Calendar.MONTH) + 1,
                nowCal.get(java.util.Calendar.YEAR)
            )
        )
    }
    var selectedTimeFormatted by remember {
        mutableStateOf(
            String.format(
                java.util.Locale("es", "UY"),
                "%02d:%02d",
                nowCal.get(java.util.Calendar.HOUR_OF_DAY),
                nowCal.get(java.util.Calendar.MINUTE)
            )
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, tint = AzulMarca, modifier = Modifier.size(22.dp))
                Text(
                    text = "Registrar Marca Manual",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontSize = 16.5.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Carga administrativa sin captura GPS. No dibuja pin en el mapa ni genera alerta de geocerca.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Selector de Funcionaria
                Text(
                    text = "Personal de Limpieza",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = AzulMarca
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { if (staffOptions.isNotEmpty()) staffMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedStaff?.fullName ?: "Sin funcionarias registradas",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.5.sp,
                            color = if (selectedStaff != null) AzulMarca else GrisTextoSecundario,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(
                        expanded = staffMenuExpanded,
                        onDismissRequest = { staffMenuExpanded = false }
                    ) {
                        staffOptions.forEach { staff ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = staff.fullName,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.5.sp,
                                        color = AzulMarca
                                    )
                                },
                                onClick = {
                                    selectedStaff = staff
                                    staffMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Selector de Edificio / Lugar
                Text(
                    text = "Edificio / Lugar de Trabajo",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = AzulMarca
                )
                if (buildingOptions.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { buildingMenuExpanded = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedBuilding?.name ?: "Seleccionar edificio",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.5.sp,
                                color = AzulMarca,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = buildingMenuExpanded,
                            onDismissRequest = { buildingMenuExpanded = false }
                        ) {
                            buildingOptions.forEach { bld ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = bld.name,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.5.sp,
                                            color = AzulMarca
                                        )
                                    },
                                    onClick = {
                                        selectedBuilding = bld
                                        buildingMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customBuildingName,
                        onValueChange = { customBuildingName = it },
                        placeholder = { Text("Ej. Edificio Central", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Tipo de evento: Entrada (E) / Salida (S)
                Text(
                    text = "Tipo de Evento",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    color = AzulMarca
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("E" to "Entrada (E)", "S" to "Salida (S)").forEach { (code, label) ->
                        val isSelected = selectedMarkType == code
                        val chipColor = if (code == "E") VerdeEsmeralda else AzulCorporativo
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.16f) else GrisNeutro,
                            border = BorderStroke(1.2.dp, if (isSelected) chipColor else GrisBorde),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedMarkType = code }
                                .testTag("chip_manual_type_$code")
                        ) {
                            Text(
                                text = label,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) chipColor else GrisTextoSecundario,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 9.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                // Selectores Fecha y Hora (Material 3 Pickers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_manual_date_picker")
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(selectedDateFormatted, fontFamily = PoppinsFontFamily, fontSize = 11.5.sp, color = AzulMarca)
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_manual_time_picker")
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = AzulCorporativo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(selectedTimeFormatted, fontFamily = PoppinsFontFamily, fontSize = 11.5.sp, color = AzulMarca)
                    }
                }

                OutlinedTextField(
                    value = adminComment,
                    onValueChange = { adminComment = it },
                    label = { Text("Comentario administrativo", fontSize = 12.sp) },
                    placeholder = { Text("Ej. Sin señal de datos en subsuelo", fontSize = 11.5.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_manual_mark_comment")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val staff = selectedStaff
                    if (staff == null) {
                        Toast.makeText(context, "Creá primero un perfil de Limpieza en Accesos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val resolvedBuildingName = selectedBuilding?.name ?: customBuildingName.trim()
                    if (resolvedBuildingName.isBlank()) {
                        Toast.makeText(context, "Seleccioná o ingresá un edificio", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSaveManualMark(
                        staff,
                        resolvedBuildingName,
                        selectedBuilding?.address ?: resolvedBuildingName,
                        selectedMarkType,
                        selectedDateFormatted,
                        selectedTimeFormatted,
                        adminComment
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_manual_mark")
            ) {
                Text("Guardar Marca", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = Blanco)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
            }
        },
        containerColor = Blanco,
        shape = RoundedCornerShape(20.dp)
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val calUtc = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = utcMillis
                        }
                        selectedDateFormatted = String.format(
                            java.util.Locale("es", "UY"),
                            "%02d/%02d/%04d",
                            calUtc.get(java.util.Calendar.DAY_OF_MONTH),
                            calUtc.get(java.util.Calendar.MONTH) + 1,
                            calUtc.get(java.util.Calendar.YEAR)
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = nowCal.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = nowCal.get(java.util.Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Seleccionar Hora",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontSize = 16.sp
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTimeFormatted = String.format(
                        java.util.Locale("es", "UY"),
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    showTimePicker = false
                }) {
                    Text("Aceptar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily)
                }
            },
            containerColor = Blanco
        )
    }
}
