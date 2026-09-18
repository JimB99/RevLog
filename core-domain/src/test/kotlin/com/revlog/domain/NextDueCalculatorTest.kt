package com.revlog.domain

import com.revlog.domain.model.ReminderRule
import com.revlog.domain.model.ServiceType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class NextDueCalculatorTest {

    private val rule = ReminderRule(
        id = 1,
        vehicleId = 1,
        serviceType = ServiceType.OIL_CHANGE,
        intervalMonths = 12,
        leadDays = 14,
    )

    @Test
    fun `nextDue adds interval months`() {
        val last = LocalDate.of(2024, 1, 15)
        assertEquals(LocalDate.of(2025, 1, 15), NextDueCalculator.nextDue(rule, last))
    }

    @Test
    fun `shouldNotify within lead window`() {
        val last = LocalDate.of(2024, 1, 15)
        val today = LocalDate.of(2025, 1, 1)
        assertTrue(NextDueCalculator.shouldNotify(rule, last, today))
    }

    @Test
    fun `shouldNotify false before lead window`() {
        val last = LocalDate.of(2024, 1, 15)
        val today = LocalDate.of(2024, 12, 1)
        assertFalse(NextDueCalculator.shouldNotify(rule, last, today))
    }

    @Test
    fun `shouldNotify false without last service`() {
        assertFalse(NextDueCalculator.shouldNotify(rule, null, LocalDate.now()))
    }
}
