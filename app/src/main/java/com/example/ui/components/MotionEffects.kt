package com.example.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import com.example.ui.theme.AmbarCalido
import com.example.ui.theme.AzulCorporativo
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.GlassWhiteSurface
import com.example.ui.theme.GrisBorde
import com.example.ui.theme.GrisTextoSecundario
import com.example.ui.theme.ModalBlurScrim
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.VerdeEsmeralda

/**
 * Modificador interactivo táctil 3D (micro-contracción 96% al pulsar).
 */
fun Modifier.bouncingClickable(
    enabled: Boolean = true,
    shrinkScale: Float = 0.96f,
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.pointerInput(enabled) {
        if (!enabled) return@pointerInput
    }
)

/**
 * Contenedor con micro-animación elástica táctil a 60fps.
 */
@Composable
fun BouncingTouchSurface(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shrinkScale: Float = 0.96f,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) shrinkScale else 1.0f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 70, easing = FastOutSlowInEasing)
        } else {
            spring(
                dampingRatio = 0.58f,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "bouncing_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val released = try {
                            tryAwaitRelease()
                        } catch (_: Exception) {
                            false
                        }
                        isPressed = false
                        if (released) {
                            onClick()
                        }
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Sombra volumétrica difusa calibrada con el Azul Marino institucional (#0D284E).
 */
fun Modifier.volumetricSoftShadow(
    elevation: Dp = 10.dp,
    shape: Shape = RoundedCornerShape(18.dp)
): Modifier = this.then(
    Modifier.shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color(0x140D284E),
        spotColor = Color(0x220D284E)
    )
)

/**
 * Borde luminoso sutil para tarjetas blancas y superficies cristalinas.
 */
fun Modifier.illuminatedBorder(
    shape: Shape = RoundedCornerShape(18.dp),
    width: Dp = 1.dp
): Modifier = this.then(
    Modifier.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(Blanco, GrisBorde)
        ),
        shape = shape
    )
)

/**
 * Aplica desenfoque gaussiano animado al contenido de fondo mientras un modal/diálogo está activo
 * (Android 12+ hardware RenderEffect blur, con respaldo limpio en versiones previas).
 */
fun Modifier.modalBackdropBlur(
    isActive: Boolean,
    blurRadius: Dp = 6.dp
): Modifier = composed {
    val animatedBlur by animateDpAsState(
        targetValue = if (isActive) blurRadius else 0.dp,
        animationSpec = tween(durationMillis = 190, easing = EaseOutCubic),
        label = "modal_bg_blur"
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && animatedBlur > 0.2.dp) {
        this.blur(radius = animatedBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        this
    }
}

/**
 * Configura desenfoque de ventana nativo (Window Blur Behind) + velo institucional #330D284E
 * detrás de cualquier Dialog / Modal en Android.
 */
@Composable
fun ApplyWindowBackdropBlur(
    scrimColor: Color = ModalBlurScrim,
    blurRadiusPx: Int = 24
) {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        window.setDimAmount(0.22f)
        window.decorView.setBackgroundColor(scrimColor.toArgb())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes = window.attributes.apply {
                this.blurBehindRadius = blurRadiusPx
            }
        }
    }
}

/**
 * Envoltorio con micro-animación de entrada suave (scale + fade) para tarjetas modales.
 */
@Composable
fun GlassDialogEntrance(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.95f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180, easing = EaseOutCubic)
        )
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
    ) {
        content()
    }
}

/**
 * Modelo unificado de pestaña para la Barra de Navegación Inferior Flotante (Glass Bottom Bar).
 */
data class FloatingGlassNavItem<T>(
    val key: T,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

/**
 * Barra de Navegación Inferior Flotante con Glassmorphism (Cristal Esmerilado):
 * - Bordes redondeados de 24.dp, fondo blanco translúcido (85% opacidad),
 *   borde fino luminoso y sombra difusa institucional (#0D284E).
 * - Respeto estricto de `navigationBarsPadding()` para móviles modernos.
 * - Micro-animaciones elásticas en píldora e ícono seleccionado.
 */
@Composable
fun <T> FloatingGlassBottomBar(
    items: List<FloatingGlassNavItem<T>>,
    selectedKey: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = shape,
            color = GlassWhiteSurface,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Blanco.copy(alpha = 0.92f),
                        Color(0x99CBD5E1)
                    )
                )
            ),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = shape,
                    ambientColor = Color(0x220D284E),
                    spotColor = Color(0x300D284E)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item.key == selectedKey
                    val pillBg by animateColorAsState(
                        targetValue = if (isSelected) AzulCorporativo.copy(alpha = 0.12f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
                        label = "navPillBg"
                    )
                    val contentTint by animateColorAsState(
                        targetValue = if (isSelected) AzulMarca else GrisTextoSecundario,
                        animationSpec = tween(durationMillis = 200, easing = EaseOutCubic),
                        label = "navTint"
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = 0.62f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "navIconScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(pillBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item.key)
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = contentTint,
                                modifier = Modifier
                                    .size(21.dp)
                                    .scale(iconScale)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = contentTint,
                                    fontSize = 10.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Punto verde o ámbar pulsante (Pulsing Live Indicator).
 */
@Composable
fun PulsingLiveIndicator(
    modifier: Modifier = Modifier,
    pulseColor: Color = VerdeEsmeralda,
    dotSize: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(dotSize * 2),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dotSize * 1.8f)
                .scale(scale)
                .background(pulseColor.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(pulseColor, CircleShape)
        )
    }
}

/**
 * Badge de Estado Vivo con degradado suave y píldora translúcida.
 */
@Composable
fun LiveStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColorStart, bgColorEnd, borderColor, textColor, isPulsing) = when {
        status.contains("Sitio", ignoreCase = true) ||
            status.contains("En Curso", ignoreCase = true) ||
            status.contains("Activo", ignoreCase = true) -> {
            Tuple5(
                VerdeEsmeralda.copy(alpha = 0.16f),
                VerdeEsmeralda.copy(alpha = 0.06f),
                VerdeEsmeralda.copy(alpha = 0.35f),
                VerdeEsmeralda,
                true
            )
        }
        status.contains("Camino", ignoreCase = true) ||
            status.contains("En Ruta", ignoreCase = true) -> {
            Tuple5(
                AmbarCalido.copy(alpha = 0.18f),
                AmbarCalido.copy(alpha = 0.07f),
                AmbarCalido.copy(alpha = 0.38f),
                AmbarCalido,
                false
            )
        }
        status.contains("Finalizada", ignoreCase = true) ||
            status.contains("Completada", ignoreCase = true) -> {
            Tuple5(
                CelesteAcento.copy(alpha = 0.15f),
                CelesteAcento.copy(alpha = 0.05f),
                CelesteAcento.copy(alpha = 0.30f),
                CelesteAcento,
                false
            )
        }
        else -> {
            Tuple5(
                AzulMarca.copy(alpha = 0.10f),
                AzulMarca.copy(alpha = 0.03f),
                AzulMarca.copy(alpha = 0.20f),
                AzulMarca,
                false
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(listOf(bgColorStart, bgColorEnd))
            )
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isPulsing) {
            PulsingLiveIndicator(dotSize = 7.dp, pulseColor = textColor)
        } else {
            Box(
                modifier = Modifier
                    .size(6.5.dp)
                    .background(textColor, CircleShape)
            )
        }

        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.5.sp
            )
        )
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
