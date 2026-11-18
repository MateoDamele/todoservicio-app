package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Hvac
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Task
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.OperationalRepository
import com.example.model.ClientProfile
import com.example.model.OperationalTask
import com.example.model.TaskOperationStatus
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.BouncingTouchSurface
import com.example.ui.components.LiveStatusBadge
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisPerlaFrio
import com.example.ui.theme.GrisTextoDespejado
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.GrisTextoTenue
import com.example.ui.theme.ModalBlurScrim
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SombraDifusaColor
import com.example.ui.theme.VerdeExito

/**
 * Modelo de datos para las tareas programadas en el Calendario Operativo.
 */
data class ScheduledCalendarTask(
    val id: String,
    val code: String = "",
    val clientId: String = "",
    val clientName: String,
    val sedeId: String = "sede-01",
    val sedeName: String = "",
    val sede: String = "",
    val assignedTechIds: List<String> = emptyList(),
    val dayNumber: Int,
    val dayOfWeekLabel: String = "",
    val date: String = "",
    val timeSlot: String,
    val time: String = "",
    val title: String,
    val technicianName: String,
    val category: String,
    val comments: String = "",
    val status: String = "Programada",
    val clientColor: Color,
    val isCompleted: Boolean = false,
    val isRescheduled: Boolean = false,
    val rescheduleReason: String = ""
)

