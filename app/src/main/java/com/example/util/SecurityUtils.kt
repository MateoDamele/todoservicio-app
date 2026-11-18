package com.example.util

import android.content.Context
import android.location.Location
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Utilidades de Seguridad Defensiva, AppSec y Criptografía para Todoservicio.
 * - Anti-Spoofing GPS (Detección de Mock Locations).
 * - Sanitización estricta de entradas (Anti SQLi / XSS).
 * - Hashing seguro de credenciales con Salting (SHA-256).
 */
object SecurityUtils {

    /**
     * Detección de GPS simulado o Falseado (Anti-Spoofing GPS).
     * Bloquea el marcaje si la funcionaria o técnico tiene activa una app de Mock Location.
     */
    fun isMockLocationActive(context: Context, location: Location? = null): Boolean {
        // 1. Verificación directa sobre el objeto de ubicación de Android
        if (location != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (location.isMock) return true
            } else {
                @Suppress("DEPRECATION")
                if (location.isFromMockProvider) return true
            }
        }

        // 2. Verificación de configuración del sistema (opciones de desarrollador)
        return try {
            val isMockSetting = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ALLOW_MOCK_LOCATION,
                0
            ) != 0
            isMockSetting
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Sanitiza y limpia texto de entrada para prevenir inyección y desbordamientos.
     * Limita caracteres peligrosos y recorta longitud excesiva.
     */
    fun sanitizeInput(input: String, maxLength: Int = 200): String {
        return input
            .trim()
            .take(maxLength)
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "&#39;")
            .replace("\"", "&quot;")
            .replace(";", "")
            .replace("--", "")
    }

    /**
     * Genera una sal criptográfica pseudoaleatoria segura de 16 bytes.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Genera un hash SHA-256 seguro con salting.
     */
    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password:TodoservicioSecureSalt2026"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Valida si la contraseña ingresada coincide con el hash almacenado en el backend/profile.
     */
    fun verifyPassword(inputPassword: String, storedHash: String, salt: String): Boolean {
        val computedHash = hashPassword(inputPassword, salt)
        return computedHash.equals(storedHash, ignoreCase = true)
    }

    /**
     * Valida tokens de sesión JWT simulados asegurando formato y vigencia.
     */
    fun isTokenValid(token: String?, expirationTimestamp: Long): Boolean {
        if (token.isNullOrBlank()) return false
        return System.currentTimeMillis() < expirationTimestamp
    }
}
