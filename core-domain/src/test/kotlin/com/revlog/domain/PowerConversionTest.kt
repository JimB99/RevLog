package com.revlog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PowerConversionTest {

    @Test
    fun `kw to ps with one decimal`() {
        assertEquals(136.0, PowerConversion.kwToPs(100.0))
    }

    @Test
    fun `ps to kw with one decimal`() {
        assertEquals(73.5, PowerConversion.psToKw(100.0))
    }

    @Test
    fun `format and parse power with comma`() {
        assertEquals("136,0", PowerConversion.formatPower(136.0, ','))
        assertEquals(110.5, PowerConversion.parsePower("110,5", ','))
        assertEquals(110.5, PowerConversion.parsePower("110.5", ','))
    }

    @Test
    fun `format and parse power with dot`() {
        assertEquals("136.0", PowerConversion.formatPower(136.0, '.'))
        assertEquals(149.6, PowerConversion.parsePower("149.6", '.'))
    }

    @Test
    fun `kw ps round trip`() {
        val ps = PowerConversion.kwToPs(81.9)
        assertEquals(111.4, ps)
        assertEquals(81.9, PowerConversion.psToKw(ps))
    }
}
