package com.revlog.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.revlog.data.locale.LocalePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "revlog_settings")

data class AppSettings(
    val languageTag: String = LocalePreferences.DEFAULT_TAG,
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_tag")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            languageTag = prefs[Keys.LANGUAGE] ?: LocalePreferences.DEFAULT_TAG,
        )
    }

    suspend fun setLanguage(languageTag: String) {
        val normalized = LocalePreferences.normalizeTag(languageTag)
        context.dataStore.edit { it[Keys.LANGUAGE] = normalized }
        LocalePreferences.write(context, normalized)
    }
}
