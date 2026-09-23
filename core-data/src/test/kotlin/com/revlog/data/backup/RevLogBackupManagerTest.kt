package com.revlog.data.backup

import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
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
    fun `export import round trip preserves vehicle data v2`() {
        val bundle = sampleBundle()
        val json = RevLogBackupManager.export(listOf(bundle))
        val backup = RevLogBackupManager.import(json)
        val restored = RevLogBackupManager.toBundles(backup).single()

        assertEquals(2, backup.version)
        assertEquals("GSX-R", restored.vehicle.name)
        assertEquals(VehicleType.MOTORCYCLE, restored.vehicle.type)
        assertEquals("W-123AB", restored.data.licensePlate)
        assertEquals(1, restored.data.tireSets.size)
        assertEquals("120/70 ZR17", restored.data.tireSets.first().rows.first().dimensions)
        assertEquals(1, restored.serviceLogs.size)
        assertEquals(ServiceType.OIL_CHANGE, restored.serviceLogs.first().type)
        assertEquals(LocalDate.parse("2025-03-01"), restored.serviceLogs.first().performedAt)
    }

    @Test
    fun `v1 backup imports legacy tire fields`() {
        val json = """
        {
          "version": 1,
          "app": "RevLog",
          "vehicles": [{
            "exportId": "id-1",
            "vehicle": {"name": "Car", "type": "CAR"},
            "data": {
              "tireDimensions": "205/55 R16",
              "tirePressureFront": "2.4",
              "tirePressureRear": "2.6"
            },
            "serviceLogs": []
          }]
        }
        """.trimIndent()
        val restored = RevLogBackupManager.toBundles(RevLogBackupManager.import(json)).single()
        assertEquals(1, restored.data.tireSets.size)
        assertEquals("205/55 R16", restored.data.tireSets.first().rows.first().dimensions)
    }

    @Test
    fun `v2 backup imports integer and string power fields`() {
        val json = """
        {
          "version": 2,
          "app": "RevLog",
          "vehicles": [{
            "exportId": "id-1",
            "vehicle": {"name": "Car", "type": "CAR"},
            "data": {
              "powerKw": 100,
              "powerPs": "136,0"
            },
            "serviceLogs": []
          }]
        }
        """.trimIndent()
        val restored = RevLogBackupManager.toBundles(RevLogBackupManager.import(json)).single()
        assertEquals(100.0, restored.data.powerKw)
        assertEquals(136.0, restored.data.powerPs)
    }

    @Test
    fun `looksLikeBackup accepts valid revlog json`() {
        val json = RevLogBackupManager.export(listOf(sampleBundle()))
        assertEquals(true, RevLogBackupManager.looksLikeBackup(json))
        assertEquals(false, RevLogBackupManager.looksLikeBackup("""{"app":"Other","version":1}"""))
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
            tireSets = listOf(
                TireSet(
                    rows = listOf(
                        TireRow(position = TirePosition.FRONT, dimensions = "120/70 ZR17", pressure = "2.5"),
                        TireRow(position = TirePosition.REAR, pressure = "2.9"),
                    ),
                ),
            ),
            powerKw = 100.0,
            powerPs = 136.0,
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
