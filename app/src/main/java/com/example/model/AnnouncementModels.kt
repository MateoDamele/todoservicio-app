package com.example.model

/**
 * Niveles de prioridad para los Avisos y Comunicados de Todoservicio.
 */
enum class AnnouncementPriority(
    val label: String,
    val sortWeight: Int
) {
    IMPORTANTE("Importante", 3),
    MEDIO("Medio", 2),
    BAJO("Baja Importancia", 1)
}

/**
 * Perfiles destinatarios habilitados para recibir comunicados segmentados.
 */
enum class AnnouncementTargetRole(val label: String) {
    LIMPIEZA("Personal de Limpieza"),
    TECNICOS("Técnicos"),
    CLIENTES("Clientes Corporativos")
}

/**
 * Comunicado o recordatorio publicado desde Administración con soporte multimedia y push local.
 */
data class AnnouncementItem(
    val id: String,
    val title: String,
    val body: String,
    val priority: AnnouncementPriority = AnnouncementPriority.MEDIO,
    val targetRoles: List<AnnouncementTargetRole> = emptyList(),
    val imageUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val createdAtLabel: String = "",
    val authorName: String = "Administración Central",
    val readByUserIds: List<String> = emptyList()
)

fun AnnouncementItem.isForUserRole(role: UserRole): Boolean {
    return when (role) {
        UserRole.LIMPIEZA -> targetRoles.contains(AnnouncementTargetRole.LIMPIEZA)
        UserRole.TECNICO -> targetRoles.contains(AnnouncementTargetRole.TECNICOS)
        UserRole.CLIENTE -> targetRoles.contains(AnnouncementTargetRole.CLIENTES)
        UserRole.ADMIN, UserRole.SUPERADMIN -> true
    }
}

fun AnnouncementItem.isUnreadBy(userId: String): Boolean {
    if (userId.isBlank()) return false
    return !readByUserIds.contains(userId)
}
