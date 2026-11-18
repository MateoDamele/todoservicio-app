package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuthRepository
import com.example.data.LocalDatabaseStore
import com.example.data.OperationalRepository
import com.example.model.ClientBranch
import com.example.model.CorporateClientAccount
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.ModalBlurScrim
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.SombraDifusaColor
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

private enum class AccessFilterTab(val label: String) {
    TODOS("Todos"),
    CLIENTES("Clientes"),
    TECNICOS("Técnicos"),
    LIMPIEZA("Limpieza")
}

/**
 * Decodifica una URI seleccionada vía Photo Picker a ImageBitmap de forma segura.
 */
@Composable
private fun rememberUriImageBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            try {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Módulo Administrativo Minimalista de Gestión de Accesos y Perfiles.
 * - Filtros en píldoras: [ Todos ] • [ Clientes ] • [ Técnicos ] • [ Limpieza ].
 * - Tarjetas compactas con avatar circular, rol, estado y botón [ ✏️ ] táctil >= 48dp.
 */
@Composable
fun AdminAccessManagementSection(
    onOpenCreateProfile: () -> Unit,
    onEditProfile: (UserAccount) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accounts by AuthRepository.accountsState.collectAsStateWithLifecycle()
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf(AccessFilterTab.TODOS) }

    val filteredAccounts = remember(accounts, selectedFilter) {
        when (selectedFilter) {
            AccessFilterTab.TODOS -> accounts
            AccessFilterTab.CLIENTES -> accounts.filter { it.role == UserRole.CLIENTE }
            AccessFilterTab.TECNICOS -> accounts.filter { it.role == UserRole.TECNICO }
            AccessFilterTab.LIMPIEZA -> accounts.filter { it.role == UserRole.LIMPIEZA }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera compacta + CTA primario
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Accesos",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = GrisGrafito,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "${accounts.size} perfiles",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onOpenCreateProfile,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzulMarca,
                        contentColor = Blanco
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_open_create_profile_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nuevo Perfil",
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Pestañas de filtro rápido en píldoras
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccessFilterTab.values().forEach { tab ->
                    val isSelected = selectedFilter == tab
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AzulMarca else Blanco,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) AzulMarca else GrisBorde
                        ),
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedFilter = tab }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = tab.label,
                                fontFamily = PoppinsFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 12.5.sp,
                                color = if (isSelected) Blanco else GrisGrafito
                            )
                        }
                    }
                }
            }
        }

        // Tarjetas de usuario compactas y aireadas o Empty State
        if (filteredAccounts.isEmpty()) {
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = GrisTextoSecundario.copy(alpha = 0.55f),
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "Sin registros aún. Toca + Nuevo Perfil para comenzar",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = GrisTextoSecundario,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredAccounts, key = { it.uid }) { account ->
            val linkedCompanyLogo = remember(account, corporateClients) {
                if (account.role == UserRole.CLIENTE) {
                    corporateClients.firstOrNull {
                        it.id == account.clientId || it.name.equals(account.companyName, ignoreCase = true)
                    }?.logoUri
                } else null
            }
            val effectiveAvatarUri = account.avatarUrl ?: linkedCompanyLogo
            val avatarBitmap = rememberUriImageBitmap(effectiveAvatarUri)

            val roleShortLabel = when (account.role) {
                UserRole.ADMIN, UserRole.SUPERADMIN -> "Admin"
                UserRole.TECNICO -> "Técnico"
                UserRole.CLIENTE -> "Cliente"
                UserRole.LIMPIEZA -> "Limpieza"
            }

            val roleBg = when (account.role) {
                UserRole.ADMIN, UserRole.SUPERADMIN -> CelesteAcentoClaro
                UserRole.TECNICO -> Color(0xFFE0F2FE)
                UserRole.CLIENTE -> VerdeExitoFondo
                UserRole.LIMPIEZA -> Color(0xFFE6F4F1)
            }

            val roleFg = when (account.role) {
                UserRole.ADMIN, UserRole.SUPERADMIN -> AzulMarca
                UserRole.TECNICO -> Color(0xFF0369A1)
                UserRole.CLIENTE -> VerdeExito
                UserRole.LIMPIEZA -> Color(0xFF0F766E)
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = SombraDifusaColor,
                        spotColor = SombraDifusaColor
                    )
                    .clickable { onEditProfile(account) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar circular (foto cargada o logo heredado o icono limpio)
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap,
                            contentDescription = account.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .border(1.dp, GrisBorde, CircleShape)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = roleBg,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (account.role) {
                                        UserRole.ADMIN, UserRole.SUPERADMIN -> Icons.Rounded.AdminPanelSettings
                                        UserRole.TECNICO -> Icons.Rounded.Engineering
                                        UserRole.CLIENTE -> Icons.Rounded.Apartment
                                        UserRole.LIMPIEZA -> Icons.Rounded.CleaningServices
                                    },
                                    contentDescription = null,
                                    tint = roleFg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Datos centrales (Nombre + Rol + Estado + Usuario/Empresa)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = account.fullName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrisGrafito,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = roleBg
                            ) {
                                Text(
                                    text = roleShortLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = roleFg,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val statusColor = if (account.isActive) VerdeExito else RojoUrgencia
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Text(
                                text = if (account.isActive) "Activa" else "Suspendida",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                            Text(
                                text = "• @${account.username.ifBlank { account.email.substringBefore("@") }}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = GrisTextoSecundario,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (account.role == UserRole.CLIENTE && account.companyName.isNotBlank()) {
                            Text(
                                text = account.companyName,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = GrisTextoSecundario,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (account.specialty.isNotBlank()) {
                            Text(
                                text = account.specialty,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = GrisTextoSecundario,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Botón de edición táctil >= 48dp
                    IconButton(
                        onClick = { onEditProfile(account) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GrisNeutro)
                            .testTag("btn_edit_profile_${account.uid}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar ${account.fullName}",
                            tint = AzulMarca,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

/**
 * BottomSheet modal unificado para Crear y Editar Perfiles de Acceso.
 * - Chips de rol táctiles: [ Cliente ] | [ Técnico ] | [ Limpieza ] | [ Admin ].
 * - Carga visual sin permisos vía Photo Picker (foto de perfil o logo corporativo heredado).
 * - Credenciales con generador seguro de 1 toque + Switch Activa / Suspendida.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProfileBottomSheet(
    existingAccount: UserAccount? = null,
    onDismiss: () -> Unit,
    onProfileCreated: (UserAccount) -> Unit
) {
    val isEditMode = existingAccount != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val corporateClients by OperationalRepository.corporateClients.collectAsStateWithLifecycle()

    var selectedRole by remember(existingAccount) {
        mutableStateOf(
            when (existingAccount?.role) {
                UserRole.SUPERADMIN -> UserRole.ADMIN
                null -> UserRole.CLIENTE
                else -> existingAccount.role
            }
        )
    }

    // Campos comunes
    var fullNameInput by remember(existingAccount) { mutableStateOf(existingAccount?.fullName ?: "") }
    var username by remember(existingAccount) { mutableStateOf(existingAccount?.username ?: "") }
    var password by remember(existingAccount) { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isAccountActive by remember(existingAccount) { mutableStateOf(existingAccount?.isActive ?: true) }
    var phone by remember(existingAccount) { mutableStateOf(existingAccount?.phone ?: "") }
    var avatarUri by remember(existingAccount) { mutableStateOf(existingAccount?.avatarUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Campos Cliente / Empresa vinculada
    val initialLinkedCompany = remember(existingAccount, corporateClients) {
        if (existingAccount?.role == UserRole.CLIENTE) {
            corporateClients.firstOrNull {
                it.id == existingAccount.clientId || it.name.equals(existingAccount.companyName, ignoreCase = true)
            }
        } else null
    }
    var linkedCompany by remember(existingAccount) { mutableStateOf<CorporateClientAccount?>(initialLinkedCompany) }
    var companyName by remember(existingAccount) { mutableStateOf(existingAccount?.companyName ?: "") }
    var rut by remember(existingAccount) { mutableStateOf(existingAccount?.rut ?: "") }
    val selectedSedes = remember(existingAccount) {
        mutableStateListOf<String>().apply {
            val existing = existingAccount?.headquarters.orEmpty()
            if (existing.isNotEmpty()) addAll(existing) else add("Montevideo")
        }
    }
    var planVisits by remember(existingAccount) {
        mutableIntStateOf(if (existingAccount?.monthlyPlanId == "plan_corp_8") 8 else 4)
    }
    var isCompanyDropdownOpen by remember { mutableStateOf(false) }

    // Especialidad (Técnico / Limpieza)
    var specialty by remember(existingAccount) {
        mutableStateOf(existingAccount?.specialty?.ifBlank { "Electricidad" } ?: "Electricidad")
    }

    val specialtyOptions = if (selectedRole == UserRole.LIMPIEZA) {
        listOf("Limpieza Integral", "Sanitización", "Vidrios y Altura", "Mantenimiento Diario")
    } else {
        listOf("Electricidad", "Sanitaria", "Climatización", "Integral")
    }
    val sedeOptions = listOf("Montevideo", "Canelones")

    // Photo Picker cero-permisos
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistedUri = LocalDatabaseStore.persistPickedImageUri(context, uri)
            avatarUri = persistedUri
            if (selectedRole == UserRole.CLIENTE && linkedCompany != null) {
                OperationalRepository.updateCorporateClientLogo(linkedCompany!!.id, persistedUri)
            }
        }
    }

    val effectivePreviewUri = avatarUri ?: linkedCompany?.logoUri
    val previewBitmap = rememberUriImageBitmap(effectivePreviewUri)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Blanco,
        scrimColor = ModalBlurScrim,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Editar Perfil" else "Nuevo Perfil",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GrisGrafito,
                        fontSize = 18.sp
                    )
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = GrisTextoSecundario
                    )
                }
            }

            // Selector de Rol (4 roles)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Rol",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = GrisGrafito
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val roles = listOf(
                        Triple(UserRole.CLIENTE, "Cliente", Icons.Rounded.Apartment),
                        Triple(UserRole.TECNICO, "Técnico", Icons.Rounded.Engineering),
                        Triple(UserRole.LIMPIEZA, "Limpieza", Icons.Rounded.CleaningServices),
                        Triple(UserRole.ADMIN, "Admin", Icons.Rounded.AdminPanelSettings)
                    )

                    roles.forEach { (role, label, icon) ->
                        val isSelected = selectedRole == role
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CelesteAcentoClaro else GrisNeutro,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AzulMarca else GrisBorde
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedRole = role
                                    errorMessage = null
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) AzulMarca else GrisTextoSecundario,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AzulMarca else GrisTextoSecundario,
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de Error
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RojoUrgenciaFondo,
                        border = BorderStroke(1.dp, RojoUrgencia.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = RojoUrgencia,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Identidad visual (Foto de perfil o Logo heredado)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GrisNeutro,
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap,
                            contentDescription = "Vista previa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, AzulMarca, CircleShape)
                        )
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = CelesteAcentoClaro,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (selectedRole == UserRole.CLIENTE) Icons.Rounded.Apartment else Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = AzulMarca,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (selectedRole == UserRole.CLIENTE) "Logo Corporativo" else "Foto de Perfil",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = GrisGrafito
                        )
                        Text(
                            text = if (effectivePreviewUri != null) "Imagen cargada" else if (selectedRole == UserRole.CLIENTE) "Heredado de empresa" else "Opcional",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AzulMarca),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = null,
                            tint = AzulMarca,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedRole == UserRole.CLIENTE) "Subir Logo" else "Subir Foto",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = AzulMarca
                        )
                    }
                }
            }

            // Campos específicos por rol
            if (selectedRole == UserRole.CLIENTE) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Dropdown "Vincular a Empresa"
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisNeutro,
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isCompanyDropdownOpen = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Vincular a Empresa",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = GrisTextoSecundario
                                    )
                                    Text(
                                        text = linkedCompany?.name ?: companyName.ifBlank { "Seleccionar empresa" },
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = GrisGrafito,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.ExpandMore,
                                    contentDescription = "Seleccionar empresa",
                                    tint = GrisTextoSecundario
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isCompanyDropdownOpen,
                            onDismissRequest = { isCompanyDropdownOpen = false }
                        ) {
                            corporateClients.forEach { corp ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = corp.name,
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = GrisGrafito
                                        )
                                    },
                                    onClick = {
                                        linkedCompany = corp
                                        companyName = corp.name
                                        rut = corp.rut
                                        if (corp.logoUri != null && avatarUri == null) {
                                            avatarUri = corp.logoUri
                                        }
                                        isCompanyDropdownOpen = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "+ Nueva Empresa",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulMarca
                                    )
                                },
                                onClick = {
                                    linkedCompany = null
                                    isCompanyDropdownOpen = false
                                }
                            )
                        }
                    }

                    if (linkedCompany == null) {
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Empresa", fontFamily = PoppinsFontFamily) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        label = { Text("Contacto Titular", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = rut,
                        onValueChange = { rut = it },
                        label = { Text("RUT", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sedeOptions.forEach { sede ->
                            val isChecked = selectedSedes.contains(sede)
                            FilterChip(
                                selected = isChecked,
                                onClick = {
                                    if (isChecked && selectedSedes.size > 1) selectedSedes.remove(sede)
                                    else if (!isChecked) selectedSedes.add(sede)
                                },
                                label = { Text(sede, fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                                leadingIcon = if (isChecked) {
                                    { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CelesteAcentoClaro,
                                    selectedLabelColor = AzulMarca
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4 to "Pyme (4)", 8 to "Corporativo (8)").forEach { (visits, label) ->
                            val isSelected = planVisits == visits
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CelesteAcentoClaro else GrisNeutro,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AzulMarca else GrisBorde
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { planVisits = visits }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) AzulMarca else GrisTextoSecundario
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = fullNameInput,
                        onValueChange = { fullNameInput = it },
                        label = { Text("Nombre Completo", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedRole == UserRole.TECNICO || selectedRole == UserRole.LIMPIEZA) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            specialtyOptions.forEach { spec ->
                                val isSelected = specialty == spec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { specialty = spec },
                                    label = { Text(spec, fontFamily = PoppinsFontFamily, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CelesteAcentoClaro,
                                        selectedLabelColor = AzulMarca
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Credenciales de acceso
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GrisNeutro,
                border = BorderStroke(1.dp, GrisBorde),
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
                        Text(
                            text = "Credenciales",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = GrisGrafito
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CelesteAcentoClaro,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    password = AuthRepository.generateSecureTemporaryPassword()
                                    isPasswordVisible = true
                                    errorMessage = null
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = null,
                                    tint = AzulMarca,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Generar Clave",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = AzulMarca
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Nombre de Usuario", fontFamily = PoppinsFontFamily) },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = AzulMarca) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_profile_username")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = {
                            Text(
                                text = if (isEditMode) "Nueva Contraseña (opcional)" else "Nueva Contraseña",
                                fontFamily = PoppinsFontFamily
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = AzulMarca) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = AzulMarca
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Blanco,
                            unfocusedContainerColor = Blanco
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_profile_password")
                    )

                    if (!isEditMode) {
                        Text(
                            text = "Contraseña provisoria: se exigirá cambio en el primer ingreso.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = GrisTextoSecundario
                        )
                    }
                }
            }

            // Switch Cuenta Activa / Suspendida
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = GrisNeutro,
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isAccountActive) "Cuenta Activa" else "Cuenta Suspendida",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (isAccountActive) VerdeExito else RojoUrgencia
                        )
                    }
                    Switch(
                        checked = isAccountActive,
                        onCheckedChange = { isAccountActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Blanco,
                            checkedTrackColor = VerdeExito,
                            uncheckedThumbColor = Blanco,
                            uncheckedTrackColor = RojoUrgencia.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            // Botón Guardar Cambios
            Button(
                onClick = {
                    if (username.isBlank()) {
                        errorMessage = "Ingrese un Nombre de Usuario."
                        return@Button
                    }
                    if (!isEditMode && password.length < 6) {
                        errorMessage = "La contraseña debe tener al menos 6 caracteres."
                        return@Button
                    }
                    if (isEditMode && password.isNotBlank() && password.length < 6) {
                        errorMessage = "La nueva contraseña debe tener al menos 6 caracteres."
                        return@Button
                    }

                    val resolvedFullName = when {
                        fullNameInput.isNotBlank() -> fullNameInput.trim()
                        selectedRole == UserRole.CLIENTE -> companyName.ifBlank { username.trim() }
                        else -> username.trim()
                    }
                    val resolvedCompany = linkedCompany?.name ?: companyName.trim()
                    val resolvedClientId = linkedCompany?.id ?: existingAccount?.clientId

                    val result = if (isEditMode && existingAccount != null) {
                        AuthRepository.updateUserProfile(
                            uid = existingAccount.uid,
                            role = selectedRole,
                            fullName = resolvedFullName,
                            username = username,
                            newPassword = password.takeIf { it.isNotBlank() },
                            companyName = resolvedCompany,
                            clientId = resolvedClientId,
                            rut = rut,
                            headquarters = selectedSedes.toList(),
                            planVisits = planVisits,
                            specialty = specialty,
                            phone = phone,
                            avatarUrl = avatarUri,
                            isActive = isAccountActive
                        )
                    } else {
                        AuthRepository.createCustomProfile(
                            role = selectedRole,
                            fullName = resolvedFullName,
                            username = username,
                            password = password,
                            companyName = resolvedCompany,
                            clientId = resolvedClientId,
                            rut = rut,
                            headquarters = selectedSedes.toList(),
                            planVisits = planVisits,
                            specialty = specialty,
                            phone = phone,
                            avatarUrl = avatarUri
                        )
                    }

                    result.onSuccess { account ->
                        if (!isEditMode && selectedRole == UserRole.CLIENTE && linkedCompany == null && resolvedCompany.isNotBlank()) {
                            val newClientId = account.clientId ?: "client-${resolvedCompany.lowercase().replace(" ", "-")}"
                            OperationalRepository.addCorporateClient(
                                CorporateClientAccount(
                                    id = newClientId,
                                    name = resolvedCompany,
                                    rut = rut.trim(),
                                    planName = if (planVisits == 8) "Plan Corporativo Plus (8 Visitas)" else "Plan Estándar Pyme (4 Visitas)",
                                    clientColorHex = 0xFF335C81,
                                    branches = selectedSedes.mapIndexed { idx, sede ->
                                        ClientBranch(
                                            id = "branch-$newClientId-$idx",
                                            name = sede,
                                            address = sede,
                                            contactPerson = resolvedFullName,
                                            phone = phone.trim()
                                        )
                                    },
                                    logoUri = avatarUri
                                )
                            )
                        }
                        Toast.makeText(
                            context,
                            if (isEditMode) "Perfil '${account.username}' actualizado." else "Perfil '${account.username}' creado.",
                            Toast.LENGTH_SHORT
                        ).show()
                        onProfileCreated(account)
                        onDismiss()
                    }.onFailure { ex ->
                        errorMessage = ex.message ?: "Error al guardar el perfil."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulMarca,
                    contentColor = Blanco
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_new_profile")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar Cambios",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                )
            }
        }
    }
}
