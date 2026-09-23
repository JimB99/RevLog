package com.revlog.domain

object DecimalSeparator {
    fun forLanguageTag(languageTag: String): Char = when (languageTag) {
        "de-AT", "es-ES" -> ','
        else -> '.'
    }
}
