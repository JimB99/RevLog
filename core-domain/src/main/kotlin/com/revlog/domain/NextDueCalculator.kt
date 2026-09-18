package com.revlog.domain

import com.revlog.domain.model.ReminderRule
import java.time.LocalDate

object NextDueCalculator {
    fun nextDue(rule: ReminderRule, lastServiceDate: LocalDate?): LocalDate? {
        val anchor = lastServiceDate ?: return null
        return anchor.plusMonths(rule.intervalMonths.toLong())
    }

    fun shouldNotify(
        rule: ReminderRule,
        lastServiceDate: LocalDate?,
        today: LocalDate,
    ): Boolean {
        if (!rule.enabled) return false
        val due = nextDue(rule, lastServiceDate) ?: return false
        val notifyFrom = due.minusDays(rule.leadDays.toLong())
        return !today.isBefore(notifyFrom)
    }
}
