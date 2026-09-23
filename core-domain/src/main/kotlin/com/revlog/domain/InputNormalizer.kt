package com.revlog.domain

object InputNormalizer {
    fun sanitizeSingleLine(input: String): String =
        input.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "")

    fun normalizeDecimalSeparator(input: String, separator: Char): String {
        val other = if (separator == ',') '.' else ','
        return input.replace(other, separator)
    }
}
