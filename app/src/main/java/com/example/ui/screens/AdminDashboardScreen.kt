package com.example.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.ui.components.FloatingGlassBottomBar
import com.example.ui.components.FloatingGlassNavItem
import com.example.ui.components.GlassDialogEntrance
import com.example.ui.components.modalBackdropBlur
import com.example.ui.theme.InstitutionalHeaderBrush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.EaseOutCubic
import com.example.R
import com.example.data.AuthRepository
import com.example.data.LocalDatabaseStore
import com.example.data.OperationalRepository
import com.example.ui.components.CreateAnnouncementDialog
import com.example.ui.components.UserProfileHeaderCapsule
import com.example.model.ClientBranch
import com.example.model.ClientSegment
import com.example.model.CorporateClientAccount
import com.example.model.OperationalTask
import com.example.model.RescheduleNotice
import com.example.model.StaffCollaborator
import com.example.model.TaskOperationStatus
import com.example.model.UserAccount
import com.example.ui.theme.AmbarCalido
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.SombraDifusaColor
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Pestañas principales de navegación móvil del Administrador Central.
 */
enum class AdminMainTab(val title: String, val icon: ImageVector) {
    INICIO("Inicio", Icons.Rounded.Home),
    CALENDARIO("Calendario", Icons.Rounded.CalendarMonth),
    CLIENTES_SEDES("Clientes", Icons.Rounded.Business),
    LIMPIEZA("Limpieza", Icons.Rounded.CleaningServices),
    MAPA_GPS("Mapa GPS", Icons.Rounded.Map),
    MENU("Menú", Icons.Rounded.Menu)
}

/**
 * Obtiene el color corporativo distintivo de cada Empresa / Cliente Matriz.
 */
fun getCompanyColor(companyName: String): Color {
    if (companyName.isBlank()) return AzulMarca
    val palette = listOf(
        AzulMarca,
        Color(0xFF00897B),
        Color(0xFF6A1B9A),
        Color(0xFF00838F),
        Color(0xFF2E7D32),
        Color(0xFFE65100)
    )
    return palette[abs(companyName.lowercase().hashCode()) % palette.size]
}

