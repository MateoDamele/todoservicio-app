package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AuthRepository
import com.example.model.UserAccount
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.RojoUrgencia
import com.example.ui.theme.RojoUrgenciaFondo
import com.example.ui.theme.SombraDifusaColor
import com.example.ui.theme.VerdeExito

@Composable
fun UpdatePasswordScreen(
    user: UserAccount,
    onPasswordUpdated: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val hasMinLength = newPassword.length >= 6
    val hasNoSpaces = !newPassword.contains(" ")
    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    fun submitNewPassword() {
        focusManager.clearFocus()
        when {
            !hasMinLength -> errorMsg = "La contraseña debe tener al menos 6 caracteres."
            !hasNoSpaces -> errorMsg = "La contraseña no puede contener espacios."
            !passwordsMatch -> errorMsg = "Las contraseñas no coinciden."
            else -> {
                val result = AuthRepository.updateTemporaryPassword(newPassword)
                result.onSuccess {
                    onPasswordUpdated()
                }.onFailure { ex ->
                    errorMsg = ex.message ?: "No se pudo actualizar la contraseña."
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .statusBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                contentDescription = "Todoservicio",
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(36.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                border = BorderStroke(1.dp, GrisBorde),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = SombraDifusaColor,
                        spotColor = SombraDifusaColor
                    )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CelesteAcentoClaro, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LockReset,
                                contentDescription = null,
                                tint = AzulMarca,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Primer Ingreso • ${user.role.label}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GrisTextoSecundario
                                )
                            )
                            Text(
                                text = "Por tu seguridad, elegí tu nueva contraseña personal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = GrisGrafito,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = errorMsg != null,
                        enter = fadeIn(tween(180, easing = EaseOutCubic)),
                        exit = fadeOut(tween(150))
                    ) {
                        errorMsg?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RojoUrgenciaFondo,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = RojoUrgencia,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Nueva Contraseña",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = GrisGrafito
                            )
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                errorMsg = null
                            },
                            placeholder = {
                                Text(
                                    "Mínimo 6 caracteres",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    color = GrisTextoSecundario
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = AzulMarca,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isNewPasswordVisible = !isNewPasswordVisible },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (isNewPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (isNewPasswordVisible) "Ocultar" else "Mostrar",
                                        tint = AzulMarca,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulMarca,
                                unfocusedBorderColor = GrisBorde,
                                focusedContainerColor = Blanco,
                                unfocusedContainerColor = Blanco
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_new_password")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Confirmar Nueva Contraseña",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = GrisGrafito
                            )
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMsg = null
                            },
                            placeholder = {
                                Text(
                                    "Repetí tu nueva contraseña",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 13.sp,
                                    color = GrisTextoSecundario
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = AzulMarca,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (isConfirmPasswordVisible) "Ocultar" else "Mostrar",
                                        tint = AzulMarca,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { submitNewPassword() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AzulMarca,
                                unfocusedBorderColor = GrisBorde,
                                focusedContainerColor = Blanco,
                                unfocusedContainerColor = Blanco
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_confirm_password")
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (hasMinLength && hasNoSpaces) VerdeExito else GrisTextoSecundario.copy(alpha = 0.45f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Mínimo 6 caracteres sin espacios",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = if (hasMinLength && hasNoSpaces) VerdeExito else GrisTextoSecundario
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (passwordsMatch) VerdeExito else GrisTextoSecundario.copy(alpha = 0.45f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Las contraseñas coinciden",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = if (passwordsMatch) VerdeExito else GrisTextoSecundario
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { submitNewPassword() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulMarca,
                            contentColor = Blanco
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_save_password")
                    ) {
                        Text(
                            text = "Guardar y Acceder",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco
                            )
                        )
                    }

                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                    ) {
                        Text(
                            text = "Cerrar Sesión",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario
                            )
                        )
                    }
                }
            }
        }
    }
}
