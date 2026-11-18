package com.example.data

import com.example.model.CleaningAttendanceMark
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.model.DefaultCleaningBuildings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repositorio central del módulo "Personal de Limpieza" (Estilo Lenox HR).
 * Controla legajo de colaboradoras, registros de asistencia con GPS, geocercas y turnos activos.
 */
object CleaningRepository {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "UY"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "UY"))

    private val _staffList = MutableStateFlow<List<CleaningStaffMember>>(emptyList())
    val staffList: StateFlow<List<CleaningStaffMember>> = _staffList.asStateFlow()

    // Base de datos de Edificios / Geocercas
    private val _buildingsList = MutableStateFlow<List<CleaningBuilding>>(DefaultCleaningBuildings)
    val buildingsList: StateFlow<List<CleaningBuilding>> = _buildingsList.asStateFlow()
    val buildings: List<CleaningBuilding> get() = _buildingsList.value

    private val _marksList = MutableStateFlow<List<CleaningAttendanceMark>>(emptyList())
    val marksList: StateFlow<List<CleaningAttendanceMark>> = _marksList.asStateFlow()

    // Estado del turno activo de la colaboradora que está usando el celular
    data class ActiveShiftState(
        val isShiftActive: Boolean = false,
        val markId: String? = null,
        val staffId: String = "",
        val staffName: String = "",
        val avatarInitials: String = "",
        val buildingName: String = "",
        val buildingAddress: String = "",
        val checkInTimestamp: Long = 0L,
        val checkInTimeFormatted: String = "",
        val isInsideGeofence: Boolean = true,
        val distanceMeters: Int = 0,
        val currentLat: Double = -34.9011,
        val currentLng: Double = -56.1645
    )

    private val _activeShift = MutableStateFlow(ActiveShiftState())
    val activeShift: StateFlow<ActiveShiftState> = _activeShift.asStateFlow()

    /**
     * Inicializa el estado de limpieza desde almacenamiento SQLite persistente.
     */
    fun init(context: android.content.Context) {
        LocalDatabaseStore.init(context.applicationContext)
        val persisted = LocalDatabaseStore.loadCleaningSnapshot()
        if (persisted != null) {
            _staffList.value = persisted.staffList
            _buildingsList.value = persisted.buildingsList
            _marksList.value = persisted.marksList
            _activeShift.value = persisted.activeShift
        }
    }

    private fun persistCleaning() {
        LocalDatabaseStore.saveCleaningSnapshot(
            PersistedCleaningSnapshot(
                staffList = _staffList.value,
                buildingsList = _buildingsList.value,
                marksList = _marksList.value,
                activeShift = _activeShift.value
            )
        )
    }

    /**
     * Agrega un nuevo Edificio / Geocerca configurada manualmente.
     */
    fun addBuilding(newBuilding: CleaningBuilding) {
        _buildingsList.value = _buildingsList.value + newBuilding
        persistCleaning()
    }

    /**
     * Realiza el Check-in de entrada georreferenciado con coordenadas GPS reales del teléfono
     * y comentario opcional de la funcionaria.
     */
    fun performCheckIn(
        staffMember: CleaningStaffMember,
        building: CleaningBuilding,
        actualLat: Double = building.lat,
        actualLng: Double = building.lng,
        comment: String = ""
    ): CleaningAttendanceMark {
        val now = System.currentTimeMillis()
        val dateStr = dateFormat.format(Date(now))
        val timeStr = timeFormat.format(Date(now))

        val distance = com.example.util.UruguayLocationProvider.haversineDistanceMeters(
            actualLat,
            actualLng,
            building.lat,
            building.lng
        )
        val insideGeofence = distance <= building.geofenceRadiusMeters

        val initials = staffMember.fullName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "PL" }

        val resolvedComment = comment.trim().ifBlank {
            if (insideGeofence) "Sin novedad"
            else "⚠️ Fuera de rango: a ${distance} m de ${building.name}"
        }

        val newMark = CleaningAttendanceMark(
            id = "mark-${System.currentTimeMillis()}",
            staffId = staffMember.id,
            staffName = staffMember.fullName,
            avatarInitials = initials,
            buildingName = building.name,
            buildingAddress = building.address,
            date = dateStr,
            markTime = timeStr,
            markType = "E",
            lat = actualLat,
            lng = actualLng,
            isInsideGeofence = insideGeofence,
            distanceToBuildingMeters = distance,
            comment = resolvedComment,
            checkInTime = "$timeStr hs",
            checkOutTime = null,
            checkInLat = actualLat,
            checkInLng = actualLng,
            notes = if (insideGeofence) {
                "Entrada en sitio verificada (${distance}m del edificio)"
            } else {
                "Alerta GPS: Entrada registrada a ${distance}m del perímetro (${building.geofenceRadiusMeters}m)"
            },
            isManualEntry = false
        )

        _activeShift.value = ActiveShiftState(
            isShiftActive = true,
            markId = newMark.id,
            staffId = staffMember.id,
            staffName = staffMember.fullName,
            avatarInitials = initials,
            buildingName = building.name,
            buildingAddress = building.address,
            checkInTimestamp = now,
            checkInTimeFormatted = "$timeStr hs",
            isInsideGeofence = insideGeofence,
            distanceMeters = distance,
            currentLat = actualLat,
            currentLng = actualLng
        )

        _marksList.value = listOf(newMark) + _marksList.value
        persistCleaning()
        return newMark
    }

    /**
     * Realiza el Check-out de salida georreferenciado con coordenadas GPS reales del teléfono
     * y comentario opcional de la funcionaria.
     */
    fun performCheckOut(
        actualLat: Double? = null,
        actualLng: Double? = null,
        assignedBuilding: CleaningBuilding? = null,
        comment: String = ""
    ): CleaningAttendanceMark? {
        val currentShift = _activeShift.value
        if (!currentShift.isShiftActive) return null

        val now = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(now))
        val dateStr = dateFormat.format(Date(now))
        val durationMinutes = ((now - currentShift.checkInTimestamp) / 60000L).toInt().coerceAtLeast(1)

        val resolvedLat = actualLat ?: currentShift.currentLat
        val resolvedLng = actualLng ?: currentShift.currentLng
        val targetBuilding = assignedBuilding
            ?: _buildingsList.value.firstOrNull { it.name == currentShift.buildingName }

        val distance = if (targetBuilding != null) {
            com.example.util.UruguayLocationProvider.haversineDistanceMeters(
                resolvedLat,
                resolvedLng,
                targetBuilding.lat,
                targetBuilding.lng
            )
        } else {
            currentShift.distanceMeters
        }
        val insideGeofence = if (targetBuilding != null) {
            distance <= targetBuilding.geofenceRadiusMeters
        } else {
            currentShift.isInsideGeofence
        }

        val resolvedComment = comment.trim().ifBlank {
            if (insideGeofence) "Sin novedad"
            else "⚠️ Fuera de rango: salida a ${distance} m de ${currentShift.buildingName}"
        }

        val outMark = CleaningAttendanceMark(
            id = "mark-out-${System.currentTimeMillis()}",
            staffId = currentShift.staffId.ifBlank { "staff-current" },
            staffName = currentShift.staffName.ifBlank { "Personal de Limpieza" },
            avatarInitials = currentShift.avatarInitials.ifBlank { "PL" },
            buildingName = currentShift.buildingName,
            buildingAddress = currentShift.buildingAddress,
            date = dateStr,
            markTime = timeStr,
            markType = "S",
            lat = resolvedLat,
            lng = resolvedLng,
            isInsideGeofence = insideGeofence,
            distanceToBuildingMeters = distance,
            comment = resolvedComment,
            checkInTime = currentShift.checkInTimeFormatted,
            checkOutTime = "$timeStr hs",
            checkInLat = currentShift.currentLat,
            checkInLng = currentShift.currentLng,
            checkOutLat = resolvedLat,
            checkOutLng = resolvedLng,
            durationMinutes = durationMinutes,
            notes = "Jornada completada. Duración registrada: ${durationMinutes / 60}h ${durationMinutes % 60}m",
            isManualEntry = false
        )

        _marksList.value = listOf(outMark) + _marksList.value
        accumulateStaffHours(currentShift.staffId, currentShift.staffName, durationMinutes)

        _activeShift.value = ActiveShiftState(
            isShiftActive = false,
            markId = null
        )

        persistCleaning()
        return outMark
    }

    /**
     * Registra una marca manual administrativa SIN coordenadas GPS (no dibuja pin en mapa
     * ni dispara alertas de fuera de rango), calculando y sumando horas al historial de la empleada.
     */
    fun registerManualMark(
        staffId: String,
        staffName: String,
        buildingName: String,
        buildingAddress: String,
        markType: String,
        dateFormatted: String,
        timeFormatted: String,
        comment: String
    ): CleaningAttendanceMark {
        val initials = staffName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifBlank { "PL" }

        val normalizedType = if (markType.equals("S", ignoreCase = true)) "S" else "E"
        val cleanComment = comment.trim().ifBlank { "Carga manual administrativa" }

        // Si es Salida (S), intenta emparejar con la última Entrada (E) del mismo día para sumar horas reales
        var computedDurationMinutes: Int? = null
        var pairedCheckInTime = ""
        if (normalizedType == "S") {
            val matchingEntry = _marksList.value.firstOrNull { m ->
                (m.staffId == staffId || m.staffName.equals(staffName, ignoreCase = true)) &&
                    m.date == dateFormatted &&
                    m.markType.equals("E", ignoreCase = true)
            }
            if (matchingEntry != null) {
                pairedCheckInTime = matchingEntry.checkInTime.ifBlank { "${matchingEntry.markTime} hs" }
                computedDurationMinutes = calculateMinutesBetween(matchingEntry.markTime, timeFormatted)
            }
        }

        val manualMark = CleaningAttendanceMark(
            id = "mark-man-${System.currentTimeMillis()}-${_marksList.value.size + 1}",
            staffId = staffId,
            staffName = staffName,
            avatarInitials = initials,
            buildingName = buildingName,
            buildingAddress = buildingAddress.ifBlank { buildingName },
            date = dateFormatted,
            markTime = timeFormatted,
            markType = normalizedType,
            lat = 0.0,
            lng = 0.0,
            isInsideGeofence = true,
            distanceToBuildingMeters = 0,
            comment = cleanComment,
            checkInTime = if (normalizedType == "E") "$timeFormatted hs" else pairedCheckInTime,
            checkOutTime = if (normalizedType == "S") "$timeFormatted hs" else null,
            checkInLat = 0.0,
            checkInLng = 0.0,
            checkOutLat = null,
            checkOutLng = null,
            durationMinutes = computedDurationMinutes,
            notes = "Registro manual administrativo sin captura GPS",
            isManualEntry = true
        )

        _marksList.value = listOf(manualMark) + _marksList.value
        if (computedDurationMinutes != null && computedDurationMinutes > 0) {
            accumulateStaffHours(staffId, staffName, computedDurationMinutes)
        }
        persistCleaning()
        return manualMark
    }

    private fun calculateMinutesBetween(startHHmm: String, endHHmm: String): Int? {
        return try {
            val startParts = startHHmm.trim().split(":")
            val endParts = endHHmm.trim().split(":")
            if (startParts.size < 2 || endParts.size < 2) return null
            val startMin = startParts[0].toInt() * 60 + startParts[1].take(2).toInt()
            val endMin = endParts[0].toInt() * 60 + endParts[1].take(2).toInt()
            val diff = endMin - startMin
            if (diff > 0) diff else null
        } catch (_: Exception) {
            null
        }
    }

    private fun accumulateStaffHours(staffId: String, staffName: String, minutesToAdd: Int) {
        if (minutesToAdd <= 0) return
        val hoursToAdd = minutesToAdd / 60.0
        _staffList.value = _staffList.value.map { staff ->
            val matches = (staffId.isNotBlank() && staff.id == staffId) ||
                staff.fullName.equals(staffName, ignoreCase = true)
            if (matches) {
                staff.copy(totalHoursThisMonth = staff.totalHoursThisMonth + hoursToAdd)
            } else {
                staff
            }
        }
    }

    /**
     * Actualiza el comentario de una marcación de asistencia.
     */
    fun updateMarkComment(markId: String, newComment: String) {
        _marksList.value = _marksList.value.map { mark ->
            if (mark.id == markId) mark.copy(comment = newComment.trim()) else mark
        }
        persistCleaning()
    }

    /**
     * Registra una nueva funcionaria en el Legajo Digital.
     */
    fun registerNewStaff(staff: CleaningStaffMember) {
        _staffList.value = _staffList.value + staff
        persistCleaning()
    }

    /**
     * Alterna estado activo/suspendido de una funcionaria.
     */
    fun toggleStaffStatus(staffId: String) {
        _staffList.value = _staffList.value.map { staff ->
            if (staff.id == staffId) staff.copy(isActive = !staff.isActive) else staff
        }
        persistCleaning()
    }
}
