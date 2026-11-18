package com.example.model

/**
 * Roles de usuario dentro del sistema RBAC de Todoservicio.
 */
enum class UserRole(val label: String) {
    ADMIN("Administrador"),
    SUPERADMIN("Super Administrador"),
    TECNICO("Técnico Especializado"),
    CLIENTE("Cliente Corporativo"),
    LIMPIEZA("Personal de Limpieza")
}

/**
 * Cuenta de usuario del sistema.
 */
data class UserAccount(
    val uid: String,
    val username: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val companyName: String = "",
    val role: UserRole = UserRole.CLIENTE,
    val clientId: String? = null,
    val monthlyPlanId: String? = null,
    val isTemporaryPassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val rut: String = "",
    val headquarters: List<String> = emptyList(),
    val specialty: String = "",
    val avatarUrl: String? = null
) {
    val name: String get() = fullName
}

/**
 * Estado de una orden de trabajo asignada a un técnico.
 */
enum class TechOrderStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_CAMINO("En Camino"),
    EN_PROCESO("En Proceso"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada")
}

/**
 * Orden de trabajo técnica detallada (OT).
 */
data class TechWorkOrder(
    val id: String,
    val code: String,
    val title: String,
    val clientName: String = "",
    val companyName: String = "",
    val address: String = "",
    val category: String = "Mantenimiento General",
    val scheduledTime: String = "09:00 hs",
    val status: TechOrderStatus = TechOrderStatus.PENDIENTE,
    val priority: RequestPriority = RequestPriority.NORMAL,
    val description: String = "",
    val requiresDec406Protocol: Boolean = false,
    val scheduledDay: String = "Hoy",
    val checkInTimestamp: String? = null,
    val checkOutTimestamp: String? = null,
    val checkInCoordinates: String? = null,
    val evidencePhotos: List<String> = emptyList(),
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val observations: String = "",
    val technicianSignatureUri: String? = null,
    val closedAtTimestamp: String? = null,
    val isStoredLocallyOffline: Boolean = false,
    val isSyncedWithCloud: Boolean = true
)

/**
 * Opción de Plan de Mantenimiento Mensual.
 */
data class MonthlyPlanOption(
    val id: String,
    val name: String,
    val description: String,
    val visitsPerMonth: Int,
    val priceMonthlyUsd: Int
)

val AvailableMonthlyPlans = listOf(
    MonthlyPlanOption(
        id = "plan_corp_8",
        name = "Plan Corporativo Plus (8 Visitas)",
        description = "Mantenimiento integral, guardia preventiva y protocolo MTSS",
        visitsPerMonth = 8,
        priceMonthlyUsd = 480
    ),
    MonthlyPlanOption(
        id = "plan_edif_12",
        name = "Plan Edificio Torre (12 Visitas)",
        description = "Atención full para complejos de oficinas, torres y copropiedades",
        visitsPerMonth = 12,
        priceMonthlyUsd = 720
    ),
    MonthlyPlanOption(
        id = "plan_base_4",
        name = "Plan Estándar Pyme (4 Visitas)",
        description = "Mantenimiento preventivo mensual para locales y sucursales",
        visitsPerMonth = 4,
        priceMonthlyUsd = 260
    )
)