/**
 * Pantalla de Operaciones y Calendario Operativo (Referencia Visual Exacta image_5.png).
 * 1. Cabecera superior desahogada (SafeArea top: 32dp) con logo "T", TODOSERVICIO y botón redondeado "+ Crear".
 * 2. Fila de pastillas horizontales deslizables [Calendario Operativo], [Mapa GPS], [Tareas]...
 * 3. Calendario mensual tradicional compacto con bordes 20dp, sombra suave 3D, píldoras con nombre/color del cliente o "Libre".
 * 4. Buscador con bordes 20dp y pastillas de filtro [Todos], [Electricidad], [Sanitaria], [Climatización].
 * 5. Cuadrícula Operativa Semanal con tarjetas "24 Jue" (Libre) y "25 Vie" (Prueba de Carga UTE / WTC Free Zone).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyGridCalendarScreen(
    clientProfile: ClientProfile,
    onNavigateToGps: (() -> Unit)? = null,
    onOpenProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // SafeArea top inset padding con respiro holgado (32dp)
    val topInsetPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 32.dp

    // Pestaña de operaciones seleccionada (0: Calendario Operativo, 1: Mapa GPS, 2: Tareas, 3: Cuadrillas)
    var selectedOperationTab by remember { mutableIntStateOf(0) }

    val localeEs = remember { java.util.Locale("es", "UY") }
    val todayCal = remember { java.util.Calendar.getInstance() }
    var monthOffset by remember { mutableIntStateOf(0) }
    val displayCal = remember(monthOffset) {
        (todayCal.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            add(java.util.Calendar.MONTH, monthOffset)
        }
    }
    val monthTitle = remember(displayCal) {
        val raw = java.text.SimpleDateFormat("MMMM yyyy", localeEs).format(displayCal.time)
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }
    val monthNameOnly = remember(displayCal) {
        val raw = java.text.SimpleDateFormat("MMMM", localeEs).format(displayCal.time)
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
    }
    val monthShortLabel = remember(displayCal) {
        val raw = java.text.SimpleDateFormat("MMM", localeEs).format(displayCal.time).replace(".", "")
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }.take(3)
    }
    val daysInMonth = displayCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val startDayOffset = (displayCal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    val yearNumber = displayCal.get(java.util.Calendar.YEAR)

    // Día seleccionado del mes
    var selectedDay by remember { mutableIntStateOf(todayCal.get(java.util.Calendar.DAY_OF_MONTH)) }
    var inspectedTask by remember { mutableStateOf<ScheduledCalendarTask?>(null) }

    // Búsqueda y categoría activa
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }

    // Estado del BottomSheet para "+ Crear"
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Tareas operativas centralizadas desde OperationalRepository (sincronizadas con Admin y Técnico)
    val repoTasks by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()

    val tasksList = remember(repoTasks, monthShortLabel) {
        repoTasks.map { op ->
            val resolvedSede = op.sedeName.ifBlank {
                op.address.substringBefore("•").trim().ifBlank { op.address }
            }
            ScheduledCalendarTask(
                id = op.id,
                code = op.code,
                clientId = op.clientId,
                clientName = op.clientName,
                sedeId = op.sedeId,
                sedeName = resolvedSede,
                sede = resolvedSede,
                assignedTechIds = op.assignedTechIds,
                dayNumber = op.dayOfMonth,
                dayOfWeekLabel = "${op.dayOfMonth.toString().padStart(2, '0')} $monthShortLabel",
                date = op.scheduledDate,
                timeSlot = op.timeSlot,
                time = op.timeSlot,
                title = op.title,
                technicianName = op.technicianName,
                category = op.category,
                comments = op.description,
                status = when (op.status) {
                    TaskOperationStatus.ABIERTA -> "Programada"
                    TaskOperationStatus.INICIO_EJECUCION -> "En Ejecución"
                    TaskOperationStatus.FINALIZADA -> "Finalizada"
                },
                clientColor = getCompanyColor(op.clientName),
                isCompleted = op.status == TaskOperationStatus.FINALIZADA,
                isRescheduled = op.rescheduleNotice?.isRescheduled == true,
                rescheduleReason = op.rescheduleNotice?.reason ?: ""
            )
        }
    }

    // ========================================================================
    // SEGURIDAD Y AISLAMIENTO MULTI-TENANT (FILTRADO ESTRICTO POR CLIENTE)
    // ========================================================================
    val isCorporateClient = clientProfile.role.contains("Cliente", ignoreCase = true) ||
            !clientProfile.companyName.contains("Todoservicio", ignoreCase = true)

    val tenantClientId = clientProfile.clientId

    // Filtro inviolable por inquilino/empresa: ningún cliente ve jamás datos de otra empresa
    val tenantTasks = remember(tasksList, isCorporateClient, tenantClientId, clientProfile.companyName) {
        if (isCorporateClient) {
            tasksList.filter { task ->
                task.clientId == tenantClientId ||
                task.clientName.equals(clientProfile.companyName, ignoreCase = true) ||
                (clientProfile.companyName.isNotBlank() && task.clientName.contains(clientProfile.companyName, ignoreCase = true)) ||
                (task.clientName.isNotBlank() && clientProfile.companyName.contains(task.clientName, ignoreCase = true))
            }
        } else {
            tasksList
        }
    }

    // Tareas visibles aplicando filtros de categoría y texto de búsqueda
    val visibleTasks = remember(tenantTasks, searchQuery, selectedCategoryFilter) {
        tenantTasks.filter { task ->
            val matchesCategory = (selectedCategoryFilter == "Todos" ||
                    task.category.contains(selectedCategoryFilter, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    task.code.contains(searchQuery, ignoreCase = true) ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.clientName.contains(searchQuery, ignoreCase = true) ||
                    task.sede.contains(searchQuery, ignoreCase = true) ||
                    task.technicianName.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Tareas del día seleccionado para agenda inmediata
    val selectedDayTasks = remember(visibleTasks, selectedDay) {
        visibleTasks.filter { it.dayNumber == selectedDay }
    }

    // Tareas filtradas por término de búsqueda y tenant
    val filteredTasks = remember(visibleTasks, searchQuery) {
        visibleTasks.filter { task ->
            searchQuery.isBlank() ||
                    task.title.contains(searchQuery, ignoreCase = true) ||
                    task.clientName.contains(searchQuery, ignoreCase = true) ||
                    task.sede.contains(searchQuery, ignoreCase = true) ||
                    task.technicianName.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GrisPerlaFrio)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================================================================
            // 1. CABECERA SUPERIOR DESAHOGADA (SAFE AREA + DEGRADADO AZUL B2B)
            // ================================================================
            item {
                Surface(
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AzulMarca, AzulCorporativo)
                                )
                            )
                            .padding(
                                top = topInsetPadding,
                                bottom = 16.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Logo "T" + Título "TODOSERVICIO"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .shadow(4.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = Blanco
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.todoservicio_icono_t),
                                        contentDescription = "Todoservicio Logo",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }

                                Text(
                                    text = "TODOSERVICIO",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.1.sp,
                                        color = Blanco,
                                        fontSize = 19.sp
                                    )
                                )
                            }

                            // Botón "+ Crear" redondeado y Botón de Perfil
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Botón "+ Crear"
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AzulCorporativo,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showCreateTaskSheet = true
                                        }
                                        .testTag("btn_header_crear_tarea")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = "Crear nueva orden",
                                            tint = Blanco,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Crear",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Blanco,
                                                fontSize = 13.sp
                                            )
                                        )
                                    }
                                }

                                // Botón de Perfil
                                Surface(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onOpenProfile?.invoke()
                                        },
                                    color = Blanco.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = "Perfil del usuario",
                                            tint = Blanco,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 2. PESTAÑAS DE OPERACIONES (Pastillas horizontales deslizables)
            // ================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val operationTabs = listOf(
                        Pair("Calendario Operativo", Icons.Rounded.CalendarMonth),
                        Pair("Mapa GPS", Icons.Rounded.Map),
                        Pair("Tareas", Icons.Rounded.Task),
                        Pair("Cuadrillas", Icons.Rounded.Engineering)
                    )

                    operationTabs.forEachIndexed { index, (label, icon) ->
                        val isSelected = selectedOperationTab == index
                        OperationPillTab(
                            label = label,
                            icon = icon,
                            isSelected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedOperationTab = index
                                if (index == 1 && onNavigateToGps != null) {
                                    onNavigateToGps()
                                }
                            }
                        )
                    }
                }
            }

            // ================================================================
            // 3. CALENDARIO MENSUAL TRADICIONAL COMPACTO (Tarjeta blanca flotante)
            // ================================================================
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0x12102C54),
                            spotColor = Color(0x18102C54)
                        )
                        .testTag("card_compact_monthly_calendar")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cabecera del Mes (Octubre 2026 + Controles)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = monthTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 17.sp
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CelesteAcentoClaro
                                ) {
                                    Text(
                                        text = "${visibleTasks.size} Tareas",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulCorporativo,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        monthOffset--
                                        selectedDay = selectedDay.coerceAtMost(daysInMonth)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                        contentDescription = "Mes anterior",
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        monthOffset++
                                        selectedDay = selectedDay.coerceAtMost(daysInMonth)
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = "Mes siguiente",
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Encabezados de días de la semana (Lunes a Domingo)
                        val weekDayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            weekDayNames.forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Grilla dinámica del Mes
                        val totalCells = daysInMonth + startDayOffset
                        val totalRows = (totalCells + 6) / 7

                        for (r in 0 until totalRows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (c in 0 until 7) {
                                    val cellIndex = r * 7 + c
                                    val dayNum = cellIndex - startDayOffset + 1

                                    if (dayNum in 1..daysInMonth) {
                                        val dayTask = visibleTasks.firstOrNull { it.dayNumber == dayNum }
                                        val isSelected = selectedDay == dayNum

                                        CalendarGridDayCell(
                                            dayNumber = dayNum,
                                            task = dayTask,
                                            isSelected = isSelected,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                selectedDay = dayNum
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 4. BUSCADOR Y FILTROS POR CATEGORÍA
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Campo de búsqueda con bordes 20dp y sombra 3D
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Blanco,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0x10102C54),
                                spotColor = Color(0x15102C54)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(22.dp)
                            )
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Buscar por técnico, cliente o código...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoTenue,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = AzulMarca,
                                    fontSize = 13.5.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_calendar_tasks")
                            )
                        }
                    }

                    // Fila de pastillas de filtro: [Todos], [Electricidad], [Sanitaria], [Climatización]
                    val categories = listOf("Todos", "Electricidad", "Sanitaria", "Climatización")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEach { category ->
                            val isCategoryActive = selectedCategoryFilter == category
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isCategoryActive) CelesteAcento else Blanco,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isCategoryActive) CelesteAcento else GrisBorde
                                ),
                                shadowElevation = if (isCategoryActive) 4.dp else 1.dp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedCategoryFilter = category
                                    }
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isCategoryActive) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isCategoryActive) Blanco else AzulCorporativo,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ================================================================
            // 5. CUADRÍCULA OPERATIVA SEMANAL (TARJETAS DE TRABAJO EXACTAS)
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Cuadrícula Operativa Semanal",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Toque para editar o reorganizar día",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            // Cuadrícula dinámica para el día seleccionado (con aislamiento multi-tenant estricto)
            if (selectedDayTasks.isNotEmpty()) {
                items(selectedDayTasks, key = { it.id }) { task ->
                    ScheduledTaskCardItem(
                        task = task,
                        onClick = { inspectedTask = task },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // Tarjeta minimalista de Jornada Despejada para el día seleccionado
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0x0F102C54),
                                spotColor = Color(0x15102C54)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GrisNeutro,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$selectedDay",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = GrisTextoSecundario,
                                            fontSize = 17.sp
                                        )
                                    )
                                    Text(
                                        text = monthShortLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GrisTextoTenue,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Jornada despejada",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulMarca,
                                        fontSize = 13.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Sin intervenciones para el $selectedDay de $monthNameOnly.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (!isCorporateClient) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CelesteAcentoClaro,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            showCreateTaskSheet = true
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Asignar tarea en día $selectedDay",
                                        tint = CelesteAcento,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lista dinámica de tareas que coinciden con el filtro/búsqueda (aisladas estrictamente por tenant)
            if (filteredTasks.isNotEmpty()) {
                item {
                    Text(
                        text = if (isCorporateClient) "Intervenciones de ${clientProfile.companyName} (${filteredTasks.size})"
                               else "Órdenes de Trabajo Filtradas (${filteredTasks.size})",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                    )
                }

                items(filteredTasks, key = { it.id }) { task ->
                    ScheduledTaskCardItem(
                        task = task,
                        onClick = { inspectedTask = task },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else if (visibleTasks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde.copy(alpha = 0.7f)),
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
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = GrisTextoDespejado,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Jornada despejada sin intervenciones",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "Sin registros aún. Toca + Crear para comenzar.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(108.dp)) }
        }
    }

    inspectedTask?.let { task ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { inspectedTask = null },
            title = {
                Text(
                    text = "${task.code.ifBlank { "OT" }} • ${task.status}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(task.title, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.SemiBold, color = AzulMarca)
                    Text("Cliente: ${task.clientName}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    Text("Sede: ${task.sede.ifBlank { "Principal" }}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    Text("Técnico: ${task.technicianName.ifBlank { "Por asignar" }}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = AzulCorporativo)
                    Text("Horario: ${task.timeSlot}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    if (task.comments.isNotBlank()) {
                        Text(task.comments, fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = GrisTextoSecundario)
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { inspectedTask = null }) {
                    Text("Cerrar", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ========================================================================
    // MODAL DINÁMICO DE CREACIÓN "+ CREAR" (BOTTOM SHEET SIN ESCRITURA MANUAL)
    // ========================================================================
    if (showCreateTaskSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCreateTaskSheet = false },
            sheetState = sheetState,
            containerColor = Blanco,
            scrimColor = ModalBlurScrim,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            CreateScheduledTaskBottomSheet(
                initialDay = selectedDay,
                monthTitle = monthTitle,
                monthNameOnly = monthNameOnly,
                monthShortLabel = monthShortLabel,
                yearNumber = yearNumber,
                onDismiss = { showCreateTaskSheet = false },
                onTaskCreated = { newTask ->
                    val saved = OperationalRepository.addTask(
                        OperationalTask(
                            id = newTask.id,
                            code = "",
                            title = newTask.title,
                            clientId = newTask.clientId,
                            clientName = newTask.clientName,
                            sedeId = newTask.sedeId,
                            sedeName = newTask.sedeName,
                            address = "${newTask.sedeName} • Montevideo",
                            technicianName = newTask.technicianName,
                            assignedTechIds = newTask.assignedTechIds,
                            scheduledDate = newTask.date,
                            dayOfMonth = newTask.dayNumber,
                            timeSlot = newTask.timeSlot,
                            status = TaskOperationStatus.ABIERTA,
                            category = newTask.category,
                            description = newTask.comments
                        )
                    )
                    selectedDay = saved.dayOfMonth
                    showCreateTaskSheet = false
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "${saved.code} agendada (${saved.dayOfMonth} $monthShortLabel).", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

/**
 * Pastilla deslizable de Operaciones [Calendario Operativo], [Mapa GPS], etc.
 */
