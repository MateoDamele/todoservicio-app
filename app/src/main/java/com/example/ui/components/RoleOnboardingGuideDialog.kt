package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.UserRole
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisNeutro
import com.example.ui.theme.GrisPerlaFrio
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeExito
import kotlinx.coroutines.launch

data class GuideFlyerItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tagLabel: String,
    val iconGradient: List<Color> = listOf(AzulMarca, AzulCorporativo)
)

/**
 * Sistema de Guía Interactiva Integrada ("Flyers / Onboarding en Modal").
 * Carrusel deslizante de 3 flyers adaptado al rol del usuario activo con navegación por dots.
 */
@Composable
fun RoleOnboardingGuideDialog(
    role: UserRole,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val flyers = when (role) {
        UserRole.ADMIN, UserRole.SUPERADMIN -> listOf(
            GuideFlyerItem(
                title = "Calendario Operativo",
                description = "Asigna y organiza visitas técnicas por fecha y horario con sincronización instantánea.",
                icon = Icons.Rounded.CalendarMonth,
                tagLabel = "Planificación Central",
                iconGradient = listOf(AzulMarca, AzulCorporativo)
            ),
            GuideFlyerItem(
                title = "Monitoreo GPS y Geocercas",
                description = "Audita la flota en calle y el personal de limpieza con perímetro verificado de 50 metros.",
                icon = Icons.Rounded.CleaningServices,
                tagLabel = "Auditoría en Sitio",
                iconGradient = listOf(AzulCorporativo, CelesteAcento)
            ),
            GuideFlyerItem(
                title = "Clientes Multi-Sede",
                description = "Da de alta empresas y sus locales con estricto aislamiento de datos y reportes.",
                icon = Icons.Rounded.Business,
                tagLabel = "Gestión Multi-Tenant",
                iconGradient = listOf(AzulMarca, CelesteAcento)
            )
        )

        UserRole.TECNICO -> listOf(
            GuideFlyerItem(
                title = "Órdenes Asignadas",
                description = "Tu hoja de ruta del día con prioridad, horarios y navegación directa a Google Maps.",
                icon = Icons.Rounded.AssignmentTurnedIn,
                tagLabel = "Jornada de Campo",
                iconGradient = listOf(AzulMarca, AzulCorporativo)
            ),
            GuideFlyerItem(
                title = "Evidencia Fotográfica",
                description = "Captura tomas de antes y después respaldando el cumplimiento del Decreto 406/88.",
                icon = Icons.Rounded.CameraAlt,
                tagLabel = "Respaldo Pericial",
                iconGradient = listOf(AzulCorporativo, CelesteAcento)
            ),
            GuideFlyerItem(
                title = "Rúbrica Técnica Digital",
                description = "Firma digital sobre lienzo ampliado con emisión instantánea de la OT para el cliente.",
                icon = Icons.Rounded.Draw,
                tagLabel = "Cierre Oficial",
                iconGradient = listOf(VerdeExito, AzulCorporativo)
            )
        )

        UserRole.LIMPIEZA -> listOf(
            GuideFlyerItem(
                title = "Marcación con Geocerca",
                description = "Registra tu Entrada y Salida con un solo toque; el sistema valida tu ubicación exacta.",
                icon = Icons.Rounded.LocationOn,
                tagLabel = "Check-in Seguro",
                iconGradient = listOf(VerdeExito, CelesteAcento)
            ),
            GuideFlyerItem(
                title = "Control de Horas",
                description = "Monitorea tus horas acumuladas en tiempo real y consulta los edificios asignados.",
                icon = Icons.Rounded.Schedule,
                tagLabel = "Jornada Transparente",
                iconGradient = listOf(AzulMarca, AzulCorporativo)
            ),
            GuideFlyerItem(
                title = "Pedido de Insumos",
                description = "Solicita materiales a Camilo por WhatsApp con un solo botón directo y sin demoras.",
                icon = Icons.Rounded.Send,
                tagLabel = "Suministros Rápidos",
                iconGradient = listOf(AzulCorporativo, CelesteAcento)
            )
        )

        UserRole.CLIENTE -> listOf(
            GuideFlyerItem(
                title = "Técnico en Camino",
                description = "Monitorea en mapa real el trayecto y tiempo estimado de llegada de tu cuadrilla asignada.",
                icon = Icons.Rounded.Speed,
                tagLabel = "Rastreo GPS en Vivo",
                iconGradient = listOf(AzulMarca, CelesteAcento)
            ),
            GuideFlyerItem(
                title = "Reportes Técnicos Oficiales",
                description = "Descarga de inmediato los certificados A4 en PDF firmados por el ingeniero actuante.",
                icon = Icons.Rounded.PictureAsPdf,
                tagLabel = "Historial & Certificados",
                iconGradient = listOf(AzulCorporativo, VerdeExito)
            ),
            GuideFlyerItem(
                title = "Solicitud de Servicios",
                description = "Genera pedidos de cotización o guardias 24/7 para tus locales con presupuesto ágil.",
                icon = Icons.Rounded.Star,
                tagLabel = "Atención Inmediata",
                iconGradient = listOf(AzulMarca, AzulCorporativo)
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { flyers.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        ApplyWindowBackdropBlur()
        GlassDialogEntrance {
            Surface(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(0.92f)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Blanco,
                border = BorderStroke(1.dp, GrisBorde),
                shadowElevation = 18.dp
            ) {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Barra superior de la Guía
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AzulMarca.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "GUÍA RÁPIDA • ${role.label.uppercase()}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GrisNeutro, CircleShape)
                            .testTag("btn_close_guide_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = AzulMarca,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Carrusel de Flyers
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                ) { pageIndex ->
                    val flyer = flyers[pageIndex]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Ícono Vectorial Grande con Degradado Corporativo
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Brush.linearGradient(flyer.iconGradient)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = flyer.icon,
                                contentDescription = flyer.title,
                                tint = Blanco,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GrisPerlaFrio,
                            border = BorderStroke(1.dp, GrisBorde)
                        ) {
                            Text(
                                text = flyer.tagLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AzulCorporativo,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = flyer.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = AzulMarca,
                                fontSize = 16.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = flyer.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PoppinsFontFamily,
                                color = GrisTextoSecundario,
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Indicador de Puntos (Dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(flyers.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val widthAnim by animateDpAsState(
                            targetValue = if (isSelected) 22.dp else 8.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "dotWidth"
                        )
                        val colorAnim by animateColorAsState(
                            targetValue = if (isSelected) CelesteAcento else GrisBorde,
                            animationSpec = tween(durationMillis = 200),
                            label = "dotColor"
                        )

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(widthAnim)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colorAnim)
                                .clickable {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de Acción Principal ("Siguiente" / "Entendido / Continuar")
                val isLastPage = pagerState.currentPage == flyers.size - 1

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isLastPage) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Omitir",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = PoppinsFontFamily,
                                    color = GrisTextoSecundario,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isLastPage) {
                                onDismiss()
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastPage) VerdeExito else AzulMarca,
                            contentColor = Blanco
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(if (isLastPage) 2f else 1.4f)
                            .height(44.dp)
                            .testTag("btn_guide_action")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isLastPage) "Entendido" else "Siguiente",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            )
                            Icon(
                                imageVector = if (isLastPage) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