/**
 * Panel de Administración Central B2B de Todoservicio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentUser: UserAccount,
    onNavigateToCreateClient: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Pestaña inferior activa (Inicia en INICIO 🏠 universal)
    var currentTab by remember { mutableStateOf(AdminMainTab.INICIO) }

    // Estado de modales y diálogos
    var showAccessManagementModal by remember { mutableStateOf(false) }
    var showCreateProfileBottomSheet by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<UserAccount?>(null) }
    var showCreateTaskBottomSheet by remember { mutableStateOf(false) }
    var preselectedClientForTask by remember { mutableStateOf<String?>(null) }
    var preselectedBranchForTask by remember { mutableStateOf<String?>(null) }

    var showAddBranchDialog by remember { mutableStateOf(false) }
    var clientForNewBranch by remember { mutableStateOf<CorporateClientAccount?>(null) }

    var showAddCorporateClientDialog by remember { mutableStateOf(false) }
    var showStaffDirectoryDialog by remember { mutableStateOf(false) }
    var showCreateAnnouncementDialog by remember { mutableStateOf(false) }
    var showWorkCatalogModal by remember { mutableStateOf(false) }
    var taskToReschedule by remember { mutableStateOf<OperationalTask?>(null) }
    var pushNotificationBanner by remember { mutableStateOf<String?>(null) }

    // Día seleccionado del mes en el Calendario Operativo (inicia en hoy)
    var selectedCalendarDay by remember {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    }

    // 1. Empresas Corporativas y Sedes Multi-Local centralizadas en OperationalRepository
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()

    // 2. Tareas Operativas del Calendario centralizadas (sincroniza Admin, Cliente y Técnico en tiempo real)
    val tasksList by OperationalRepository.operationalTasks.collectAsStateWithLifecycle()

    // 3. Colaboradores técnicos y personal operativo reactivo
    val collaborators by AuthRepository.staffCollaboratorsFlow.collectAsStateWithLifecycle()

    val isAnyAdminModalOpen = showAccessManagementModal ||
        showCreateProfileBottomSheet ||
        showCreateTaskBottomSheet ||
        taskToReschedule != null ||
        showAddBranchDialog ||
        showAddCorporateClientDialog ||
        showStaffDirectoryDialog ||
        showCreateAnnouncementDialog ||
        showWorkCatalogModal

    // Estructura EndDrawer derecha (LayoutDirection RTL para el contenedor del drawer)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = Modifier.modalBackdropBlur(isAnyAdminModalOpen),
            drawerContent = {
                // Volver a LTR dentro del contenido del Drawer para que el texto y los iconos se lean correctamente
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AdminRightEndDrawerContent(
                        currentUser = currentUser,
                        onClose = { coroutineScope.launch { drawerState.close() } },
                        onOpenAccessManagement = {
                            coroutineScope.launch { drawerState.close() }
                            showAccessManagementModal = true
                        },
                        onCreateProfile = {
                            coroutineScope.launch { drawerState.close() }
                            accountToEdit = null
                            showCreateProfileBottomSheet = true
                        },
                        onCreateTask = {
                            coroutineScope.launch { drawerState.close() }
                            preselectedClientForTask = null
                            preselectedBranchForTask = null
                            showCreateTaskBottomSheet = true
                        },
                        onOpenStaffDirectory = {
                            coroutineScope.launch { drawerState.close() }
                            showStaffDirectoryDialog = true
                        },
                        onOpenWorkCatalog = {
                            coroutineScope.launch { drawerState.close() }
                            showWorkCatalogModal = true
                        },
                        onExportPdfReport = {
                            coroutineScope.launch { drawerState.close() }
                            try {
                                val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale("es", "UY")).format(Calendar.getInstance().time)
                                val generatedFile = PdfReportGenerator.generatePdfReport(
                                    context = context,
                                    data = PdfReportGenerator.ReportData(
                                        orderCode = "RESUMEN-${OperationalRepository.peekNextOrderCode()}",
                                        serviceTitle = "Informe Ejecutivo de Operaciones",
                                        category = "Gestión Integral Multisede",
                                        executionDate = todayStr,
                                        clientName = currentUser.fullName,
                                        companyName = "Todoservicio S.A.S.",
                                        clientAddress = "Montevideo y Canelones, Uruguay",
                                        leadTechnician = currentUser.fullName,
                                        tasksExecuted = tasksList.map { "${it.code} • ${it.clientName}: ${it.title} (${it.status.label})" }
                                            .ifEmpty { listOf("Sin órdenes operativas activas registradas") },
                                        findingsSummary = "Clientes activos: ${corporateClients.size}. Órdenes registradas: ${tasksList.size}."
                                    )
                                )
                                if (generatedFile != null) {
                                    PdfReportGenerator.openPdf(context, generatedFile)
                                    Toast.makeText(context, "Informe A4 generado y listo para compartir", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "No se pudo generar el archivo PDF", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al generar informe: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onLogout = {
                            coroutineScope.launch { drawerState.close() }
                            onLogout()
                        }
                    )
                }
            }
        ) {
            // Contenido principal LTR
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = modifier.fillMaxSize(),
                        containerColor = GrisNeutro,
                        topBar = {
                            AdminDrasticRespiroHeader(
                                userName = currentUser.fullName.split(" ").firstOrNull() ?: "Mateo",
                                avatarUrl = currentUser.avatarUrl
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding())
                        ) {
                        // Banner de Notificación Push / Aviso Cruzado
                        AnimatedVisibility(
                            visible = pushNotificationBanner != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(12.dp)
                        ) {
                            pushNotificationBanner?.let { bannerText ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = AzulMarca,
                                    shape = RoundedCornerShape(16.dp),
                                    shadowElevation = 8.dp,
                                    border = BorderStroke(1.dp, Color(0xFF1D4E89))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(CelesteAcento, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.NotificationsActive,
                                                contentDescription = null,
                                                tint = Blanco,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Aviso Cruzado Automático",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CelesteAcentoClaro,
                                                fontFamily = PoppinsFontFamily
                                            )
                                            Text(
                                                text = bannerText,
                                                fontSize = 11.sp,
                                                color = Blanco,
                                                fontFamily = PoppinsFontFamily,
                                                lineHeight = 14.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { pushNotificationBanner = null },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Cerrar",
                                                tint = Blanco.copy(alpha = 0.8f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Crossfade(
                            targetState = currentTab,
                            animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
                            label = "AdminTabTransition"
                        ) { tab ->
                            when (tab) {
                                AdminMainTab.INICIO -> {
                                    AdminExecutiveHomeSection(
                                        tasks = tasksList,
                                        collaborators = collaborators,
                                        onNavigateToCalendar = { currentTab = AdminMainTab.CALENDARIO },
                                        onNavigateToClients = { currentTab = AdminMainTab.CLIENTES_SEDES },
                                        onNavigateToGps = { currentTab = AdminMainTab.MAPA_GPS },
                                        onOpenCreateTask = {
                                            preselectedClientForTask = null
                                            preselectedBranchForTask = null
                                            showCreateTaskBottomSheet = true
                                        },
                                        onOpenAccessManagement = { showAccessManagementModal = true },
                                        onOpenCreateAnnouncement = { showCreateAnnouncementDialog = true },
                                        onOpenWorkCatalog = { showWorkCatalogModal = true },
                                        onOpenStaffDirectory = { showStaffDirectoryDialog = true }
                                    )
                                }
                                AdminMainTab.CALENDARIO -> {
                                    AdminMonthlyCalendarSection(
                                        tasks = tasksList,
                                        selectedDay = selectedCalendarDay,
                                        onSelectDay = { selectedCalendarDay = it },
                                        onOpenCreateTask = {
                                            preselectedClientForTask = null
                                            preselectedBranchForTask = null
                                            showCreateTaskBottomSheet = true
                                        },
                                        onRescheduleTask = { task ->
                                            taskToReschedule = task
                                        }
                                    )
                                }
                                AdminMainTab.CLIENTES_SEDES -> {
                                    AdminMultiBranchClientsSection(
                                        corporateClients = corporateClients,
                                        onAddNewCompany = { showAddCorporateClientDialog = true },
                                        onAddNewBranch = { client ->
                                            clientForNewBranch = client
                                            showAddBranchDialog = true
                                        },
                                        onAssignTaskToBranch = { client, branch ->
                                            preselectedClientForTask = client.name
                                            preselectedBranchForTask = branch.name
                                            showCreateTaskBottomSheet = true
                                        },
                                        onViewOnMap = { branch ->
                                            currentTab = AdminMainTab.MAPA_GPS
                                            Toast.makeText(context, "Mostrando ${branch.name} en el Mapa GPS", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                AdminMainTab.LIMPIEZA -> {
                                    AdminCleaningControlScreen(
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AdminMainTab.MAPA_GPS -> {
                                    GeneralGpsMapSection(
                                        onInspectTaskDetail = { _ ->
                                            currentTab = AdminMainTab.CALENDARIO
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                AdminMainTab.MENU -> {
                                    // El tab Menú abre el drawer directamente
                                    Box(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }

                    AdminBottomNavigationBar(
                        selectedTab = currentTab,
                        onTabSelected = { tab ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (tab == AdminMainTab.MENU) {
                                coroutineScope.launch { drawerState.open() }
                            } else {
                                currentTab = tab
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    // Modal Gestión de Accesos y Perfiles (Admin)
    if (showAccessManagementModal) {
        Dialog(
            onDismissRequest = { showAccessManagementModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            ApplyWindowBackdropBlur()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = GrisNeutro
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = AzulMarca,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                    bottom = 12.dp,
                                    start = 16.dp,
                                    end = 12.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Control de Accesos",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Blanco
                            )
                            IconButton(
                                onClick = { showAccessManagementModal = false },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cerrar Accesos",
                                    tint = Blanco
                                )
                            }
                        }
                    }

                    AdminAccessManagementSection(
                        onOpenCreateProfile = {
                            accountToEdit = null
                            showCreateProfileBottomSheet = true
                        },
                        onEditProfile = { account ->
                            accountToEdit = account
                            showCreateProfileBottomSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Modal Exclusivo Administrador: Catálogo Editable de Tareas y Servicios
    if (showWorkCatalogModal) {
        Dialog(
            onDismissRequest = { showWorkCatalogModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
        ) {
            ApplyWindowBackdropBlur()
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = GrisNeutro
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = AzulMarca,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                    bottom = 12.dp,
                                    start = 16.dp,
                                    end = 12.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Catálogo de Tareas",
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Blanco
                            )
                            IconButton(
                                onClick = { showWorkCatalogModal = false },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("close_work_catalog_modal")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Cerrar Catálogo",
                                    tint = Blanco
                                )
                            }
                        }
                    }

                    AdminMasterServicesSection(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    // Modal Crear / Editar Perfil (CreateProfileBottomSheet)
    if (showCreateProfileBottomSheet) {
        CreateProfileBottomSheet(
            existingAccount = accountToEdit,
            onDismiss = {
                showCreateProfileBottomSheet = false
                accountToEdit = null
            },
            onProfileCreated = { savedProfile ->
                val wasEdit = accountToEdit != null
                showCreateProfileBottomSheet = false
                accountToEdit = null
                Toast.makeText(
                    context,
                    if (wasEdit) "Perfil actualizado: ${savedProfile.username}" else "Perfil creado: ${savedProfile.username}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Modal Burbuja Flotante Animado "+ Asignar Tarea Operativa"
    if (showCreateTaskBottomSheet) {
        CascadeTaskAssignmentBubbleDialog(
            corporateClients = corporateClients,
            collaborators = collaborators,
            initialClientName = preselectedClientForTask,
            initialBranchName = preselectedBranchForTask,
            initialDay = selectedCalendarDay,
            onDismiss = { showCreateTaskBottomSheet = false },
            onTaskCreated = { newTask ->
                val savedTask = OperationalRepository.addTask(newTask)
                selectedCalendarDay = savedTask.dayOfMonth
                showCreateTaskBottomSheet = false
                val techNames = if (savedTask.assignedTechnicians.isNotEmpty()) {
                    savedTask.assignedTechnicians.joinToString(", ")
                } else {
                    savedTask.technicianName
                }
                pushNotificationBanner = "Notificación enviada a ${savedTask.clientName} y $techNames: ${savedTask.code} (${savedTask.scheduledDate})"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(context, "${savedTask.code} programada • ${savedTask.scheduledDate}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Diálogo de Reprogramación Inmediata de Tarea
    taskToReschedule?.let { task ->
        RescheduleTaskDialog(
            task = task,
            onDismiss = { taskToReschedule = null },
            onConfirmReschedule = { newDay, newTimeSlot, reason ->
                val newDateStr = "$newDay de Octubre, 2026"
                OperationalRepository.rescheduleTask(
                    taskId = task.id,
                    newDate = newDateStr,
                    reason = reason,
                    newDay = newDay,
                    newTimeSlot = newTimeSlot
                )
                selectedCalendarDay = newDay
                val techNames = if (task.assignedTechnicians.isNotEmpty()) {
                    task.assignedTechnicians.joinToString(", ")
                } else {
                    task.technicianName
                }
                pushNotificationBanner = "Aviso enviado a ${task.clientName} y $techNames: ${task.code} reprogramada al $newDay de Octubre ($newTimeSlot)"
                taskToReschedule = null
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(
                    context,
                    "${task.code} reprogramada para el $newDay de Octubre.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // Diálogo para Agregar Nueva Sede a una Empresa Existente
    if (showAddBranchDialog && clientForNewBranch != null) {
        AddNewBranchDialog(
            company = clientForNewBranch!!,
            onDismiss = {
                showAddBranchDialog = false
                clientForNewBranch = null
            },
            onBranchAdded = { newBranch ->
                val client = clientForNewBranch!!
                OperationalRepository.addClientBranch(client.id, newBranch)
                showAddBranchDialog = false
                clientForNewBranch = null
                Toast.makeText(context, "Sede '${newBranch.name}' agregada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Diálogo para Registrar Nueva Empresa Matriz
    if (showAddCorporateClientDialog) {
        AddNewCorporateCompanyDialog(
            onDismiss = { showAddCorporateClientDialog = false },
            onCompanyAdded = { newCompany ->
                OperationalRepository.addCorporateClient(newCompany)
                showAddCorporateClientDialog = false
                Toast.makeText(context, "Empresa '${newCompany.name}' registrada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Diálogo / Panel de Gestión de Personal y Colaboradores
    if (showStaffDirectoryDialog) {
        StaffDirectoryDialog(
            collaborators = collaborators,
            onDismiss = { showStaffDirectoryDialog = false },
            onCallCollaborator = { phone ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Llamando a $phone", Toast.LENGTH_SHORT).show()
                }
            },
            onAddCollaborator = { newColab ->
                AuthRepository.addStaffCollaborator(newColab)
                Toast.makeText(context, "Colaborador incorporado exitosamente", Toast.LENGTH_SHORT).show()
            },
            onToggleStatus = { id ->
                AuthRepository.toggleStaffCollaboratorStatus(id)
            },
            onDeleteCollaborator = { id ->
                AuthRepository.removeStaffCollaborator(id)
                Toast.makeText(context, "Colaborador eliminado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal de Administración: Subir Aviso / Comunicado con Push Local
    if (showCreateAnnouncementDialog) {
        CreateAnnouncementDialog(
            authorName = currentUser.fullName,
            onDismiss = { showCreateAnnouncementDialog = false },
            onAnnouncementPublished = { published ->
                showCreateAnnouncementDialog = false
                pushNotificationBanner = "Aviso '${published.title}' publicado y enviado por Push a ${published.targetRoles.joinToString { it.label }}"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                Toast.makeText(context, "Aviso publicado y notificación enviada", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * 1. ENCABEZADO SUPERIOR DRÁSTICO Y DESAHOGADO:
 * - SafeArea estricto con Padding(top: 36.0, bottom: 16.0, horizontal: 16.0).
 * - Logotipo oficial adjunto "Todoservicio_Logo_HD.png" (R.drawable.todoservicio_logo_hd) a 38-42dp de altura.
 * - Subtítulo sobrio "Panel de Operaciones" (#E0E6ED, 12sp, letterSpacing: 1.1).
 * - REMUEVE por completo los botones "+ Perfil" y "+ Tarea" del encabezado para dejarlo limpio.
 * - Menú hamburguesa eliminado: navegación centralizada en la barra inferior (Bottom Bar).
 */
