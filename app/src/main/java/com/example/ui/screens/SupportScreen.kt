package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientProfile
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
import com.example.ui.theme.VerdeExito

@Composable
fun SupportScreen(
    clientProfile: ClientProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(GrisNeutro)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Cabecera
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "CENTRO DE ATENCIÓN Y CONTACTO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = CelesteAcento,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Soporte Técnico y Consultas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca
                        )
                    )
                    Text(
                        text = "Estamos a su disposición las 24 horas del día. Si necesita asistencia inmediata para emergencias en su edificio o empresa, presione el botón de llamada directa.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario
                        )
                    )
                }
            }
        }

        // Bloque Urgencia 24/7 (Botón Gigante de Llamada)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AzulMarca),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFF5252), CircleShape)
                        )
                        Text(
                            text = "LÍNEA DE GUARDIA 24 HORAS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    Text(
                        text = "¿Corte imprevisto o rotura de caño matriz?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+59899000111")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CelesteAcento,
                            contentColor = Blanco
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_call_guardia_247")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneInTalk,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Llamar a Guardia (+598 99 000 111)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Blanco
                            )
                        )
                    }
                }
            }
        }

        // Canales Directos
        item {
            Text(
                text = "Canales de Comunicación Directa",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ContactChannelRow(
                    icon = Icons.AutoMirrored.Rounded.Chat,
                    title = "WhatsApp Facility Manager",
                    subtitle = "Respuesta inmediata por mensajería",
                    actionLabel = "Chatear",
                    iconColor = VerdeExito,
                    onClick = {
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=59899000111&text=Hola%20Todoservicio,%20necesito%20asistencia%20en%20${Uri.encode(clientProfile.companyName)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    },
                    testTag = "channel_whatsapp"
                )

                ContactChannelRow(
                    icon = Icons.Rounded.Email,
                    title = "Correo de Operaciones",
                    subtitle = "contacto@todoservicio.com.uy",
                    actionLabel = "Escribir",
                    iconColor = AzulCorporativo,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:contacto@todoservicio.com.uy?subject=Consulta%20T%C3%A9cnica%20-%20${Uri.encode(clientProfile.companyName)}")
                        }
                        context.startActivity(intent)
                    },
                    testTag = "channel_email"
                )

                ContactChannelRow(
                    icon = Icons.Rounded.LocationOn,
                    title = "Sede Central Montevideo",
                    subtitle = "Plaza Independencia 838, Montevideo, Uruguay",
                    actionLabel = "Ver Mapa",
                    iconColor = CelesteAcento,
                    onClick = {
                        com.example.ui.components.openExternalGoogleMaps(
                            context = context,
                            queryOrAddress = "Plaza Independencia 838, Montevideo, Uruguay",
                            lat = -34.9063,
                            lng = -56.1998
                        )
                    },
                    testTag = "channel_location"
                )
            }
        }

        // Preguntas Frecuentes Accesibles
        item {
            Text(
                text = "Preguntas Frecuentes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AzulMarca
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FaqItem(
                    question = "¿Qué alcance tiene el Decreto 406/88 del MTSS?",
                    answer = "El Decreto 406/88 regula la Seguridad, Higiene y Salud Ocupacional en Uruguay. Todoservicio emite informes técnicos con firma de ingenieros matriculados, certificando que tableros eléctricos, salas de máquinas, extintores y vías de evacuación cumplen con las exigencias del Ministerio de Trabajo y Seguridad Social."
                )

                FaqItem(
                    question = "¿Cómo se coordina una visita de urgencia?",
                    answer = "Al tocar el botón de guardia o enviar una solicitud con prioridad 'Emergencia', el sistema despacha una cuadrilla técnica de turno. El tiempo medio de respuesta en Montevideo es inferior a 45 minutos."
                )

                FaqItem(
                    question = "¿Dónde descargo los certificados anuales?",
                    answer = "En la sección 'Historial de Servicios', encontrará todos los informes con firma digital, descargables para presentar ante aseguradoras, la Intendencia o inspecciones oficiales."
                )
            }
        }

        item { Spacer(modifier = Modifier.height(108.dp)) }
    }
}

@Composable
private fun ContactChannelRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Blanco,
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulMarca
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CelesteAcentoClaro
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulCorporativo
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Blanco,
        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = AzulMarca
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = AzulCorporativo,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        color = GrisTextoSecundario,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
