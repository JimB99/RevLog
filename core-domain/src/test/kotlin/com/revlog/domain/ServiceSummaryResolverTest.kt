package com.revlog.domain

import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.VehicleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ServiceSummaryResolverTest {

    @Test
    fun `car excludes chain service types`() {
        val types = ServiceSummaryResolver.applicableTypes(VehicleType.CAR)
        assertEquals(4, types.size)
        assertEquals(
            listOf(
                ServiceType.TIRE_CHANGE,
                ServiceType.OIL_CHANGE,
                ServiceType.BRAKE_FLUID_CHANGE,
                ServiceType.INSPECTION,
            ),
            types,
        )
    }

    @Test
    fun `motorcycle includes all service types`() {
        assertEquals(6, ServiceSummaryResolver.applicableTypes(VehicleType.MOTORCYCLE).size)
    }

    @Test
    fun `latestByType returns most recent date per type`() {
        val logs = listOf(
            entry(1, ServiceType.OIL_CHANGE, "2024-01-01"),
            entry(2, ServiceType.OIL_CHANGE, "2025-06-01"),
            entry(3, ServiceType.TIRE_CHANGE, "2024-12-01"),
        )
        val summary = ServiceSummaryResolver.latestByType(logs, VehicleType.CAR)

        assertEquals(LocalDate.parse("2025-06-01"), summary[ServiceType.OIL_CHANGE])
        assertEquals(LocalDate.parse("2024-12-01"), summary[ServiceType.TIRE_CHANGE])
        assertNull(summary[ServiceType.INSPECTION])
    }

    @Test
    fun `latestByType ignores chain logs for cars`() {
        val logs = listOf(
            entry(1, ServiceType.CHAIN_SERVICE, "2025-01-01"),
        )
        val summary = ServiceSummaryResolver.latestByType(logs, VehicleType.CAR)
        assertNull(summary[ServiceType.CHAIN_SERVICE])
    }

    private fun entry(id: Long, type: ServiceType, date: String) = ServiceLogEntry(
        id = id,
        vehicleId = 1,
        type = type,
        performedAt = LocalDate.parse(date),
        odometerKm = null,
        note = null,
    )
}
