package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AzulMarca
import com.example.ui.theme.Blanco
import com.example.ui.theme.CelesteAcento
import com.example.ui.theme.InstitutionalHeaderBrush
import com.example.ui.theme.PoppinsFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Reestructuración del Splash Screen (Animación "Logo Reveal" limpia de 1.2s).
 * - REGLA ESTRICTA DE ASSETS: Utiliza ÚNICA Y EXCLUSIVAMENTE "Todoservicio_Logo_HD.png".
 * - Fondo completo: Azul Marca corporativo (#102C54).
 * - Secuencia: FadeIn + Scale (0.0s - 0.5s), Shimmer / brillo sutil de barrido (0.5s - 1.1s), Transición al Login a los 1.2s.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.9f) }
    val alphaAnim = remember { Animatable(0f) }
    val shimmerAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500)
            )
        }
        launch {
            delay(500)
            shimmerAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        // Transición estricta a los 1.2 segundos
        delay(1200)
        onTimeout()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(InstitutionalHeaderBrush)
            .statusBarsPadding()
            .padding(24.dp)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Imagotipo oficial único "Todoservicio_Logo_HD.png" con Scale y FadeIn
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .scale(scaleAnim.value)
                    .alpha(alphaAnim.value)
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                color = Blanco
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.todoservicio_logo_hd),
                        contentDescription = "Todoservicio Imagotipo Oficial",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Facility Management & Mantenimiento Integral",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = PoppinsFontFamily,
                    color = CelesteAcento,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }

        // Pie institucional
        Text(
            text = "Uruguay • Dec. 406/88 MTSS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = PoppinsFontFamily,
                color = Blanco.copy(alpha = 0.5f),
                fontSize = 11.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
