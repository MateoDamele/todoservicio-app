package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatSenderRole
import com.example.model.ServiceChatMessage
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisGrafito
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

private const val MAX_CHAT_MESSAGE_CHARS = 140

/**
 * Panel compacto de Chat en Tiempo Real embebido en tarjetas de "Servicio en Curso".
 * Permite intercambiar mensajes breves exclusivamente mientras la OT está activa.
 */
@Composable
fun ServiceChatPanel(
    orderCode: String,
    messages: List<ServiceChatMessage>,
    currentRole: ChatSenderRole,
    counterpartyLabel: String,
    isChatActive: Boolean,
    isCompleted: Boolean = false,
    initiallyExpanded: Boolean = isChatActive,
    onSendMessage: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(orderCode, isChatActive) { mutableStateOf(initiallyExpanded) }
    var draftMessage by remember(orderCode) { mutableStateOf("") }
    val messagesScrollState = rememberScrollState()

    LaunchedEffect(messages.size, isExpanded) {
        if (isExpanded && messages.isNotEmpty()) {
            messagesScrollState.animateScrollTo(messagesScrollState.maxValue)
        }
    }

    val quickReplies = remember(currentRole) {
        if (currentRole == ChatSenderRole.CLIENTE) {
            listOf(
                "Acceso habilitado",
                "Te esperan en recepción",
                "¿Tiempo estimado?"
            )
        } else {
            listOf(
                "Llegando en 10 min",
                "Ya en recepción",
                "En sala de tableros"
            )
        }
    }

    val containerColor = Blanco
    val statusText = when {
        isChatActive -> "En vivo"
        isCompleted -> "Cerrado"
        else -> "Bloqueado"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, if (isChatActive) AzulMarca.copy(alpha = 0.25f) else GrisBorde),
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_chat_panel_$orderCode")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabecera compacta del chat con indicador de estado y toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("service_chat_toggle_$orderCode"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isChatActive) CelesteAcentoClaro else GrisNeutro,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isChatActive) Icons.AutoMirrored.Rounded.Chat else Icons.Rounded.Lock,
                            contentDescription = "Chat del servicio",
                            tint = if (isChatActive) AzulMarca else GrisTextoSecundario,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chat directo • $counterpartyLabel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = GrisGrafito,
                                fontSize = 12.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (messages.isEmpty()) {
                                if (isChatActive) "Sin mensajes aún" else "Disponible solo con OT activa"
                            } else {
                                "${messages.size} mensaje(s)"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isChatActive) VerdeExitoFondo else GrisNeutro,
                        border = BorderStroke(1.dp, if (isChatActive) VerdeExito.copy(alpha = 0.35f) else GrisBorde)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isChatActive) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(VerdeExito, CircleShape)
                                )
                            }
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChatActive) VerdeExito else GrisTextoSecundario,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer chat" else "Expandir chat",
                        tint = GrisTextoSecundario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = GrisBorde.copy(alpha = 0.7f))

                    // Lista compacta de mensajes en tiempo real
                    if (messages.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisNeutro,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isChatActive) {
                                    "Coordina el acceso o detalles breves mientras dura el servicio."
                                } else if (isCompleted) {
                                    "OT finalizada. No se intercambiaron mensajes."
                                } else {
                                    "El chat se habilita cuando el técnico inicia el trayecto o el trabajo."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 72.dp, max = 185.dp)
                                .background(GrisNeutro, RoundedCornerShape(12.dp))
                                .verticalScroll(messagesScrollState)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            messages.forEach { msg ->
                                val isOwn = msg.senderRole == currentRole
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (isOwn) 14.dp else 4.dp,
                                            bottomEnd = if (isOwn) 4.dp else 14.dp
                                        ),
                                        color = if (isOwn) AzulMarca else Blanco,
                                        border = if (isOwn) null else BorderStroke(1.dp, GrisBorde),
                                        modifier = Modifier.widthIn(max = 250.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = msg.message,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    color = if (isOwn) Blanco else GrisGrafito,
                                                    fontSize = 12.sp
                                                )
                                            )
                                            Text(
                                                text = "${if (isOwn) "Tú" else msg.senderName} • ${msg.timestamp}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    color = if (isOwn) Blanco.copy(alpha = 0.78f) else GrisTextoSecundario,
                                                    fontSize = 9.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isChatActive) {
                        // Respuestas breves rápidas en 1 toque
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickReplies.forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = CelesteAcentoClaro,
                                    border = BorderStroke(1.dp, AzulMarca.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSendMessage(preset) }
                                ) {
                                    Text(
                                        text = preset,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AzulMarca,
                                            fontSize = 11.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Campo de mensaje breve + botón enviar de 48dp
                        val submitAction = {
                            if (draftMessage.isNotBlank()) {
                                val sent = onSendMessage(draftMessage)
                                if (sent) {
                                    draftMessage = ""
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftMessage,
                                onValueChange = { if (it.length <= MAX_CHAT_MESSAGE_CHARS) draftMessage = it },
                                placeholder = {
                                    Text(
                                        text = "Mensaje breve...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario
                                        )
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { submitAction() }),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AzulMarca,
                                    unfocusedBorderColor = GrisBorde,
                                    focusedContainerColor = GrisNeutro,
                                    unfocusedContainerColor = GrisNeutro
                                ),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisGrafito
                                ),
                                trailingIcon = {
                                    Text(
                                        text = "${draftMessage.length}/$MAX_CHAT_MESSAGE_CHARS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario,
                                            fontSize = 9.5.sp
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("service_chat_input_$orderCode")
                            )

                            val canSend = draftMessage.isNotBlank()
                            IconButton(
                                onClick = submitAction,
                                enabled = canSend,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (canSend) AzulMarca else GrisBorde,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .testTag("service_chat_send_$orderCode")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Enviar mensaje",
                                    tint = if (canSend) Blanco else GrisTextoSecundario,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Aviso estricto de restricción por estado de OT
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GrisNeutro,
                            border = BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = GrisTextoSecundario,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isCompleted) {
                                        "Servicio finalizado • Chat cerrado"
                                    } else {
                                        "Chat restringido • Disponible al iniciar la OT"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GrisTextoSecundario,
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
}
