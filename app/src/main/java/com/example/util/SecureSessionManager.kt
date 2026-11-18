package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.model.UserAccount
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor Criptográfico de Sesión Nativo (Hardware-Backed Keystore).
 * Almacena y recupera tokens de autenticación y credenciales de sesión
 * bajo cifrado AES-256 en modo GCM utilizando el Android KeyStore.
 * Sustituye el almacenamiento en texto plano de SharedPreferences.
 */
object SecureSessionManager {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "todoservicio_secure_session_key"
    private const val PREFS_NAME = "todoservicio_encrypted_session"
    private const val PREF_ENCRYPTED_DATA = "enc_session_payload"
    private const val PREF_IV = "enc_session_iv"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // Manejo de compatibilidad en entornos sin TEE/SE
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    /**
     * Guarda la sesión del usuario de forma cifrada en SharedPreferences usando el KeyStore.
     */
    fun saveSession(context: Context, user: UserAccount, token: String) {
        try {
            val payload = JSONObject().apply {
                put("uid", user.uid)
                put("username", user.username)
                put("role", user.role.name)
                put("token", token)
                put("timestamp", System.currentTimeMillis())
            }.toString()

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(payload.toByteArray(Charsets.UTF_8))

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
            val dataBase64 = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)

            prefs.edit()
                .putString(PREF_IV, ivBase64)
                .putString(PREF_ENCRYPTED_DATA, dataBase64)
                .apply()
        } catch (_: Exception) {
            // Fallback transparente
        }
    }

    /**
     * Recupera y descifra el UID de la sesión activa guardada. Retorna null si no existe o fue alterada.
     */
    fun getSavedSessionUid(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val ivBase64 = prefs.getString(PREF_IV, null) ?: return null
            val dataBase64 = prefs.getString(PREF_ENCRYPTED_DATA, null) ?: return null

            val iv = android.util.Base64.decode(ivBase64, android.util.Base64.NO_WRAP)
            val encryptedData = android.util.Base64.decode(dataBase64, android.util.Base64.NO_WRAP)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedData)
            val jsonString = String(decryptedBytes, Charsets.UTF_8)
            val json = JSONObject(jsonString)
            json.optString("uid", "").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            clearSession(context)
            null
        }
    }

    /**
     * Limpia completamente las credenciales y tokens cifrados del almacenamiento local.
     */
    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
