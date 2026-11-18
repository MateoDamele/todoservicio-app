package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@Composable
fun IdentityDetailsDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 16.dp)
            ) {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cabecera con botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NUESTRA IDENTIDAD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = CelesteAcento,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Todoservicio",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = GrisTextoSecundario
                        )
                    }
                }

                // Misión
                Surface(
                    color = GrisNeutro,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "MISIÓN CORPORATIVA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            )
                        )
                        Text(
                            text = "Proveer soluciones integrales de mantenimiento y gestión de infraestructura que garanticen la continuidad operativa, la seguridad y la valorización de los activos de nuestros clientes.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Visión
                Surface(
                    color = GrisNeutro,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "VISIÓN ESTRATÉGICA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulCorporativo
                            )
                        )
                        Text(
                            text = "Consolidarnos como la plataforma de servicios de referencia, reconocidos por la excelencia técnica, la innovación y el compromiso normativo.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                color = AzulMarca,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // 4 Pilares Estratégicos
                Text(
                    text = "Los 4 Pilares Estratégicos",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )

                val pillars = listOf(
                    Pair("Excelencia Técnica", "Protocolos de ingeniería rigurosos y cuadrillas altamente calificadas."),
                    Pair("Seguridad (Dec. 406/88)", "Cumplimiento estricto del marco normativo nacional de seguridad e higiene laboral en Uruguay."),
                    Pair("Transparencia", "Trazabilidad completa con informes digitales, fotografías y firmas periciales."),
                    Pair("Adaptabilidad", "Soluciones a medida para corporaciones, edificios residenciales y plantas industriales.")
                )

                pillars.forEach { (name, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(CelesteAcentoClaro, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario
                                )
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Entendido",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Blanco
                        )
                    )
                }
            }
        }
        }
    }
}
