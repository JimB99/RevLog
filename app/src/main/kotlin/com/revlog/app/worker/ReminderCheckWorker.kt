package com.revlog.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.revlog.app.notification.RevLogNotificationHelper
import com.revlog.data.repository.ReminderRepository
import com.revlog.data.repository.SettingsRepository
import com.revlog.data.repository.VehicleRepository
import com.revlog.domain.NextDueCalculator
import com.revlog.domain.NotifyTimeResolver
import com.revlog.app.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reminderRepository: ReminderRepository,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: RevLogNotificationHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()
        if (!settings.remindersEnabled) return Result.success()

        val today = LocalDate.now()
        val now = LocalTime.now()
        val rules = reminderRepository.getAllEnabledRules()
        rules.forEach { rule ->
            val effectiveMinutes = NotifyTimeResolver.effectiveMinutes(rule, settings.defaultNotifyTimeMinutes)
            val notifyTime = LocalTime.of(effectiveMinutes / 60, effectiveMinutes % 60)
            if (now.isBefore(notifyTime)) return@forEach

            val vehicle = vehicleRepository.observeVehicle(rule.vehicleId).first() ?: return@forEach
            val logs = vehicleRepository.observeServiceLogs(rule.vehicleId).first()
            val lastDate = logs.filter { it.type == rule.serviceType }
                .maxByOrNull { it.performedAt }
                ?.performedAt
            if (!NextDueCalculator.shouldNotify(rule, lastDate, today)) return@forEach
            val due = NextDueCalculator.nextDue(rule, lastDate) ?: return@forEach
            if (rule.lastNotifiedDueDate == due) return@forEach

            notificationHelper.showMaintenanceReminder(
                notificationId = rule.id.toInt(),
                vehicleId = rule.vehicleId,
                vehicleName = vehicle.name,
                serviceLabel = serviceLabel(rule.serviceType),
            )
            reminderRepository.markNotified(rule.id, due.toString())
        }
        return Result.success()
    }

    private fun serviceLabel(type: com.revlog.domain.model.ServiceType): String = when (type) {
        com.revlog.domain.model.ServiceType.TIRE_CHANGE -> applicationContext.getString(R.string.service_tire_change)
        com.revlog.domain.model.ServiceType.OIL_CHANGE -> applicationContext.getString(R.string.service_oil_change)
        com.revlog.domain.model.ServiceType.BRAKE_FLUID_CHANGE -> applicationContext.getString(R.string.service_brake_fluid)
        com.revlog.domain.model.ServiceType.CHAIN_REPLACEMENT -> applicationContext.getString(R.string.service_chain_replacement)
        com.revlog.domain.model.ServiceType.CHAIN_SERVICE -> applicationContext.getString(R.string.service_chain_service)
        com.revlog.domain.model.ServiceType.INSPECTION -> applicationContext.getString(R.string.service_inspection)
    }

    companion object {
        const val WORK_NAME = "reminder_check"

        fun millisUntilNext(notifyTimeMinutes: Int): Long {
            val now = LocalDateTime.now()
            var target = now.toLocalDate().atTime(
                notifyTimeMinutes / 60,
                notifyTimeMinutes % 60,
            )
            if (!target.isAfter(now)) {
                target = target.plusDays(1)
            }
            return Duration.between(now, target).toMillis()
        }

        fun schedule(context: Context, notifyTimeMinutes: Int = 540) {
            val delayMs = millisUntilNext(notifyTimeMinutes)
            val request = PeriodicWorkRequestBuilder<ReminderCheckWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}