@Composable
fun AdminDrasticRespiroHeader(
    userName: String = "Mateo",
    avatarUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val safeTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInsetPadding = (safeTop + 8.dp).coerceAtLeast(32.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AzulMarca,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(InstitutionalHeaderBrush)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = topInsetPadding,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                    contentDescription = "Todoservicio Logotipo Oficial",
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("official_logo_header"),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Panel de Operaciones",
                    color = Color(0xFFE0E6ED),
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            UserProfileHeaderCapsule(
                firstName = userName,
                avatarUrl = avatarUrl
            )
        }
    }
}

/**
 * Tarjeta de acceso rápido con microinteracción táctil y sombra 3D para el Panel Central del Administrador.
 */
@Composable
fun AdminHomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBgColor: Color = GrisNeutro
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AzulMarca,
                fontFamily = PoppinsFontFamily
            )
            Text(
                text = subtitle,
                fontSize = 10.5.sp,
                color = GrisTextoSecundario,
                fontFamily = PoppinsFontFamily,
                lineHeight = 13.sp
            )
        }
    }
}

/**
 * PANTALLA INICIO DEL ADMINISTRADOR (Panel Central):
 * - Saludo ejecutivo: "Bienvenido, Mateo | Panel Central".
 * - Métricas rápidas y balance diario: tarjetas compactas con visitas programadas hoy, cuadrillas en ruta y alertas pendientes.
 * - Accesos rápidos en un toque: [ 📅 Ver Calendario ], [ 👥 Crear Cliente / Sede ], [ 🗺️ Monitorear Flota GPS ], [ + Asignar Nueva Orden ].
 * - Cuadrillas activas y despacho operativo en tiempo real.
 */
