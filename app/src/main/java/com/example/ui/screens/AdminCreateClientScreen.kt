package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AddBusiness
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AuthRepository
import com.example.data.OperationalRepository
import com.example.model.AvailableMonthlyPlans
import com.example.model.ClientBranch
import com.example.model.ClientSegment
import com.example.model.CorporateClientAccount
import com.example.model.MonthlyPlanOption
import com.example.model.UserAccount
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
import com.example.ui.theme.VerdeExitoFondo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCreateClientScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Bloque 1: Datos Generales
    var fullName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+598 ") }
    var email by remember { mutableStateOf("") }

    // Bloque 2: Vinculación de Plan Mensual
    var isPlanDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<MonthlyPlanOption>(AvailableMonthlyPlans[1]) } // Plan Corporativo Plus

    // Bloque 3: Generación de Credenciales
    var temporaryPassword by remember { mutableStateOf(AuthRepository.generateSecureTemporaryPassword()) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var createdUserModal by remember { mutableStateOf<UserAccount?>(null) }

    fun copyToClipboard(text: String, label: String = "Credenciales") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Cabecera con botón volver
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Blanco, CircleShape)
                        .border(1.dp, GrisBorde, CircleShape)
                        .testTag("btn_back_from_create_client")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = AzulMarca
                    )
                }

                Column {
                    Text(
                        text = "PANEL DE ADMINISTRACIÓN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcento,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Alta Manual de Clientes (Opción B)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )
                }
            }
        }

        // Mensaje de Error
        item {
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = RojoUrgenciaFondo,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = RojoUrgencia,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // ====================================================================
        // BLOQUE 1: DATOS GENERALES DEL CLIENTE
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CelesteAcentoClaro, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Badge,
                                contentDescription = null,
                                tint = AzulCorporativo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Bloque 1: Datos Generales",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    // Nombre del contacto
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre del Contacto / Administrador", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("Ej: Ing. Gabriel Rodríguez", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_fullname")
                    )

                    // Nombre de la Empresa / Razón Social
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Empresa / Razón Social / Edificio", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("Ej: Torres del Prado S.A. o Edificio Victoria", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_company")
                    )

                    // Teléfono de contacto
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono de Contacto (Uruguay)", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("+598 99 123 456", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_phone")
                    )

                    // Correo corporativo
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico Corporativo", fontFamily = PoppinsFontFamily) },
                        placeholder = { Text("administracion@empresa.com.uy", fontFamily = PoppinsFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CelesteAcento,
                            unfocusedBorderColor = GrisBorde
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_email")
                    )
                }
            }
        }

        // ====================================================================
        // BLOQUE 2: VINCULACIÓN DE PLAN MENSUAL
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CelesteAcentoClaro, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddBusiness,
                                contentDescription = null,
                                tint = AzulCorporativo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Bloque 2: Vinculación de Plan Mensual",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    // Dropdown de Selección de Plan
                    ExposedDropdownMenuBox(
                        expanded = isPlanDropdownExpanded,
                        onExpandedChange = { isPlanDropdownExpanded = !isPlanDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPlan.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Plan Mensual Contratado", fontFamily = PoppinsFontFamily) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isPlanDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CelesteAcento,
                                unfocusedBorderColor = GrisBorde
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = isPlanDropdownExpanded,
                            onDismissRequest = { isPlanDropdownExpanded = false }
                        ) {
                            AvailableMonthlyPlans.forEach { plan ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = plan.name,
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca
                                            )
                                            Text(
                                                text = "${plan.description} - USD ${plan.priceMonthlyUsd}/mes",
                                                fontFamily = PoppinsFontFamily,
                                                fontSize = 11.sp,
                                                color = GrisTextoSecundario
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPlan = plan
                                        isPlanDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Detalle del plan seleccionado
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = GrisNeutro,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Detalle del Plan: ${selectedPlan.visitsPerMonth} visitas técnicas programadas",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulCorporativo
                                )
                            )
                            Text(
                                text = selectedPlan.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario
                                )
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // BLOQUE 3: GENERACIÓN DE CREDENCIALES
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CelesteAcentoClaro, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Key,
                                    contentDescription = null,
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "Bloque 3: Credenciales",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                        }

                        // Botón regenerar clave aleatoria
                        OutlinedButton(
                            onClick = { temporaryPassword = AuthRepository.generateSecureTemporaryPassword() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_regenerate_password")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Regenerar",
                                modifier = Modifier.size(16.dp),
                                tint = CelesteAcento
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Nueva Clave",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = AzulCorporativo
                                )
                            )
                        }
                    }

                    // Caja destacada con la contraseña temporal
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AzulMarca.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CelesteAcento.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CONTRASEÑA TEMPORAL GENERADA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = CelesteAcento,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = temporaryPassword,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulMarca,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = { copyToClipboard(temporaryPassword, "Contraseña Temporal") },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Blanco, RoundedCornerShape(10.dp))
                                    .border(1.dp, GrisBorde, RoundedCornerShape(10.dp))
                                    .testTag("btn_copy_password")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = "Copiar contraseña",
                                    tint = AzulCorporativo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "El cliente deberá actualizar obligatoriamente esta clave temporal en su primer inicio de sesión.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        )
                    )
                }
            }
        }

        // ====================================================================
        // BOTÓN "REGISTRAR CLIENTE" (Color Azul Marca #102C54 con indicador de carga)
        // ====================================================================
        item {
            Button(
                onClick = {
                    if (fullName.isBlank() || companyName.isBlank() || email.isBlank() || phone.isBlank()) {
                        errorMessage = "Por favor complete todos los campos obligatorios."
                    } else {
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch {
                            delay(250)
                            val res = AuthRepository.createClientManual(
                                fullName = fullName,
                                companyName = companyName,
                                phone = phone,
                                email = email,
                                monthlyPlanId = selectedPlan.id,
                                temporaryPassword = temporaryPassword
                            )
                            isLoading = false
                            res.onSuccess { newAccount ->
                                val cid = newAccount.clientId
                                if (!cid.isNullOrBlank() &&
                                    OperationalRepository.corporateClients.value.none { it.id == cid }
                                ) {
                                    OperationalRepository.addCorporateClient(
                                        CorporateClientAccount(
                                            id = cid,
                                            name = newAccount.companyName,
                                            rut = newAccount.rut,
                                            planName = selectedPlan.name,
                                            clientColorHex = 0xFF335C81L,
                                            segment = ClientSegment.EMPRESAS,
                                            branches = listOf(
                                                ClientBranch(
                                                    id = "br-${cid}-01",
                                                    name = "Casa Central",
                                                    address = "Montevideo",
                                                    contactPerson = newAccount.fullName,
                                                    phone = newAccount.phone
                                                )
                                            ),
                                            logoUri = newAccount.avatarUrl
                                        )
                                    )
                                }
                                createdUserModal = newAccount
                            }.onFailure { ex ->
                                errorMessage = ex.message ?: "Error al registrar cliente."
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulMarca,
                    contentColor = Blanco,
                    disabledContainerColor = AzulMarca.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_register_client_submit")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Blanco,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Registrar Cliente en Todoservicio",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Modal de Confirmación y Compartir Credenciales
    createdUserModal?.let { createdUser ->
        ClientCreatedSuccessDialog(
            user = createdUser,
            temporaryPassword = temporaryPassword,
            onDismiss = {
                createdUserModal = null
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun ClientCreatedSuccessDialog(
    user: UserAccount,
    temporaryPassword: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val messageText = """
        Estimado/a ${user.fullName}, le damos la bienvenida a Todoservicio (Facility Management).
        Su cuenta corporativa para ${user.companyName} ha sido creada con éxito.
        
        Datos de acceso al portal:
        • Correo: ${user.email}
        • Contraseña temporal: $temporaryPassword
        
        Por motivos de seguridad, la plataforma le solicitará cambiar esta contraseña en su primer ingreso.
        Atentamente, Administración de Todoservicio.
    """.trimIndent()

    Dialog(onDismissRequest = onDismiss) {
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(VerdeExitoFondo, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = VerdeExito,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "¡Cliente Registrado con Éxito!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Cuenta guardada en base segura",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = VerdeExito,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GrisNeutro,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Empresa: ${user.companyName}",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AzulMarca
                        )
                        Text(
                            text = "Usuario: ${user.email}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = AzulCorporativo
                        )
                        Text(
                            text = "Clave temporal: $temporaryPassword",
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = CelesteAcento
                        )
                    }
                }

                Text(
                    text = "Compartir credenciales con el cliente:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GrisTextoSecundario
                    )
                )

                // Acciones para compartir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp
                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=${user.phone.replace(" ", "").replace("+", "")}&text=${Uri.encode(messageText)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = null,
                            tint = VerdeExito,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = AzulMarca)
                    }

                    // Email
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${user.email}?subject=Credenciales%20de%20Acceso%20-%20Todoservicio&body=${Uri.encode(messageText)}")
                            }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = CelesteAcento,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Correo", fontFamily = PoppinsFontFamily, fontSize = 12.sp, color = AzulMarca)
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Listo y Volver al Panel", fontFamily = PoppinsFontFamily, color = Blanco)
                }
            }
        }
    }
}