@Composable
private fun OperationPillTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) CelesteAcento else Blanco,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) CelesteAcento else GrisBorde
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Blanco else AzulCorporativo,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Blanco else AzulMarca,
                    fontSize = 12.5.sp
                )
            )
        }
    }
}

/**
 * Celda individual de día en el Calendario Mensual.
 * Los días con servicio asignado muestran píldora compacta o punto de color del cliente.
 * Los días libres dicen "Libre" con tipografía limpia.
 */
@Composable
private fun CalendarGridDayCell(
    dayNumber: Int,
    task: ScheduledCalendarTask?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasTask = task != null

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> CelesteAcentoClaro
                    hasTask -> Blanco
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.8.dp,
                color = when {
                    isSelected -> CelesteAcento
                    hasTask -> task.clientColor.copy(alpha = 0.5f)
                    else -> GrisBorde.copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp, vertical = 4.dp)
        ) {
            // Número del Día
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = if (isSelected || hasTask) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AzulMarca else if (hasTask) AzulMarca else GrisTextoSecundario,
                    fontSize = 12.sp
                )
            )

            // Píldora de Cliente o etiqueta "Libre"
            if (hasTask) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = task.clientColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.clientName.take(6),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 8.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            } else {
                Text(
                    text = "Libre",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoTenue,
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

/**
 * Tarjeta individual de tarea programada con soporte visual, micro-interacción táctil 3D y badge de estado vivo.
 */
@Composable
private fun ScheduledTaskCardItem(
    task: ScheduledCalendarTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BouncingTouchSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.linearGradient(listOf(Blanco, Color(0xFFE2E8F0)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color(0x0F102C54),
                    spotColor = Color(0x18102C54)
                )
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
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
                        if (task.code.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CelesteAcentoClaro
                            ) {
                                Text(
                                    text = task.code,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = task.clientColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = task.clientName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = task.clientColor,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    LiveStatusBadge(
                        status = if (task.isCompleted) "Finalizada" else task.status
                    )
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GrisGrafito,
                        fontSize = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.isRescheduled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NaranjaAlertaFondo
                    ) {
                        Text(
                            text = "Reprogramada: ${task.rescheduleReason.ifBlank { task.date }}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = NaranjaAlerta,
                                fontSize = 10.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(13.5.dp)
                        )
                        Text(
                            text = task.timeSlot,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = GrisTextoSecundario,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = task.sede.ifBlank { "Sede Central" },
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
        }
    }
}

/**
 * BottomSheet Modal de Creación Dinámica de Tareas (Sin escritura manual).
 * Desplegables de clientes, sedes, técnicos y chips de un solo toque para servicios y horarios.
 */
@Composable
private fun CreateScheduledTaskBottomSheet(
    initialDay: Int,
    monthTitle: String,
    monthNameOnly: String,
    monthShortLabel: String,
    yearNumber: Int,
    onDismiss: () -> Unit,
    onTaskCreated: (ScheduledCalendarTask) -> Unit
) {
    val context = LocalContext.current
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    val staffAccounts by com.example.data.AuthRepository.staffCollaboratorsFlow.collectAsStateWithLifecycle()

    val techAccounts = remember(staffAccounts) {
        staffAccounts.filter { it.roleCategory.equals("Técnico", ignoreCase = true) && it.isActive }
    }
    val timeSlots = listOf("Mañana (08:00 - 12:00)", "Tarde (13:00 - 17:00)", "Jornada Completa", "Horario Puntual")
    val servicesChips = listOf(
        Pair("Mantenimiento Preventivo", "Climatización"),
        Pair("Prueba de Carga Eléctrica", "Electricidad"),
        Pair("Bombas y Desobstrucción", "Sanitaria"),
        Pair("Revisión de Tablero", "Electricidad"),
        Pair("Limpieza Filtros HVAC", "Climatización")
    )

    var selectedClientObj by remember(corporateClients) { mutableStateOf(corporateClients.firstOrNull()) }
    var selectedBranchObj by remember(selectedClientObj) { mutableStateOf(selectedClientObj?.branches?.firstOrNull()) }
    var selectedTechObj by remember(techAccounts) { mutableStateOf(techAccounts.firstOrNull()) }
    var selectedTimeSlot by remember { mutableStateOf(timeSlots[0]) }
    var selectedServiceIndex by remember { mutableIntStateOf(0) }
    val selectedDayNumber = initialDay

    var isClientDropdownOpen by remember { mutableStateOf(false) }
    var isSedeDropdownOpen by remember { mutableStateOf(false) }
    var isTechDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado del Modal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Agendar Tarea",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "$initialDay de $monthTitle",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cerrar", tint = AzulMarca)
            }
        }

        if (corporateClients.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NaranjaAlertaFondo,
                border = androidx.compose.foundation.BorderStroke(1.dp, NaranjaAlerta.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sin clientes registrados aún. Crea un cliente en Clientes y Sedes antes de programar una OT.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulMarca
                    ),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Selector de Cliente y Sede
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Cliente y Sede",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dropdown Cliente
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrisNeutro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = corporateClients.isNotEmpty()) { isClientDropdownOpen = true }
                    ) {
                        Text(
                            text = selectedClientObj?.name ?: "Sin clientes",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedClientObj != null) AzulMarca else GrisTextoSecundario
                            ),
                            modifier = Modifier.padding(12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded = isClientDropdownOpen,
                        onDismissRequest = { isClientDropdownOpen = false }
                    ) {
                        corporateClients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client.name, fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedClientObj = client
                                    selectedBranchObj = client.branches.firstOrNull()
                                    isClientDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Dropdown Sede
                Box(modifier = Modifier.weight(1f)) {
                    val branches = selectedClientObj?.branches.orEmpty()
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GrisNeutro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = branches.isNotEmpty()) { isSedeDropdownOpen = true }
                    ) {
                        Text(
                            text = selectedBranchObj?.name ?: "Sede Principal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulMarca
                            ),
                            modifier = Modifier.padding(12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    DropdownMenu(
                        expanded = isSedeDropdownOpen,
                        onDismissRequest = { isSedeDropdownOpen = false }
                    ) {
                        branches.forEach { branch ->
                            DropdownMenuItem(
                                text = { Text(branch.name, fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                                onClick = {
                                    selectedBranchObj = branch
                                    isSedeDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Selector de Técnico Asignado
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Técnico Asignado",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GrisNeutro,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = techAccounts.isNotEmpty()) { isTechDropdownOpen = true }
                ) {
                    Text(
                        text = selectedTechObj?.fullName ?: "Sin técnico asignado (se asigna luego)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedTechObj != null) AzulMarca else GrisTextoSecundario
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                DropdownMenu(
                    expanded = isTechDropdownOpen,
                    onDismissRequest = { isTechDropdownOpen = false }
                ) {
                    techAccounts.forEach { tech ->
                        DropdownMenuItem(
                            text = { Text(tech.fullName, fontFamily = PoppinsFontFamily) },
                            onClick = {
                                selectedTechObj = tech
                                isTechDropdownOpen = false
                            }
                        )
                    }
                }
            }
        }

        // Selector de Servicio por Chips de 1 Toque
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Servicio",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                servicesChips.forEachIndexed { idx, (serviceTitle, _) ->
                    val isSelected = selectedServiceIndex == idx
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) CelesteAcento else Blanco,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CelesteAcento else GrisBorde
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedServiceIndex = idx }
                    ) {
                        Text(
                            text = serviceTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Blanco else AzulMarca,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Franja Horaria (Chips)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Franja Horaria",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.forEach { slot ->
                    val isSlotSelected = selectedTimeSlot == slot
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSlotSelected) AzulMarca else Blanco,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSlotSelected) AzulMarca else GrisBorde
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedTimeSlot = slot }
                    ) {
                        Text(
                            text = slot,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSlotSelected) Blanco else GrisTextoSecundario,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Botón de Confirmación
        Button(
            onClick = {
                val clientObj = selectedClientObj
                if (clientObj == null) {
                    Toast.makeText(context, "Registra un cliente primero para agendar una OT.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val serviceData = servicesChips[selectedServiceIndex]
                val sedeNameResolved = selectedBranchObj?.name ?: "Sede Principal"
                val newTask = ScheduledCalendarTask(
                    id = "task-${System.currentTimeMillis()}",
                    clientId = clientObj.id,
                    clientName = clientObj.name,
                    sedeId = selectedBranchObj?.id ?: "sede-principal",
                    sedeName = sedeNameResolved,
                    sede = sedeNameResolved,
                    assignedTechIds = selectedTechObj?.let { listOf(it.id) } ?: emptyList(),
                    dayNumber = selectedDayNumber,
                    dayOfWeekLabel = "$selectedDayNumber $monthShortLabel",
                    date = "$selectedDayNumber de $monthNameOnly, $yearNumber",
                    timeSlot = selectedTimeSlot,
                    time = selectedTimeSlot,
                    title = serviceData.first,
                    technicianName = selectedTechObj?.fullName ?: "Por asignar",
                    category = serviceData.second,
                    comments = "Servicio técnico programado",
                    status = "Programada",
                    clientColor = getCompanyColor(clientObj.name)
                )
                onTaskCreated(newTask)
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CelesteAcento,
                contentColor = Blanco
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_confirm_schedule_task")
        ) {
            Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Programar Orden",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Blanco,
                    fontSize = 14.5.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