@Composable
fun AdminExecutiveHomeSection(
    tasks: List<OperationalTask>,
    collaborators: List<StaffCollaborator>,
    onNavigateToCalendar: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToGps: () -> Unit,
    onOpenCreateTask: () -> Unit,
    onOpenAccessManagement: () -> Unit = {},
    onOpenCreateAnnouncement: () -> Unit = {},
    onOpenWorkCatalog: () -> Unit = {},
    onOpenStaffDirectory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val todayCal = remember { Calendar.getInstance() }
    val todayDayOfMonth = remember { todayCal.get(Calendar.DAY_OF_MONTH) }
    val todayLabel = remember {
        SimpleDateFormat("d MMM yyyy", Locale("es", "UY")).format(todayCal.time)
            .replaceFirstChar { it.uppercase() }
    }
    val todayTasksCount = remember(tasks, todayDayOfMonth) { tasks.count { it.dayOfMonth == todayDayOfMonth } }
    val inTransitCount = remember(tasks) { tasks.count { it.isEnCamino } }
    val alertsCount = remember(tasks) { tasks.count { it.rescheduleNotice != null } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tarjeta Saludo Ejecutivo minimalista
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = SombraDifusaColor,
                    spotColor = SombraDifusaColor
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco),
            border = BorderStroke(1.dp, GrisBorde)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = VerdeExitoFondo,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(VerdeExito, CircleShape)
                            )
                            Text(
                                text = "EN LÍNEA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeExito,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }

                    Text(
                        text = todayLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                }

                Text(
                    text = "Panel Central",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisGrafito,
                    fontFamily = PoppinsFontFamily
                )

                Text(
                    text = "Montevideo • Canelones",
                    fontSize = 12.sp,
                    color = GrisTextoSecundario,
                    fontFamily = PoppinsFontFamily
                )
            }
        }

        // 2. Métricas Rápidas
        Text(
            text = "Resumen Diario",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = GrisGrafito,
            fontFamily = PoppinsFontFamily
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Métrica 1: Visitas Hoy (reactiva)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(10.dp, RoundedCornerShape(18.dp), ambientColor = SombraDifusaColor, spotColor = SombraDifusaColor),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(CelesteAcentoClaro, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = AzulMarca,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "$todayTasksCount",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrisGrafito,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "Visitas Hoy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }

            // Métrica 2: Cuadrillas en Ruta
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(VerdeExito.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = VerdeExito,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "$inTransitCount",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "En Ruta GPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }

            // Métrica 3: Alertas Pendientes
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF102C54).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AzulCorporativo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "$alertsCount",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alertsCount > 0) NaranjaAlerta else VerdeExito,
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "Alertas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                }
            }
        }

        // 3. Accesos Rápidos en un Toque
        Text(
            text = "Accesos Rápidos Ejecutivos",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AzulMarca,
            fontFamily = PoppinsFontFamily
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Fila 1: [ 📅 Ver Calendario ] y [ 👥 Crear Cliente / Sede ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminHomeActionCard(
                    title = "Ver Calendario",
                    subtitle = "Planificación mensual",
                    icon = Icons.Rounded.CalendarMonth,
                    iconTint = CelesteAcento,
                    borderColor = CelesteAcento.copy(alpha = 0.5f),
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f)
                )

                AdminHomeActionCard(
                    title = "Clientes y Sedes",
                    subtitle = "Empresas y locales",
                    icon = Icons.Rounded.Business,
                    iconTint = AzulCorporativo,
                    borderColor = GrisBorde,
                    onClick = onNavigateToClients,
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 2: [ 🗺️ Monitorear Flota GPS ] y [ + Asignar Nueva Orden ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminHomeActionCard(
                    title = "Flota GPS",
                    subtitle = "Móviles en tiempo real",
                    icon = Icons.Rounded.Map,
                    iconTint = AzulCorporativo,
                    borderColor = GrisBorde,
                    onClick = onNavigateToGps,
                    modifier = Modifier.weight(1f)
                )

                AdminHomeActionCard(
                    title = "Asignar Orden",
                    subtitle = "Despacho inmediato",
                    icon = Icons.Rounded.Add,
                    iconTint = Blanco,
                    iconBgColor = AzulMarca,
                    borderColor = AzulMarca,
                    onClick = onOpenCreateTask,
                    modifier = Modifier.weight(1f)
                )
            }

            // Fila 3: Control de Accesos + Nuevo Aviso / Comunicado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminHomeActionCard(
                    title = "Gestión de Accesos",
                    subtitle = "Perfiles y permisos",
                    icon = Icons.Rounded.Security,
                    iconTint = AzulMarca,
                    iconBgColor = CelesteAcentoClaro,
                    borderColor = GrisBorde,
                    onClick = onOpenAccessManagement,
                    modifier = Modifier.weight(1f)
                )

                AdminHomeActionCard(
                    title = "Nuevo Aviso",
                    subtitle = "Comunicado y Push",
                    icon = Icons.Rounded.Campaign,
                    iconTint = AzulCorporativo,
                    iconBgColor = Color(0xFFFEF3C7),
                    borderColor = AmbarCalido.copy(alpha = 0.7f),
                    onClick = onOpenCreateAnnouncement,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_admin_new_announcement")
                )
            }

            // Fila 4: Catálogo de Tareas (Solo Admin) + Equipo y Técnicos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminHomeActionCard(
                    title = "Catálogo de Tareas",
                    subtitle = "Servicios y rubros",
                    icon = Icons.Rounded.Build,
                    iconTint = AzulCorporativo,
                    iconBgColor = CelesteAcentoClaro,
                    borderColor = GrisBorde,
                    onClick = onOpenWorkCatalog,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_admin_work_catalog")
                )

                AdminHomeActionCard(
                    title = "Equipo Técnico",
                    subtitle = "Personal y contactos",
                    icon = Icons.Rounded.Engineering,
                    iconTint = VerdeExito,
                    iconBgColor = VerdeExitoFondo,
                    borderColor = GrisBorde,
                    onClick = onOpenStaffDirectory,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_admin_staff_directory")
                )
            }
        }

        // 4. Cuadrillas Despachadas Hoy
        Text(
            text = "Cuadrillas Activas",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AzulMarca,
            fontFamily = PoppinsFontFamily
        )

        if (collaborators.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Engineering,
                        contentDescription = null,
                        tint = GrisTextoSecundario.copy(alpha = 0.5f),
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = "Sin personal asignado aún. Toca Gestión de Accesos para comenzar",
                        fontSize = 12.5.sp,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            collaborators.take(3).forEach { tech ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF102C54), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Engineering,
                                contentDescription = null,
                                tint = Blanco,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tech.fullName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = "${tech.jobTitle} • ${tech.phone}",
                                fontSize = 11.sp,
                                color = GrisTextoSecundario,
                                fontFamily = PoppinsFontFamily
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = VerdeExitoFondo,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (tech.lastCheckIn.isNotBlank()) "En Ruta: ${tech.lastCheckIn}" else "Disponible",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeExito,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateToGps,
                            modifier = Modifier
                                .size(36.dp)
                                .background(CelesteAcentoClaro, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "Ver en GPS",
                                tint = CelesteAcento,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. BARRA DE NAVEGACIÓN INFERIOR B2B (5 Accesos Directos Fijos):
 * [ 🏠 INICIO ] • [ 📅 CALENDARIO ] • [ 👥 CLIENTES Y SEDES ] • [ 🗺️ MAPA GPS ] • [ ☰ MENÚ ]
 */
@Composable
fun AdminBottomNavigationBar(
    selectedTab: AdminMainTab,
    onTabSelected: (AdminMainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = remember {
        AdminMainTab.values().map { tab ->
            FloatingGlassNavItem(
                key = tab,
                label = tab.title,
                icon = tab.icon,
                testTag = "admin_tab_${tab.name.lowercase()}"
            )
        }
    }
    FloatingGlassBottomBar(
        items = navItems,
        selectedKey = selectedTab,
        onItemSelected = onTabSelected,
        modifier = modifier
    )
}

/**
 * 3. CALENDARIO MENSUAL VINCULADO CON REPROGRAMACIÓN / DRAG & DROP:
 * Cuadrícula estilo image_5.png en tarjeta blanca flotante con esquinas redondeadas (20dp).
 * Celdas de día con cápsulas coloreadas por cliente.
 * Modal de reprogramación instantánea con motivo (lluvias Dec. 406/88, cliente, etc.).
 */
@Composable
fun AdminMonthlyCalendarSection(
    tasks: List<OperationalTask>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onOpenCreateTask: () -> Unit,
    onRescheduleTask: (OperationalTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var monthOffset by remember { mutableIntStateOf(0) }
    var inspectedTask by remember { mutableStateOf<OperationalTask?>(null) }

    val localeEs = remember { java.util.Locale("es", "UY") }
    val todayCal = remember { java.util.Calendar.getInstance() }
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
    val daysInMonth = displayCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    // Convertir Calendar.DAY_OF_WEEK (Dom=1, Lun=2..Sáb=7) a índice (Lun=0..Dom=6)
    val offsetStart = (displayCal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
    val totalRows = ((daysInMonth + offsetStart) + 6) / 7
    val currentDayOfMonth = todayCal.get(java.util.Calendar.DAY_OF_MONTH)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra de Acción Superior del Calendario
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calendario Operativo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = AzulMarca
                )
                Text(
                    text = "$monthTitle • Programación Multisede",
                    fontSize = 12.sp,
                    color = GrisTextoSecundario,
                    fontFamily = PoppinsFontFamily
                )
            }

            // Botón "+ Asignar Tarea"
            Button(
                onClick = onOpenCreateTask,
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Blanco
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Asignar Tarea",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blanco,
                    fontFamily = PoppinsFontFamily
                )
            }
        }

        // Tarjeta Cuadrícula Mensual Flotante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Blanco)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Cabecera del Mes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = monthTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontFamily = PoppinsFontFamily
                    )
                    Row {
                        IconButton(
                            onClick = {
                                monthOffset--
                                onSelectDay(selectedDay.coerceAtMost(daysInMonth))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Mes Anterior", tint = GrisTextoSecundario)
                        }
                        IconButton(
                            onClick = {
                                monthOffset++
                                onSelectDay(selectedDay.coerceAtMost(daysInMonth))
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.ArrowForward, contentDescription = "Mes Siguiente", tint = GrisTextoSecundario)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Días de la semana
                val weekDayNames = listOf("L", "M", "M", "J", "V", "S", "D")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekDayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GrisTextoSecundario,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val slotIndex = row * 7 + col
                            val dayNumber = slotIndex - offsetStart + 1

                            if (dayNumber in 1..daysInMonth) {
                                val dayTasks = tasks.filter { it.dayOfMonth == dayNumber }
                                val isSelected = dayNumber == selectedDay
                                val isToday = monthOffset == 0 && dayNumber == currentDayOfMonth

                                Column(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> CelesteAcento
                                                isToday -> CelesteAcentoClaro
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                            color = if (isToday && !isSelected) CelesteAcento else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { onSelectDay(dayNumber) }
                                        .padding(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Blanco else AzulMarca
                                    )

                                    // Cápsulas de color por cliente
                                    if (dayTasks.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            dayTasks.take(3).forEach { task ->
                                                val compColor = getCompanyColor(task.clientName)
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .padding(horizontal = 0.5.dp)
                                                        .background(
                                                            color = if (isSelected) Blanco else compColor,
                                                            shape = CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }
        }

        // Título del Día Seleccionado y Lista de Tareas
        val tasksForSelectedDay = tasks.filter { it.dayOfMonth == selectedDay }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tareas del $selectedDay de $monthNameOnly",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFontFamily,
                color = AzulMarca
            )
            Surface(
                color = CelesteAcentoClaro,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${tasksForSelectedDay.size} asignadas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulCorporativo,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (tasksForSelectedDay.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditCalendar,
                        contentDescription = null,
                        tint = GrisTextoSecundario,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin tareas programadas para este día",
                        fontSize = 14.sp,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onOpenCreateTask,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Programar Tarea", color = CelesteAcento, fontSize = 12.sp)
                    }
                }
            }
        } else {
            tasksForSelectedDay.forEach { task ->
                AdminTaskCardItem(
                    task = task,
                    onClick = { inspectedTask = task },
                    onRescheduleClick = { onRescheduleTask(task) }
                )
            }
        }

        Spacer(modifier = Modifier.height(108.dp))
    }

    val ratingDetails by OperationalRepository.ratingDetails.collectAsStateWithLifecycle()

    inspectedTask?.let { task ->
        val ratingRecord = ratingDetails[task.code]
        val stars = task.clientRatingStars ?: ratingRecord?.stars
        val comment = task.clientRatingComment.ifBlank { ratingRecord?.comment.orEmpty() }
        AlertDialog(
            onDismissRequest = { inspectedTask = null },
            title = {
                Text(
                    text = "${task.code} • ${task.status.label}",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = task.title,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulMarca
                    )
                    Text("Cliente: ${task.clientName}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    Text("Dirección: ${task.address}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    Text("Técnico: ${task.technicianName}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = AzulCorporativo)
                    Text("Horario: ${task.timeSlot}", fontFamily = PoppinsFontFamily, fontSize = 12.5.sp, color = GrisTextoSecundario)
                    if (!task.checkInTime.isNullOrBlank() || !task.checkOutTime.isNullOrBlank()) {
                        val inStr = task.checkInTime?.ifBlank { "--:--" } ?: "--:--"
                        val outStr = task.checkOutTime?.ifBlank { "--:--" } ?: "--:--"
                        Text(
                            text = "Check-in: $inStr • Check-out: $outStr",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VerdeExito
                        )
                    }
                    if (stars != null) {
                        val starsRow = "★".repeat(stars) + "☆".repeat((5 - stars).coerceAtLeast(0))
                        Text(
                            text = "Calificación cliente: $starsRow ($stars/5)",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmbarCalido
                        )
                        if (comment.isNotBlank()) {
                            Text(
                                text = "\"$comment\"",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.5.sp,
                                color = GrisTextoSecundario
                            )
                        }
                    }
                    if (task.description.isNotBlank()) {
                        Text("Detalle: ${task.description}", fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = GrisTextoSecundario)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val toReschedule = task
                        inspectedTask = null
                        onRescheduleTask(toReschedule)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento)
                ) {
                    Text("Reprogramar", color = Blanco)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectedTask = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

/**
 * Tarjeta individual de tarea operativa con franja lateral de color de empresa matriz,
 * desglose de sede, técnico y botón directo de reprogramación.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminTaskCardItem(
    task: OperationalTask,
    onClick: () -> Unit,
    onRescheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val companyColor = getCompanyColor(task.clientName)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onRescheduleClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Franja lateral con el color corporativo del cliente
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(130.dp)
                    .background(companyColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Código de OT y Chip de Estado
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = companyColor,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(task.status.badgeColorHex).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = task.status.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(task.status.badgeColorHex),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Botón claro para reprogramar (Reasignar Fecha)
                    OutlinedButton(
                        onClick = onRescheduleClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        border = BorderStroke(1.dp, CelesteAcento),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reprogramar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcento,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontFamily = PoppinsFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Empresa y Local
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = GrisTextoSecundario,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${task.clientName} • ${task.address}",
                        fontSize = 12.sp,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Horario y Técnico Asignado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Engineering,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.technicianName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AzulCorporativo,
                            fontFamily = PoppinsFontFamily
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = GrisTextoSecundario,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = task.timeSlot,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                if (task.accessInstructions.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Acceso: ${task.accessInstructions}",
                            fontSize = 11.sp,
                            color = GrisTextoSecundario,
                            fontFamily = PoppinsFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!task.checkInTime.isNullOrBlank() || !task.checkOutTime.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val inStr = task.checkInTime?.ifBlank { "--:--" } ?: "--:--"
                    val outStr = task.checkOutTime?.ifBlank { "--:--" } ?: "--:--"
                    Text(
                        text = "Entrada: $inStr • Salida: $outStr",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdeExito,
                        fontFamily = PoppinsFontFamily
                    )
                }

                val liveRatings by OperationalRepository.ratingDetails.collectAsStateWithLifecycle()
                val ratingStars = task.clientRatingStars ?: liveRatings[task.code]?.stars
                if (ratingStars != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AmbarCalido,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Calificación cliente: $ratingStars/5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                // Aviso de Reprogramación previa (si existe)
                task.rescheduleNotice?.let { notice ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = NaranjaAlertaFondo,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                tint = NaranjaAlerta,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reprogramada: ${notice.reason}",
                                fontSize = 10.sp,
                                color = NaranjaAlerta,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. ESTRUCTURA DE CLIENTES MULTI-SEDE / LOCALES (ej. Rudy Burger con Pocitos, Carrasco, Punta Carretas):
 * Listado jerárquico de empresas matrices con tarjetas desplegables.
 * Muestra nombre de la sede, dirección real, contacto y teléfono con acciones directas de llamada, mapa y nueva tarea.
 */
@Composable
fun AdminMultiBranchClientsSection(
    corporateClients: List<CorporateClientAccount>,
    onAddNewCompany: () -> Unit,
    onAddNewBranch: (CorporateClientAccount) -> Unit,
    onAssignTaskToBranch: (CorporateClientAccount, ClientBranch) -> Unit,
    onViewOnMap: (ClientBranch) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf(ClientSegment.TODOS) }

    val filteredClients = corporateClients.filter { client ->
        val matchesSegment = selectedSegment == ClientSegment.TODOS || client.segment == selectedSegment
        val matchesSearch = client.name.contains(searchQuery, ignoreCase = true) ||
                client.branches.any { it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true) }
        matchesSegment && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Título y Botón "+ Nueva Empresa Matriz"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clientes y Sedes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFontFamily,
                        color = AzulMarca
                    )
                    Text(
                        text = "Estructura Corporativa Multi-Local",
                        fontSize = 12.sp,
                        color = GrisTextoSecundario,
                        fontFamily = PoppinsFontFamily
                    )
                }

                Button(
                    onClick = onAddNewCompany,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Blanco)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nueva Matriz", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blanco)
                }
            }
        }

        item {
            // Buscador de Clientes y Sedes
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar empresa matriz, sede o dirección...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = GrisTextoSecundario) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CelesteAcento,
                    unfocusedBorderColor = GrisBorde,
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco
                ),
                singleLine = true
            )
        }

        item {
            // Chips de filtro de segmento
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ClientSegment.values()) { segment ->
                    FilterChip(
                        selected = selectedSegment == segment,
                        onClick = { selectedSegment = segment },
                        label = { Text(segment.label, fontSize = 11.sp, fontFamily = PoppinsFontFamily) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CelesteAcento,
                            selectedLabelColor = Blanco,
                            containerColor = Blanco,
                            labelColor = AzulMarca
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Listado de Empresas Matrices
        if (filteredClients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    border = BorderStroke(1.dp, GrisBorde)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Business,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.55f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Sin registros aún. Toca + Nueva Matriz para comenzar",
                            fontSize = 13.sp,
                            color = GrisTextoSecundario,
                            fontFamily = PoppinsFontFamily,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredClients, key = { it.id }) { company ->
                CorporateCompanyCard(
                    company = company,
                    onAddBranch = { onAddNewBranch(company) },
                    onAssignTaskToBranch = { branch -> onAssignTaskToBranch(company, branch) },
                    onViewOnMap = onViewOnMap
                )
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

/**
 * Tarjeta Corporativa de Empresa Matriz con sus Sedes Dependientes (e.g. Rudy Burger).
 */
@Composable
fun CorporateCompanyCard(
    company: CorporateClientAccount,
    onAddBranch: () -> Unit,
    onAssignTaskToBranch: (ClientBranch) -> Unit,
    onViewOnMap: (ClientBranch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }
    val compColor = Color(company.clientColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Cabecera de la Empresa Matriz
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val loadedCompanyLogo = remember(company.logoUri) {
                        if (company.logoUri != null) {
                            try {
                                val uri = Uri.parse(company.logoUri)
                                val inputStream = context.contentResolver.openInputStream(uri)
                                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                            } catch (_: Exception) {
                                null
                            }
                        } else null
                    }

                    if (loadedCompanyLogo != null) {
                        Image(
                            bitmap = loadedCompanyLogo,
                            contentDescription = "Logo de ${company.name}",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = company.name.split(" ")
                            .filter { it.isNotBlank() }
                            .take(2)
                            .map { it.first().uppercase() }
                            .joinToString("")
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(compColor.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (initials.isNotBlank()) initials else "🏢",
                                color = compColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = company.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Text(
                            text = "RUT ${company.rut} • ${company.planName}",
                            fontSize = 11.sp,
                            color = GrisTextoSecundario,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }

                // Botón Desplegar / Colapsar
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = "Expandir/Colapsar Sedes",
                        tint = GrisTextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subcabecera con conteo de sedes y botón "+ Agregar Sede"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = compColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${company.branches.size} Locales / Sedes Activas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = compColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = onAddBranch,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = CelesteAcento)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Sede", color = CelesteAcento, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Lista de Sedes Dependientes
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    company.branches.forEach { branch ->
                        BranchItemCard(
                            branch = branch,
                            companyColor = compColor,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${branch.phone}"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Llamando a ${branch.phone}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onMap = { onViewOnMap(branch) },
                            onAssignTask = { onAssignTaskToBranch(branch) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Item individual de una Sede/Local dependiente de una Empresa Matriz.
 */
@Composable
fun BranchItemCard(
    branch: ClientBranch,
    companyColor: Color,
    onCall: () -> Unit,
    onMap: () -> Unit,
    onAssignTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = GrisNeutro,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GrisBorde)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(companyColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = branch.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca,
                        fontFamily = PoppinsFontFamily
                    )
                }

                // Acciones rápidas (Llamar, Mapa, Asignar)
                Row {
                    IconButton(onClick = onCall, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Call, contentDescription = "Llamar", tint = VerdeExito, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMap, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = "Ver en Mapa", tint = CelesteAcento, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onAssignTask, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = "Asignar Tarea", tint = AzulCorporativo, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Dirección real
            Text(
                text = branch.address,
                fontSize = 11.sp,
                color = GrisTextoSecundario,
                fontFamily = PoppinsFontFamily
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Contacto y Teléfono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Contacto: ${branch.contactPerson}",
                    fontSize = 11.sp,
                    color = AzulCorporativo,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = branch.phone,
                    fontSize = 11.sp,
                    color = GrisTextoSecundario
                )
            }
        }
    }
}

/**
 * 5. ENDDRAWER LATERAL DERECHO DE GESTIÓN:
 * Despliega desde la derecha con categorías corporativas:
 * - [ + Crear Nuevo Perfil ]
 * - [ + Nueva Orden de Trabajo / Tarea ]
 * - [ Directorio de Técnicos y Colaboradores ]
 * - [ Informes Generales y Descargas A4 ]
 * - [ Ajustes de Cuenta y Cerrar Sesión ]
 */
@Composable
fun AdminRightEndDrawerContent(
    currentUser: UserAccount,
    onClose: () -> Unit,
    onOpenAccessManagement: () -> Unit,
    onCreateProfile: () -> Unit,
    onCreateTask: () -> Unit,
    onOpenStaffDirectory: () -> Unit,
    onOpenWorkCatalog: () -> Unit = {},
    onExportPdfReport: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxSize(),
        drawerContainerColor = Blanco
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cabecera del Drawer con Azul Marca y Logotipo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AzulMarca)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                            contentDescription = "Todoservicio Logo",
                            modifier = Modifier.height(32.dp),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = "Cerrar Menú", tint = Blanco)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(AzulCorporativo, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.fullName.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "MD" },
                                color = Blanco,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentUser.fullName,
                                color = Blanco,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = "Administrador",
                                color = CelesteAcentoClaro,
                                fontSize = 11.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                    }
                }
            }

            // Opciones del Menú Lateral
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ACCIONES RÁPIDAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisTextoSecundario,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.Security,
                    title = "Gestión de Accesos",
                    subtitle = "Perfiles, roles y cuentas",
                    accentColor = AzulMarca,
                    onClick = onOpenAccessManagement
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.PersonAdd,
                    title = "Nuevo Perfil",
                    subtitle = "Clientes, técnicos y limpieza",
                    accentColor = CelesteAcento,
                    onClick = onCreateProfile
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.Add,
                    title = "Nueva Orden",
                    subtitle = "Asignación multisede",
                    accentColor = AzulCorporativo,
                    onClick = onCreateTask
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "OPERACIONES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrisTextoSecundario,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.Build,
                    title = "Catálogo de Tareas",
                    subtitle = "Editar tareas y servicios",
                    accentColor = AzulCorporativo,
                    onClick = onOpenWorkCatalog
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.Engineering,
                    title = "Equipo",
                    subtitle = "Técnicos y personal",
                    accentColor = Color(0xFF00897B),
                    onClick = onOpenStaffDirectory
                )

                DrawerMenuActionButton(
                    icon = Icons.Rounded.PictureAsPdf,
                    title = "Informes A4",
                    subtitle = "Exportar PDF",
                    accentColor = Color(0xFFE65100),
                    onClick = onExportPdfReport
                )
            }

            HorizontalDivider(color = GrisBorde)

            // Cerrar Sesión en la parte inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cerrar Sesión",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        fontFamily = PoppinsFontFamily
                    )
                    Text(
                        text = "Finalizar sesión de administración",
                        fontSize = 11.sp,
                        color = GrisTextoSecundario
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerMenuActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Blanco,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GrisBorde)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accentColor.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca,
                    fontFamily = PoppinsFontFamily
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = GrisTextoSecundario
                )
            }
            Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * MODAL EN CASCADA "+ ASIGNAR TAREA OPERATIVA":
 * 1. Selector de Cliente Corporativo (ej. "Rudy Burger").
 * 2. Selector dinámico de Local / Sede dependiente (ej. "Local Pocitos").
 * 3. Selector de Técnico asignado.
 * 4. Horario programado puntual.
 * 5. Chips rápidos de rubro de un toque.
 */
