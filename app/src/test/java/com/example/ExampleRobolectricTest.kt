package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AuthRepository
import com.example.data.CleaningRepository
import com.example.data.LocalDatabaseStore
import com.example.data.OperationalRepository
import com.example.model.CorporateClientAccount
import com.example.model.UserRole
import com.example.util.PdfReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Todoservicio", appName)
    }

    @Test
    fun `pdf report data model clean defaults use global OT-0001`() {
        val reportData = PdfReportGenerator.ReportData()
        assertEquals("OT-0001", reportData.orderCode)
        assertFalse(reportData.dec406Compliant)
        assertEquals("", reportData.leadTechnician)
    }

    @Test
    fun `sqlite persistence retains users passwords clients and global OT counter`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        OperationalRepository.init(context)
        CleaningRepository.init(context)
        AuthRepository.init(context)

        // Mutate Auth & Operational state
        val created = AuthRepository.createCustomProfile(
            role = UserRole.CLIENTE,
            fullName = "Ana Torres",
            username = "anatorres.persist",
            password = "TempUser99",
            companyName = "Café Centro"
        )
        assertTrue(created.isSuccess)
        assertTrue(created.getOrNull()!!.isTemporaryPassword)

        OperationalRepository.addCorporateClient(
            CorporateClientAccount(
                id = "client-cafe-centro",
                name = "Café Centro",
                rut = "210001110018",
                planName = "Plan Estándar Pyme (4 Visitas)",
                clientColorHex = 0xFF335C81L
            )
        )
        val ot1 = OperationalRepository.nextOrderCode()
        assertEquals("OT-0001", ot1)

        // Flush executor writes to SQLite
        LocalDatabaseStore.awaitPendingWrites()

        // Verify persisted snapshots reload cleanly from SQLite
        val authSnap = LocalDatabaseStore.loadAuthSnapshot()
        assertNotNull(authSnap)
        assertTrue(authSnap!!.users.any { it.username == "MateoD" && !it.isTemporaryPassword })
        assertTrue(authSnap.users.any { it.username == "anatorres.persist" && it.isTemporaryPassword })

        val opSnap = LocalDatabaseStore.loadOperationalSnapshot()
        assertNotNull(opSnap)
        assertEquals(1, opSnap!!.globalOrderCounter)
        assertTrue(opSnap.corporateClients.any { it.name == "Café Centro" })
    }
}
