package com.revlog.domain

import com.revlog.domain.model.ReminderRule

object NotifyTimeResolver {
    fun effectiveMinutes(rule: ReminderRule, defaultMinutes: Int): Int =
        rule.notifyTimeMinutes ?: defaultMinutes
}
