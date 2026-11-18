package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.CleaningRepository
import com.example.data.OperationalRepository
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.model.UserAccount
import com.example.model.isForUserRole
import com.example.model.isUnreadBy
import com.example.ui.components.AnnouncementsInboxModal
import com.example.ui.components.BouncingTouchSurface
import com.example.ui.components.HeaderNotificationBell
import com.example.ui.components.PulsingLiveIndicator
import com.example.ui.components.RequestAndroidNotificationPermissionEffect
import com.example.ui.components.UserProfileHeaderCapsule
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
import com.example.ui.theme.InstitutionalHeaderBrush
import com.example.ui.theme.GrisTextoTenue
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.VerdeEsmeralda
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.util.SecurityUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dashboard Operativo para el Personal de Limpieza (Experiencia Lenox HR).
 * - Marcaje de entrada/salida con captura GPS y validación perimetral de geocerca.
 * - Cronómetro vivo en turno activo.
 * - Widget de clima, fecha y hora en vivo.
 * - Historial y métricas de jornada.
 * - Botón de emergencia SOS con supervisor directo.
 */
@Composable
fun CleaningDashboardScreen(
    currentUser: UserAccount,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val staffList by CleaningRepository.staffList.collectAsStateWithLifecycle()
    val marksList by CleaningRepository.marksList.collectAsStateWithLifecycle()
    val buildingsList by CleaningRepository.buildingsList.collectAsStateWithLifecycle()
    val activeShift by CleaningRepository.activeShift.collectAsStateWithLifecycle()
    val allAnnouncements by OperationalRepository.announcements.collectAsStateWithLifecycle()

    RequestAndroidNotificationPermissionEffect()

    val cleaningAnnouncements = remember(allAnnouncements, currentUser.role) {
        allAnnouncements.filter { it.isForUserRole(currentUser.role) }
    }
    val unreadAnnouncementsCount = remember(cleaningAnnouncements, currentUser.uid) {
        cleaningAnnouncements.count { it.isUnreadBy(currentUser.uid) }
    }

    // Datos del legajo de la colaboradora actual (fallback seguro al usuario logueado)
    val staffMember = remember(staffList, currentUser) {
        staffList.find {
            it.username.equals(currentUser.username, ignoreCase = true) ||
                it.id == currentUser.uid
        } ?: CleaningStaffMember(
            id = currentUser.uid,
            fullName = currentUser.fullName,
            ci = "",
            birthDate = "",
            address = "",
            phone = currentUser.phone,
            assignedBuildings = currentUser.headquarters,
            username = currentUser.username
        )
    }

    // Edificio seleccionado para marcaje (null-safe si aún no hay geocercas cargadas)
    var selectedBuildingId by remember(buildingsList) {
        mutableStateOf(buildingsList.firstOrNull()?.id)
    }
    val selectedBuilding = remember(buildingsList, selectedBuildingId) {
        buildingsList.firstOrNull { it.id == selectedBuildingId } ?: buildingsList.firstOrNull()
    }
    var isBuildingDropdownOpen by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val locationState = com.example.util.rememberUruguayLocationState(autoRequestOnLaunch = true)
    var isCapturingGpsMark by remember { mutableStateOf(false) }
    var markCommentInput by remember { mutableStateOf("") }
    var showCleaningInboxModal by remember { mutableStateOf(false) }

    // Estado del diálogo SOS / Reportar Incidencia
    var showSosDialog by remember { mutableStateOf(false) }
    var sosIncidentType by remember { mutableStateOf("Falta de Insumos") }
    var sosNotes by remember { mutableStateOf("") }

    // Reloj digital en vivo
    var currentClockTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale("es", "UY")).format(Date()))
    }
    var currentDateFormatted by remember {
        mutableStateOf(SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "UY")).format(Date()).replaceFirstChar { it.uppercase() })
    }

    // Cronómetro en vivo de la jornada activa (segundos transcurridos)
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            currentClockTime = SimpleDateFormat("HH:mm:ss", Locale("es", "UY")).format(Date(now))
            if (activeShift.isShiftActive && activeShift.checkInTimestamp > 0L) {
                elapsedSeconds = (now - activeShift.checkInTimestamp) / 1000L
            } else {
                elapsedSeconds = 0L
            }
            delay(1000L)
        }
    }

    val safeTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInsetPadding = (safeTop + 8.dp).coerceAtLeast(32.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GrisPerlaFrio)
            .modalBackdropBlur(showSosDialog || showCleaningInboxModal)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InstitutionalHeaderBrush)
                            .padding(
                                top = topInsetPadding,
                                bottom = 14.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    unreadCount = unreadAnnouncementsCount,
                                    onClick = { showCleaningInboxModal = true }
                                )

                                UserProfileHeaderCapsule(
                                    firstName = staffMember.fullName,
                                    avatarUrl = currentUser.avatarUrl ?: staffMember.profileImageUri
                                )

                                Surface(
                                    shape = CircleShape,
                                    color = Blanco.copy(alpha = 0.14f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onLogout()
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
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

                        Text(
                            text = "Control de Limpieza",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                color = CelesteAcentoClaro,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ================================================================
            // CÁPSULA DIARIA DE SEGURIDAD Y ÁNIMO (TIP OPERATIVO)
            // ================================================================
            item {
                val dailyTips = listOf(
                    "¡Buen día! Recordá: nunca mezclar hipoclorito (lavandina) con amoníaco o detergentes ácidos.",
                    "¡A cuidar la salud! Usá siempre guantes y gafas protectoras al manipular rociadores químicos.",
                    "Que tengas una gran jornada. Recordá mantener colocado tu calzado de seguridad en zonas húmedas."
                )
                var currentTipIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            currentTipIndex = (currentTipIndex + 1) % dailyTips.size
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🧤", fontSize = 18.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Consejo del Día • Tip Operativo",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E),
                                        fontSize = 11.sp
                                    )
                                )
                                Text("✨", fontSize = 11.sp)
                            }
                            Text(
                                text = dailyTips[currentTipIndex],
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = Color(0xFF78350F),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }

            // ================================================================
            // 2. WIDGET DE CLIMA, FECHA Y HORA EN VIVO
            // ================================================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(Blanco, Color(0xFFE2E8F0)))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x0F102C54),
                            spotColor = Color(0x18102C54)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = currentClockTime,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 24.sp
                                    )
                                )
                            }

                            Text(
                                text = currentDateFormatted,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrisTextoSecundario,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        // Widget Clima
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WbSunny,
                                    contentDescription = "Clima",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "21°C • Soleado",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulMarca,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Text(
                                        text = "Montevideo / Canelones",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 3. PANEL CENTRAL DE MARCAJE (CHECK-IN / CHECK-OUT GEORREFERENCIADO)
            // ================================================================
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (activeShift.isShiftActive) AmbarCalido.copy(alpha = 0.4f) else VerdeEsmeralda.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(22.dp),
                            ambientColor = Color(0x0F102C54),
                            spotColor = Color(0x20102C54)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header del Panel de Marcaje
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Registro de Asistencia GPS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 15.sp
                                )
                            )

                            if (activeShift.isShiftActive) {
                                Row(
                                    modifier = Modifier
                                        .background(VerdeEsmeralda.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .border(1.dp, VerdeEsmeralda.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    PulsingLiveIndicator(dotSize = 7.dp, pulseColor = VerdeEsmeralda)
                                    Text(
                                        text = "Jornada Activa",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = VerdeEsmeralda,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GrisNeutro
                                ) {
                                    Text(
                                        text = "Sin turno activo",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario,
                                            fontSize = 10.5.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Selector del Edificio/Sede asignado para el día
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Edificio o Sede de Servicio:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 11.5.sp
                                )
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GrisNeutro,
                                    border = BorderStroke(1.dp, GrisBorde),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = !activeShift.isShiftActive && buildingsList.isNotEmpty()) {
                                            isBuildingDropdownOpen = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Apartment,
                                                contentDescription = null,
                                                tint = CelesteAcento,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = when {
                                                        activeShift.isShiftActive -> activeShift.buildingName
                                                        selectedBuilding != null -> selectedBuilding.name
                                                        else -> "Sin edificios asignados"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AzulMarca,
                                                        fontSize = 13.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = when {
                                                        activeShift.isShiftActive -> activeShift.buildingAddress
                                                        selectedBuilding != null -> selectedBuilding.address
                                                        else -> "El administrador debe cargar una geocerca"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = PoppinsFontFamily,
                                                        color = GrisTextoSecundario,
                                                        fontSize = 10.5.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        if (!activeShift.isShiftActive && buildingsList.isNotEmpty()) {
                                            Text(
                                                text = "Cambiar",
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

                                DropdownMenu(
                                    expanded = isBuildingDropdownOpen,
                                    onDismissRequest = { isBuildingDropdownOpen = false }
                                ) {
                                    buildingsList.forEach { building ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = building.name,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = building.address,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontSize = 11.sp,
                                                        color = GrisTextoSecundario
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedBuildingId = building.id
                                                isBuildingDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Mapa HD con geocerca real del edificio asignado y ubicación GPS del teléfono
                        val displayedBuilding = remember(activeShift.isShiftActive, activeShift.buildingName, selectedBuilding, buildingsList) {
                            if (activeShift.isShiftActive) {
                                buildingsList.firstOrNull { it.name == activeShift.buildingName } ?: selectedBuilding
                            } else {
                                selectedBuilding
                            }
                        }
                        val liveStaffLat = if (activeShift.isShiftActive) {
                            activeShift.currentLat
                        } else {
                            locationState.userLat
                        }
                        val liveStaffLng = if (activeShift.isShiftActive) {
                            activeShift.currentLng
                        } else {
                            locationState.userLng
                        }
                        val liveDistanceToBuilding = remember(displayedBuilding, liveStaffLat, liveStaffLng) {
                            if (displayedBuilding != null && liveStaffLat != null && liveStaffLng != null) {
                                com.example.util.UruguayLocationProvider.haversineDistanceMeters(
                                    liveStaffLat,
                                    liveStaffLng,
                                    displayedBuilding.lat,
                                    displayedBuilding.lng
                                )
                            } else {
                                null
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, GrisBorde, RoundedCornerShape(14.dp))
                        ) {
                            com.example.ui.components.RealStreetInteractiveMap(
                                centerLat = displayedBuilding?.lat ?: liveStaffLat ?: com.example.util.URUGUAY_DEFAULT_LAT,
                                centerLng = displayedBuilding?.lng ?: liveStaffLng ?: com.example.util.URUGUAY_DEFAULT_LNG,
                                zoomLevel = if (displayedBuilding != null) 16 else 13,
                                geofences = displayedBuilding?.let { b ->
                                    listOf(
                                        com.example.ui.components.MapGeocenceItem(
                                            id = b.id,
                                            name = b.name,
                                            address = b.address,
                                            lat = b.lat,
                                            lng = b.lng,
                                            radiusMeters = b.geofenceRadiusMeters
                                        )
                                    )
                                } ?: emptyList(),
                                markers = if (activeShift.isShiftActive) {
                                    listOf(
                                        com.example.ui.components.MapMarkerItem(
                                            id = "cleaning-staff-pos",
                                            title = currentUser.fullName,
                                            subtitle = activeShift.buildingName,
                                            lat = activeShift.currentLat,
                                            lng = activeShift.currentLng,
                                            type = "E",
                                            isAlert = !activeShift.isInsideGeofence,
                                            badgeText = "E",
                                            timeText = activeShift.checkInTimeFormatted
                                        )
                                    )
                                } else {
                                    emptyList()
                                },
                                centerOnUserLocationIfNoTarget = (displayedBuilding == null),
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Validación de Geocerca GPS en pantalla en tiempo real
                        val isLiveInside = if (displayedBuilding != null && liveDistanceToBuilding != null) {
                            liveDistanceToBuilding <= displayedBuilding.geofenceRadiusMeters
                        } else {
                            true
                        }
                        val geofenceBg = when {
                            !locationState.hasPermission -> NaranjaAlertaFondo
                            displayedBuilding == null && !activeShift.isShiftActive -> GrisNeutro
                            activeShift.isShiftActive && !activeShift.isInsideGeofence -> NaranjaAlertaFondo
                            !activeShift.isShiftActive && !isLiveInside -> NaranjaAlertaFondo
                            else -> VerdeExitoFondo
                        }
                        val geofenceTint = when {
                            !locationState.hasPermission -> NaranjaAlerta
                            displayedBuilding == null && !activeShift.isShiftActive -> GrisTextoSecundario
                            activeShift.isShiftActive && !activeShift.isInsideGeofence -> NaranjaAlerta
                            !activeShift.isShiftActive && !isLiveInside -> NaranjaAlerta
                            else -> VerdeExito
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(geofenceBg, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (!locationState.hasPermission) {
                                        locationState.requestMyLocation()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PinDrop,
                                contentDescription = null,
                                tint = geofenceTint,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    !locationState.hasPermission -> "Tocá aquí para habilitar el GPS del dispositivo"
                                    displayedBuilding == null && !activeShift.isShiftActive -> "Sin geocerca asignada"
                                    activeShift.isShiftActive && !activeShift.isInsideGeofence ->
                                        "⚠️ Fuera de rango: a ${activeShift.distanceMeters} metros"
                                    activeShift.isShiftActive ->
                                        "GPS Verificado: en sitio (${activeShift.distanceMeters} m)"
                                    liveDistanceToBuilding != null && !isLiveInside ->
                                        "⚠️ Fuera de rango: a $liveDistanceToBuilding metros del edificio"
                                    liveDistanceToBuilding != null ->
                                        "En rango GPS ($liveDistanceToBuilding m / ${displayedBuilding?.geofenceRadiusMeters} m)"
                                    else -> "Listo para marcar en ${displayedBuilding?.name}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = geofenceTint,
                                    fontSize = 11.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Cronómetro vivo durante la jornada activa
                        if (activeShift.isShiftActive) {
                            val hours = elapsedSeconds / 3600
                            val minutes = (elapsedSeconds % 3600) / 60
                            val seconds = elapsedSeconds % 60
                            val timerFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Tiempo transcurrido en el turno:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = timerFormatted,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 32.sp,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                                Text(
                                    text = "Entrada registrada a las ${activeShift.checkInTimeFormatted}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = GrisTextoTenue,
                                        fontSize = 10.5.sp
                                    )
                                )
                            }
                        }

                        // Campo opcional de comentario / novedad antes del Check-in o Check-out
                        OutlinedTextField(
                            value = markCommentInput,
                            onValueChange = { markCommentInput = it },
                            label = {
                                Text(
                                    text = "Agregar comentario o motivo (opcional)",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.5.sp
                                )
                            },
                            placeholder = {
                                Text(
                                    text = if (activeShift.isShiftActive) {
                                        "Ej. Se dejó llave en portería, reposición pendiente..."
                                    } else {
                                        "Ej. Llegada por cambio de parada, ingreso por portón..."
                                    },
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.5.sp,
                                    color = GrisTextoTenue
                                )
                            },
                            maxLines = 2,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GrisNeutro,
                                unfocusedContainerColor = GrisNeutro,
                                focusedBorderColor = CelesteAcento,
                                unfocusedBorderColor = GrisBorde
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cleaning_mark_comment_input")
                        )

                        // BOTÓN CENTRAL DE MARCAJE CON CAPTURA GPS REAL DEL TELÉFONO
                        BouncingTouchSurface(
                            onClick = {
                                if (isCapturingGpsMark) return@BouncingTouchSurface
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                if (!com.example.util.UruguayLocationProvider.hasLocationPermission(context)) {
                                    locationState.requestMyLocation()
                                    Toast.makeText(
                                        context,
                                        "Autorizá el permiso de ubicación GPS para marcar asistencia",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@BouncingTouchSurface
                                }

                                if (SecurityUtils.isMockLocationActive(context)) {
                                    Toast.makeText(
                                        context,
                                        "⚠️ ALERTA DE SEGURIDAD: Se detectó ubicación simulada (Mock GPS). Marcación bloqueada.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@BouncingTouchSurface
                                }

                                if (!activeShift.isShiftActive && selectedBuilding == null) {
                                    Toast.makeText(
                                        context,
                                        "No hay edificio asignado. Solicite alta al Administrador.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@BouncingTouchSurface
                                }

                                scope.launch {
                                    isCapturingGpsMark = true
                                    val gpsFix = com.example.util.UruguayLocationProvider.fetchCurrentGpsLocation(context)
                                    isCapturingGpsMark = false

                                    if (!gpsFix.hasPermission) {
                                        locationState.requestMyLocation()
                                        Toast.makeText(context, "Se requiere GPS activo para marcar.", Toast.LENGTH_LONG).show()
                                        return@launch
                                    }
                                    if (gpsFix.isMocked) {
                                        Toast.makeText(
                                            context,
                                            "⚠️ Ubicación simulada detectada. Desactiva Mock GPS.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@launch
                                    }

                                    if (!activeShift.isShiftActive) {
                                        val targetBuilding = selectedBuilding ?: return@launch
                                        val mark = CleaningRepository.performCheckIn(
                                            staffMember = staffMember,
                                            building = targetBuilding,
                                            actualLat = gpsFix.lat,
                                            actualLng = gpsFix.lng,
                                            comment = markCommentInput
                                        )
                                        markCommentInput = ""
                                        val statusLabel = if (mark.isInsideGeofence) {
                                            "Entrada registrada (${mark.distanceToBuildingMeters} m del edificio)"
                                        } else {
                                            "⚠️ Entrada fuera de rango: a ${mark.distanceToBuildingMeters} m de ${mark.buildingName}"
                                        }
                                        Toast.makeText(context, statusLabel, Toast.LENGTH_LONG).show()
                                    } else {
                                        val mark = CleaningRepository.performCheckOut(
                                            actualLat = gpsFix.lat,
                                            actualLng = gpsFix.lng,
                                            assignedBuilding = displayedBuilding,
                                            comment = markCommentInput
                                        )
                                        markCommentInput = ""
                                        val outMsg = if (mark != null && !mark.isInsideGeofence) {
                                            "⚠️ Salida registrada fuera de rango: a ${mark.distanceToBuildingMeters} m"
                                        } else {
                                            "Salida registrada con éxito. Turno completado."
                                        }
                                        Toast.makeText(context, outMsg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (activeShift.isShiftActive) Color(0xFFDC2626) else VerdeEsmeralda,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .testTag("btn_toggle_attendance_mark")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isCapturingGpsMark) {
                                        CircularProgressIndicator(
                                            color = Blanco,
                                            strokeWidth = 2.5.dp,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Verificando GPS real...",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Blanco,
                                                fontSize = 15.sp
                                            )
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (activeShift.isShiftActive) Icons.Rounded.ExitToApp else Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = Blanco,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = if (activeShift.isShiftActive) "Marcar Salida (Check-out)" else "Marcar Entrada (Check-in)",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Blanco,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 3.5. BOTÓN DIRECTO: SOLICITAR MATERIALES DE LIMPIEZA (WHATSAPP CAMILO)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    BouncingTouchSurface(
                        onClick = {
                            val buildingText = selectedBuilding?.name ?: "el servicio de limpieza"
                            val staffName = staffMember.name
                            val message = "Hola Camilo, buenas tardes. Te escribo desde el servicio de limpieza en $buildingText. Me estoy quedando con poco material de limpieza y quería pedirte si es posible coordinar la reposición de insumos cuando puedas. Muchas gracias. Saludos, $staffName."
                            val encodedMsg = Uri.encode(message)
                            val whatsappUrl = "https://wa.me/59895800458?text=$encodedMsg"

                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                                context.startActivity(intent)
                                Toast.makeText(context, "Abriendo WhatsApp con Camilo...", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Contactar a Camilo: +598 95 800 458", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Blanco,
                            border = BorderStroke(1.2.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_solicitar_materiales_limpieza")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFDCFCE7),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "🧴",
                                                fontSize = 22.sp
                                            )
                                        }
                                    }
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Solicitar Materiales de Limpieza",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca,
                                                fontSize = 14.5.sp
                                            )
                                        )
                                        Text(
                                            text = "Coordina reposición directa por WhatsApp con Camilo",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 11.5.sp
                                            )
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF25D366),
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Call,
                                            contentDescription = null,
                                            tint = Blanco,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "WhatsApp",
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

            // ================================================================
            // 4. RESUMEN Y MÉTRICAS DE ASISTENCIA (HORAS Y HISTORIAL)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Resumen de Horas y Asistencia",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 15.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Horas hoy
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Horas Hoy",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = if (activeShift.isShiftActive) {
                                        val hrs = elapsedSeconds / 3600.0
                                        String.format("%.1f hrs", hrs)
                                    } else {
                                        val completedMins = marksList
                                            .filter { it.staffId == staffMember.id || it.staffName.equals(staffMember.fullName, ignoreCase = true) }
                                            .sumOf { it.durationMinutes ?: 0 }
                                        String.format("%.1f hrs", completedMins / 60.0)
                                    },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = CelesteAcento,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }

                        // Total acumulado mes
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier
                                .weight(1f)
                                .shadow(4.dp, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Acumulado Mes",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = "${staffMember.totalHoursThisMonth} hrs",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 20.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Historial de Marcas Recientes
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
                            text = "Historial Reciente de Marcas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 14.5.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            tint = GrisTextoSecundario,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val myMarks = remember(marksList, staffMember) {
                        marksList.filter {
                            it.staffId == staffMember.id ||
                                it.staffName.equals(staffMember.fullName, ignoreCase = true)
                        }
                    }

                    if (myMarks.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            border = BorderStroke(1.dp, GrisBorde.copy(alpha = 0.8f)),
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
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = GrisTextoSecundario.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Sin marcaciones registradas aún",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.5.sp,
                                    color = GrisTextoSecundario,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        myMarks.take(4).forEach { mark ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Blanco),
                                border = BorderStroke(1.dp, GrisBorde.copy(alpha = 0.8f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (mark.checkOutTime != null) CelesteAcentoClaro else VerdeEsmeralda.copy(alpha = 0.15f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (mark.checkOutTime != null) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = if (mark.checkOutTime != null) CelesteAcento else VerdeEsmeralda,
                                            modifier = Modifier
                                                .padding(11.dp)
                                                .size(24.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mark.buildingName,
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
                                            text = "Entrada: ${mark.checkInTime.ifBlank { mark.markTime }} ${if (mark.checkOutTime != null) "| Salida: ${mark.checkOutTime}" else "| En turno activo"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 11.sp
                                            )
                                        )
                                        if (mark.comment.isNotBlank() && !mark.comment.equals("Sin novedad", ignoreCase = true)) {
                                            Text(
                                                text = "💬 ${mark.comment}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    color = AzulCorporativo,
                                                    fontSize = 10.5.sp
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            mark.isManualEntry -> Color(0xFFF1F5F9)
                                            mark.isInsideGeofence -> VerdeExitoFondo
                                            else -> NaranjaAlertaFondo
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                mark.isManualEntry -> "Carga Manual"
                                                mark.isInsideGeofence -> "En rango"
                                                else -> "Desviado"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    mark.isManualEntry -> Color(0xFF475569)
                                                    mark.isInsideGeofence -> VerdeExito
                                                    else -> NaranjaAlerta
                                                },
                                                fontSize = 9.5.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 5. BOTÓN DIRECTO DE EMERGENCIA / REPORTE SOS
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    BouncingTouchSurface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSosDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = RojoUrgenciaFondo,
                            border = BorderStroke(1.2.dp, RojoUrgencia.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_sos_report_emergency")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = RojoUrgencia,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🚨 Botón SOS / Reportar Incidencia",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = RojoUrgencia,
                                        fontSize = 14.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(36.dp)) }
        }
    }

    // Modal SOS / Reportar Incidencia al Supervisor
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = RojoUrgencia,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Reporte SOS / Incidencia",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Comuníquese de inmediato con el supervisor de guardia de Todoservicio o elabore un reporte de urgencia:",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.5.sp,
                        color = GrisTextoSecundario
                    )

                    // Botón para llamada telefónica directa
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+59899000111"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RojoUrgencia),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Rounded.Call, contentDescription = null, tint = Blanco)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Llamar a Supervisor (+598 99 000 111)",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "O registre la incidencia por escrito:",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AzulMarca
                    )

                    OutlinedTextField(
                        value = sosNotes,
                        onValueChange = { sosNotes = it },
                        placeholder = { Text("Ej. Faltan guantes/bolsas, corte de agua en edificio...", fontSize = 11.5.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RojoUrgencia,
                            unfocusedBorderColor = GrisBorde
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Incidencia transmitida al supervisor de turno", Toast.LENGTH_LONG).show()
                        showSosDialog = false
                        sosNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enviar Reporte", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                }
            },
            containerColor = Blanco,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showCleaningInboxModal) {
        AnnouncementsInboxModal(
            currentUser = currentUser,
            announcements = cleaningAnnouncements,
            onDismiss = { showCleaningInboxModal = false }
        )
    }
}
