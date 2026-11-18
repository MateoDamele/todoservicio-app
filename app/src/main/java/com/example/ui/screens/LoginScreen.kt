package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.AuthRepository
import com.example.model.UserAccount
import com.example.model.UserRole
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
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.VerdeExito
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Perfiles de acceso en el selector: Cliente Corporativo, Técnico / Cuadrilla, Administrador.
 */
enum class LoginProfile(
    val title: String,
    val buttonLabel: String,
    val role: UserRole,
    val icon: ImageVector,
    val testTag: String
) {
    CLIENTE(
        title = "Cliente Corporativo",
        buttonLabel = "Cliente Corporativo",
        role = UserRole.CLIENTE,
        icon = Icons.Rounded.Apartment,
        testTag = "login_tab_cliente"
    ),
    TECNICO(
        title = "Técnico / Cuadrilla",
        buttonLabel = "Técnico / Cuadrilla",
        role = UserRole.TECNICO,
        icon = Icons.Rounded.Engineering,
        testTag = "login_tab_tecnico"
    ),
    ADMIN(
        title = "Administrador",
        buttonLabel = "Administrador",
        role = UserRole.ADMIN,
        icon = Icons.Rounded.AdminPanelSettings,
        testTag = "login_tab_admin"
    ),
    LIMPIEZA(
        title = "Personal de Limpieza",
        buttonLabel = "Personal de Limpieza",
        role = UserRole.LIMPIEZA,
        icon = Icons.Rounded.CleaningServices,
        testTag = "login_tab_limpieza"
    )
}

