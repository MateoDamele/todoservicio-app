package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Security
import com.example.model.UserAccount
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppDestination
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

/**
 * CustomAppDrawer: Menú lateral corporativo de Todoservicio.
 * Diseñado con criterios de alta gama, máxima accesibilidad (touch targets >= 48dp)
 * e identidad visual estricta según el Manual de Marca oficial.
 */
@Composable
fun CustomAppDrawer(
    currentDestination: AppDestination,
    clientProfile: ClientProfile = remember { ClientProfile() },
    currentUser: UserAccount? = null,
    onDestinationSelected: (AppDestination) -> Unit,
    onLogout: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isIdentityExpanded by remember { mutableStateOf(false) }
    val drawerScrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .width(340.dp)
            .fillMaxHeight(),
        color = Blanco,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(drawerScrollState)
        ) {
            // ================================================================
            // 1. CABECERA DEL DRAWER (DrawerHeader)
            // ================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AzulMarca, AzulCorporativo)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Isotipo / Isologotipo en caja blanca redondeada de contraste
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(6.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp)),
                            color = Blanco
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.todoservicio_icono_t),
                                contentDescription = "Logo de Todoservicio",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Column {
                            Text(
                                text = "TODOSERVICIO",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Blanco
                                )
                            )
                            Text(
                                text = "Facility Management",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CelesteAcento,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Blanco.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )

                    // Datos del cliente / empresa y rol actual
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(CelesteAcento, CircleShape)
                            )
                            Text(
                                text = currentUser?.companyName ?: clientProfile.companyName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Blanco,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "${currentUser?.fullName ?: clientProfile.clientName} • ${currentUser?.role?.label ?: clientProfile.role}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Blanco.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Badge de normativa Dec. 406/88
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(
                                    color = Blanco.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = CelesteAcento,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Habilitación Dec. 406/88 Uruguay",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Blanco,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ================================================================
            // 2. LISTA DE OPCIONES DE NAVEGACIÓN (ListTiles con min 48dp)
            // ================================================================
            val clientDestinations = listOf(
                AppDestination.INICIO,
                AppDestination.PLAN_MENSUAL,
                AppDestination.SOLICITAR_TRABAJO,
                AppDestination.CALENDARIO,
                AppDestination.MAPA_GPS,
                AppDestination.DOBLE_FIRMA,
                AppDestination.HISTORIAL_SERVICIOS,
                AppDestination.SOPORTE_CONSULTAS
            )
            val destinationsToShow = if (currentUser?.role == com.example.model.UserRole.ADMIN || currentUser?.role == com.example.model.UserRole.SUPERADMIN) {
                AppDestination.values().toList()
            } else {
                clientDestinations
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                destinationsToShow.forEachIndexed { index, destination ->
                    val isSelected = currentDestination == destination

                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1f,
                        animationSpec = tween(durationMillis = 150, easing = EaseOutCubic),
                        label = "drawerItemScale_${destination.name}"
                    )

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) CelesteAcentoClaro.copy(alpha = 0.5f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "drawerItemBg"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) AzulMarca else AzulCorporativo,
                        animationSpec = tween(durationMillis = 200),
                        label = "drawerItemText"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) CelesteAcento else AzulCorporativo,
                        animationSpec = tween(durationMillis = 200),
                        label = "drawerItemIcon"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Color(0x12102C54),
                                        spotColor = Color(0x20102C54)
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDestinationSelected(destination)
                            }
                            .minimumInteractiveComponentSize()
                            .testTag(destination.testTag),
                        color = if (isSelected) Blanco else backgroundColor,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.2.dp, CelesteAcento) else null,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Badge de orden numérico secuencial 1..7
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (isSelected) CelesteAcento else GrisNeutro,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Blanco else GrisTextoSecundario
                                    )
                                )
                            }

                            // Icono en caja estilizada
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = if (isSelected) CelesteAcentoClaro else GrisNeutro,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Título y Subtítulo
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = contentColor,
                                        fontSize = 12.5.sp
                                    )
                                )
                                Text(
                                    text = destination.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        color = GrisTextoSecundario,
                                        fontSize = 10.5.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = CelesteAcento,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (onLogout != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLogout()
                            }
                            .testTag("drawer_item_logout"),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        color = RojoUrgencia.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ExitToApp,
                                    contentDescription = "Cerrar Sesión",
                                    tint = RojoUrgencia,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cerrar Sesión",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = RojoUrgencia
                                    )
                                )
                                Text(
                                    text = "Salir de la cuenta actual",
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

            Spacer(modifier = Modifier.height(16.dp))

            // ================================================================
            // 3. SECCIÓN FIJA EN EL PIE: "Nuestra Identidad"
            // ================================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = GrisBorde,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    color = GrisNeutro,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Título y botón interactivo para desplegar Misión y Visión
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isIdentityExpanded = !isIdentityExpanded
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Nuestra Identidad",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CelesteAcento,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                                Text(
                                    text = "Todoservicio - Facility Management",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = PoppinsFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AzulMarca
                                    )
                                )
                            }

                            Icon(
                                imageVector = if (isIdentityExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (isIdentityExpanded) "Ocultar" else "Ver Misión y Visión",
                                tint = AzulCorporativo,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Contenido desplegable: Misión y Visión oficiales
                        AnimatedVisibility(
                            visible = isIdentityExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Misión
                                Surface(
                                    color = Blanco,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "MISIÓN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulCorporativo
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Proveer soluciones integrales de mantenimiento y gestión de infraestructura que garanticen la continuidad operativa, la seguridad y la valorización de los activos de nuestros clientes.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = AzulMarca,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            )
                                        )
                                    }
                                }

                                // Visión
                                Surface(
                                    color = Blanco,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "VISIÓN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = AzulCorporativo
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Consolidarnos como la plataforma de servicios de referencia, reconocidos por la excelencia técnica, la innovación y el compromiso normativo.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                color = AzulMarca,
                                                fontSize = 11.5.sp,
                                                lineHeight = 16.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pilares Estratégicos",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GrisTextoSecundario,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Chips / Badges horizontales con los 4 Pilares Estratégicos
                        val strategicPillars = listOf(
                            "Excelencia Técnica",
                            "Seguridad (Dec. 406/88)",
                            "Transparencia",
                            "Adaptabilidad"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            strategicPillars.forEach { pillar ->
                                Surface(
                                    color = Blanco,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CelesteAcento.copy(alpha = 0.6f)),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = CelesteAcento,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = pillar,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = PoppinsFontFamily,
                                                fontWeight = FontWeight.Medium,
                                                color = AzulCorporativo,
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

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
