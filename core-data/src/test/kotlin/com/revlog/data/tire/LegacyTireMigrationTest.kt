package com.revlog.data.tire

import com.revlog.domain.model.TirePosition
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyTireMigrationTest {

    @Test
    fun `only dimensions becomes single row`() {
        val sets = LegacyTireMigration.toTireSets("205/55 R16", null, null, null, null)
        assertEquals(1, sets.size)
        assertEquals(1, sets.first().rows.size)
        assertEquals(TirePosition.FRONT, sets.first().rows.first().position)
        assertEquals("205/55 R16", sets.first().rows.first().dimensions)
    }

    @Test
    fun `front rear pressures map to rows`() {
        val sets = LegacyTireMigration.toTireSets("120/70", "2.5", "2.9", null, null)
        assertEquals(2, sets.first().rows.size)
        assertEquals("2.5", sets.first().rows.first().pressure)
        assertEquals("2.9", sets.first().rows[1].pressure)
    }
}
