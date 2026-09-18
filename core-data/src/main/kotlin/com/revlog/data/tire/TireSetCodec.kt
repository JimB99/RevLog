package com.revlog.data.tire

import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class TireRowDto(
    val position: String,
    val dimensions: String? = null,
    val pressureLoaded: String? = null,
    val pressureUnladen: String? = null,
    val pressure: String? = null,
)

@Serializable
private data class TireSetDto(val rows: List<TireRowDto> = emptyList())

object TireSetCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun encode(sets: List<TireSet>): String =
        json.encodeToString(sets.map { it.toDto() })

    fun decode(raw: String?): List<TireSet> {
        if (raw.isNullOrBlank()) return emptyList()
        return json.decodeFromString<List<TireSetDto>>(raw).map { it.toDomain() }
    }

    private fun TireSet.toDto(): TireSetDto = TireSetDto(
        rows = rows.map { row ->
            TireRowDto(
                position = row.position.name,
                dimensions = row.dimensions,
                pressureLoaded = row.pressureLoaded,
                pressureUnladen = row.pressureUnladen,
                pressure = row.pressure,
            )
        },
    )

    private fun TireSetDto.toDomain(): TireSet = TireSet(
        rows = rows.map { row ->
            TireRow(
                position = TirePosition.valueOf(row.position),
                dimensions = row.dimensions,
                pressureLoaded = row.pressureLoaded,
                pressureUnladen = row.pressureUnladen,
                pressure = row.pressure,
            )
        },
    )
}
