package com.example.data

import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object SupabaseCloudSync {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val syncExecutor = Executors.newSingleThreadExecutor()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun baseUrl(): String = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
    private fun anonKey(): String = BuildConfig.SUPABASE_ANON_KEY.trim()

    fun isConfigured(): Boolean {
        val url = baseUrl()
        val key = anonKey()
        return url.startsWith("https://") && key.length > 20
    }

    fun pushStateJson(storeKey: String, rawJson: String) {
        if (!isConfigured()) return
        syncExecutor.execute {
            try {
                val wrapper = JSONObject().apply {
                    put("store_key", storeKey)
                    put("payload", JSONObject(rawJson))
                    put("updated_at", java.time.Instant.now().toString())
                }

                val request = Request.Builder()
                    .url("${baseUrl()}/rest/v1/todoservicio_sync_state?on_conflict=store_key")
                    .addHeader("apikey", anonKey())
                    .addHeader("Authorization", "Bearer ${anonKey()}")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                    .post(wrapper.toString().toRequestBody(jsonMediaType))
                    .build()

                client.newCall(request).execute().close()
            } catch (_: Exception) {
                // Mantiene respaldo local si no hay señal momentánea
            }
        }
    }

    fun pullStateJson(storeKey: String): String? {
        if (!isConfigured()) return null
        return try {
            val request = Request.Builder()
                .url("${baseUrl()}/rest/v1/todoservicio_sync_state?store_key=eq.$storeKey&select=payload")
                .addHeader("apikey", anonKey())
                .addHeader("Authorization", "Bearer ${anonKey()}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val array = JSONArray(body)
                if (array.length() == 0) return null
                val payloadObj = array.getJSONObject(0).optJSONObject("payload") ?: return null
                if (payloadObj.length() == 0) return null
                payloadObj.toString()
            }
        } catch (_: Exception) {
            null
        }
    }
}
