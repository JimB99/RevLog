package com.revlog.domain

import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
import com.revlog.domain.model.VehicleType

object TireDisplayResolver {
    fun visibleRows(set: TireSet, vehicleType: VehicleType): List<TireRow> {
        val withData = set.rows.filter { it.hasAnyValue() }
        if (withData.isEmpty()) return emptyList()
        if (vehicleType == VehicleType.CAR && withData.size == 1) {
            return withData
        }
        return withData
    }

    fun editorRows(set: TireSet, vehicleType: VehicleType): List<TireRow> {
        if (set.rows.isNotEmpty()) return set.rows
        return TireSet.defaultFor(vehicleType).rows
    }

    fun ensurePositions(rows: List<TireRow>, vehicleType: VehicleType): List<TireRow> {
        if (rows.isEmpty()) return TireSet.defaultFor(vehicleType).rows
        return rows
    }

    fun rowLabel(position: TirePosition): TirePosition = position
}
