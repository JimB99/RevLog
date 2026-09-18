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
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class RevLogApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var notificationHelper: RevLogNotificationHelper

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
        ReminderCheckWorker.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
