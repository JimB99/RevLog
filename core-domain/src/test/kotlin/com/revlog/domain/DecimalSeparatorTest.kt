package com.revlog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DecimalSeparatorTest {

    @Test
    fun `comma locales use comma separator`() {
        assertEquals(',', DecimalSeparator.forLanguageTag("de-AT"))
        assertEquals(',', DecimalSeparator.forLanguageTag("es-ES"))
    }

    @Test
    fun `english and unknown use dot separator`() {
        assertEquals('.', DecimalSeparator.forLanguageTag("en-GB"))
        assertEquals('.', DecimalSeparator.forLanguageTag("unknown"))
    }
}
