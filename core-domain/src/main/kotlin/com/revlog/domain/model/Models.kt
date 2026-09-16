package com.revlog.domain.model

import java.time.LocalDate

enum class VehicleType {
    CAR,
    MOTORCYCLE,
}

enum class ServiceType {
    TIRE_CHANGE,
    OIL_CHANGE,
    BRAKE_FLUID_CHANGE,
    CHAIN_REPLACEMENT,
    CHAIN_SERVICE,
    INSPECTION,
}

data class Vehicle(
    val id: Long,
    val exportId: String,
    val name: String,
    val type: VehicleType,
    val sortOrder: Int,
    val createdAt: Long,
)

data class VehicleData(
    val vehicleId: Long,
    val licensePlate: String?,
    val vin: String?,
    val firstRegistration: LocalDate?,
    val purchasedAt: LocalDate?,
    val purchasedKm: Int?,
    val tireDimensions: String?,
    val tirePressureFront: String?,
    val tirePressureRear: String?,
    val tirePressureLoaded: String?,
    val tirePressureUnladen: String?,
    val powerKw: Int?,
    val powerPs: Int?,
    val displacementCc: Int?,
    val engineOil: String?,
    val brakeFluid: String?,
)

data class ServiceLogEntry(
    val id: Long,
    val vehicleId: Long,
    val type: ServiceType,
    val performedAt: LocalDate,
    val odometerKm: Int?,
    val note: String?,
)

data class VehicleBundle(
    val vehicle: Vehicle,
    val data: VehicleData,
    val serviceLogs: List<ServiceLogEntry>,
)

enum class ImportConflictMode {
    ADD_NEW,
    UPDATE_EXISTING,
}
