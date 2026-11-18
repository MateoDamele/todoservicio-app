package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Modos de Tema para Todoservicio.
 * - AUTO_HORARIO: Evalúa la hora del dispositivo.
 *   Modo Claro de 05:00 a 18:59 hs (Fondo Gris Neutro #F4F6F9, Tarjetas Blanco #FFFFFF).
 *   Modo Oscuro de 19:00 a 04:59 hs (Fondo Dark Slate #0A1118, Tarjetas Azul Noche #101B27).
 * - MODO_CLARO: Forzado Claro.
 * - MODO_OSCURO: Forzado Oscuro.
 */
enum class ThemeMode(val label: String) {
    AUTO_HORARIO("Automático (05:00 a 18:59 hs Claro / 19:00 a 04:59 hs Oscuro)"),
    MODO_CLARO("Modo Diurno (Claro)"),
    MODO_OSCURO("Modo Nocturno (Dark Slate)")
}

object ThemeController {
    private val _themeMode = MutableStateFlow(ThemeMode.AUTO_HORARIO)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleNextMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.AUTO_HORARIO -> ThemeMode.MODO_OSCURO
            ThemeMode.MODO_OSCURO -> ThemeMode.MODO_CLARO
            ThemeMode.MODO_CLARO -> ThemeMode.AUTO_HORARIO
        }
    }

    /**
     * Determina si el modo oscuro está activo en este momento.
     */
    fun isDarkThemeActive(): Boolean {
        return when (_themeMode.value) {
            ThemeMode.MODO_CLARO -> false
            ThemeMode.MODO_OSCURO -> true
            ThemeMode.AUTO_HORARIO -> {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                // Claro de 05:00 a 18:59 hs; Oscuro de 19:00 a 04:59 hs
                hour !in 5..18
            }
        }
    }

    fun getStatusDescription(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeFormatted = String.format("%02d:%02d hs", hour, minute)
        val isDark = isDarkThemeActive()

        return when (_themeMode.value) {
            ThemeMode.AUTO_HORARIO -> {
                if (isDark) "Auto Nocturno ($timeFormatted • Dark Slate)"
                else "Auto Diurno ($timeFormatted • Gris Neutro)"
            }
            ThemeMode.MODO_CLARO -> "Modo Diurno Fijo"
            ThemeMode.MODO_OSCURO -> "Modo Dark Slate Fijo"
        }
    }
}
