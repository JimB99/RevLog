package com.revlog.app

import android.app.Application
import android.content.Context
import com.revlog.app.util.LocaleController
import com.revlog.data.locale.LocalePreferences
import com.revlog.data.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class RevLogApplication : Application() {

    @Inject lateinit var settingsRepository: SettingsRepository

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
    }
}
