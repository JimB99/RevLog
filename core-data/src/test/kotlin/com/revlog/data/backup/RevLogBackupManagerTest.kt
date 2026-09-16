package com.revlog.data.backup

import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleBundle
import com.revlog.domain.model.VehicleData
import com.revlog.domain.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

class RevLogBackupManagerTest {

    @Test
    fun `export import round trip preserves vehicle data`() {
        val bundle = sampleBundle()
        val json = RevLogBackupManager.export(listOf(bundle))
        val backup = RevLogBackupManager.import(json)
        val restored = RevLogBackupManager.toBundles(backup).single()

        assertEquals(1, backup.version)
        assertEquals("GSX-R", restored.vehicle.name)
        assertEquals(VehicleType.MOTORCYCLE, restored.vehicle.type)
        assertEquals("W-123AB", restored.data.licensePlate)
        assertEquals(1, restored.serviceLogs.size)
        assertEquals(ServiceType.OIL_CHANGE, restored.serviceLogs.first().type)
        assertEquals(LocalDate.parse("2025-03-01"), restored.serviceLogs.first().performedAt)
    }

    @Test
    fun `rejects unsupported backup version`() {
        val json = """{"version":99,"app":"RevLog","vehicles":[]}"""
        assertThrows(IllegalArgumentException::class.java) {
            RevLogBackupManager.import(json)
        }
    }

    private fun sampleBundle() = VehicleBundle(
        vehicle = Vehicle(
            id = 1,
            exportId = "550e8400-e29b-41d4-a716-446655440000",
            name = "GSX-R",
            type = VehicleType.MOTORCYCLE,
            sortOrder = 0,
            createdAt = 1_700_000_000_000,
        ),
        data = VehicleData(
            vehicleId = 1,
            licensePlate = "W-123AB",
            vin = "JH2RC456789",
            firstRegistration = LocalDate.parse("2020-05-01"),
            purchasedAt = LocalDate.parse("2022-01-15"),
            purchasedKm = 5000,
            tireDimensions = "120/70 ZR17",
            tirePressureFront = "2.5",
            tirePressureRear = "2.9",
            tirePressureLoaded = null,
            tirePressureUnladen = null,
            powerKw = 100,
            powerPs = 136,
            displacementCc = 999,
            engineOil = "10W-40",
            brakeFluid = "DOT 4",
        ),
        serviceLogs = listOf(
            ServiceLogEntry(
                id = 1,
                vehicleId = 1,
                type = ServiceType.OIL_CHANGE,
                performedAt = LocalDate.parse("2025-03-01"),
                odometerKm = 12000,
                note = null,
            ),
        ),
    )
}
