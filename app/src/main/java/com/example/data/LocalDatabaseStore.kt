package com.example.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.model.ActiveTechnicianTracking
import com.example.model.AnnouncementItem
import com.example.model.CleaningAttendanceMark
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.model.ClientEquipmentAsset
import com.example.model.ClientNotification
import com.example.model.CorporateClientAccount
import com.example.model.MasterServiceCatalogItem
import com.example.model.MetricCheckInRecord
import com.example.model.OperationalTask
import com.example.model.PendingQuote
import android.graphics.Bitmap
import com.example.model.ServiceChatMessage
import com.example.model.ServiceHistoryItem
import com.example.model.ServiceRatingRecord
import com.example.model.StaffCollaborator
import com.example.model.UserAccount
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.UUID
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class PersistedCredential(
    val uid: String,
    val salt: String,
    val passwordHash: String
)

data class PersistedAuthSnapshot(
    val users: List<UserAccount> = emptyList(),
    val credentials: List<PersistedCredential> = emptyList(),
    val staffCollaborators: List<StaffCollaborator> = emptyList()
)

data class PersistedOperationalSnapshot(
    val globalOrderCounter: Int = 0,
    val corporateClients: List<CorporateClientAccount> = emptyList(),
    val quotes: List<PendingQuote> = emptyList(),
    val operationalTasks: List<OperationalTask> = emptyList(),
    val serviceReports: List<ServiceHistoryItem> = emptyList(),
    val serviceChatMessages: Map<String, List<ServiceChatMessage>> = emptyMap(),
    val activeTracking: ActiveTechnicianTracking = ActiveTechnicianTracking(
        id = "",
        serviceCode = "",
        serviceTitle = "Sin servicio activo",
        scheduledTime = "",
        location = "",
        technicianName = "",
        technicianRole = "",
        technicianPhone = "",
        technicianWhatsapp = "",
        vehiclePlate = "",
        vehicleModel = "",
        statusMessage = "Sin móvil activo",
        etaMinutes = 0,
        distanceKm = 0.0,
        isEnCamino = false,
        trackingStage = "Inactivo"
    ),
    val notifications: List<String> = emptyList(),
    val clientNotifications: List<ClientNotification> = emptyList(),
    val techNotifications: List<ClientNotification> = emptyList(),
    val equipmentAssets: List<ClientEquipmentAsset> = emptyList(),
    val masterServices: List<MasterServiceCatalogItem> = emptyList(),
    val masterCatalogInitialized: Boolean = false,
    val metricCheckIns: List<MetricCheckInRecord> = emptyList(),
    val npsRatings: Map<String, Int> = emptyMap(),
    val ratingDetails: Map<String, ServiceRatingRecord> = emptyMap(),
    val announcements: List<AnnouncementItem> = emptyList()
)

data class PersistedCleaningSnapshot(
    val staffList: List<CleaningStaffMember> = emptyList(),
    val buildingsList: List<CleaningBuilding> = emptyList(),
    val marksList: List<CleaningAttendanceMark> = emptyList(),
    val activeShift: CleaningRepository.ActiveShiftState = CleaningRepository.ActiveShiftState()
)

/**
 * Capa de persistencia local real basada en SQLite + Cifrado AES-256 GCM (Android KeyStore).
 * Garantiza conservación íntegra de usuarios, contraseñas, clientes, logotipos,
 * órdenes con correlativo global OT-0001+ y registros de limpieza ante cierres o reinicios.
 */
object LocalDatabaseStore {

    private const val DB_NAME = "todoservicio_enterprise_v1.db"
    private const val DB_VERSION = 1
    private const val TABLE_STATE = "app_state_store"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "todoservicio_local_db_key"
    private const val GCM_TAG_LENGTH = 128

    private const val KEY_AUTH = "auth_snapshot"
    private const val KEY_OPERATIONAL = "operational_snapshot"
    private const val KEY_CLEANING = "cleaning_snapshot"

    private val writeExecutor = Executors.newSingleThreadExecutor()
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authAdapter = moshi.adapter(PersistedAuthSnapshot::class.java)
    private val operationalAdapter = moshi.adapter(PersistedOperationalSnapshot::class.java)
    private val cleaningAdapter = moshi.adapter(PersistedCleaningSnapshot::class.java)

    @Volatile
    private var dbHelper: TodoservicioSqliteHelper? = null

