package com.revlog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DateParserTest {

    @Test
    fun `format uses dd MM yyyy`() {
        assertEquals("15.03.2024", DateParser.format(LocalDate.of(2024, 3, 15)))
    }

    @Test
    fun `parseOrNull accepts display format`() {
        assertEquals(LocalDate.of(2024, 3, 15), DateParser.parseOrNull("15.03.2024"))
    }

    @Test
    fun `parseOrNull accepts legacy ISO`() {
        assertEquals(LocalDate.of(2024, 3, 15), DateParser.parseOrNull("2024-03-15"))
    }

    @Test
    fun `parseOrNull rejects invalid input`() {
        assertNull(DateParser.parseOrNull("abc"))
        assertNull(DateParser.parseOrNull("15.03."))
    }

    @Test
    fun `isValidDisplayInput`() {
        assertTrue(DateParser.isValidDisplayInput(""))
        assertTrue(DateParser.isValidDisplayInput("01.01.2020"))
        assertFalse(DateParser.isValidDisplayInput("invalid"))
    }
}
