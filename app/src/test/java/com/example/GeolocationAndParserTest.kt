package com.example

import com.example.data.CleaningRepository
import com.example.model.CleaningBuilding
import com.example.model.CleaningStaffMember
import com.example.util.GoogleMapsUrlParser
import com.example.util.UruguayLocationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeolocationAndParserTest {

    @Test
    fun `parses exact pin !3d!4d over viewport @ coordinates and extracts place label`() {
        val url = "https://www.google.com/maps/place/Edificio+Montevideo/@-34.9000,-56.1500,17z/data=!3m1!4b1!4m6!3m5!1s0x0:0x0!8m2!3d-34.9082!4d-56.1653"
        val parsed = GoogleMapsUrlParser.parseCoordinatesFromText(url)
        assertNotNull(parsed)
        assertEquals(-34.9082, parsed!!.lat, 0.0001)
        assertEquals(-56.1653, parsed.lng, 0.0001)
        assertEquals("Edificio Montevideo", parsed.placeLabel)
    }

    @Test
    fun `parses query param and raw Uruguay coordinates`() {
        val qUrl = "https://maps.google.com/?q=-34.8836,-56.1645"
        val qParsed = GoogleMapsUrlParser.parseCoordinatesFromText(qUrl)
        assertNotNull(qParsed)
        assertEquals(-34.8836, qParsed!!.lat, 0.0001)
        assertEquals(-56.1645, qParsed.lng, 0.0001)

        val rawParsed = GoogleMapsUrlParser.parseCoordinatesFromText("-34.8642, -56.1021")
        assertNotNull(rawParsed)
        assertEquals(-34.8642, rawParsed!!.lat, 0.0001)
        assertEquals(-56.1021, rawParsed.lng, 0.0001)

        assertNull(GoogleMapsUrlParser.parseCoordinatesFromText("sin coordenadas"))
    }

    @Test
    fun `calculates accurate Haversine distance in meters`() {
        val samePoint = UruguayLocationProvider.haversineDistanceMeters(-34.9011, -56.1645, -34.9011, -56.1645)
        assertEquals(0, samePoint)

        // ~111m per 0.001 degree latitude
        val hundredTenMeters = UruguayLocationProvider.haversineDistanceMeters(-34.9011, -56.1645, -34.9021, -56.1645)
        assertTrue(hundredTenMeters in 105..118)
    }

    @Test
    fun `cleaning check-in and check-out validate real geofence distance accurately`() {
        val building = CleaningBuilding(
            id = "bld-test-geo",
            name = "Torre Centro Montevideo",
            address = "Av. 18 de Julio 1450",
            lat = -34.9058,
            lng = -56.1856,
            geofenceRadiusMeters = 100
        )
        val staff = CleaningStaffMember(
            id = "staff-test-1",
            fullName = "María Fernández",
            ci = "4.111.222-3",
            birthDate = "10/03/1990",
            address = "Montevideo",
            phone = "+598 99 123 456",
            assignedBuildings = listOf(building.name)
        )

        // Entrada dentro del perímetro (~15m)
        val inMark = CleaningRepository.performCheckIn(
            staffMember = staff,
            building = building,
            actualLat = -34.9059,
            actualLng = -56.1856
        )
        assertEquals("E", inMark.markType)
        assertTrue(inMark.isInsideGeofence)
        assertTrue(inMark.distanceToBuildingMeters <= 25)

        // Salida fuera del perímetro (~330m)
        val outMark = CleaningRepository.performCheckOut(
            actualLat = -34.9088,
            actualLng = -56.1856,
            assignedBuilding = building
        )
        assertNotNull(outMark)
        assertEquals("S", outMark!!.markType)
        assertFalse(outMark.isInsideGeofence)
        assertTrue(outMark.distanceToBuildingMeters > 250)
        assertTrue(outMark.comment.contains("Fuera de rango"))
    }
}
