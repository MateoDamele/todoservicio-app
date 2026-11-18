package com.example.model

/**
 * Colaborador / Empleado de la empresa Todoservicio.
 */
data class StaffCollaborator(
    val id: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val jobTitle: String, // ej. "Técnico Electricista", "Especialista Climatización", "Personal de Limpieza"
    val userType: String, // "Administrador" o "Empleado"
    val lastCheckIn: String, // "08:15 hs • En sitio", "09:00 hs • En ruta", "Pendiente"
    val isActive: Boolean = true,
    val permissions: List<String> = listOf("Órdenes de Trabajo", "Check-in GPS", "Firma Digital"),
    val roleCategory: String = "Técnico", // "Técnico" o "Limpieza"
    val emergencyContact: String = "",
    val photoUri: String? = null
)

/**
 * Estado operativo de una tarea en el calendario / informe de 2Workers.
 */
enum class TaskOperationStatus(
    val label: String,
    val badgeColorHex: Long,
    val stripeColorHex: Long
) {
    ABIERTA(
        label = "Abierta / Programada",
        badgeColorHex = 0xFFF57C00, // Naranja / Amarillo
        stripeColorHex = 0xFFE91E63  // Franja Rosa / Roja
    ),
    INICIO_EJECUCION(
        label = "Inició Ejecución",
        badgeColorHex = 0xFF2E7D32, // Verde
        stripeColorHex = 0xFF2E7D32  // Franja Verde
    ),
    FINALIZADA(
        label = "Finalizada",
        badgeColorHex = 0xFF1D4E89, // Azul Corporativo
        stripeColorHex = 0xFF4A90D9  // Franja Celeste / Azul
    )
}

/**
 * Notificación y motivo de reprogramación de una visita técnica operativa (ej. clima adverso).
 */
data class RescheduleNotice(
    val isRescheduled: Boolean = true,
    val originalDate: String,
    val newDate: String,
    val reason: String, // ej. "Postergado por lluvias para trabajos en azotea y seguridad en altura Dec. 406/88"
    val rescheduledBy: String = "Administración Central Todoservicio",
    val timestamp: String = "26 Oct, 10:30 hs"
)

/**
 * Tarea programada en el Calendario Operativo y Tablero de Informes.
 */
data class OperationalTask(
    val id: String,
    val code: String, // ej. "OT-4821"
    val title: String,
    val clientId: String = "",
    val clientName: String,
    val sedeId: String = "",
    val sedeName: String = "",
    val address: String,
    val technicianName: String,
    val assignedTechIds: List<String> = emptyList(),
    val scheduledDate: String, // ej. "26 de Octubre, 2026"
    val date: String = "",
    val dayOfMonth: Int, // 26
    val timeSlot: String, // "09:30 - 12:00"
    val time: String = "",
    val status: TaskOperationStatus,
    val checkInTime: String? = null, // "Check-in: 09:35 hs"
    val checkOutTime: String? = null, // "Check-out: 11:20 hs"
    val category: String, // "Electricidad", "HVAC", "Sanitaria", etc.
    val isDec406: Boolean = true,
    val description: String = "",
    val comments: String = "",
    val rescheduleNotice: RescheduleNotice? = null,
    val accessInstructions: String = "",
    val assignedTechnicians: List<String> = emptyList(),
    val isEnCamino: Boolean = false,
    val clientRatingStars: Int? = null,
    val clientRatingComment: String = ""
)

enum class ClientSegment(val label: String) {
    TODOS("Todos los Segmentos"),
    EMPRESAS("Empresas"),
    HOTELES("Hoteles"),
    RESTAURANTES("Restaurantes"),
    EDIFICIOS("Edificios"),
    ORGANISMOS_PUBLICOS("Organismos Públicos")
}

/**
 * Registro de cliente en el directorio administrativo.
 */
data class AdminClientRecord(
    val id: String,
    val companyName: String,
    val address: String,
    val phone: String,
    val accountManager: String,
    val corporateEmail: String,
    val lastVisit: String,
    val status: String = "Activo",
    val planName: String = "Plan Corporativo Plus (8 Visitas)",
    val segment: ClientSegment = ClientSegment.EMPRESAS,
    val logoUri: String? = null
)

/**
 * Sede o Local específico de una Empresa Matriz / Cliente Corporativo (ej. "Local Pocitos", "Local Carrasco", "Torre A").
 */
data class ClientBranch(
    val id: String,
    val name: String,
    val address: String,
    val contactPerson: String,
    val phone: String,
    val lat: Double = -34.8836,
    val lng: Double = -56.1645,
    val geofenceRadiusMeters: Int = 100,
    val googleMapsUrl: String = ""
)

/**
 * Empresa Matriz / Cuenta Corporativa con soporte para múltiples Sedes y Locales (ej. "Rudy Burger").
 */
data class CorporateClientAccount(
    val id: String,
    val name: String,
    val rut: String,
    val planName: String,
    val clientColorHex: Long,
    val segment: ClientSegment = ClientSegment.EMPRESAS,
    val branches: List<ClientBranch> = emptyList(),
    val logoUri: String? = null
)

