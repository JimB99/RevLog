package com.revlog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InputNormalizerTest {

    @Test
    fun `normalize decimal separator to comma for de-AT`() {
        val sep = DecimalSeparator.forLanguageTag("de-AT")
        assertEquals("2,4", InputNormalizer.normalizeDecimalSeparator("2.4", sep))
        assertEquals("2,4", InputNormalizer.normalizeDecimalSeparator("2,4", sep))
    }

    @Test
    fun `normalize decimal separator to dot for en-GB`() {
        val sep = DecimalSeparator.forLanguageTag("en-GB")
        assertEquals("2.4", InputNormalizer.normalizeDecimalSeparator("2,4", sep))
        assertEquals("2.4", InputNormalizer.normalizeDecimalSeparator("2.4", sep))
    }

    @Test
    fun `sanitize single line strips newlines`() {
        assertEquals("line oneline two", InputNormalizer.sanitizeSingleLine("line one\nline two"))
        assertEquals("ab", InputNormalizer.sanitizeSingleLine("a\r\nb"))
    }
}
