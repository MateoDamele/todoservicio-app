package com.example

import com.example.data.AuthRepository
import com.example.model.AppDestination
import com.example.model.ClientProfile
import com.example.model.UserRole
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests verifying production RBAC roles, clean initial state,
 * single root admin seed (MateoD), and mandatory first-login password change.
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verify_all_destinations_exist() {
        val destinations = AppDestination.values()
        assertEquals(9, destinations.size)
        assertEquals("INICIO", destinations[0].title)
        assertEquals("CALENDARIO OPERATIVO", destinations[3].title)
        assertEquals("GESTIÓN DE ACCESOS", destinations[8].title)
    }

    @Test
    fun verify_client_profile_clean_default() {
        val profile = ClientProfile()
        assertEquals("", profile.clientName)
        assertEquals(null, profile.planType)
        assertEquals(0, profile.visitsThisMonth)
    }

    @Test
    fun verify_all_roles_exist() {
        val roles = UserRole.values()
        assertEquals(5, roles.size)
        assertTrue(roles.contains(UserRole.ADMIN))
        assertTrue(roles.contains(UserRole.SUPERADMIN))
        assertTrue(roles.contains(UserRole.TECNICO))
        assertTrue(roles.contains(UserRole.CLIENTE))
        assertTrue(roles.contains(UserRole.LIMPIEZA))
    }

    @Test
    fun verify_single_seed_admin_and_mandatory_first_login_password_change() {
        // Login with single seed administrator account MateoD
        val adminRes = AuthRepository.login("MateoD", "Mateo2007_")
        assertTrue(adminRes.isSuccess)
        val admin = adminRes.getOrNull()!!
        assertEquals(UserRole.ADMIN, admin.role)
        assertEquals("Mateo", admin.fullName)
        assertFalse(admin.isTemporaryPassword)

        // Creating a new profile sets isTemporaryPassword = true (mandatory change on first login)
        val uniqueUser = "tech.test.${System.nanoTime()}"
        val createRes = AuthRepository.createCustomProfile(
            role = UserRole.TECNICO,
            fullName = "Lucía Méndez",
            username = uniqueUser,
            password = "TempPass123"
        )
        assertTrue(createRes.isSuccess)
        val createdTech = createRes.getOrNull()!!
        assertTrue(createdTech.isTemporaryPassword)
        assertEquals(UserRole.TECNICO, createdTech.role)

        // Admin stays logged in while creating profiles
        assertEquals("MateoD", AuthRepository.currentUser.value?.username)

        // First login with created user triggers mandatory password change flag
        val techLoginRes = AuthRepository.login(uniqueUser, "TempPass123")
        assertTrue(techLoginRes.isSuccess)
        assertTrue(techLoginRes.getOrNull()!!.isTemporaryPassword)

        // Updating password clears isTemporaryPassword and invalidates old provisional password
        val updateRes = AuthRepository.updateTemporaryPassword("NuevaClave2026!")
        assertTrue(updateRes.isSuccess)
        assertFalse(AuthRepository.currentUser.value!!.isTemporaryPassword)

        AuthRepository.logout()
        assertTrue(AuthRepository.login(uniqueUser, "TempPass123").isFailure)
        val reloginRes = AuthRepository.login(uniqueUser, "NuevaClave2026!")
        assertTrue(reloginRes.isSuccess)
        assertFalse(reloginRes.getOrNull()!!.isTemporaryPassword)

        AuthRepository.logout()
    }
}
