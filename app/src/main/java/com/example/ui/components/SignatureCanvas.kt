package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.CelesteAcentoClaro
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import com.example.ui.theme.VerdeExitoFondo

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp

/**
 * Trazo individual compuesto por una lista de puntos consecutivos.
 */
data class SignaturePath(
    val points: List<Offset>
)

/**
 * Lienzo interactivo de captura de firma digital en pantalla.
 * Permite al técnico trazar su rúbrica táctil con el dedo o stylus sobre una superficie espaciosa
 * de 220px-260px (240dp), con línea guía punteada sutil y aclaración automática de especialista y horario.
 */
@Composable
fun InteractiveSignatureCanvas(
    title: String,
    signerName: String,
    signerRole: String,
    onSignatureChanged: (hasSignature: Boolean, paths: List<SignaturePath>) -> Unit,
    modifier: Modifier = Modifier,
    initialPaths: List<SignaturePath> = emptyList(),
    canvasHeight: Dp = 240.dp,
    technicianSpecialty: String? = null,
    closingTime: String? = null,
    onConfirmSignature: (() -> Unit)? = null,
    testTag: String = "signature_canvas"
) {
    val strokes = remember { mutableStateListOf<SignaturePath>().apply { addAll(initialPaths) } }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Blanco)
            .border(1.2.dp, GrisBorde, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado del panel de firma
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = AzulMarca
                    )
                )
                Text(
                    text = "$signerName • $signerRole",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = AzulCorporativo,
                        fontSize = 12.sp
                    )
                )
            }

            // Badge de estado
            val isSigned = strokes.isNotEmpty() || currentPoints.isNotEmpty()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSigned) VerdeExitoFondo else CelesteAcentoClaro
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSigned) Icons.Rounded.Check else Icons.Rounded.Draw,
                        contentDescription = null,
                        tint = if (isSigned) VerdeExito else CelesteAcento,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isSigned) "Firmado" else "Pendiente",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (isSigned) VerdeExito else CelesteAcento,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Área táctil de firma espaciosa (240dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(canvasHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Blanco)
                .border(1.2.dp, Color(0xFFD6DFEC), RoundedCornerShape(12.dp))
                .testTag(testTag)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPoints = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPoints = currentPoints + change.position
                            },
                            onDragEnd = {
                                if (currentPoints.isNotEmpty()) {
                                    strokes.add(SignaturePath(currentPoints))
                                    currentPoints = emptyList()
                                    onSignatureChanged(strokes.isNotEmpty(), strokes.toList())
                                }
                            },
                            onDragCancel = {
                                currentPoints = emptyList()
                            }
                        )
                    }
            ) {
                // Línea punteada sutil de base de firma
                val baselineY = size.height * 0.78f
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(24f, baselineY),
                    end = Offset(size.width - 24f, baselineY),
                    strokeWidth = 1.8f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )

                // Dibujar trazos guardados
                strokes.forEach { stroke ->
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = AzulMarca,
                            style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    } else if (stroke.points.isNotEmpty()) {
                        drawCircle(
                            color = AzulMarca,
                            radius = 2.5f,
                            center = stroke.points.first()
                        )
                    }
                }

                // Dibujar trazo en curso
                if (currentPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = AzulMarca,
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // Marca de agua si está vacío
            if (strokes.isEmpty() && currentPoints.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Draw,
                        contentDescription = null,
                        tint = CelesteAcento.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Trace su rúbrica técnica aquí con el dedo o stylus",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario.copy(alpha = 0.7f),
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }

        // Aclaración automática de rúbrica
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = GrisNeutro,
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde),
            modifier = Modifier.fillMaxWidth()
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
                        text = "Aclaración: $signerName",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = AzulMarca,
                            fontSize = 11.5.sp
                        )
                    )
                    closingTime?.let { time ->
                        Text(
                            text = "Hora de Cierre: $time",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = AzulCorporativo,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                technicianSpecialty?.let { specialty ->
                    Text(
                        text = "Especialidad: $specialty",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = PoppinsFontFamily,
                            color = GrisTextoSecundario,
                            fontSize = 11.sp
                        )
                    )
                }
                Text(
                    text = "Validez y dictamen conforme al Decreto 406/88 (MTSS Uruguay)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = PoppinsFontFamily,
                        color = VerdeExito,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp
                    )
                )
            }
        }

        // Barra inferior con botones: "Limpiar trazo" y opcionalmente "Validar y Estampar Firma"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    strokes.clear()
                    currentPoints = emptyList()
                    onSignatureChanged(false, emptyList())
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AzulCorporativo),
                modifier = Modifier.height(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Limpiar trazo",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            if (onConfirmSignature != null) {
                androidx.compose.material3.Button(
                    onClick = onConfirmSignature,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (strokes.isNotEmpty()) VerdeExito else CelesteAcento,
                        contentColor = Blanco
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Validar y Estampar Firma",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
