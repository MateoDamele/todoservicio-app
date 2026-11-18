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
 * Repositorio de Autenticación y Control de Accesos (RBAC).
 * - Único usuario semilla inicial: MateoD (Administrador Central).
 * - Persistencia cifrada en SQLite + Android KeyStore (cero pérdida de datos al reiniciar).
 * - Cambio obligatorio de contraseña en primer ingreso para perfiles creados por Administración.
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
     * Inicializa la base persistente cifrada de usuarios y restaura sesión si fue marcada para recordar.
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

    /**
     * Iniciar sesión con sanitización de entradas, verificación estricta de hashes criptográficos
     * y almacenamiento cifrado de sesión ("Recordar sesión").
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
     * Route Guard: Valida si el usuario activo tiene los privilegios del rol requerido.
     */
    fun validateUserRole(requiredRole: UserRole): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.SUPERADMIN) return true
        return user.role == requiredRole
    }

    /**
     * Multi-Tenant Guard: Valida si el usuario activo tiene permiso para acceder a la empresa cliente.
     */
    fun hasAccessToClient(targetClientId: String): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.ADMIN || user.role == UserRole.SUPERADMIN) return true
        return user.clientId == targetClientId
    }

    /**
     * Cerrar sesión del usuario actual y purgar credenciales cifradas de sesión.
     */
    fun logout(context: Context? = null) {
        val ctx = context ?: appContext
        if (ctx != null) {
            SecureSessionManager.clearSession(ctx)
        }
        _currentUser.value = null
    }

    /**
     * Actualiza la contraseña provisoria del usuario actual, retira isTemporaryPassword
     * y persiste de inmediato en la base SQLite cifrada.
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
            _accountsState.value = usersDatabase.toList()
            _currentUser.value = updatedUser
        }

        persistAuth()
        return Result.success(Unit)
    }

    /**
     * Solicitar restablecimiento de contraseña vía email.
     */
    fun sendPasswordResetEmail(email: String): Result<String> {
        val trimmedEmail = email.trim().lowercase()
        val user = usersDatabase.find { it.email.lowercase() == trimmedEmail }
            ?: return Result.failure(Exception("No encontramos ninguna cuenta con el correo: $email"))

        return Result.success("Se ha enviado una notificación de restablecimiento para ${user.email}.")
    }

    /**
     * Alta manual de cliente desde Administración.
     * Queda guardado con isTemporaryPassword = true (cambio obligatorio en primer ingreso).
     */
    fun createClientManual(
        fullName: String,
        companyName: String,
        phone: String,
        email: String,
        monthlyPlanId: String,
        temporaryPassword: String
    ): Result<UserAccount> {
        val trimmedEmail = email.trim().lowercase()

        if (usersDatabase.any { it.email.lowercase() == trimmedEmail }) {
            return Result.failure(Exception("Ya existe una cuenta registrada con el correo: $trimmedEmail"))
        }

        val resolvedUsername = trimmedEmail.substringBefore("@").ifBlank { trimmedEmail }
        val newUid = "usr-client-${UUID.randomUUID().toString().take(8)}"
        val resolvedCompany = companyName.trim()
        val resolvedClientId = "client-${resolvedCompany.lowercase().replace(" ", "-")}"

        val newAccount = UserAccount(
            uid = newUid,
            username = resolvedUsername,
            email = trimmedEmail,
            fullName = fullName.trim(),
            phone = phone.trim(),
            companyName = resolvedCompany,
            role = UserRole.CLIENTE,
            clientId = resolvedClientId,
            monthlyPlanId = monthlyPlanId,
            isTemporaryPassword = true,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        usersDatabase.add(0, newAccount)
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(temporaryPassword, salt)
        secureCredentialsMap[newUid] = CredentialRecord(salt, hash)
        _accountsState.value = usersDatabase.toList()
        persistAuth()

        return Result.success(newAccount)
    }

    /**
     * Alta de perfiles desde Gestión de Accesos (Admin, Técnico, Limpieza o Cliente).
     * Se crea siempre con isTemporaryPassword = true para exigir cambio de contraseña en el primer login.
     */
    fun createCustomProfile(
        role: UserRole,
        fullName: String,
        username: String,
        password: String,
        companyName: String = "",
        clientId: String? = null,
        rut: String = "",
        headquarters: List<String> = listOf("Montevideo"),
        planVisits: Int = 4,
        specialty: String = "",
        phone: String = "",
        avatarUrl: String? = null
    ): Result<UserAccount> {
        val trimmedUser = SecurityUtils.sanitizeInput(username)
        if (trimmedUser.isBlank()) {
            return Result.failure(Exception("El nombre de usuario es obligatorio."))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña provisoria debe tener al menos 6 caracteres."))
        }
        if (usersDatabase.any { it.username.equals(trimmedUser, ignoreCase = true) }) {
            return Result.failure(Exception("Ya existe una cuenta con el usuario: $trimmedUser"))
        }
        val planId = if (planVisits == 8) "plan_corp_8" else "plan_pyme_4"
        val email = "${trimmedUser.lowercase()}@todoservicio.com.uy"
        val newUid = "usr-${role.name.lowercase()}-${UUID.randomUUID().toString().take(6)}"
        val resolvedClientId = if (role == UserRole.CLIENTE) {
            clientId?.takeIf { it.isNotBlank() } ?: "client-${companyName.ifBlank { trimmedUser }.lowercase().replace(" ", "-")}"
        } else {
            clientId
        }
        val newAccount = UserAccount(
            uid = newUid,
            username = trimmedUser,
            email = email,
            fullName = fullName.ifBlank { trimmedUser },
            phone = phone.ifBlank { "+598 99 000 000" },
            companyName = if (role == UserRole.CLIENTE) companyName.ifBlank { "Empresa $trimmedUser" } else "Todoservicio Uruguay",
            role = role,
            clientId = resolvedClientId,
            monthlyPlanId = if (role == UserRole.CLIENTE) planId else null,
            isTemporaryPassword = true,
            createdAt = System.currentTimeMillis(),
            isActive = true,
            rut = rut.ifBlank { "218944510014" },
            headquarters = headquarters,
            specialty = specialty,
            avatarUrl = avatarUrl
        )
        usersDatabase.add(0, newAccount)
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        secureCredentialsMap[newUid] = CredentialRecord(salt, hash)
        _accountsState.value = usersDatabase.toList()

        if (role == UserRole.TECNICO || role == UserRole.LIMPIEZA) {
            val category = if (role == UserRole.TECNICO) "Técnico" else "Limpieza"
            addStaffCollaborator(
                StaffCollaborator(
                    id = "colab-$newUid",
                    fullName = newAccount.fullName,
                    phone = newAccount.phone,
                    email = newAccount.email,
                    jobTitle = specialty.ifBlank { role.label },
                    userType = "Empleado",
                    lastCheckIn = "Disponible • Nuevo ingreso",
                    isActive = true,
                    roleCategory = category,
                    photoUri = avatarUrl
                )
            )
            if (role == UserRole.LIMPIEZA) {
                CleaningRepository.registerNewStaff(
                    com.example.model.CleaningStaffMember(
                        id = "staff-$newUid",
                        fullName = newAccount.fullName,
                        ci = rut.ifBlank { "" },
                        birthDate = "",
                        address = "",
                        phone = newAccount.phone,
                        assignedBuildings = headquarters,
                        username = newAccount.username,
                        profileImageUri = avatarUrl,
                        isActive = true
                    )
                )
            }
        }
        persistAuth()
        return Result.success(newAccount)
    }

    /**
     * Edición integral de perfil desde Gestión de Accesos (Admin).
     * Actualiza rol, empresa/logo, foto de perfil, estado Activo/Suspendido y contraseña opcional.
     */
    fun updateUserProfile(
        uid: String,
        role: UserRole,
        fullName: String,
        username: String,
        newPassword: String? = null,
        companyName: String = "",
        clientId: String? = null,
        rut: String = "",
        headquarters: List<String> = listOf("Montevideo"),
        planVisits: Int = 4,
        specialty: String = "",
        phone: String = "",
        avatarUrl: String? = null,
        isActive: Boolean = true
    ): Result<UserAccount> {
        val index = usersDatabase.indexOfFirst { it.uid == uid }
        if (index == -1) {
            return Result.failure(Exception("Cuenta no encontrada."))
        }
        val trimmedUser = SecurityUtils.sanitizeInput(username)
        if (trimmedUser.isBlank()) {
            return Result.failure(Exception("El nombre de usuario es obligatorio."))
        }
        if (usersDatabase.any { it.uid != uid && it.username.equals(trimmedUser, ignoreCase = true) }) {
            return Result.failure(Exception("El usuario '$trimmedUser' ya está asignado a otra cuenta."))
        }
        if (!newPassword.isNullOrBlank() && newPassword.length < 6) {
            return Result.failure(Exception("La nueva contraseña debe tener al menos 6 caracteres."))
        }

        val existing = usersDatabase[index]
        val planId = if (planVisits == 8) "plan_corp_8" else "plan_pyme_4"
        val resolvedClientId = if (role == UserRole.CLIENTE) {
            clientId?.takeIf { it.isNotBlank() } ?: existing.clientId ?: "client-${companyName.ifBlank { trimmedUser }.lowercase().replace(" ", "-")}"
        } else {
            clientId
        }
        val forceTempChange = if (!newPassword.isNullOrBlank()) {
            uid != _currentUser.value?.uid
        } else {
            existing.isTemporaryPassword
        }

        val updated = existing.copy(
            role = role,
            fullName = fullName.ifBlank { existing.fullName },
            username = trimmedUser,
            companyName = if (role == UserRole.CLIENTE) companyName.ifBlank { existing.companyName } else "Todoservicio Uruguay",
            clientId = resolvedClientId,
            rut = rut.ifBlank { existing.rut },
            headquarters = headquarters.ifEmpty { existing.headquarters },
            monthlyPlanId = if (role == UserRole.CLIENTE) planId else null,
            specialty = specialty,
            phone = phone.ifBlank { existing.phone },
            avatarUrl = avatarUrl,
            isActive = isActive,
            isTemporaryPassword = forceTempChange
        )

        usersDatabase[index] = updated
        if (!newPassword.isNullOrBlank()) {
            val newSalt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
            secureCredentialsMap[uid] = CredentialRecord(newSalt, newHash)
        }
        _accountsState.value = usersDatabase.toList()
        if (_currentUser.value?.uid == uid) {
            _currentUser.value = updated
        }
        persistAuth()
        return Result.success(updated)
    }

    /**
     * Activa o suspende una cuenta de forma inmediata y persistente.
     */
    fun toggleAccountActiveStatus(uid: String) {
        val index = usersDatabase.indexOfFirst { it.uid == uid }
        if (index != -1) {
            val updated = usersDatabase[index].copy(isActive = !usersDatabase[index].isActive)
            usersDatabase[index] = updated
            _accountsState.value = usersDatabase.toList()
            persistAuth()
        }
    }

    fun getAllAccounts(): List<UserAccount> = usersDatabase.toList()

    fun getRegisteredClients(): List<UserAccount> {
        return usersDatabase.filter { it.role == UserRole.CLIENTE }
    }

    fun generateSecureTemporaryPassword(): String {
        val random = SecureRandom()
        val digits = 1000 + random.nextInt(9000)
        val symbols = listOf("!", "#", "$", "*")
        val symbol = symbols[random.nextInt(symbols.size)]
        val words = listOf("Seguro", "Operativo", "Norma", "Control", "Facil")
        val word = words[random.nextInt(words.size)]
        return "TS-$digits-$word$symbol"
    }

    fun getStaffCollaborators(): List<StaffCollaborator> = _staffCollaborators.value

    fun addStaffCollaborator(collaborator: StaffCollaborator) {
        val current = _staffCollaborators.value.toMutableList()
        current.add(0, collaborator)
        _staffCollaborators.value = current
        persistAuth()
    }

    fun removeStaffCollaborator(id: String) {
        _staffCollaborators.value = _staffCollaborators.value.filter { it.id != id }
        persistAuth()
    }

    fun toggleStaffCollaboratorStatus(id: String) {
        _staffCollaborators.value = _staffCollaborators.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        persistAuth()
    }
}
