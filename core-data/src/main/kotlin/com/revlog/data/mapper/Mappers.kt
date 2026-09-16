package com.revlog.data.mapper

import com.revlog.data.local.entity.ServiceLogEntryEntity
import com.revlog.data.local.entity.VehicleDataEntity
import com.revlog.data.local.entity.VehicleEntity
import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleBundle
import com.revlog.domain.model.VehicleData
import com.revlog.domain.model.VehicleType
import java.time.LocalDate
import java.util.UUID

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    exportId = exportId,
    name = name,
    type = VehicleType.valueOf(type),
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun Vehicle.toEntity(): VehicleEntity = VehicleEntity(
    id = id,
    exportId = exportId,
    name = name,
    type = type.name,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

fun VehicleDataEntity.toDomain(): VehicleData = VehicleData(
    vehicleId = vehicleId,
    licensePlate = licensePlate,
    vin = vin,
    firstRegistration = firstRegistration?.let(LocalDate::parse),
    purchasedAt = purchasedAt?.let(LocalDate::parse),
    purchasedKm = purchasedKm,
    tireDimensions = tireDimensions,
    tirePressureFront = tirePressureFront,
    tirePressureRear = tirePressureRear,
    tirePressureLoaded = tirePressureLoaded,
    tirePressureUnladen = tirePressureUnladen,
    powerKw = powerKw,
    powerPs = powerPs,
    displacementCc = displacementCc,
    engineOil = engineOil,
    brakeFluid = brakeFluid,
)

fun VehicleData.toEntity(): VehicleDataEntity = VehicleDataEntity(
    vehicleId = vehicleId,
    licensePlate = licensePlate,
    vin = vin,
    firstRegistration = firstRegistration?.toString(),
    purchasedAt = purchasedAt?.toString(),
    purchasedKm = purchasedKm,
    tireDimensions = tireDimensions,
    tirePressureFront = tirePressureFront,
    tirePressureRear = tirePressureRear,
    tirePressureLoaded = tirePressureLoaded,
    tirePressureUnladen = tirePressureUnladen,
    powerKw = powerKw,
    powerPs = powerPs,
    displacementCc = displacementCc,
    engineOil = engineOil,
    brakeFluid = brakeFluid,
)

fun ServiceLogEntryEntity.toDomain(): ServiceLogEntry = ServiceLogEntry(
    id = id,
    vehicleId = vehicleId,
    type = ServiceType.valueOf(type),
    performedAt = LocalDate.parse(performedAt),
    odometerKm = odometerKm,
    note = note,
)

fun ServiceLogEntry.toEntity(): ServiceLogEntryEntity = ServiceLogEntryEntity(
    id = id,
    vehicleId = vehicleId,
    type = type.name,
    performedAt = performedAt.toString(),
    odometerKm = odometerKm,
    note = note,
)

fun emptyVehicleData(vehicleId: Long): VehicleData = VehicleData(
    vehicleId = vehicleId,
    licensePlate = null,
    vin = null,
    firstRegistration = null,
    purchasedAt = null,
    purchasedKm = null,
    tireDimensions = null,
    tirePressureFront = null,
    tirePressureRear = null,
    tirePressureLoaded = null,
    tirePressureUnladen = null,
    powerKw = null,
    powerPs = null,
    displacementCc = null,
    engineOil = null,
    brakeFluid = null,
)

fun newExportId(): String = UUID.randomUUID().toString()

fun toBundle(
    vehicle: VehicleEntity,
    data: VehicleDataEntity?,
    logs: List<ServiceLogEntryEntity>,
): VehicleBundle = VehicleBundle(
    vehicle = vehicle.toDomain(),
    data = (data ?: emptyVehicleData(vehicle.id).toEntity()).toDomain(),
    serviceLogs = logs.map { it.toDomain() },
)
