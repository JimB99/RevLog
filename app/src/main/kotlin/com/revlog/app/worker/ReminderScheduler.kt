package com.revlog.app.worker

import android.content.Context
import com.revlog.data.repository.ReminderRepository
import com.revlog.data.repository.SettingsRepository
import com.revlog.domain.NotifyTimeResolver
import kotlinx.coroutines.flow.first

object ReminderScheduler {
    suspend fun reschedule(context: Context, reminderRepository: ReminderRepository, settingsRepository: SettingsRepository) {
        val settings = settingsRepository.settings.first()
        val rules = reminderRepository.getAllEnabledRules()
        val minutes = if (rules.isEmpty()) {
            settings.defaultNotifyTimeMinutes
        } else {
            rules.minOf { NotifyTimeResolver.effectiveMinutes(it, settings.defaultNotifyTimeMinutes) }
        }
        ReminderCheckWorker.schedule(context, minutes)
    }
}
