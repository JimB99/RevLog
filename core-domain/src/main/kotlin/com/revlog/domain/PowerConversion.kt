package com.revlog.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object PowerConversion {
    private val FACTOR = BigDecimal("1.35962")

    fun kwToPs(kw: Double): Double =
        BigDecimal(kw.toString())
            .multiply(FACTOR)
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()

    fun psToKw(ps: Double): Double =
        BigDecimal(ps.toString())
            .divide(FACTOR, 1, RoundingMode.HALF_UP)
            .toDouble()

    fun formatPower(value: Double, decimalSeparator: Char): String =
        "%.1f".format(Locale.ROOT, value).replace('.', decimalSeparator)

    fun parsePower(input: String, decimalSeparator: Char): Double? {
        val other = if (decimalSeparator == ',') '.' else ','
        return input.trim()
            .replace(decimalSeparator, '.')
            .replace(other, '.')
            .toBigDecimalOrNull()
            ?.setScale(1, RoundingMode.HALF_UP)
            ?.toDouble()
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? =
        runCatching { BigDecimal(this) }.getOrNull()
}
