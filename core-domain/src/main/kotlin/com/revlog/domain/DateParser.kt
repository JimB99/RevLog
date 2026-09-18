package com.revlog.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

object DateParser {
    val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

    private val ISO_FORMAT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun format(date: LocalDate?): String =
        date?.format(DISPLAY_FORMAT) ?: ""

    fun parseOrNull(input: String): LocalDate? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        return tryParse(trimmed, DISPLAY_FORMAT) ?: tryParse(trimmed, ISO_FORMAT)
    }

    fun isValidDisplayInput(input: String): Boolean {
        val trimmed = input.trim()
        return trimmed.isBlank() || parseOrNull(trimmed) != null
    }

    private fun tryParse(value: String, formatter: DateTimeFormatter): LocalDate? =
        try {
            LocalDate.parse(value, formatter)
        } catch (_: DateTimeParseException) {
            null
        }
}
