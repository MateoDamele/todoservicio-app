package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// PALETA CORPORATIVA OFICIAL - TODOSERVICIO (IDENTIDAD Todoservicio_Logo_HD)
// ============================================================================

// 1. Azul Marino Institucional (Primary Brand - tono exacto del isotipo/logo oficial)
val AzulMarca = Color(0xFF0D284E)
val AzulHeaderFin = Color(0xFF163A6C)

// 2. Azul Corporativo Medio (Secondary / Interactive)
val AzulCorporativo = Color(0xFF1E4B82)

// 3. Acentos Interactivos y Estados Activos
val CelesteAcento = Color(0xFF1E4B82)
val CelesteAcentoClaro = Color(0xFFEAF0F8)
val CelesteAcentoHover = Color(0xFF173B68)

// 4. Gris Hielo Ejecutivo (Background) y Blanco Puro (Surface / Contrast)
val GrisNeutro = Color(0xFFF4F7FA)
val GrisSuperficie = Color(0xFFFFFFFF)

// 4.1 Modo Oscuro Opcional
val DarkSlateFondo = Color(0xFF081426)
val AzulNocheTarjetas = Color(0xFF0E1F38)
val AzulNocheBorde = Color(0xFF1D3354)
val AzulNocheSuperficieVariante = Color(0xFF142947)

// 5. Gris Pizarra Neutro (Typography / Borders)
val GrisGrafito = Color(0xFF334155)
val GrisBorde = Color(0xFFE2E8F0)
val GrisTextoSecundario = Color(0xFF64748B)
val GrisTextoTenue = Color(0xFF94A3B8)
val Blanco = Color(0xFFFFFFFF)
val SombraDifusaColor = Color(0x140D284E)

// 6. Glassmorphism & Motion Blur Tokens
val GlassWhiteSurface = Color(0xD9FFFFFF)
val GlassWhiteBorder = Color(0x99FFFFFF)
val ModalBlurScrim = Color(0x330D284E)

val InstitutionalHeaderBrush: Brush
    get() = Brush.linearGradient(
        colors = listOf(AzulMarca, AzulHeaderFin)
    )

// 7. Acentos de Estado (Desaturados y Elegantes)
val VerdeEsmeralda = Color(0xFF10B981)
val VerdeExito = Color(0xFF10B981)
val VerdeExitoFondo = Color(0xFFE8F7F1)

val AmbarCalido = Color(0xFFF59E0B)
val NaranjaAlerta = Color(0xFFF59E0B)
val NaranjaAlertaFondo = Color(0xFFFEF6E6)
val AmarilloAdvertencia = Color(0xFFF59E0B)

val RojoUrgencia = Color(0xFFDC2626)
val RojoUrgenciaFondo = Color(0xFFFEE2E2)
val RojoError = Color(0xFFDC2626)

// Superficies auxiliares ejecutivas
val GrisPerlaFrio = Color(0xFFF4F7FA)
val GrisTextoDespejado = Color(0xFF94A3B8)
