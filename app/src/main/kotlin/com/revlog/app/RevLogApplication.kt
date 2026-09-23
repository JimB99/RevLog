package com.revlog.app

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.revlog.app.notification.RevLogNotificationHelper
import com.revlog.app.util.LocaleController
import com.revlog.app.worker.ReminderCheckWorker
import com.revlog.data.locale.LocalePreferences
import com.revlog.data.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import com.revlog.app.worker.ReminderScheduler
import com.revlog.data.repository.ReminderRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class RevLogApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationHelper: RevLogNotificationHelper
    @Inject lateinit var reminderRepository: ReminderRepository

    override fun attachBaseContext(base: Context) {
        LocaleController.apply(LocalePreferences.read(base))
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        runCatching {
            runBlocking {
                settingsRepository.ensureLocalePersisted()
            }
        }
        notificationHelper.ensureChannel()
        runCatching {
            runBlocking {
                ReminderScheduler.reschedule(applicationContext, reminderRepository, settingsRepository)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
