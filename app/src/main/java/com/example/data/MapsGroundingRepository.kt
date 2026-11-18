package com.example.data

import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Fuente verificada de Google Maps asociada a una consulta geoespacial.
 */
data class MapsGroundingSource(
    val title: String,
    val uri: String,
    val placeId: String? = null
)

/**
 * Resultado de una consulta de Maps Grounding.
 * Cuando Firebase AI Logic no está habilitado, [isDemoPlaceholder] es true y
 * la UI muestra el aviso correspondiente.
 */
data class MapsGroundingResult(
    val answer: String,
    val sources: List<MapsGroundingSource>,
    val isDemoPlaceholder: Boolean,
    val notice: String? = null
)

/**
 * Contrato único para consultas de ubicación, sedes y referencias mediante Maps Grounding.
 */
interface MapsGroundingRepository {
    suspend fun queryLocationContext(
        query: String,
        lat: Double,
        lng: Double
    ): MapsGroundingResult
}

/**
 * Implementación de respaldo cuando Firebase AI Logic no está habilitado en el proyecto.
 * Retorna respuestas de demostración claramente etiquetadas sin realizar llamadas directas
 * con claves de API de Gemini en el cliente.
 */
class PlaceholderMapsGroundingRepository : MapsGroundingRepository {

    override suspend fun queryLocationContext(
        query: String,
        lat: Double,
        lng: Double
    ): MapsGroundingResult {
        delay(250)
        val cleanQuery = query.trim().ifBlank { "Referencias operativas cercanas" }
        val lower = cleanQuery.lowercase()

        val (summary, placeTitle, queryTerm) = when {
            lower.contains("plaza independencia") || lower.contains("centro") -> Triple(
                "Plaza Independencia 838 se ubica en el límite entre Centro y Ciudad Vieja, con acceso vehicular por Av. 18 de Julio y Juncal. Estacionamiento tarifado en laterales y parada de ómnibus a menos de 100 m.",
                "Plaza Independencia, Montevideo",
                "Plaza Independencia 838, Montevideo, Uruguay"
            )
            lower.contains("pocitos") || lower.contains("21 de setiembre") || lower.contains("rivera") -> Triple(
                "Zona Pocitos / Av. Rivera y 21 de Setiembre cuenta con alta densidad residencial y comercial. Acceso rápido por Bv. España o Rambla República del Perú; geocerca recomendada de 80 m para edificios de propiedad horizontal.",
                "Pocitos Central, Montevideo",
                "21 de Setiembre 2840, Montevideo, Uruguay"
            )
            lower.contains("prado") || lower.contains("suárez") || lower.contains("suarez") -> Triple(
                "Av. Joaquín Suárez al 3150 (Prado) conecta directamente con Av. Agraciada y Bv. Artigas. Zona de fácil estacionamiento para furgonetas técnicas y descarga de equipos HVAC.",
                "Prado, Montevideo",
                "Av. Joaquín Suárez 3150, Montevideo, Uruguay"
            )
            lower.contains("wtc") || lower.contains("buceo") -> Triple(
                "Complejo WTC Free Zone (Dr. Luis Bonavita 1294, Buceo) dispone de ingreso técnico coordinado por Luis A. de Herrera y 26 de Marzo. Se recomienda validar acceso vehicular previo.",
                "WTC Montevideo Free Zone",
                "Dr. Luis Bonavita 1294, Montevideo, Uruguay"
            )
            lower.contains("giannattasio") || lower.contains("costa") || lower.contains("solymar") -> Triple(
                "Corredor Av. Giannattasio km 21.500 (Solymar, Ciudad de la Costa): acceso directo por calzada lateral y rotonda de retorno. Tránsito fluido fuera de horas pico (08:00–09:30 y 17:30–19:00).",
                "Av. Giannattasio km 21.5, Solymar",
                "Av. Giannattasio km 21.5, Ciudad de la Costa, Canelones, Uruguay"
            )
            else -> Triple(
                "Referencia de ejemplo para \"$cleanQuery\" cerca de (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}) en el área metropolitana de Montevideo / Canelones.",
                "Montevideo y Área Metropolitana",
                "$cleanQuery Montevideo Uruguay"
            )
        }

        val encodedQuery = URLEncoder.encode(queryTerm, StandardCharsets.UTF_8.name())
        val mapsUri = "https://www.google.com/maps/search/?api=1&query=$encodedQuery"

        return MapsGroundingResult(
            answer = "[Respuesta de ejemplo] $summary",
            sources = listOf(
                MapsGroundingSource(
                    title = placeTitle,
                    uri = mapsUri
                )
            ),
            isDemoPlaceholder = true,
            notice = "Aviso: Las funciones de IA con Maps Grounding requieren activar Firebase AI Logic. Mostrando datos de muestra."
        )
    }
}

object MapsGroundingServiceLocator {
    val repository: MapsGroundingRepository by lazy {
        PlaceholderMapsGroundingRepository()
    }
}
