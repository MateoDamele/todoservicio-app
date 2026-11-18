package com.example.data

import android.content.Context
import com.example.model.StaffCollaborator
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.util.SecureSessionManager
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.UUID

/**
- Repositorio de Autenticación y Control de Accesos (RBAC).
- - Único usuario semilla inicial: MateoD (Administrador Central).
- - Persistencia cifrada en SQLite + Android KeyStore sincronizada con Supabase.
- - Cambio obligatorio de contraseña en primer ingreso para perfiles creados por Administración.
 */
object AuthRepository {

    private var appContext: Context? = null

    private fun createSeedUsers(): MutableList<UserAccount> = mutableListOf(
        UserAccount(
            uid = "usr-admin-01",
            username = "MateoD",
            email = "admin@todoservicio.com.uy",
            fullName = "Mateo",
            phone = "+598 99 000 111",
            companyName = "Todoservicio Central",
            role = UserRole.ADMIN,
            monthlyPlanId = null,
            isTemporaryPassword = false,
            createdAt = 1700000000000L,
            isActive = true,
            rut = "218944510014",
            headquarters = listOf("Montevideo Central", "Canelones"),
            specialty = "Administración General"
        )
    )

    private data class CredentialRecord(val salt: String, val passwordHash: String)

    private fun createSeedCredentials(): MutableMap<String, CredentialRecord> {
        val defaultSalt = "TodoservicioSalt2026"
        val adminHash = SecurityUtils.hashPassword("Mateo2007_", defaultSalt)
        return mutableMapOf(
            "usr-admin-01" to CredentialRecord(defaultSalt, adminHash)
        )
    }

    private val usersDatabase = createSeedUsers()
    private val secureCredentialsMap = createSeedCredentials()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val _accountsState = MutableStateFlow<List<UserAccount>>(usersDatabase.toList())
    val accountsState: StateFlow<List<UserAccount>> = _accountsState.asStateFlow()

    private val _staffCollaborators = MutableStateFlow<List<StaffCollaborator>>(emptyList())
    val staffCollaboratorsFlow: StateFlow<List<StaffCollaborator>> = _staffCollaborators.asStateFlow()

    @Synchronized
    private fun persistAuth() {
        val snapshot = PersistedAuthSnapshot(
            users = usersDatabase.toList(),
            credentials = secureCredentialsMap.map { (uid, rec) ->
                PersistedCredential(uid = uid, salt = rec.salt, passwordHash = rec.passwordHash)
            },
            staffCollaborators = _staffCollaborators.value
        )
        LocalDatabaseStore.saveAuthSnapshot(snapshot)
    }

    /**
- Inicializa la base persistente cifrada de usuarios y restaura sesión si fue marcada para recordar.
     */
    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        LocalDatabaseStore.init(ctx)

        val persisted = LocalDatabaseStore.loadAuthSnapshot()
        if (persisted != null && persisted.users.isNotEmpty()) {
            synchronized(this) {
                usersDatabase.clear()
                usersDatabase.addAll(persisted.users)
                secureCredentialsMap.clear()
                persisted.credentials.forEach { cred ->
                    secureCredentialsMap[cred.uid] = CredentialRecord(cred.salt, cred.passwordHash)
                }
                _accountsState.value = usersDatabase.toList()
                _staffCollaborators.value = persisted.staffCollaborators
            }
        } else {
            persistAuth()
        }

        val savedUid = SecureSessionManager.getSavedSessionUid(ctx)
        if (!savedUid.isNullOrBlank()) {
            val user = usersDatabase.find { it.uid == savedUid && it.isActive }
            if (user != null) {
                _currentUser.value = user
            }
        }
    }

    @Synchronized
    fun reloadFromStore() {
        val persisted = LocalDatabaseStore.loadAuthSnapshot()
        if (persisted != null && persisted.users.isNotEmpty()) {
            usersDatabase.clear()
            usersDatabase.addAll(persisted.users)
            secureCredentialsMap.clear()
            persisted.credentials.forEach { cred ->
                secureCredentialsMap[cred.uid] = CredentialRecord(cred.salt, cred.passwordHash)
            }
            _accountsState.value = usersDatabase.toList()
            _staffCollaborators.value = persisted.staffCollaborators
        }
    }

    /**
- Iniciar sesión con sanitización de entradas, verificación estricta de hashes criptográficos
- y almacenamiento cifrado de sesión ("Recordar sesión").
     */
    fun login(
        identifier: String,
        password: String,
        rememberMe: Boolean = false,
        context: Context? = null
    ): Result<UserAccount> {
        val sanitized = SecurityUtils.sanitizeInput(identifier)
        val user = usersDatabase.find {
            it.username.equals(sanitized, ignoreCase = true) || it.email.equals(sanitized, ignoreCase = true)
        } ?: return Result.failure(Exception("Credenciales inválidas o cuenta no registrada."))

        if (!user.isActive) {
            return Result.failure(Exception("Esta cuenta se encuentra inactiva. Contacte a la administración."))
        }

        val creds = secureCredentialsMap[user.uid]
        val isValid = if (creds != null) {
            SecurityUtils.verifyPassword(password, creds.passwordHash, creds.salt)
        } else {
            false
        }

        if (!isValid) {
            return Result.failure(Exception("La contraseña ingresada es incorrecta."))
        }

        _currentUser.value = user

        val ctx = context ?: appContext
        if (ctx != null) {
            if (rememberMe) {
                val simulatedJwt = "jwt_${UUID.randomUUID()}_${System.currentTimeMillis() + 86400000L * 7}"
                SecureSessionManager.saveSession(ctx, user, simulatedJwt)
            } else {
                SecureSessionManager.clearSession(ctx)
            }
        }

        return Result.success(user)
    }

    /**
- Route Guard: Valida si el usuario activo tiene los privilegios del rol requerido.
     */
    fun validateUserRole(requiredRole: UserRole): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.SUPERADMIN) return true
        return user.role == requiredRole
    }

    /**
- Multi-Tenant Guard: Valida si el usuario activo tiene permiso para acceder a la empresa cliente.
     */
    fun hasAccessToClient(targetClientId: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.ADMIN || user.role == UserRole.SUPERADMIN) return true
        return user.clientId == targetClientId
    }

    /**
- Cerrar sesión del usuario actual y purgar credenciales cifradas de sesión.
     */
    fun logout(context: Context? = null) {
        val ctx = context ?: appContext
        if (ctx != null) {
            SecureSessionManager.clearSession(ctx)
        }
        _currentUser.value = null
    }

    /**
- Actualiza la contraseña provisoria del usuario actual, retira isTemporaryPassword
- y persiste de inmediato en la base SQLite cifrada.
     */
    fun updateTemporaryPassword(newPassword: String): Result<Unit> {
        val current = _currentUser.value ?: return Result.failure(Exception("No hay sesión activa"))

        if (newPassword.length < 6 || newPassword.contains(" ")) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres y sin espacios."))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        secureCredentialsMap[current.uid] = CredentialRecord(newSalt, newHash)

        val index = usersDatabase.indexOfFirst { it.uid == current.uid }
        if (index != -1) {
            val updatedUser = usersDatabase[index].copy(isTemporaryPassword = false)
            usersDatabase[index] = updatedUser
            _accountsState.
