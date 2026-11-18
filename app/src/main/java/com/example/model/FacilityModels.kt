package com.example.model

/**
 * Perfil del cliente corporativo conectado.
 */
data class ClientProfile(
    val clientId: String = "",
    val clientName: String = "",
    val companyName: String = "",
    val role: String = "Cliente Corporativo",
    val address: String = "",
    val rut: String = "",
    val planType: String? = null,
    val coverage: String = "",
    val visitsThisMonth: Int = 0,
    val totalPlanVisits: Int = 0,
    val hasActiveVisit: Boolean = false,
    val logoUri: String? = null
)

/**
 * Tarea de mantenimiento preventivo mensual.
 */
data class PreventiveTask(
    val id: String,
    val title: String,
    val category: String,
    val scheduledDate: String,
    val status: TaskStatus,
    val technicianName: String,
    val frequency: String,
    val dec406Compliance: Boolean = true,
    val notes: String = ""
)

enum class TaskStatus(val label: String) {
    COMPLETED("Completado"),
    SCHEDULED("Programado"),
    IN_PROGRESS("En Curso"),
    PENDING("Pendiente")
}

/**
 * Solicitud de trabajo o cotización enviada por el cliente.
 */
data class ServiceRequest(
    val id: String,
    val title: String,
    val category: String,
    val priority: RequestPriority,
    val description: String,
    val location: String,
    val creationDate: String,
    val status: String,
    val estimatedQuote: String? = null
)

enum class RequestPriority(val label: String, val badgeColorHex: Long) {
    NORMAL("Normal (48 - 72h)", 0xFF1D4E89),
    ALTA("Prioritaria (24h)", 0xFFE67E22),
    EMERGENCIA("Emergencia Guardia 24/7", 0xFFD32F2F)
}

/**
 * Registro en el Historial de Servicios e Inspecciones.
 */
data class ServiceHistoryItem(
    val id: String,
    val serviceCode: String, // ej. "OT-0001" correlativo global
    val title: String,
    val category: String,
    val clientId: String = "",
    val companyName: String = "",
    val sedeName: String = "",
    val address: String = "",
    val executionDate: String,
    val executionTime: String = "",
    val leadTechnician: String,
    val summary: String,
    val technicianComments: String = "",
    val certifiedDec406: Boolean = true,
    val resolutionHours: Double = 0.0,
    val signedBy: String = "",
    val evidencePhotos: List<String> = emptyList(),
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val clientRatingStars: Int? = null,
    val clientRatingComment: String = ""
)

/**
 * Calificación y reseña del cliente corporativo sobre una Orden de Trabajo finalizada.
 */
data class ServiceRatingRecord(
    val orderCode: String,
    val stars: Int,
    val comment: String = "",
    val ratedAtLabel: String = ""
)

/**
 * Cotización pendiente de aprobación por el cliente corporativo.
 */
data class PendingQuote(
    val id: String,
    val quoteCode: String,
    val clientCompanyName: String = "",
    val title: String,
    val category: String,
    val amountUsd: Double,
    val issueDate: String,
    val validUntil: String,
    val scopeDescription: String,
    val isApproved: Boolean = false,
    val approvedDate: String? = null,
    val revisionFeedback: String? = null,
    val attachedFileName: String? = null,
    val attachedFileSize: String? = null,
    val attachedFileType: String? = null
)

/**
 * Servicio en tiempo real con seguimiento de técnico en vivo.
 */
data class ActiveTechnicianTracking(
    val id: String,
    val serviceCode: String,
    val serviceTitle: String,
    val scheduledTime: String,
    val location: String,
    val technicianName: String,
    val technicianRole: String,
    val technicianPhone: String,
    val technicianWhatsapp: String = "",
    val vehiclePlate: String,
    val vehicleModel: String,
    val statusMessage: String,
    val etaMinutes: Int,
    val distanceKm: Double,
    val isEnCamino: Boolean = false,
    val currentLatitude: Double = -34.9011,
    val currentLongitude: Double = -56.1645,
    val destinationLatitude: Double = -34.9011,
    val destinationLongitude: Double = -56.1645,
    val destinationAddress: String = "",
    val trackingStage: String = "Inactivo",
    val clientId: String = "",
    val clientName: String = "",
    val lastUpdatedMillis: Long = 0L,
    val etaSource: String = ""
)

/**
 * Notificación en el portal del cliente y alertas cruzadas de técnicos.
 */
data class ClientNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isUnread: Boolean,
    val type: String,
    val targetClientId: String = "",
    val targetClientName: String = "",
    val targetTechName: String = ""
)

enum class ChatSenderRole {
    CLIENTE,
    TECNICO
}

/**
 * Mensaje breve en tiempo real entre Cliente y Técnico durante una OT activa.
 */
data class ServiceChatMessage(
    val id: String,
    val orderCode: String,
    val senderRole: ChatSenderRole,
    val senderName: String,
    val message: String,
    val timestamp: String
)
