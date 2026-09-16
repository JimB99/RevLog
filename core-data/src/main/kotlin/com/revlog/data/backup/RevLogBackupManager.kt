package com.revlog.data.backup

import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleBundle
import com.revlog.domain.model.VehicleData
import com.revlog.domain.model.VehicleType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

@Serializable
data class RevLogBackup(
    val version: Int = CURRENT_VERSION,
    val exportedAt: String = Instant.now().toString(),
    val app: String = "RevLog",
    val vehicles: List<VehicleBackup> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class VehicleBackup(
    val exportId: String,
    val vehicle: VehicleBackupMeta,
    val data: VehicleDataBackup,
    val serviceLogs: List<ServiceLogBackup> = emptyList(),
)

@Serializable
data class VehicleBackupMeta(
    val name: String,
    val type: String,
    val sortOrder: Int = 0,
    val createdAt: Long = 0,
)

@Serializable
data class VehicleDataBackup(
    val licensePlate: String? = null,
    val vin: String? = null,
    val firstRegistration: String? = null,
    val purchasedAt: String? = null,
    val purchasedKm: Int? = null,
    val tireDimensions: String? = null,
    val tirePressureFront: String? = null,
    val tirePressureRear: String? = null,
    val tirePressureLoaded: String? = null,
    val tirePressureUnladen: String? = null,
    val powerKw: Int? = null,
    val powerPs: Int? = null,
    val displacementCc: Int? = null,
    val engineOil: String? = null,
    val brakeFluid: String? = null,
)

@Serializable
data class ServiceLogBackup(
    val type: String,
    val performedAt: String,
    val odometerKm: Int? = null,
    val note: String? = null,
)

object RevLogBackupManager {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun export(bundles: List<VehicleBundle>): String =
        json.encodeToString(RevLogBackup(vehicles = bundles.map { it.toBackup() }))

    fun import(data: String): RevLogBackup {
        val backup = json.decodeFromString<RevLogBackup>(data)
        require(backup.version <= RevLogBackup.CURRENT_VERSION) {
            "Unsupported backup version: ${backup.version}"
        }
        return backup
    }

    fun toBundles(backup: RevLogBackup): List<VehicleBundle> =
        backup.vehicles.map { it.toBundle() }

    private fun VehicleBundle.toBackup(): VehicleBackup = VehicleBackup(
        exportId = vehicle.exportId,
        vehicle = VehicleBackupMeta(
            name = vehicle.name,
            type = vehicle.type.name,
            sortOrder = vehicle.sortOrder,
            createdAt = vehicle.createdAt,
        ),
        data = data.toBackup(),
        serviceLogs = serviceLogs.map { it.toBackup() },
    )

    private fun VehicleData.toBackup(): VehicleDataBackup = VehicleDataBackup(
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

    private fun ServiceLogEntry.toBackup(): ServiceLogBackup = ServiceLogBackup(
        type = type.name,
        performedAt = performedAt.toString(),
        odometerKm = odometerKm,
        note = note,
    )

    private fun VehicleBackup.toBundle(): VehicleBundle {
        val vehicleId = 0L
        return VehicleBundle(
            vehicle = Vehicle(
                id = vehicleId,
                exportId = exportId,
                name = vehicle.name,
                type = VehicleType.valueOf(vehicle.type),
                sortOrder = vehicle.sortOrder,
                createdAt = vehicle.createdAt,
            ),
            data = VehicleData(
                vehicleId = vehicleId,
                licensePlate = data.licensePlate,
                vin = data.vin,
                firstRegistration = data.firstRegistration?.let(LocalDate::parse),
                purchasedAt = data.purchasedAt?.let(LocalDate::parse),
                purchasedKm = data.purchasedKm,
                tireDimensions = data.tireDimensions,
                tirePressureFront = data.tirePressureFront,
                tirePressureRear = data.tirePressureRear,
                tirePressureLoaded = data.tirePressureLoaded,
                tirePressureUnladen = data.tirePressureUnladen,
                powerKw = data.powerKw,
                powerPs = data.powerPs,
                displacementCc = data.displacementCc,
                engineOil = data.engineOil,
                brakeFluid = data.brakeFluid,
            ),
            serviceLogs = serviceLogs.map { log ->
                ServiceLogEntry(
                    id = 0,
                    vehicleId = vehicleId,
                    type = ServiceType.valueOf(log.type),
                    performedAt = LocalDate.parse(log.performedAt),
                    odometerKm = log.odometerKm,
                    note = log.note,
                )
            },
        )
    }
}