/**
 * 5. CALENDARIO: MODAL BURBUJA FLOTANTE ANIMADO, MULTI-TÉCNICO Y REPROGRAMACIÓN
 * - Al tocar '+ Asignar Tarea' o presionar sobre un día del calendario:
 *   * Modal burbuja flotante animado con escala y rebote desde el centro (Dialog con scaleIn/spring).
 *   * Selectores en cascada: Cliente Corporativo -> Sede -> Selección múltiple de técnicos (Checkboxes / cuadrillas de >= 2 técnicos) -> Selector de horario -> Rubro en un toque -> Campo de instrucciones de acceso y comentarios (accessInstructions).
 *   * Persistencia real y despacho inmediato.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CascadeTaskAssignmentBubbleDialog(
    corporateClients: List<CorporateClientAccount>,
    collaborators: List<StaffCollaborator>,
    initialClientName: String? = null,
    initialBranchName: String? = null,
    initialDay: Int = 26,
    onDismiss: () -> Unit,
    onTaskCreated: (OperationalTask) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    // Estados de cascada (null-safe cuando aún no hay empresas cargadas)
    var selectedClient by remember(corporateClients) {
        mutableStateOf(
            corporateClients.firstOrNull { it.name == initialClientName } ?: corporateClients.firstOrNull()
        )
    }

    var availableBranches by remember(selectedClient) {
        mutableStateOf(selectedClient?.branches ?: emptyList())
    }

    var selectedBranch by remember(selectedClient) {
        mutableStateOf(
            availableBranches.firstOrNull { it.name == initialBranchName } ?: availableBranches.firstOrNull()
        )
    }

    // Selección múltiple de técnicos para cuadrillas
    val selectedCollaborators = remember(collaborators) {
        mutableStateListOf<StaffCollaborator>().apply {
            collaborators.firstOrNull { it.isActive && it.roleCategory == "Técnico" }?.let { add(it) }
        }
    }

    var scheduledDay by remember { mutableIntStateOf(initialDay.coerceIn(1, 31)) }
    var scheduledTimeSlot by remember { mutableStateOf("09:30 - 12:00") }

    val presetTimeSlots = listOf(
        "08:30 - 11:00",
        "09:30 - 12:00",
        "13:00 - 15:30",
        "15:30 - 18:00"
    )

    val masterServices by OperationalRepository.masterServices.collectAsStateWithLifecycle()
    var selectedCatalogItem by remember(masterServices) {
        mutableStateOf(masterServices.firstOrNull())
    }
    var accessInstructions by remember { mutableStateOf("") }

    // Menús desplegables
    var clientMenuExpanded by remember { mutableStateOf(false) }
    var branchMenuExpanded by remember { mutableStateOf(false) }
    var catalogMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .padding(vertical = 20.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0x20102C54),
                        spotColor = Color(0x30102C54)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Blanco,
                border = BorderStroke(1.dp, GrisBorde)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Cabecera del Diálogo Burbuja
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                color = CelesteAcentoClaro,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "CORRELATIVO ${OperationalRepository.peekNextOrderCode()}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Nueva Asignación",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrisGrafito,
                                fontFamily = PoppinsFontFamily
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(GrisNeutro, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Cerrar",
                                tint = GrisTextoSecundario,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 1. Selector Cascada: Cliente Corporativo Matriz
                    Column {
                        Text(
                            text = "1. Cliente Corporativo Matriz:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = corporateClients.isNotEmpty()) { clientMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GrisBorde),
                            color = GrisNeutro
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(getCompanyColor(selectedClient?.name.orEmpty()), CircleShape)
                                    )
                                    Text(
                                        text = selectedClient?.name ?: "Sin clientes registrados (crea uno primero)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedClient != null) AzulMarca else GrisTextoSecundario,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                                Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = GrisTextoSecundario)
                            }
                            DropdownMenu(
                                expanded = clientMenuExpanded,
                                onDismissRequest = { clientMenuExpanded = false }
                            ) {
                                corporateClients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.name, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedClient = client
                                            availableBranches = client.branches
                                            selectedBranch = client.branches.firstOrNull()
                                            clientMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Selector Cascada: Sede o Local Dependiente
                    Column {
                        Text(
                            text = "2. Local o Sede Específica:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { branchMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GrisBorde),
                            color = GrisNeutro
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedBranch?.name ?: "Sin sedes registradas",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontFamily = PoppinsFontFamily
                                )
                                Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = GrisTextoSecundario)
                            }
                            DropdownMenu(
                                expanded = branchMenuExpanded,
                                onDismissRequest = { branchMenuExpanded = false }
                            ) {
                                availableBranches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(branch.name, fontWeight = FontWeight.Bold)
                                                Text(branch.address, fontSize = 11.sp, color = GrisTextoSecundario)
                                            }
                                        },
                                        onClick = {
                                            selectedBranch = branch
                                            branchMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Selección Múltiple de Técnicos (Checkboxes para cuadrillas >= 2)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. Cuadrilla Técnica (Selección Múltiple):",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulMarca,
                                fontFamily = PoppinsFontFamily
                            )
                            Surface(
                                color = if (selectedCollaborators.size >= 2) VerdeExitoFondo else CelesteAcentoClaro,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${selectedCollaborators.size} seleccionados",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCollaborators.size >= 2) VerdeExito else CelesteAcento,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GrisNeutro, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            collaborators.filter { it.isActive && it.roleCategory == "Técnico" }.forEach { tech ->
                                val isChecked = selectedCollaborators.any { it.id == tech.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isChecked) {
                                                if (selectedCollaborators.size > 1) {
                                                    selectedCollaborators.removeAll { it.id == tech.id }
                                                }
                                            } else {
                                                selectedCollaborators.add(tech)
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (!isChecked) selectedCollaborators.add(tech)
                                            } else {
                                                if (selectedCollaborators.size > 1) {
                                                    selectedCollaborators.removeAll { it.id == tech.id }
                                                }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = CelesteAcento,
                                            checkmarkColor = Blanco
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tech.fullName,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulMarca,
                                            fontFamily = PoppinsFontFamily
                                        )
                                        Text(
                                            text = tech.jobTitle,
                                            fontSize = 10.5.sp,
                                            color = GrisTextoSecundario,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Selector de Día y Franja Horaria
                    Column {
                        Text(
                            text = "4. Día Programado y Franja Horaria:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = "$scheduledDay",
                                onValueChange = { input -> input.toIntOrNull()?.let { scheduledDay = it.coerceIn(1, 31) } },
                                label = { Text("Día (Oct)") },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = scheduledTimeSlot,
                                onValueChange = { scheduledTimeSlot = it },
                                label = { Text("Horario") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presetTimeSlots) { slot ->
                                Surface(
                                    modifier = Modifier.clickable { scheduledTimeSlot = slot },
                                    color = if (scheduledTimeSlot == slot) CelesteAcento else GrisNeutro,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (scheduledTimeSlot == slot) CelesteAcento else GrisBorde)
                                ) {
                                    Text(
                                        text = slot,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (scheduledTimeSlot == slot) Blanco else AzulMarca,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Selector Desplegable conectado al Catálogo de Tareas del Administrador
                    Column {
                        Text(
                            text = "5. Tarea / Servicio del Catálogo:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = masterServices.isNotEmpty()) { catalogMenuExpanded = true }
                                .testTag("catalog_task_selector"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GrisBorde),
                            color = GrisNeutro
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = selectedCatalogItem?.name ?: "Mantenimiento Preventivo",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        fontFamily = PoppinsFontFamily,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    selectedCatalogItem?.let { item ->
                                        Text(
                                            text = "${item.code} • ${item.category}",
                                            fontSize = 11.sp,
                                            color = GrisTextoSecundario,
                                            fontFamily = PoppinsFontFamily
                                        )
                                    }
                                }
                                Icon(Icons.Rounded.ExpandMore, contentDescription = null, tint = GrisTextoSecundario)
                            }
                            DropdownMenu(
                                expanded = catalogMenuExpanded,
                                onDismissRequest = { catalogMenuExpanded = false }
                            ) {
                                masterServices.forEach { catalogItem ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = catalogItem.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = PoppinsFontFamily,
                                                    color = AzulMarca
                                                )
                                                Text(
                                                    text = "${catalogItem.code} • ${catalogItem.category}",
                                                    fontSize = 11.sp,
                                                    color = GrisTextoSecundario
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCatalogItem = catalogItem
                                            catalogMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. Campo de Instrucciones de Acceso y Comentarios (accessInstructions)
                    Column {
                        Text(
                            text = "6. Instrucciones de Acceso / Comentarios:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AzulMarca,
                            fontFamily = PoppinsFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = accessInstructions,
                            onValueChange = { accessInstructions = it },
                            placeholder = { Text("ej. Ingresar por rampa lateral subsuelo, pedir llave a conserjería", fontSize = 11.5.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Botón Confirmar y Asignar
                    Button(
                        enabled = selectedClient != null,
                        onClick = {
                            val clientObj = selectedClient ?: return@Button
                            val branchName = selectedBranch?.name ?: "Sede Central"
                            val branchAddress = selectedBranch?.address ?: "Montevideo, Uruguay"
                            val techNames = selectedCollaborators.map { it.fullName }
                            val mainTechName = techNames.joinToString(", ").ifBlank { "Sin asignar" }
                            val monthYearStr = SimpleDateFormat("MMMM, yyyy", Locale("es", "UY"))
                                .format(Calendar.getInstance().time)
                                .replaceFirstChar { it.uppercase() }
                            val chosenTaskName = selectedCatalogItem?.name ?: "Mantenimiento Preventivo"
                            val chosenCategory = selectedCatalogItem?.category ?: "Mantenimiento Edilicio"

                            val newTask = OperationalTask(
                                id = "ot-${System.currentTimeMillis()}",
                                code = "",
                                title = "$chosenTaskName en $branchName",
                                clientId = clientObj.id,
                                clientName = clientObj.name,
                                sedeId = selectedBranch?.id ?: "sede-01",
                                sedeName = branchName,
                                address = "$branchName • $branchAddress",
                                technicianName = mainTechName,
                                assignedTechIds = selectedCollaborators.map { it.id },
                                assignedTechnicians = techNames,
                                accessInstructions = accessInstructions.trim(),
                                scheduledDate = "$scheduledDay de $monthYearStr",
                                dayOfMonth = scheduledDay,
                                timeSlot = scheduledTimeSlot,
                                status = TaskOperationStatus.ABIERTA,
                                category = chosenCategory,
                                description = if (accessInstructions.isNotBlank()) accessInstructions else "Mantenimiento preventivo programado."
                            )
                            onTaskCreated(newTask)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(6.dp, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Blanco, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedClient != null) "Confirmar y Asignar" else "Registre una empresa primero",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontFamily = PoppinsFontFamily
                        )
                    }
                }
            }
        }
    }
}

/**
 * DIÁLOGO DE REPROGRAMACIÓN INMEDIATA:
 */
