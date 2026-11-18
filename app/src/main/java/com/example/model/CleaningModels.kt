package com.example.model

/**
 * Modelo de Colaboradora de Limpieza (Legajo Digital Lenox HR).
 */
data class CleaningStaffMember(
    val id: String,
    val fullName: String,
    val ci: String, // Cédula de Identidad (ej. "4.892.314-5")
    val birthDate: String, // ej. "14/05/1992"
    val address: String, // ej. "Av. 8 de Octubre 2410, La Blanqueada"
    val phone: String, // ej. "+598 99 333 777"
    val assignedBuildings: List<String>, // Edificios asignados habitualmente
    val contractedHoursPerWeek: Int = 40,
    val emergencyContactName: String = "",
    val emergencyContactRelation: String = "",
    val emergencyContactPhone: String = "",
    val bloodType: String = "",
    val medicalConditions: String = "",
    val username: String = "",
    val profileImageUri: String? = null,
    val isActive: Boolean = true,
    val joinedDate: String = "",
    val totalHoursThisMonth: Double = 0.0
) {
    val name: String get() = fullName
}

/**
 * Registro de Marcación Individual de Entrada (E) o Salida (S) Georreferenciada (Estilo Lenox HR).
 */
data class CleaningAttendanceMark(
    val id: String,
    val staffId: String,
    val staffName: String,
    val avatarInitials: String = "",
    val buildingName: String,
    val buildingAddress: String,
    val date: String,
    val markTime: String = "",
    val markType: String = "E", // "E" = Entrada, "S" = Salida
    val lat: Double = -34.9011,
    val lng: Double = -56.1645,
    val isInsideGeofence: Boolean = true,
    val distanceToBuildingMeters: Int = 0,
    val comment: String = "Sin novedad",
    // Compatibilidad con turno activo
    val checkInTime: String = "",
    val checkOutTime: String? = null,
    val checkInLat: Double = -34.9011,
    val checkInLng: Double = -56.1645,
    val checkOutLat: Double? = null,
    val checkOutLng: Double? = null,
    val durationMinutes: Int? = null,
    val notes: String = "",
    val isManualEntry: Boolean = false
)

/**
 * Edificio / Sede con coordenadas para validación perimetral (Geocerca Lenox HR).
 */
data class CleaningBuilding(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val geofenceRadiusMeters: Int = 100,
    val assignedStaffNames: List<String> = emptyList(),
    val googleMapsUrl: String = ""
)

val DefaultCleaningBuildings: List<CleaningBuilding> = emptyList()
