package com.revlog.data.locale

import android.content.Context

object LocalePreferences {
    const val DEFAULT_TAG = "de-AT"

    private const val PREFS = "revlog_locale"
    private const val KEY = "language_tag"

    val SUPPORTED_TAGS = setOf("de-AT", "en-GB", "es-ES")

    fun read(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, DEFAULT_TAG)
            ?.takeIf { it in SUPPORTED_TAGS }
            ?: DEFAULT_TAG

    fun write(context: Context, languageTag: String) {
        val normalized = normalizeTag(languageTag)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, normalized)
            .commit()
    }

    fun normalizeTag(tag: String?): String =
        tag?.takeIf { it in SUPPORTED_TAGS } ?: DEFAULT_TAG
}
