package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

/**
 * Coordenada extraída y normalizada desde un enlace o texto de Google Maps.
 */
data class ParsedGeoPoint(
    val lat: Double,
    val lng: Double,
    val placeLabel: String? = null,
    val sourceUrl: String = ""
)

sealed interface GoogleMapsParseResult {
    data class Success(val point: ParsedGeoPoint) : GoogleMapsParseResult
    data class Error(val message: String) : GoogleMapsParseResult
}

/**
 * Parser robusto de enlaces de Google Maps (enlaces largos, place/@, !3d!4d, ?q=,
 * coordenadas puras y enlaces acortados maps.app.goo.gl / goo.gl/maps).
 */
object GoogleMapsUrlParser {

    private val exactPinRegex = Regex("""!3d(-?\d{1,2}\.\d+)!4d(-?\d{1,3}\.\d+)""")
    private val altPinRegex = Regex("""!2d(-?\d{1,3}\.\d+)!3d(-?\d{1,2}\.\d+)""")
    private val atCoordsRegex = Regex("""@(-?\d{1,2}\.\d+),(-?\d{1,3}\.\d+)""")
    private val queryCoordsRegex = Regex(
        """(?:[?&](?:q|query|ll|center|sll|destination|daddr)=)(-?\d{1,2}\.\d+)(?:,|%2C|\+|%20|\s)+(-?\d{1,3}\.\d+)""",
        RegexOption.IGNORE_CASE
    )
    private val ogCenterRegex = Regex(
        """center=(-?\d{1,2}\.\d+)(?:%2C|,)(-?\d{1,3}\.\d+)""",
        RegexOption.IGNORE_CASE
    )
    private val rawLatLngRegex = Regex("""^\s*(-?\d{1,2}\.\d+)\s*[,;\s]\s*(-?\d{1,3}\.\d+)\s*$""")
    private val placeNameRegex = Regex("""/maps/place/([^/@?]+)""")

    fun isShortGoogleMapsUrl(rawInput: String): Boolean {
        val lower = rawInput.trim().lowercase()
        return lower.contains("maps.app.goo.gl/") ||
            lower.contains("goo.gl/maps/") ||
            lower.contains("g.co/maps/")
    }

    fun looksLikeUrl(rawInput: String): Boolean {
        val trimmed = rawInput.trim().lowercase()
        return trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.contains("google.com/maps") ||
            trimmed.contains("maps.google.") ||
            isShortGoogleMapsUrl(trimmed)
    }

    /**
     * Extrae coordenadas de forma síncrona cuando el texto ya contiene la latitud y longitud.
     */
    fun parseCoordinatesFromText(rawInput: String): ParsedGeoPoint? {
        val cleaned = rawInput.trim()
        if (cleaned.isBlank()) return null

        val decoded = try {
            URLDecoder.decode(cleaned, "UTF-8")
        } catch (_: Exception) {
            cleaned
        }

        val placeLabel = extractPlaceLabel(cleaned)

        // 1. Prioridad máxima: pin exacto del comercio/edificio (!3dLAT!4dLNG)
        exactPinRegex.findAll(decoded).lastOrNull()?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, placeLabel, cleaned)
            }
        }

        // 2. Variante inversa (!2dLNG!3dLAT)
        altPinRegex.findAll(decoded).lastOrNull()?.let { match ->
            val lng = match.groupValues[1].toDoubleOrNull()
            val lat = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, placeLabel, cleaned)
            }
        }

        // 3. Parámetros de consulta (?q=lat,lng, ?ll=lat,lng, ?query=lat,lng)
        queryCoordsRegex.find(cleaned)?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, placeLabel, cleaned)
            }
        }

        // 4. Centro de vista (@lat,lng,zoom)
        atCoordsRegex.find(decoded)?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, placeLabel, cleaned)
            }
        }

        // 5. OpenGraph / staticmap center=lat,lng
        ogCenterRegex.find(cleaned)?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, placeLabel, cleaned)
            }
        }

        // 6. Coordenadas puras ingresadas directamente ("-34.9011, -56.1645")
        rawLatLngRegex.find(decoded)?.let { match ->
            val lat = match.groupValues[1].toDoubleOrNull()
            val lng = match.groupValues[2].toDoubleOrNull()
            if (isValidLatLng(lat, lng)) {
                return ParsedGeoPoint(lat!!, lng!!, null, cleaned)
            }
        }

        return null
    }

    /**
     * Resuelve enlaces acortados (maps.app.goo.gl / goo.gl/maps) siguiendo redirecciones HTTP
     * en background y extrae las coordenadas exactas.
     */
    suspend fun resolveAndParseGoogleMapsUrl(rawInput: String): GoogleMapsParseResult {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank()) {
            return GoogleMapsParseResult.Error("Pegá un enlace de Google Maps válido")
        }

        parseCoordinatesFromText(trimmed)?.let { direct ->
            return GoogleMapsParseResult.Success(direct)
        }

        if (!looksLikeUrl(trimmed)) {
            return GoogleMapsParseResult.Error("No se encontraron coordenadas en el texto ingresado")
        }

        val normalizedUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }

        return withContext(Dispatchers.IO) {
            try {
                var currentUrl = normalizedUrl
                var finalBodySnippet = ""

                for (hop in 0 until 6) {
                    val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        connectTimeout = 6000
                        readTimeout = 6000
                        requestMethod = "GET"
                        setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0 Mobile Safari/537.36"
                        )
                    }

                    val code = conn.responseCode
                    if (code in 300..399) {
                        val locationHeader = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (locationHeader.isNullOrBlank()) break
                        currentUrl = URL(URL(currentUrl), locationHeader).toString()
                        parseCoordinatesFromText(currentUrl)?.let { resolved ->
                            return@withContext GoogleMapsParseResult.Success(resolved.copy(sourceUrl = normalizedUrl))
                        }
                    } else {
                        finalBodySnippet = try {
                            conn.inputStream.bufferedReader().use { reader ->
                                val buf = CharArray(32768)
                                val readCount = reader.read(buf)
                                if (readCount > 0) String(buf, 0, readCount) else ""
                            }
                        } catch (_: Exception) {
                            ""
                        } finally {
                            conn.disconnect()
                        }
                        break
                    }
                }

                parseCoordinatesFromText(currentUrl)?.let {
                    return@withContext GoogleMapsParseResult.Success(it.copy(sourceUrl = normalizedUrl))
                }

                if (finalBodySnippet.isNotBlank()) {
                    parseCoordinatesFromText(finalBodySnippet)?.let {
                        return@withContext GoogleMapsParseResult.Success(it.copy(sourceUrl = normalizedUrl))
                    }
                }

                GoogleMapsParseResult.Error(
                    "No se pudieron extraer coordenadas del enlace. Abrí el punto en Google Maps y compartí el enlace con ubicación."
                )
            } catch (_: Exception) {
                GoogleMapsParseResult.Error(
                    "Sin conexión para expandir el enlace corto. Pegá un enlace completo (@lat,lng) o verificá tu red."
                )
            }
        }
    }

    private fun extractPlaceLabel(url: String): String? {
        val match = placeNameRegex.find(url) ?: return null
        val encoded = match.groupValues[1]
        return try {
            URLDecoder.decode(encoded.replace("+", " "), "UTF-8")
                .trim()
                .takeIf { it.isNotBlank() && !it.startsWith("@") }
        } catch (_: Exception) {
            null
        }
    }

    private fun isValidLatLng(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        return lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0)
    }
}
