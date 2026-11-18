package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.OperationalRepository
import com.example.model.ServiceHistoryItem
import com.example.ui.components.ApplyWindowBackdropBlur
import com.example.util.PdfReportGenerator
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.NaranjaAlerta
import com.example.ui.theme.NaranjaAlertaFondo
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo
import java.io.File

/**
 * ORDEN DE TRABAJO (OT) EJECUTIVA EN FORMATO FICHA TÉCNICA A4.
 *
 * Incluye:
 * 1. Membrete oficial con Todoservicio_Logo_HD.png, Razón Social ("TODOSERVICIO S.A.S."),
 *    RUT ("218944510014"), Número correlativo de OT, Fecha, Hora y Sede.
 * 2. Grilla de evidencia fotográfica: Antes ([ Estado Inicial / Diagnóstico ]) y Después ([ Trabajo Concluido / Solución ]).
 * 3. Bloque de dictamen técnico formal y observaciones bajo Dec. 406/88 MTSS.
 * 4. Módulo de Firma Técnico Exclusivo con aclaración automática de nombre, especialidad y hora de cierre.
 */
@Composable
fun TechnicalReportDialog(
    item: ServiceHistoryItem,
    canRateService: Boolean = true,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ratingDetailsMap by OperationalRepository.ratingDetails.collectAsStateWithLifecycle()
    val savedRating = ratingDetailsMap[item.serviceCode]
    val existingStars = savedRating?.stars ?: item.clientRatingStars
    val existingComment = savedRating?.comment?.ifBlank { item.clientRatingComment } ?: item.clientRatingComment

    var selectedStars by remember(item.serviceCode, existingStars) {
        mutableIntStateOf(existingStars ?: 0)
    }
    var ratingComment by remember(item.serviceCode, existingComment) {
        mutableStateOf(existingComment)
    }

    val resolvedCheckIn = item.checkInTime.ifBlank { "--:--" }
    val resolvedCheckOut = item.checkOutTime.ifBlank { item.executionTime.ifBlank { "--:--" } }

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
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Barra superior de la Hoja A4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ficha Técnica A4 Oficial",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )
                        Text(
                            text = "Certificado Pericial de Mantenimiento Edilicio",
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
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = AzulMarca
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenido estilo hoja de reporte técnico A4
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. MEMBRETE INSTITUCIONAL (Hoja A4)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Blanco,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                                    contentDescription = "Todoservicio Logo HD",
                                    modifier = Modifier.height(34.dp)
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AzulMarca
                                ) {
                                    Text(
                                        text = item.serviceCode,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Blanco,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AzulCorporativo.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ORDEN DE TRABAJO (OT) EJECUTIVA",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = PoppinsFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AzulMarca,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "TODOSERVICIO S.A.S. • Facility Management",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = AzulCorporativo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "RUT 218944510014",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // 2. DATOS CONSOLIDADOS DEL SERVICIO Y SEDE
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GrisNeutro),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "DATOS DE LA INTERVENCIÓN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulCorporativo
                                )
                            )

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cliente / Sede:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = listOf(item.companyName, item.sedeName)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" - ")
                                        .ifBlank { "Cliente Corporativo" },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulMarca
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Fecha Ejecución:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = item.executionDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulMarca
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hora Entrada (Check-in):",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = resolvedCheckIn,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Hora Salida (Check-out):",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = resolvedCheckOut,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tiempo de Intervención:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = "${item.resolutionHours} horas certificadas",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CelesteAcento
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Especialista Asignado:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario
                                    )
                                )
                                Text(
                                    text = item.leadTechnician,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = AzulCorporativo
                                    )
                                )
                            }
                        }
                    }

                    // 3. GRILLA DE EVIDENCIA FOTOGRÁFICA (ANTES / DESPUÉS)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EVIDENCIA FOTOGRÁFICA PERICIAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )
                            Surface(shape = RoundedCornerShape(4.dp), color = if (item.evidencePhotos.isNotEmpty()) VerdeExitoFondo else GrisNeutro) {
                                Text(
                                    text = if (item.evidencePhotos.isEmpty()) "Sin fotos adjuntas" else "${item.evidencePhotos.size} Tomas Verificadas",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (item.evidencePhotos.isNotEmpty()) VerdeExito else GrisTextoSecundario,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        if (item.evidencePhotos.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GrisNeutro,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Image,
                                        contentDescription = null,
                                        tint = GrisTextoSecundario.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "Sin capturas fotográficas registradas en esta OT",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PoppinsFontFamily,
                                            color = GrisTextoSecundario
                                        )
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item.evidencePhotos.take(4).forEachIndexed { idx, photoEntry ->
                                    val bitmap = remember(photoEntry) {
                                        decodeReportPhotoBitmap(context, photoEntry)
                                    }
                                    val isAfter = idx > 0
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = GrisNeutro,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isAfter) VerdeExito.copy(alpha = 0.6f) else GrisBorde
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(104.dp)
                                    ) {
                                        if (bitmap != null) {
                                            Box(modifier = Modifier.fillMaxSize()) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = "Evidencia fotográfica ${idx + 1}",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(10.dp))
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = AzulMarca.copy(alpha = 0.85f),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .padding(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Foto ${idx + 1}",
                                                        color = Blanco,
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isAfter) Icons.Rounded.CheckCircle else Icons.Rounded.Image,
                                                    contentDescription = null,
                                                    tint = if (isAfter) VerdeExito else CelesteAcento,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = photoEntry,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = PoppinsFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAfter) VerdeExito else AzulMarca,
                                                        fontSize = 10.5.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. BLOQUE DE DICTAMEN TÉCNICO FORMAL Y OBSERVACIONES
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "DICTAMEN TÉCNICO FORMAL",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Blanco,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.summary,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = AzulMarca,
                                    lineHeight = 20.sp
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Certificación MTSS Dec 406/88
                    if (item.certifiedDec406) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = VerdeExitoFondo,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VerdeExito.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = VerdeExito,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Certificación de Cumplimiento Normativo Dec. 406/88 (MTSS Uruguay)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // 5. MÓDULO DE FIRMA DEL TÉCNICO A CARGO (EXCLUSIVA, SIN FIRMA DE CLIENTE)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "RÚBRICA Y CONFORMIDAD TÉCNICA",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Blanco,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, VerdeExito),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = VerdeExito,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Firma y Aprobación del Técnico Actuante",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca,
                                                fontSize = 12.5.sp
                                            )
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = VerdeExitoFondo
                                    ) {
                                        Text(
                                            text = "Estampada",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = VerdeExito,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                // Lienzo estilizado de firma técnica registrada
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(GrisNeutro, RoundedCornerShape(8.dp))
                                        .border(1.dp, GrisBorde, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "✍️ ${item.signedBy}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulMarca,
                                                fontSize = 16.sp
                                            )
                                        )
                                        Text(
                                            text = "Firma Digital pericial criptográfica SHA-256",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }

                                // Aclaración automática de Técnico y Hora
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GrisNeutro
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Aclaración: ${item.leadTechnician}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AzulMarca
                                                )
                                            )
                                            Text(
                                                text = "Hora Cierre: $resolvedCheckOut",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = PoppinsFontFamily,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AzulCorporativo
                                                )
                                            )
                                        }
                                        Text(
                                            text = "Especialidad: ${item.category} • Todoservicio S.A.S.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = GrisTextoSecundario,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. SECCIÓN "CALIFICÁ NUESTRO SERVICIO" (5 ESTRELLAS + COMENTARIO CLIENTE)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (existingStars != null) VerdeExitoFondo else NaranjaAlertaFondo.copy(alpha = 0.55f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (existingStars != null) VerdeExito.copy(alpha = 0.5f) else NaranjaAlerta.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (existingStars != null) "Calificación del Cliente Registrada" else "Calificá nuestro servicio",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AzulMarca
                                )
                            )

                            if (existingStars != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector = if (star <= existingStars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                            contentDescription = "$star estrellas",
                                            tint = if (star <= existingStars) NaranjaAlerta else GrisTextoSecundario,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$existingStars / 5",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = VerdeExito
                                    )
                                }
                                if (existingComment.isNotBlank()) {
                                    Text(
                                        text = "\"$existingComment\"",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        color = AzulMarca
                                    )
                                }
                            } else if (canRateService) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    (1..5).forEach { star ->
                                        IconButton(
                                            onClick = { selectedStars = star },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("btn_rate_star_$star")
                                        ) {
                                            Icon(
                                                imageVector = if (star <= selectedStars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                contentDescription = "Calificar con $star estrellas",
                                                tint = if (star <= selectedStars) NaranjaAlerta else GrisTextoSecundario,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = ratingComment,
                                    onValueChange = { ratingComment = it },
                                    placeholder = {
                                        Text(
                                            "Ej: Excelente atención y prolijidad (opcional)",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp
                                        )
                                    },
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_client_rating_comment")
                                )

                                Button(
                                    onClick = {
                                        if (selectedStars < 1) {
                                            Toast.makeText(
                                                context,
                                                "Seleccioná de 1 a 5 estrellas para enviar tu calificación",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            OperationalRepository.submitNpsRating(
                                                orderCode = item.serviceCode,
                                                stars = selectedStars,
                                                comment = ratingComment
                                            )
                                            Toast.makeText(
                                                context,
                                                "¡Gracias! Calificación $selectedStars★ registrada",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulCorporativo),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_submit_client_rating")
                                ) {
                                    Text(
                                        text = "Enviar Calificación",
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Blanco
                                    )
                                }
                            } else {
                                Text(
                                    text = "Pendiente de calificación del cliente.",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = GrisTextoSecundario
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botones de acción inferiores
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generatePdfReport(
                                context = context,
                                data = PdfReportGenerator.ReportData(
                                    orderCode = item.serviceCode,
                                    serviceTitle = item.title,
                                    category = item.category,
                                    executionDate = item.executionDate,
                                    checkInTime = resolvedCheckIn,
                                    checkOutTime = resolvedCheckOut,
                                    totalDuration = "${item.resolutionHours} h",
                                    leadTechnician = item.leadTechnician,
                                    companyName = item.companyName.ifBlank { "Cliente Corporativo" },
                                    clientAddress = item.address.ifBlank { item.sedeName },
                                    tasksExecuted = listOf(item.title),
                                    findingsSummary = item.technicianComments.ifBlank { item.summary },
                                    dec406Compliant = item.certifiedDec406,
                                    npsRating = existingStars,
                                    npsComment = existingComment
                                )
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.openPdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Error generando constancia PDF", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulMarca),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Descargar Constancia Oficial A4",
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

private fun decodeReportPhotoBitmap(context: android.content.Context, rawUriOrPath: String): androidx.compose.ui.graphics.ImageBitmap? {
    return try {
        val trimmed = rawUriOrPath.trim()
        val bmp = when {
            trimmed.startsWith("file://") -> {
                val path = Uri.parse(trimmed).path ?: return null
                BitmapFactory.decodeFile(path)
            }
            trimmed.startsWith("content://") -> {
                context.contentResolver.openInputStream(Uri.parse(trimmed))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            trimmed.startsWith("/") && File(trimmed).exists() -> {
                BitmapFactory.decodeFile(trimmed)
            }
            else -> null
        }
        bmp?.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}