@Composable
fun RescheduleTaskDialog(
    task: OperationalTask,
    onDismiss: () -> Unit,
    onConfirmReschedule: (newDay: Int, newTimeSlot: String, reason: String) -> Unit
) {
    var newDayNumber by remember { mutableIntStateOf(task.dayOfMonth) }
    var selectedTimeSlot by remember { mutableStateOf(task.timeSlot) }
    val presetTimeSlots = listOf("08:30 - 11:00", "09:30 - 12:00", "13:00 - 15:30", "15:30 - 18:00")

    val reasons = listOf(
        "Lluvias / Clima Adverso",
        "Solicitud de Gerencia del Local",
        "Reasignación de Cuadrilla Técnica",
        "Urgencia Operativa Prioritaria"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = null,
                    tint = CelesteAcento,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Reprogramar Tarea ${task.code}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFontFamily,
                    color = AzulMarca
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${task.title}\n${task.clientName} (${task.address})",
                    fontSize = 12.sp,
                    color = GrisTextoSecundario,
                    fontFamily = PoppinsFontFamily
                )

                // 1. Selección de nuevo día
                Text(
                    text = "1. Seleccionar nuevo día de Octubre:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulMarca,
                    fontFamily = PoppinsFontFamily
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items((1..31).toList()) { day ->
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { newDayNumber = day },
                            color = if (newDayNumber == day) CelesteAcento else GrisNeutro,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (newDayNumber == day) CelesteAcento else GrisBorde)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$day",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newDayNumber == day) Blanco else AzulMarca
                                )
                            }
                        }
                    }
                }

                // 2. Selección de nueva franja horaria
                Text(
                    text = "2. Franja Horaria:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulMarca,
                    fontFamily = PoppinsFontFamily
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetTimeSlots) { slot ->
                        Surface(
                            modifier = Modifier.clickable { selectedTimeSlot = slot },
                            color = if (selectedTimeSlot == slot) CelesteAcento else GrisNeutro,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedTimeSlot == slot) CelesteAcento else GrisBorde)
                        ) {
                            Text(
                                text = slot,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTimeSlot == slot) Blanco else AzulMarca,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // 3. Motivo
                Text(
                    text = "3. Motivo de la reprogramación:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AzulMarca,
                    fontFamily = PoppinsFontFamily
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .border(2.dp, if (selectedReason == reason) CelesteAcento else GrisBorde, CircleShape)
                                    .padding(3.dp)
                                    .background(if (selectedReason == reason) CelesteAcento else Color.Transparent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reason, fontSize = 11.sp, color = AzulMarca, fontFamily = PoppinsFontFamily)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmReschedule(newDayNumber, selectedTimeSlot, selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirmar Cambio y Notificar", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = PoppinsFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 12.sp, color = GrisTextoSecundario, fontFamily = PoppinsFontFamily)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Blanco
    )
}

/**
 * Diálogo para agregar una nueva Sede a una Empresa Matriz existente.
 */
@Composable
fun AddNewBranchDialog(
    company: CorporateClientAccount,
    onDismiss: () -> Unit,
    onBranchAdded: (ClientBranch) -> Unit
) {
    var branchName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mapsLinkInput by remember { mutableStateOf("") }
    var resolvedLat by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LAT) }
    var resolvedLng by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LNG) }
    var hasResolvedCoords by remember { mutableStateOf(false) }
    var selectedRadiusMeters by remember { mutableIntStateOf(100) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nueva Sede para ${company.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AzulMarca
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = branchName,
                    onValueChange = { branchName = it },
                    label = { Text("Nombre del Local / Sede (ej. Local Carrasco)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección física (Montevideo / Canelones)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                com.example.ui.components.GoogleMapsLinkPickerField(
                    linkValue = mapsLinkInput,
                    onLinkValueChange = { mapsLinkInput = it },
                    previewName = branchName.ifBlank { company.name },
                    previewAddress = address,
                    resolvedLat = resolvedLat,
                    resolvedLng = resolvedLng,
                    hasResolvedCoordinates = hasResolvedCoords,
                    geofenceRadiusMeters = selectedRadiusMeters,
                    onRadiusSelected = { selectedRadiusMeters = it },
                    onCoordinatesResolved = { lat, lng, placeLabel, _ ->
                        resolvedLat = lat
                        resolvedLng = lng
                        hasResolvedCoords = true
                        if (branchName.isBlank() && !placeLabel.isNullOrBlank()) {
                            branchName = placeLabel
                        }
                    }
                )
                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Persona de Contacto / Encargado") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de Contacto (+598)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (branchName.isNotBlank() && address.isNotBlank()) {
                        val newBranch = ClientBranch(
                            id = "branch-${System.currentTimeMillis() % 10000}",
                            name = branchName.trim(),
                            address = address.trim(),
                            contactPerson = contactPerson.ifBlank { "Encargado de Local" },
                            phone = phone.ifBlank { "+598 99 000 000" },
                            lat = resolvedLat,
                            lng = resolvedLng,
                            geofenceRadiusMeters = selectedRadiusMeters,
                            googleMapsUrl = mapsLinkInput.trim()
                        )
                        onBranchAdded(newBranch)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Sede", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = GrisTextoSecundario)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Blanco
    )
}

/**
 * Diálogo para registrar una nueva Empresa Matriz (Cliente Corporativo).
 */
@Composable
fun AddNewCorporateCompanyDialog(
    onDismiss: () -> Unit,
    onCompanyAdded: (CorporateClientAccount) -> Unit
) {
    val context = LocalContext.current
    var companyName by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var planName by remember { mutableStateOf("Plan Corporativo Plus (6 Visitas)") }
    var initialBranchName by remember { mutableStateOf("Sede Central") }
    var initialBranchAddress by remember { mutableStateOf("") }
    var mapsLinkInput by remember { mutableStateOf("") }
    var resolvedLat by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LAT) }
    var resolvedLng by remember { mutableStateOf(com.example.util.URUGUAY_DEFAULT_LNG) }
    var hasResolvedCoords by remember { mutableStateOf(false) }
    var selectedRadiusMeters by remember { mutableIntStateOf(100) }
    var selectedLogoUri by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedLogoUri = LocalDatabaseStore.persistPickedImageUri(context, uri)
        }
    }

    val loadedLogoBitmap = remember(selectedLogoUri) {
        if (selectedLogoUri != null) {
            try {
                val uri = Uri.parse(selectedLogoUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Registrar Empresa Matriz", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AzulMarca)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Selector interactivo de logotipo corporativo
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (loadedLogoBitmap != null) {
                            Image(
                                bitmap = loadedLogoBitmap,
                                contentDescription = "Logo cargado",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = companyName.split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .map { it.first().uppercase() }
                                .joinToString("")
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (initials.isNotBlank()) initials else "🏢",
                                    color = AzulMarca,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedLogoUri != null) "Logotipo Seleccionado" else "Subir Logotipo de la Empresa",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontFamily = PoppinsFontFamily
                            )
                            Text(
                                text = if (selectedLogoUri != null) "Toca para cambiar imagen" else "Galería del dispositivo",
                                fontSize = 11.sp,
                                color = GrisTextoSecundario
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Seleccionar Imagen",
                            tint = CelesteAcento,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nombre de la Empresa (ej. Rudy Burger)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT (ej. 21.904.332.0018)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = initialBranchName,
                    onValueChange = { initialBranchName = it },
                    label = { Text("Nombre del Primer Local / Sede") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = initialBranchAddress,
                    onValueChange = { initialBranchAddress = it },
                    label = { Text("Dirección de la Sede") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                com.example.ui.components.GoogleMapsLinkPickerField(
                    linkValue = mapsLinkInput,
                    onLinkValueChange = { mapsLinkInput = it },
                    previewName = initialBranchName.ifBlank { companyName },
                    previewAddress = initialBranchAddress,
                    resolvedLat = resolvedLat,
                    resolvedLng = resolvedLng,
                    hasResolvedCoordinates = hasResolvedCoords,
                    geofenceRadiusMeters = selectedRadiusMeters,
                    onRadiusSelected = { selectedRadiusMeters = it },
                    onCoordinatesResolved = { lat, lng, placeLabel, _ ->
                        resolvedLat = lat
                        resolvedLng = lng
                        hasResolvedCoords = true
                        if (initialBranchName == "Sede Central" && !placeLabel.isNullOrBlank()) {
                            initialBranchName = placeLabel
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (companyName.isNotBlank()) {
                        val newCompany = CorporateClientAccount(
                            id = "corp-${System.currentTimeMillis() % 10000}",
                            name = companyName.trim(),
                            rut = rut.ifBlank { "21.000.000.0000" },
                            planName = planName,
                            clientColorHex = 0xFF0D284E,
                            logoUri = selectedLogoUri,
                            branches = listOf(
                                ClientBranch(
                                    id = "branch-01",
                                    name = initialBranchName.ifBlank { "Sede Principal" },
                                    address = initialBranchAddress.ifBlank { "Montevideo, Uruguay" },
                                    contactPerson = "Administración",
                                    phone = "+598 99 100 200",
                                    lat = resolvedLat,
                                    lng = resolvedLng,
                                    geofenceRadiusMeters = selectedRadiusMeters,
                                    googleMapsUrl = mapsLinkInput.trim()
                                )
                            )
                        )
                        onCompanyAdded(newCompany)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar Empresa", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = GrisTextoSecundario)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Blanco
    )
}

/**
 * Diálogo / Panel de Gestión Integral y Reactiva de Personal (Técnicos y Funcionarias).
 * - Lista de tarjetas limpias con avatar, rol ([Técnico] / [Limpieza]), estado y botones de baja o llamada.
 * - Botón minimalista "+ Nuevo Colaborador" con formulario de pocos pasos: Nombre, Rol, Teléfono, Contacto de Emergencia.
 * - Sincronización en tiempo real en toda la app.
 */
@Composable
fun StaffDirectoryDialog(
    collaborators: List<StaffCollaborator>,
    onDismiss: () -> Unit,
    onCallCollaborator: (String) -> Unit,
    onAddCollaborator: (StaffCollaborator) -> Unit,
    onToggleStatus: (String) -> Unit,
    onDeleteCollaborator: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("TODOS") } // "TODOS", "TECNICOS", "LIMPIEZA"
    var showAddCollaboratorModal by remember { mutableStateOf(false) }

    val filteredList = when (selectedFilter) {
        "TECNICOS" -> collaborators.filter { it.roleCategory == "Técnico" }
        "LIMPIEZA" -> collaborators.filter { it.roleCategory == "Limpieza" }
        else -> collaborators
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Blanco,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Cabecera del Panel de Personal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Equipo y Personal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "${collaborators.count { it.isActive }} activos de ${collaborators.size} registrados",
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
                        Icon(Icons.Rounded.Close, contentDescription = "Cerrar", tint = AzulMarca)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Barra con Filtros y Botón "+ Nuevo Colaborador"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segmentos de filtro
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            Triple("TODOS", "Todos", null),
                            Triple("TECNICOS", "Técnicos", Icons.Rounded.Engineering),
                            Triple("LIMPIEZA", "Limpieza", Icons.Rounded.CleaningServices)
                        ).forEach { (key, label, icon) ->
                            val isSelected = selectedFilter == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AzulMarca else GrisNeutro,
                                border = BorderStroke(1.dp, if (isSelected) AzulMarca else GrisBorde),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedFilter = key }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) Blanco else AzulCorporativo,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Blanco else AzulCorporativo,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Botón "+ Nuevo"
                    Button(
                        onClick = { showAddCollaboratorModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Blanco, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Nuevo",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lista reactiva de colaboradores
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredList.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonAdd,
                                    contentDescription = null,
                                    tint = GrisTextoSecundario.copy(alpha = 0.55f),
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = "Sin colaboradores aún. Toca Nuevo para comenzar",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.5.sp,
                                    color = GrisTextoSecundario,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    items(filteredList, key = { it.id }) { staff ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (staff.isActive) Blanco else GrisNeutro.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (staff.isActive) GrisBorde else GrisBorde.copy(alpha = 0.5f)),
                            shadowElevation = if (staff.isActive) 2.dp else 0.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Avatar con inicial
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (staff.roleCategory == "Técnico") CelesteAcentoClaro else VerdeExitoFondo
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = staff.fullName.firstOrNull()?.toString() ?: "C",
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (staff.roleCategory == "Técnico") AzulMarca else VerdeExito,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = staff.fullName,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (staff.isActive) AzulMarca else GrisTextoSecundario,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                            Text(
                                                text = staff.jobTitle,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    color = GrisTextoSecundario,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    // Badge de Rol [Técnico] / [Limpieza]
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (staff.roleCategory == "Técnico") CelesteAcentoClaro else VerdeExitoFondo
                                    ) {
                                        Text(
                                            text = staff.roleCategory,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = if (staff.roleCategory == "Técnico") AzulCorporativo else VerdeExito,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Tel: ${staff.phone}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = AzulCorporativo,
                                                fontSize = 11.sp
                                            )
                                        )
                                        if (staff.emergencyContact.isNotBlank()) {
                                            Text(
                                                text = "Emergencia: ${staff.emergencyContact}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    color = GrisTextoSecundario,
                                                    fontSize = 10.sp
                                                )
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Estado Activo/Baja
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (staff.isActive) VerdeExitoFondo else GrisNeutro,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onToggleStatus(staff.id) }
                                        ) {
                                            Text(
                                                text = if (staff.isActive) "Activo" else "De baja",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (staff.isActive) VerdeExito else GrisTextoSecundario,
                                                    fontSize = 10.5.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        // Llamar
                                        IconButton(
                                            onClick = { onCallCollaborator(staff.phone) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Call,
                                                contentDescription = "Llamar",
                                                tint = VerdeExito,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }

                                        // Eliminar
                                        IconButton(
                                            onClick = { onDeleteCollaborator(staff.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Eliminar",
                                                tint = RojoUrgencia,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Cerrar Directorio", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal formulario "+ Nuevo Colaborador"
    if (showAddCollaboratorModal) {
        var newName by remember { mutableStateOf("") }
        var newRoleCategory by remember { mutableStateOf("Técnico") }
        var newJobTitle by remember { mutableStateOf("") }
        var newPhone by remember { mutableStateOf("+598 ") }
        var newEmergency by remember { mutableStateOf("+598 ") }

        AlertDialog(
            onDismissRequest = { showAddCollaboratorModal = false },
            title = {
                Text(
                    text = "Nuevo Colaborador",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selector de Rol: [ Técnico ] / [ Limpieza ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Técnico", "Limpieza").forEach { role ->
                            val isSelected = newRoleCategory == role
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AzulMarca else GrisNeutro,
                                border = BorderStroke(1.dp, if (isSelected) AzulMarca else GrisBorde),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        newRoleCategory = role
                                        if (newJobTitle.isBlank()) {
                                            newJobTitle = if (role == "Técnico") "Técnico Mantenimiento" else "Personal de Limpieza"
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = role,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Blanco else AzulCorporativo,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre Completo") },
                        placeholder = { Text("ej. Rodrigo Martínez") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newJobTitle,
                        onValueChange = { newJobTitle = it },
                        label = { Text("Especialidad / Cargo") },
                        placeholder = { Text(if (newRoleCategory == "Técnico") "ej. Electricista Matriculado" else "ej. Limpieza y Sanitización") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Teléfono de Contacto") },
                        placeholder = { Text("+598 99 123 456") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newEmergency,
                        onValueChange = { newEmergency = it },
                        label = { Text("Contacto de Emergencia") },
                        placeholder = { Text("+598 98 765 432") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newPhone.isNotBlank()) {
                            val newColab = StaffCollaborator(
                                id = "colab-${System.currentTimeMillis() % 10000}",
                                fullName = newName.trim(),
                                phone = newPhone.trim(),
                                email = "${newName.lowercase().replace(" ", ".")}@todoservicio.com.uy",
                                jobTitle = if (newJobTitle.isNotBlank()) newJobTitle.trim() else newRoleCategory,
                                userType = "Empleado",
                                lastCheckIn = "Recién incorporado",
                                isActive = true,
                                roleCategory = newRoleCategory,
                                emergencyContact = newEmergency.trim(),
                                permissions = listOf("Órdenes de Trabajo", "Check-in GPS")
                            )
                            onAddCollaborator(newColab)
                            showAddCollaboratorModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeExito),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar Colaborador", fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCollaboratorModal = false }) {
                    Text("Cancelar", fontFamily = PoppinsFontFamily)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Blanco
        )
    }
}
