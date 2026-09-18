package com.revlog.domain

import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
import com.revlog.domain.model.VehicleType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TireDisplayResolverTest {

    @Test
    fun `car single dimension shows one row`() {
        val set = TireSet(rows = listOf(TireRow(position = TirePosition.FRONT, dimensions = "205/55 R16")))
        val visible = TireDisplayResolver.visibleRows(set, VehicleType.CAR)
        assertEquals(1, visible.size)
        assertEquals("205/55 R16", visible.first().dimensions)
    }

    @Test
    fun `car front and rear shows both rows with data`() {
        val set = TireSet(
            rows = listOf(
                TireRow(position = TirePosition.FRONT, dimensions = "205/55", pressureLoaded = "2.4"),
                TireRow(position = TirePosition.REAR, dimensions = "205/55", pressureLoaded = "2.6"),
            ),
        )
        val visible = TireDisplayResolver.visibleRows(set, VehicleType.CAR)
        assertEquals(2, visible.size)
    }

    @Test
    fun `motorcycle sparse rows hide empty`() {
        val set = TireSet(
            rows = listOf(
                TireRow(position = TirePosition.FRONT, dimensions = "120/70", pressure = "2.5"),
                TireRow(position = TirePosition.REAR),
            ),
        )
        val visible = TireDisplayResolver.visibleRows(set, VehicleType.MOTORCYCLE)
        assertEquals(1, visible.size)
        assertEquals("2.5", visible.first().pressure)
    }

    @Test
    fun `empty set returns no visible rows`() {
        val set = TireSet.defaultFor(VehicleType.CAR)
        assertTrue(TireDisplayResolver.visibleRows(set, VehicleType.CAR).isEmpty())
    }
}
