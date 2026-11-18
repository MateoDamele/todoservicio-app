package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.testTag
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthRepository
import com.example.data.CleaningRepository
import com.example.data.OperationalRepository
import com.example.model.AppDestination
import com.example.model.ClientBottomTab
import com.example.model.ClientProfile
import com.example.model.ServiceHistoryItem
import com.example.model.UserRole
import com.example.model.isForUserRole
import com.example.model.isUnreadBy
import com.example.ui.components.AnnouncementsInboxModal
import com.example.ui.components.CustomAppDrawer
import com.example.ui.components.FloatingGlassBottomBar
import com.example.ui.components.FloatingGlassNavItem
import com.example.ui.components.RequestAndroidNotificationPermissionEffect
import com.example.ui.components.TopAppBarTodoservicio
import com.example.ui.components.modalBackdropBlur
import com.example.ui.screens.AdminCreateClientScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CleaningDashboardScreen
import com.example.ui.screens.ClientNotificationsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IdentityDetailsDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MonthlyPlanScreen
import com.example.ui.screens.RequestWorkScreen
import com.example.ui.screens.ServiceHistoryScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TechOrdersScreen
import com.example.ui.screens.TechnicalReportDialog
import com.example.ui.screens.TrackingAndPdfReportScreen
import com.example.ui.screens.UpdatePasswordScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.MonthlyGridCalendarScreen
import com.example.ui.screens.GeneralGpsMapSection
import com.example.ui.screens.CreateProfileBottomSheet
import com.example.model.MobileBottomTab
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.TodoservicioTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OperationalRepository.init(this)
        CleaningRepository.init(this)
        AuthRepository.init(this)
        enableEdgeToEdge()
        setContent {
            TodoservicioTheme {
                TodoservicioAppRouter()
            }
        }
    }
}

/**
 * Enrutador principal inteligente con RBAC y manejo de sesión.
 */