/**
 * Tipos de puntos para el mapa de monitoreo GPS interactivo (estilo 2Workers).
 */
enum class GpsPointType(val label: String) {
    EN_SITIO("En Sitio / Activo"),
    EN_CAMINO("En Camino (En Trayecto)"),
    PENDIENTE("Tarea Pendiente")
}

/**
 * Punto de monitoreo en el mapa GPS interactivo de Montevideo y Ciudad de la Costa.
 */
data class GpsMonitoringPoint(
    val id: String,
    val initials: String,
    val name: String,
    val role: String,
    val pointType: GpsPointType,
    val taskCode: String,
    val taskTitle: String,
    val clientName: String,
    val address: String,
    val zone: String,
    val coordinates: String,
    val mapX: Float, // 0.0f - 1.0f relativo en el mapa
    val mapY: Float, // 0.0f - 1.0f relativo en el mapa
    val etaOrCheckIn: String,
    val batteryPct: Int,
    val speedKmh: Int? = null,
    val phoneNumber: String,
    val isDec406: Boolean = false
)

/**
 * Maquinaria y activos registrados por cliente (Catálogo de Equipos 2Workers).
 */
data class ClientEquipmentAsset(
    val id: String,
    val assetCode: String, // ej. "#082769359"
    val name: String, // ej. "Aire Acondicionado 3 - Tableros Planta"
    val category: String, // "HVAC / Climatización", "Tablero Eléctrico", "Bomba Hidroneumática", "Grupo Electrógeno"
    val clientCompanyName: String,
    val locationInBuilding: String, // "Sala de Máquinas Subsuelo 1", "Azotea Torre Central"
    val status: String = "Operativo",
    val serialNumber: String = "",
    val brandModel: String = "",
    val lastMaintenanceDate: String = "",
    val nextMaintenanceDate: String = ""
)

/**
 * Catálogo Maestro de Servicios estandarizados de Todoservicio.
 */
data class MasterServiceCatalogItem(
    val id: String,
    val code: String, // ej. "SRV-ELEC-01"
    val name: String, // ej. "Mantenimiento Eléctrico & Auditoría Termográfica"
    val category: String, // "Electricidad", "Climatización (HVAC)", "Sanitaria", "Limpieza Graseras", "Grupos Electrógenos"
    val description: String,
    val estimatedDurationHours: Double,
    val dec406Compliant: Boolean = true,
    val basePriceUsd: Double
)

/**
 * Registro de Check-in y Métricas de Rendimiento de Colaboradores.
 */
data class MetricCheckInRecord(
    val id: String,
    val collaboratorName: String,
    val collaboratorRole: String,
    val date: String,
    val checkInTime: String,
    val checkOutTime: String?,
    val totalHoursWorked: Double,
    val punctualityStatus: String, // "Puntual", "Tardanza (10 min)", "En Sitio"
    val destinationCompany: String,
    val isGeofenceVerified: Boolean = true
)

/**
 * Categorías y Módulos de la Navegación Enterprise 2Workers en PC / Móvil.
 */
enum class AdminModuleGroup(val title: String) {
    OPERACION("Operación"),
    REGISTROS("Registros"),
    INFORMES("Informes"),
    COTIZACIONES("Cotizaciones")
}

enum class AdminModuleTab(
    val id: String,
    val title: String,
    val group: AdminModuleGroup
) {
    // Operación
    CALENDARIO_OPERATIVO("calendario", "Calendario Operativo", AdminModuleGroup.OPERACION),
    MAPA_GPS_FLOTA("mapa_gps", "Mapa GPS Flota en Vivo", AdminModuleGroup.OPERACION),
    TABLERO_TAREAS("tablero_tareas", "Tablero de Tareas", AdminModuleGroup.OPERACION),

    // Registros
    GESTION_ACCESOS("accesos", "Gestión de Accesos", AdminModuleGroup.REGISTROS),
    COLABORADORES("colaboradores", "Gestionar Colaboradores", AdminModuleGroup.REGISTROS),
    CLIENTES_GRUPOS("clientes_grupos", "Gestionar Clientes / Grupos", AdminModuleGroup.REGISTROS),
    CATALOGO_EQUIPOS("catalogo_equipos", "Catálogo de Equipos", AdminModuleGroup.REGISTROS),
    CATALOGO_SERVICIOS("catalogo_servicios", "Catálogo de Servicios", AdminModuleGroup.REGISTROS),

    // Informes
    METRICAS_CHECKIN("metricas", "Métricas y Check-in", AdminModuleGroup.INFORMES),
    EXPORTAR_REPORTES("exportar", "Exportar Reportes PDF/Excel", AdminModuleGroup.INFORMES),

    // Cotizaciones
    EMISOR_PRESUPUESTOS("cotizaciones", "Emisor de Presupuestos", AdminModuleGroup.COTIZACIONES)
}