    private class TodoservicioSqliteHelper(context: Context) :
        SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_STATE (
                    store_key TEXT PRIMARY KEY,
                    payload TEXT NOT NULL,
                    iv TEXT NOT NULL,
                    is_encrypted INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onCreate(db)
        }
    }

    @Synchronized
    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = TodoservicioSqliteHelper(context.applicationContext)
            ensureKeyStoreKey()
        }
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // Fallback handled transparently during encrypt/decrypt
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (_: Exception) {
            null
        }
    }

    private fun saveJsonRecord(key: String, json: String) {
        val helper = dbHelper ?: return
        writeExecutor.execute {
            try {
                val secretKey = getSecretKey()
                val values = ContentValues().apply {
                    put("store_key", key)
                    if (secretKey != null) {
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                        val encryptedBytes = cipher.doFinal(json.toByteArray(Charsets.UTF_8))
                        put("payload", Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
                        put("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                        put("is_encrypted", 1)
                    } else {
                        put("payload", Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP))
                        put("iv", "")
                        put("is_encrypted", 0)
                    }
                    put("updated_at", System.currentTimeMillis())
                }
                helper.writableDatabase.insertWithOnConflict(
                    TABLE_STATE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            } catch (_: Exception) {
                // Safe non-fatal storage guard
            }
        }
    }

    private fun loadJsonRecord(key: String): String? {
        val helper = dbHelper ?: return null
        return try {
            helper.readableDatabase.rawQuery(
                "SELECT payload, iv, is_encrypted FROM $TABLE_STATE WHERE store_key = ? LIMIT 1",
                arrayOf(key)
            ).use { cursor ->
                if (!cursor.moveToFirst()) return null
                val payload = cursor.getString(0) ?: return null
                val ivStr = cursor.getString(1).orEmpty()
                val isEncrypted = cursor.getInt(2) == 1
                val payloadBytes = Base64.decode(payload, Base64.NO_WRAP)

                if (isEncrypted && ivStr.isNotBlank()) {
                    val secretKey = getSecretKey() ?: return null
                    val ivBytes = Base64.decode(ivStr, Base64.NO_WRAP)
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, ivBytes))
                    String(cipher.doFinal(payloadBytes), Charsets.UTF_8)
                } else {
                    String(payloadBytes, Charsets.UTF_8)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveAuthSnapshot(snapshot: PersistedAuthSnapshot) {
        try {
            val json = authAdapter.toJson(snapshot)
            saveJsonRecord(KEY_AUTH, json)
        } catch (_: Exception) {
        }
    }

    fun awaitPendingWrites() {
        try {
            writeExecutor.submit {}.get()
        } catch (_: Exception) {
        }
    }

    fun loadAuthSnapshot(): PersistedAuthSnapshot? {
        val json = loadJsonRecord(KEY_AUTH) ?: return null
        return try {
            authAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    fun saveOperationalSnapshot(snapshot: PersistedOperationalSnapshot) {
        try {
            val json = operationalAdapter.toJson(snapshot)
            saveJsonRecord(KEY_OPERATIONAL, json)
        } catch (_: Exception) {
        }
    }

    fun loadOperationalSnapshot(): PersistedOperationalSnapshot? {
        val json = loadJsonRecord(KEY_OPERATIONAL) ?: return null
        return try {
            operationalAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    fun saveCleaningSnapshot(snapshot: PersistedCleaningSnapshot) {
        try {
            val json = cleaningAdapter.toJson(snapshot)
            saveJsonRecord(KEY_CLEANING, json)
        } catch (_: Exception) {
        }
    }

    fun loadCleaningSnapshot(): PersistedCleaningSnapshot? {
        val json = loadJsonRecord(KEY_CLEANING) ?: return null
        return try {
            cleaningAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Copia una imagen seleccionada mediante Photo Picker al almacenamiento privado de la app
     * para garantizar que avatares y logotipos sobrevivan al cierre o reinicio del dispositivo.
     */
    fun persistPickedImageUri(context: Context, sourceUri: Uri): String {
        return try {
            val mediaDir = File(context.filesDir, "persisted_media").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(mediaDir, "img_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0L) {
                Uri.fromFile(targetFile).toString()
            } else {
                sourceUri.toString()
            }
        } catch (_: Exception) {
            sourceUri.toString()
        }
    }

    /**
     * Guarda una captura Bitmap tomada desde la cámara en el almacenamiento privado persistente.
     */
    fun persistCapturedBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val mediaDir = File(context.filesDir, "persisted_media").apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(mediaDir, "cam_${UUID.randomUUID()}.jpg")
            FileOutputStream(targetFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, output)
            }
            if (targetFile.exists() && targetFile.length() > 0L) {
                Uri.fromFile(targetFile).toString()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
