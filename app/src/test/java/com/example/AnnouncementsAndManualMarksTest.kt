package com.example

import com.example.data.CleaningRepository
import com.example.data.OperationalRepository
import com.example.model.AnnouncementPriority
import com.example.model.AnnouncementTargetRole
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.model.UserRole
import com.example.model.isForUserRole
import com.example.model.isUnreadBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncementsAndManualMarksTest {

    @Test
    fun announcement_targets_filter_by_role_and_mark_read_clears_unread_badge() {
        val urgentNotice = OperationalRepository.publishAnnouncement(
            context = null,
            title = "Corte programado de agua",
            body = "Mañana de 08:00 a 10:00 hs en Torre Centro.",
            priority = AnnouncementPriority.IMPORTANTE,
            targetRoles = listOf(AnnouncementTargetRole.LIMPIEZA, AnnouncementTargetRole.CLIENTES),
            authorName = "Mateo"
        )

        val techOnlyNotice = OperationalRepository.publishAnnouncement(
            context = null,
            title = "Nuevo protocolo de tableros",
            body = "Revisar torque de bornes según anexo técnico.",
            priority = AnnouncementPriority.MEDIO,
            targetRoles = listOf(AnnouncementTargetRole.TECNICOS),
            authorName = "Mateo"
        )

        assertTrue(urgentNotice.isForUserRole(UserRole.LIMPIEZA))
        assertTrue(urgentNotice.isForUserRole(UserRole.CLIENTE))
        assertFalse(urgentNotice.isForUserRole(UserRole.TECNICO))

        assertTrue(techOnlyNotice.isForUserRole(UserRole.TECNICO))
        assertFalse(techOnlyNotice.isForUserRole(UserRole.LIMPIEZA))

        val cleaningUserId = "uid-cleaning-test-${System.currentTimeMillis()}"
        assertTrue(urgentNotice.isUnreadBy(cleaningUserId))

        OperationalRepository.markAnnouncementsAsRead(cleaningUserId, UserRole.LIMPIEZA)
        val refreshed = OperationalRepository.announcements.value.first { it.id == urgentNotice.id }
        assertFalse(refreshed.isUnreadBy(cleaningUserId))
    }

    @Test
    fun cleaning_check_in_and_check_out_persist_optional_comments() {
        val building = CleaningBuilding(
            id = "bld-cmt-${System.currentTimeMillis()}",
            name = "Sede Cordón",
            address = "Av. 18 de Julio 1800",
            lat = -34.9041,
            lng = -56.1782,
            geofenceRadiusMeters = 100
        )
        val staff = CleaningStaffMember(
            id = "staff-cmt-${System.currentTimeMillis()}",
            fullName = "Lucía Pereyra",
            ci = "4.555.666-7",
            birthDate = "12/05/1992",
            address = "Cordón, Montevideo",
            phone = "+598 99 888 777",
            assignedBuildings = listOf(building.name)
        )

        val checkInMark = CleaningRepository.performCheckIn(
            staffMember = staff,
            building = building,
            actualLat = -34.9041,
            actualLng = -56.1782,
            comment = "Demora por paro de ómnibus"
        )
        assertEquals("Demora por paro de ómnibus", checkInMark.comment)
        assertFalse(checkInMark.isManualEntry)

        val checkOutMark = CleaningRepository.performCheckOut(
            actualLat = -34.9041,
            actualLng = -56.1782,
            assignedBuilding = building,
            comment = "Se dejó llave en portería"
        )
        assertNotNull(checkOutMark)
        assertEquals("Se dejó llave en portería", checkOutMark!!.comment)
        assertFalse(checkOutMark.isManualEntry)
    }

    @Test
    fun manual_attendance_mark_records_no_gps_no_alerts_and_adds_paired_hours() {
        val staff = CleaningStaffMember(
            id = "staff-manual-${System.currentTimeMillis()}",
            fullName = "Andrea Silva",
            ci = "3.999.111-2",
            birthDate = "01/08/1988",
            address = "Montevideo",
            phone = "+598 98 222 333",
            assignedBuildings = listOf("Edificio Constituyente"),
            totalHoursThisMonth = 0.0
        )
        CleaningRepository.registerNewStaff(staff)

        val entryMark = CleaningRepository.registerManualMark(
            staffId = staff.id,
            staffName = staff.fullName,
            buildingName = "Edificio Constituyente",
            buildingAddress = "Constituyente 1500",
            markType = "E",
            dateFormatted = "18/11/2026",
            timeFormatted = "08:00",
            comment = "Sin señal en subsuelo"
        )

        assertTrue(entryMark.isManualEntry)
        assertEquals(0.0, entryMark.lat, 0.0)
        assertEquals(0.0, entryMark.lng, 0.0)
        assertTrue(entryMark.isInsideGeofence)
        assertEquals("Sin señal en subsuelo", entryMark.comment)

        val exitMark = CleaningRepository.registerManualMark(
            staffId = staff.id,
            staffName = staff.fullName,
            buildingName = "Edificio Constituyente",
            buildingAddress = "Constituyente 1500",
            markType = "S",
            dateFormatted = "18/11/2026",
            timeFormatted = "12:00",
            comment = "Carga administrativa de salida"
        )

        assertTrue(exitMark.isManualEntry)
        assertEquals(0.0, exitMark.lat, 0.0)
        assertEquals(0.0, exitMark.lng, 0.0)
        assertEquals(240, exitMark.durationMinutes)

        val updatedStaff = CleaningRepository.staffList.value.first { it.id == staff.id }
        assertEquals(4.0, updatedStaff.totalHoursThisMonth, 0.01)
    }
}