@Composable
fun TodoservicioAppRouter() {
    val context = LocalContext.current
    val currentUser by AuthRepository.currentUser.collectAsStateWithLifecycle()
    var showSplash by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSplash && currentUser == null) {
            SplashScreen(
                onTimeout = { showSplash = false }
            )
        } else {
            Crossfade(
                targetState = currentUser,
                animationSpec = tween(durationMillis = 220, easing = EaseOutCubic),
                label = "authRouter"
            ) { user ->
                when {
                    // 1. Sin sesión activa -> Pantalla de Inicio de Sesión
                    user == null -> {
                        LoginScreen(
                            onLoginSuccess = { /* Estado administrado en AuthRepository */ }
                        )
                    }

                    // 2. Clave temporal obligatoria de actualizar antes de acceder
                    user.isTemporaryPassword -> {
                        UpdatePasswordScreen(
                            user = user,
                            onPasswordUpdated = { /* AuthRepository actualiza la bandera isTemporaryPassword a false */ },
                            onLogout = { AuthRepository.logout() }
                        )
                    }

                    // 3. Rol ADMIN o SUPERADMIN -> Panel de Administración
                    user.role == UserRole.ADMIN || user.role == UserRole.SUPERADMIN -> {
                        AdminFlowContainer(
                            currentUser = user,
                            onLogout = { AuthRepository.logout() }
                        )
                    }

                    // 4. Rol TÉCNICO -> Portal de Órdenes de Trabajo en Calle
                    user.role == UserRole.TECNICO -> {
                        TechOrdersScreen(
                            currentUser = user,
                            onLogout = { AuthRepository.logout() }
                        )
                    }

                    // 5. Rol CLIENTE -> Portal Personal del Cliente
                    user.role == UserRole.CLIENTE -> {
                        ClientPortalContainer(
                            currentUser = user,
                            onLogout = { AuthRepository.logout() }
                        )
                    }

                    // 6. Rol LIMPIEZA -> Dashboard de la Funcionaria de Limpieza (Lenox HR)
                    user.role == UserRole.LIMPIEZA -> {
                        CleaningDashboardScreen(
                            currentUser = user,
                            onLogout = { AuthRepository.logout() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Flujo de pantallas para Administradores (Dashboard general y Alta Manual de Clientes).
 */
@Composable
private fun AdminFlowContainer(
    currentUser: com.example.model.UserAccount,
    onLogout: () -> Unit
) {
    var showCreateClientScreen by remember { mutableStateOf(false) }

    BackHandler(enabled = showCreateClientScreen) {
        showCreateClientScreen = false
    }

    Crossfade(
        targetState = showCreateClientScreen,
        animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
        label = "adminFlow"
    ) { isCreateScreen ->
        if (isCreateScreen) {
            AdminCreateClientScreen(
                onNavigateBack = { showCreateClientScreen = false }
            )
        } else {
            AdminDashboardScreen(
                currentUser = currentUser,
                onNavigateToCreateClient = { showCreateClientScreen = true },
                onLogout = onLogout
            )
        }
    }
}

/**
 * Portal del Cliente con Menú Lateral (Drawer) y navegación modular.
 */
@Composable
private fun ClientPortalContainer(
    currentUser: com.example.model.UserAccount,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    var currentDestination by remember { mutableStateOf(AppDestination.INICIO) }

    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    val allClientNotifications by OperationalRepository.clientNotifications.collectAsStateWithLifecycle()
    val allAnnouncements by OperationalRepository.announcements.collectAsStateWithLifecycle()

    RequestAndroidNotificationPermissionEffect()

    val roleAnnouncements = remember(allAnnouncements, currentUser.role) {
        allAnnouncements.filter { it.isForUserRole(currentUser.role) }
    }
    val unreadAnnouncementsCount = remember(roleAnnouncements, currentUser.uid) {
        roleAnnouncements.count { it.isUnreadBy(currentUser.uid) }
    }

    val resolvedClientId = remember(currentUser, corporateClients) {
        currentUser.clientId?.takeIf { it.isNotBlank() }
            ?: corporateClients.firstOrNull {
                it.name.equals(currentUser.companyName, ignoreCase = true) ||
                    currentUser.companyName.contains(it.name, ignoreCase = true)
            }?.id
            ?: "client-${currentUser.companyName.lowercase().replace(" ", "-")}"
    }

    val matchedCompany = remember(resolvedClientId, corporateClients) {
        corporateClients.firstOrNull { it.id == resolvedClientId }
    }
    val companyBranches = remember(matchedCompany) {
        matchedCompany?.branches?.map {
            if (it.address.isNotBlank()) "${it.name} - ${it.address}" else it.name
        } ?: emptyList()
    }
    var activeBranch by remember(companyBranches) {
        mutableStateOf(companyBranches.firstOrNull().orEmpty())
    }

    val inheritedLogoUri = remember(resolvedClientId, currentUser.avatarUrl, matchedCompany) {
        currentUser.avatarUrl ?: matchedCompany?.logoUri
    }

    val clientProfile = remember(currentUser, resolvedClientId, inheritedLogoUri, matchedCompany, activeBranch) {
        ClientProfile(
            clientId = resolvedClientId,
            clientName = currentUser.fullName,
            companyName = currentUser.companyName,
            role = currentUser.role.label,
            address = activeBranch.ifBlank {
                matchedCompany?.branches?.firstOrNull()?.address
                    ?: currentUser.headquarters.firstOrNull().orEmpty()
            },
            rut = currentUser.rut,
            planType = if (currentUser.monthlyPlanId != null) "Plan Corporativo" else null,
            visitsThisMonth = 0,
            hasActiveVisit = false,
            logoUri = inheritedLogoUri
        )
    }

    var showIdentityDialog by remember { mutableStateOf(false) }
    var selectedReportForDialog by remember { mutableStateOf<ServiceHistoryItem?>(null) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val notifications = remember(allClientNotifications, resolvedClientId, currentUser.companyName) {
        allClientNotifications.filter { notif ->
            (notif.targetClientId.isNotBlank() && notif.targetClientId == resolvedClientId) ||
                (notif.targetClientName.isNotBlank() && notif.targetClientName.equals(currentUser.companyName, ignoreCase = true))
        }
    }

    BackHandler(enabled = drawerState.isOpen || currentDestination != AppDestination.INICIO) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentDestination = AppDestination.INICIO
        }
    }

    val isAnyClientModalOpen = showNotificationsDialog || showIdentityDialog || selectedReportForDialog != null

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .modalBackdropBlur(isAnyClientModalOpen)
    ) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Pantalla ancha (Tablet / PC / Web Responsiva)
            Row(modifier = Modifier.fillMaxSize()) {
                CustomAppDrawer(
                    currentDestination = currentDestination,
                    clientProfile = clientProfile,
                    currentUser = currentUser,
                    onDestinationSelected = { destination ->
                        currentDestination = destination
                    },
                    onLogout = onLogout,
                    modifier = Modifier.width(340.dp)
                )

                Scaffold(
                    topBar = {
                        TopAppBarTodoservicio(
                            title = "TODOSERVICIO",
                            subtitle = when (currentUser.role) {
                                UserRole.ADMIN, UserRole.SUPERADMIN -> "Panel de Operaciones"
                                UserRole.TECNICO -> "Modo Técnico de Campo"
                                else -> currentUser.companyName.ifBlank { "Portal Cliente" }
                            },
                            companyGreeting = if (currentUser.companyName.isNotBlank()) "¡Hola, ${currentUser.companyName}!" else "¡Hola, ${currentUser.fullName}!",
                            selectedBranch = activeBranch,
                            availableBranches = companyBranches,
                            onBranchChange = { activeBranch = it },
                            hasUnreadNotifications = notifications.any { it.isUnread } || unreadAnnouncementsCount > 0,
                            unreadNotificationCount = notifications.count { it.isUnread } + unreadAnnouncementsCount,
                            onNotificationsClick = { showNotificationsDialog = true },
                            onMenuClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Navegación fija activa en pantalla ancha")
                                }
                            },
                            userFirstName = currentUser.fullName,
                            userAvatarUrl = inheritedLogoUri
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = GrisNeutro
                ) { innerPadding ->
                    ClientScreensRouter(
                        currentDestination = currentDestination,
                        clientProfile = clientProfile,
                        onNavigate = { currentDestination = it },
                        onOpenIdentityInfo = { showIdentityDialog = true },
                        onViewReport = { selectedReportForDialog = it },
                        onEmergencyCall = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+59899000111")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        } else {
            // Dispositivos móviles estándar
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = true,
                scrimColor = Color(0x66102C54),
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.Transparent,
                        drawerTonalElevation = 0.dp
                    ) {
                        CustomAppDrawer(
                            currentDestination = currentDestination,
                            clientProfile = clientProfile,
                            currentUser = currentUser,
                            onDestinationSelected = { destination ->
                                currentDestination = destination
                                coroutineScope.launch { drawerState.close() }
                            },
                            onLogout = onLogout
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            if (currentDestination != AppDestination.CALENDARIO) {
                                TopAppBarTodoservicio(
                                    title = "TODOSERVICIO",
                                    subtitle = when (currentUser.role) {
                                        UserRole.ADMIN, UserRole.SUPERADMIN -> "Panel de Operaciones"
                                        UserRole.TECNICO -> "Modo Técnico de Campo"
                                        else -> currentUser.companyName.ifBlank { "Portal Cliente" }
                                    },
                                    companyGreeting = if (currentUser.companyName.isNotBlank()) "¡Hola, ${currentUser.companyName}!" else "¡Hola, ${currentUser.fullName}!",
                                    selectedBranch = activeBranch,
                                    availableBranches = companyBranches,
                                    onBranchChange = { activeBranch = it },
                                    hasUnreadNotifications = notifications.any { it.isUnread } || unreadAnnouncementsCount > 0,
                                    unreadNotificationCount = notifications.count { it.isUnread } + unreadAnnouncementsCount,
                                    onNotificationsClick = { showNotificationsDialog = true },
                                    onMenuClick = {
                                        coroutineScope.launch {
                                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                        }
                                    },
                                    userFirstName = currentUser.fullName,
                                    userAvatarUrl = inheritedLogoUri
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = GrisNeutro
                    ) { innerPadding ->
                        ClientScreensRouter(
                            currentDestination = currentDestination,
                            clientProfile = clientProfile,
                            onNavigate = { currentDestination = it },
                            onOpenIdentityInfo = { showIdentityDialog = true },
                            onViewReport = { selectedReportForDialog = it },
                            onEmergencyCall = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:+59899000111")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding())
                        )
                    }

                    val clientGlassItems = remember {
                        ClientBottomTab.values().map { tab ->
                            FloatingGlassNavItem(
                                key = tab.destination,
                                label = tab.label,
                                icon = tab.icon,
                                testTag = tab.testTag
                            )
                        }
                    }
                    FloatingGlassBottomBar(
                        items = clientGlassItems,
                        selectedKey = currentDestination,
                        onItemSelected = { currentDestination = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }

    // Diálogos de Misión/Visión, Notificaciones/Avisos y Reportes
    if (showNotificationsDialog) {
        AnnouncementsInboxModal(
            currentUser = currentUser,
            announcements = roleAnnouncements,
            extraAlerts = notifications,
            onDismiss = {
                OperationalRepository.markClientNotificationsRead(resolvedClientId, currentUser.companyName)
                showNotificationsDialog = false
            }
        )
    }

    if (showIdentityDialog) {
        IdentityDetailsDialog(onDismiss = { showIdentityDialog = false })
    }

    selectedReportForDialog?.let { report ->
        TechnicalReportDialog(
            item = report,
            onDismiss = { selectedReportForDialog = null }
        )
    }
}

@Composable
private fun ClientScreensRouter(
    currentDestination: AppDestination,
    clientProfile: ClientProfile,
    onNavigate: (AppDestination) -> Unit,
    onOpenIdentityInfo: () -> Unit,
    onViewReport: (ServiceHistoryItem) -> Unit,
    onEmergencyCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = currentDestination,
        animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
        modifier = modifier,
        label = "clientScreensRouter"
    ) { destination ->
        when (destination) {
            AppDestination.INICIO -> HomeScreen(
                clientProfile = clientProfile,
                onNavigate = onNavigate,
                onOpenIdentityInfo = onOpenIdentityInfo,
                onViewReport = onViewReport
            )
            AppDestination.PLAN_MENSUAL -> MonthlyPlanScreen(
                clientProfile = clientProfile,
                onNavigateToRequestWork = { onNavigate(AppDestination.SOLICITAR_TRABAJO) },
                onViewReport = onViewReport
            )
            AppDestination.SOLICITAR_TRABAJO -> RequestWorkScreen(
                clientProfile = clientProfile,
                onEmergencyCall = onEmergencyCall
            )
            AppDestination.HISTORIAL_SERVICIOS -> TrackingAndPdfReportScreen(
                clientProfile = clientProfile,
                initialTab = 2,
                onViewReport = onViewReport
            )
            AppDestination.SOPORTE_CONSULTAS -> SupportScreen(
                clientProfile = clientProfile
            )
            AppDestination.CALENDARIO -> MonthlyGridCalendarScreen(
                clientProfile = clientProfile,
                onNavigateToGps = { onNavigate(AppDestination.MAPA_GPS) },
                onOpenProfile = onOpenIdentityInfo
            )
            AppDestination.DOBLE_FIRMA -> TrackingAndPdfReportScreen(
                clientProfile = clientProfile,
                initialTab = 1,
                onViewReport = onViewReport
            )
            AppDestination.MAPA_GPS -> TrackingAndPdfReportScreen(
                clientProfile = clientProfile,
                initialTab = 0,
                onViewReport = onViewReport
            )
            AppDestination.ACCESOS -> HomeScreen(
                clientProfile = clientProfile,
                onNavigate = onNavigate,
                onOpenIdentityInfo = onOpenIdentityInfo,
                onViewReport = onViewReport
            )
        }
    }
}
