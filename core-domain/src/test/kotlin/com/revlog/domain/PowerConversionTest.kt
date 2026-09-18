package com.revlog.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PowerConversionTest {

    @Test
    fun `kw to ps bidirectional conversion`() {
        assertEquals(135, PowerConversion.kwToPs(100))
        assertEquals(73, PowerConversion.psToKw(100))
    }
}
