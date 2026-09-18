package com.revlog.data.tire

import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet

object LegacyTireMigration {
    fun toTireSets(
        tireDimensions: String?,
        tirePressureFront: String?,
        tirePressureRear: String?,
        tirePressureLoaded: String?,
        tirePressureUnladen: String?,
    ): List<TireSet> {
        val hasAny = listOf(
            tireDimensions,
            tirePressureFront,
            tirePressureRear,
            tirePressureLoaded,
            tirePressureUnladen,
        ).any { !it.isNullOrBlank() }
        if (!hasAny) return emptyList()

        val onlyDimensions = !tireDimensions.isNullOrBlank() &&
            tirePressureFront.isNullOrBlank() &&
            tirePressureRear.isNullOrBlank() &&
            tirePressureLoaded.isNullOrBlank() &&
            tirePressureUnladen.isNullOrBlank()

        if (onlyDimensions) {
            return listOf(
                TireSet(
                    rows = listOf(
                        TireRow(position = TirePosition.FRONT, dimensions = tireDimensions),
                    ),
                ),
            )
        }

        return listOf(
            TireSet(
                rows = listOf(
                    TireRow(
                        position = TirePosition.FRONT,
                        dimensions = tireDimensions,
                        pressureLoaded = tirePressureLoaded,
                        pressureUnladen = tirePressureUnladen,
                        pressure = tirePressureFront,
                    ),
                    TireRow(
                        position = TirePosition.REAR,
                        dimensions = tireDimensions,
                        pressure = tirePressureRear,
                    ),
                ),
            ),
        )
    }
}
