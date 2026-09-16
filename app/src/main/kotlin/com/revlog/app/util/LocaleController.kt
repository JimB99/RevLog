package com.revlog.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.revlog.data.locale.LocalePreferences

object LocaleController {
    val supportedTags = listOf("de-AT", "en-GB", "es-ES")

    fun normalizeTag(tag: String?): String =
        tag?.takeIf { it in LocalePreferences.SUPPORTED_TAGS } ?: LocalePreferences.DEFAULT_TAG

    fun apply(languageTag: String) {
        val desired = LocaleListCompat.forLanguageTags(normalizeTag(languageTag))
        if (AppCompatDelegate.getApplicationLocales() == desired) return
        AppCompatDelegate.setApplicationLocales(desired)
    }

    fun applyStored(context: Context) {
        apply(LocalePreferences.read(context))
    }

    fun flagForTag(tag: String): String = when (normalizeTag(tag)) {
        "de-AT" -> "🇦🇹"
        "es-ES" -> "🇪🇸"
        else -> "🇬🇧"
    }

    fun nativeLabelForTag(tag: String): String = when (normalizeTag(tag)) {
        "de-AT" -> "Deutsch"
        "es-ES" -> "Español"
        else -> "English"
    }
}