/**
 * Pantalla de Inicio de Sesión de Todoservicio:
 * - Acceso por Nombre de Usuario o Correo institucional.
 * - Autenticación segura de producción (Zero Hardcoded Secrets).
 * - SafeArea estricta con Padding(top: 24.dp, horizontal: 16.dp).
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (UserAccount) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    // Perfil seleccionado por defecto: Administrador o Cliente
    var selectedProfile by remember { mutableStateOf(LoginProfile.ADMIN) }

    // Campos de credenciales: COMPLETAMENTE VACÍOS por defecto según especificación
    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    fun executeLogin(username: String, pass: String) {
        focusManager.clearFocus()
        if (username.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor ingrese su Nombre de Usuario y Contraseña."
            return
        }

        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            delay(300)
            val result = AuthRepository.login(username, pass, rememberMe, context)
            isLoading = false
            result.onSuccess { user ->
                onLoginSuccess(user)
            }.onFailure { ex ->
                errorMessage = ex.message ?: "Credenciales incorrectas. Verifique sus datos."
            }
        }
    }

    // Contenedor principal con SafeArea estricta y espaciado de protección
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .statusBarsPadding()
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ================================================================
            // 1. IDENTIDAD VISUAL CORPORATIVA (LOGOTIPO OFICIAL HD CENTRADO)
            // ================================================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AzulMarca,
                    modifier = Modifier
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = AzulMarca.copy(alpha = 0.16f),
                            spotColor = AzulMarca.copy(alpha = 0.22f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("login_official_logo_container")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                            contentDescription = "Todoservicio Logotipo Oficial",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .width(220.dp)
                                .testTag("login_official_logo")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Facility Management & Mantenimiento Integral",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulCorporativo,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // ================================================================
            // TARJETA PRINCIPAL DE LOGIN
            // ================================================================
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card")
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Marca de agua suave del Isotipo "T"
                    WatermarkIsotypeT(
                        modifier = Modifier
                            .size(220.dp)
                            .align(Alignment.Center)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Selector de perfil
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Perfil de acceso:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca,
                                    fontSize = 12.sp
                                )
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val chunkedProfiles = LoginProfile.values().toList().chunked(2)
                                chunkedProfiles.forEach { rowProfiles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowProfiles.forEach { profile ->
                                            val isSelected = selectedProfile == profile
                                            val targetBorderColor = if (isSelected) CelesteAcento else GrisBorde
                                            val targetContainerColor = if (isSelected) CelesteAcentoClaro else Blanco
                                            val targetContentColor = if (isSelected) AzulMarca else GrisTextoSecundario

                                            val containerColor by animateColorAsState(
                                                targetValue = targetContainerColor,
                                                animationSpec = tween(180),
                                                label = "profileTabContainer"
                                            )
                                            val borderColor by animateColorAsState(
                                                targetValue = targetBorderColor,
                                                animationSpec = tween(180),
                                                label = "profileTabBorder"
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = containerColor,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = borderColor
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(54.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        selectedProfile = profile
                                                        errorMessage = null
                                                    }
                                                    .testTag(profile.testTag)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.Center,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = profile.icon,
                                                        contentDescription = profile.title,
                                                        tint = if (isSelected) CelesteAcento else GrisTextoSecundario,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = when (profile) {
                                                            LoginProfile.CLIENTE -> "Cliente"
                                                            LoginProfile.TECNICO -> "Técnico"
                                                            LoginProfile.ADMIN -> "Admin"
                                                            LoginProfile.LIMPIEZA -> "Limpieza"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = PoppinsFontFamily,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                            color = targetContentColor,
                                                            fontSize = 11.5.sp
                                                        ),
                                                        textAlign = TextAlign.Start,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }



                        // Mensaje de Error
                        AnimatedVisibility(visible = errorMessage != null) {
                            errorMessage?.let { msg ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = RojoUrgenciaFondo,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RojoUrgencia.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = RojoUrgencia,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Campo 1: "Nombre de Usuario" (estrictamente sin la palabra Correo o Email)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Nombre de Usuario",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulMarca
                                )
                            )
                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = {
                                    usernameInput = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        "Ingrese su nombre de usuario",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 13.sp,
                                        color = GrisTextoSecundario
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = "Nombre de Usuario",
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CelesteAcento,
                                    unfocusedBorderColor = AzulCorporativo.copy(alpha = 0.35f),
                                    focusedContainerColor = Blanco,
                                    unfocusedContainerColor = Blanco
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 14.sp,
                                    color = AzulMarca
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("login_username_input")
                            )
                        }

                        // Campo 2: "Contraseña"
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Contraseña",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulMarca
                                )
                            )
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    errorMessage = null
                                },
                                placeholder = {
                                    Text(
                                        "Ingrese su contraseña",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 13.sp,
                                        color = GrisTextoSecundario
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Contraseña",
                                        tint = AzulCorporativo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { isPasswordVisible = !isPasswordVisible },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = if (isPasswordVisible) "Ocultar" else "Mostrar",
                                            tint = AzulCorporativo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { executeLogin(usernameInput, passwordInput) }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CelesteAcento,
                                    unfocusedBorderColor = AzulCorporativo.copy(alpha = 0.35f),
                                    focusedContainerColor = Blanco,
                                    unfocusedContainerColor = Blanco
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 14.sp,
                                    color = AzulMarca
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("login_password_input")
                            )
                        }

                        // Casilla "Recordar mi sesión en este dispositivo"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    rememberMe = !rememberMe
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CelesteAcento,
                                    uncheckedColor = AzulCorporativo.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("checkbox_remember_me")
                            )
                            Text(
                                text = "Recordar mi sesión en este dispositivo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = AzulMarca,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // Botón de Iniciar Sesión
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                executeLogin(usernameInput, passwordInput)
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CelesteAcento,
                                contentColor = Blanco
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_login_submit")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Blanco,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = "Iniciar Sesión como ${selectedProfile.buttonLabel}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Blanco
                                    )
                                )
                            }
                        }

                        // Enlace Secundario
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(
                                    text = "¿Olvidó su contraseña?",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulCorporativo,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            initialUser = usernameInput,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

@Composable
private fun WatermarkIsotypeT(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val boxSide = minOf(w, h) * 0.82f
        val left = (w - boxSide) / 2f
        val top = (h - boxSide) / 2f
        val corner = boxSide * 0.22f

        drawRoundRect(
            color = AzulMarca.copy(alpha = 0.045f),
            topLeft = Offset(left, top),
            size = Size(boxSide, boxSide),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = boxSide * 0.08f)
        )

        val barThickness = boxSide * 0.12f
        val topBarWidth = boxSide * 0.52f
        val stemHeight = boxSide * 0.48f
        val centerX = w / 2f
        val topBarY = top + boxSide * 0.26f

        drawRoundRect(
            color = AzulMarca.copy(alpha = 0.045f),
            topLeft = Offset(centerX - topBarWidth / 2f, topBarY),
            size = Size(topBarWidth, barThickness),
            cornerRadius = CornerRadius(barThickness / 3f, barThickness / 3f)
        )

        drawRoundRect(
            color = AzulMarca.copy(alpha = 0.045f),
            topLeft = Offset(centerX - barThickness / 2f, topBarY),
            size = Size(barThickness, stemHeight),
            cornerRadius = CornerRadius(barThickness / 3f, barThickness / 3f)
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    initialUser: String,
    onDismiss: () -> Unit
) {
    var user by remember { mutableStateOf(initialUser) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Restablecer Acceso",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                Text(
                    text = "Ingrese su nombre de usuario. El administrador recibirá la solicitud para generar una clave temporal segura.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        fontSize = 12.sp
                    )
                )

                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    placeholder = { Text("Nombre de usuario", fontFamily = PoppinsFontFamily) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CelesteAcento,
                        unfocusedBorderColor = GrisBorde
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (successMsg != null) {
                    Text(
                        text = successMsg!!,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VerdeExito,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cerrar", fontFamily = PoppinsFontFamily, color = GrisTextoSecundario)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            successMsg = "Solicitud enviada a la administración de Todoservicio."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteAcento),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Solicitar", fontFamily = PoppinsFontFamily, color = Blanco)
                    }
                }
            }
        }
        }
    }
}
