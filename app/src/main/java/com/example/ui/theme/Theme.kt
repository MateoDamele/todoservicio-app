package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ============================================================================
// CONFIGURACIÓN DEL TEMA GLOBAL DE TODOSERVICIO
// ============================================================================

private val TodoservicioLightColorScheme = lightColorScheme(
    primary = AzulMarca,
    onPrimary = Blanco,
    primaryContainer = AzulCorporativo,
    onPrimaryContainer = Blanco,
    secondary = AzulCorporativo,
    onSecondary = Blanco,
    secondaryContainer = CelesteAcentoClaro,
    onSecondaryContainer = AzulMarca,
    tertiary = CelesteAcento,
    onTertiary = Blanco,
    tertiaryContainer = CelesteAcentoClaro,
    onTertiaryContainer = AzulMarca,
    background = GrisNeutro,
    onBackground = GrisGrafito,
    surface = GrisSuperficie,
    onSurface = GrisGrafito,
    surfaceVariant = GrisNeutro,
    onSurfaceVariant = GrisTextoSecundario,
    outline = GrisBorde,
    outlineVariant = GrisBorde.copy(alpha = 0.6f),
    error = RojoUrgencia,
    onError = Blanco
)

private val TodoservicioDarkColorScheme = darkColorScheme(
    primary = CelesteAcento,
    onPrimary = Blanco,
    primaryContainer = AzulCorporativo,
    onPrimaryContainer = Blanco,
    secondary = CelesteAcento,
    onSecondary = Blanco,
    secondaryContainer = AzulNocheSuperficieVariante,
    onSecondaryContainer = CelesteAcento,
    tertiary = CelesteAcento,
    onTertiary = Blanco,
    background = DarkSlateFondo,
    onBackground = Blanco,
    surface = AzulNocheTarjetas,
    onSurface = Blanco,
    surfaceVariant = AzulNocheSuperficieVariante,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = AzulNocheBorde,
    outlineVariant = AzulNocheBorde.copy(alpha = 0.6f),
    error = RojoUrgencia,
    onError = Blanco
)

val TodoservicioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun TodoservicioTheme(
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val themeMode by ThemeController.themeMode.collectAsStateWithLifecycle()
    val isDark = forceDarkTheme ?: ThemeController.isDarkThemeActive()

    val targetScheme = if (isDark) TodoservicioDarkColorScheme else TodoservicioLightColorScheme

    // Transición suave de colores para evitar recargas bruscas de pantalla
    val animBackground by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.background,
        animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.EaseOutCubic),
        label = "animBackground"
    )
    val animSurface by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.surface,
        animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.EaseOutCubic),
        label = "animSurface"
    )
    val animSurfaceVariant by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.surfaceVariant,
        animationSpec = androidx.compose.animation.core.tween(240, easing = androidx.compose.animation.core.EaseOutCubic),
        label = "animSurfaceVariant"
    )
    val animPrimary by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.primary,
        animationSpec = androidx.compose.animation.core.tween(350),
        label = "animPrimary"
    )
    val animOnBackground by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.onBackground,
        animationSpec = androidx.compose.animation.core.tween(350),
        label = "animOnBackground"
    )
    val animOnSurface by androidx.compose.animation.animateColorAsState(
        targetValue = targetScheme.onSurface,
        animationSpec = androidx.compose.animation.core.tween(350),
        label = "animOnSurface"
    )

    val animatedColorScheme = targetScheme.copy(
        background = animBackground,
        surface = animSurface,
        surfaceVariant = animSurfaceVariant,
        primary = animPrimary,
        onBackground = animOnBackground,
        onSurface = animOnSurface
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = TodoservicioTypography,
        shapes = TodoservicioShapes,
        content = content
    )
}

