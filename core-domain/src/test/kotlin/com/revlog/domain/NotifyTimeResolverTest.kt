package com.revlog.domain

import com.revlog.domain.model.ReminderRule
import com.revlog.domain.model.ServiceType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NotifyTimeResolverTest {

    private val rule = ReminderRule(
        id = 1,
        vehicleId = 1,
        serviceType = ServiceType.OIL_CHANGE,
        intervalMonths = 12,
    )

    @Test
    fun `uses global default when rule has no override`() {
        assertEquals(540, NotifyTimeResolver.effectiveMinutes(rule, defaultMinutes = 540))
    }

    @Test
    fun `uses rule override when set`() {
        assertEquals(
            600,
            NotifyTimeResolver.effectiveMinutes(rule.copy(notifyTimeMinutes = 600), defaultMinutes = 540),
        )
    }
}
