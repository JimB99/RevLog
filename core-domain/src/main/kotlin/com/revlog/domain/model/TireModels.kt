package com.revlog.domain.model

enum class TirePosition {
    FRONT,
    REAR,
}

data class TireRow(
    val position: TirePosition,
    val dimensions: String? = null,
    val pressureLoaded: String? = null,
    val pressureUnladen: String? = null,
    val pressure: String? = null,
) {
    fun hasAnyValue(): Boolean = listOf(
        dimensions,
        pressureLoaded,
        pressureUnladen,
        pressure,
    ).any { !it.isNullOrBlank() }
}

data class TireSet(val rows: List<TireRow> = emptyList()) {
    companion object {
        fun defaultFor(vehicleType: VehicleType): TireSet = when (vehicleType) {
            VehicleType.CAR -> TireSet(
                rows = listOf(
                    TireRow(position = TirePosition.FRONT),
                    TireRow(position = TirePosition.REAR),
                ),
            )
            VehicleType.MOTORCYCLE -> TireSet(
                rows = listOf(
                    TireRow(position = TirePosition.FRONT),
                    TireRow(position = TirePosition.REAR),
                ),
            )
        }
    }
}
