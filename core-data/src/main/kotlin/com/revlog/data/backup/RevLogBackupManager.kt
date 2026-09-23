package com.revlog.data.backup

import com.revlog.data.tire.LegacyTireMigration
import com.revlog.domain.DateParser
import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.ServiceType
import com.revlog.domain.model.TirePosition
import com.revlog.domain.model.TireRow
import com.revlog.domain.model.TireSet
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
        const val CURRENT_VERSION = 2
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
    val tireSets: List<TireSetBackup> = emptyList(),
    @Serializable(with = FlexibleNullableDoubleSerializer::class)
    val powerKw: Double? = null,
    @Serializable(with = FlexibleNullableDoubleSerializer::class)
    val powerPs: Double? = null,
    val displacementCc: Int? = null,
    val engineOil: String? = null,
    val brakeFluid: String? = null,
)

@Serializable
data class TireSetBackup(
    val rows: List<TireRowBackup> = emptyList(),
)

@Serializable
data class TireRowBackup(
    val position: String,
    val dimensions: String? = null,
    val pressureLoaded: String? = null,
    val pressureUnladen: String? = null,
    val pressure: String? = null,
)

@Serializable
data class ServiceLogBackup(
    val type: String,
    val performedAt: String,
    val odometerKm: Int? = null,
    val note: String? = null,
)

object RevLogBackupManager {
    private val exportJson = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val importJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun export(bundles: List<VehicleBundle>): String =
        exportJson.encodeToString(RevLogBackup(vehicles = bundles.map { it.toBackup() }))

    fun import(data: String): RevLogBackup {
        val backup = importJson.decodeFromString<RevLogBackup>(data.trim())
        require(backup.version <= RevLogBackup.CURRENT_VERSION) {
            "Unsupported backup version: ${backup.version}"
        }
        return backup
    }

    fun looksLikeBackup(data: String): Boolean =
        runCatching {
            val backup = import(data)
            backup.app.equals("RevLog", ignoreCase = true)
        }.getOrDefault(false)

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
        tireSets = tireSets.map { set ->
            TireSetBackup(
                rows = set.rows.map { row ->
                    TireRowBackup(
                        position = row.position.name,
                        dimensions = row.dimensions,
                        pressureLoaded = row.pressureLoaded,
                        pressureUnladen = row.pressureUnladen,
                        pressure = row.pressure,
                    )
                },
            )
        },
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
            data = data.toDomain(vehicleId),
            serviceLogs = serviceLogs.map { log ->
                ServiceLogEntry(
                    id = 0,
                    vehicleId = vehicleId,
                    type = ServiceType.valueOf(log.type),
                    performedAt = DateParser.parseOrNull(log.performedAt)
                        ?: LocalDate.parse(log.performedAt),
                    odometerKm = log.odometerKm,
                    note = log.note,
                )
            },
        )
    }

    private fun VehicleDataBackup.toDomain(vehicleId: Long): VehicleData {
        val sets = when {
            tireSets.isNotEmpty() -> tireSets.map { set ->
                TireSet(
                    rows = set.rows.map { row ->
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
            else -> LegacyTireMigration.toTireSets(
                tireDimensions,
                tirePressureFront,
                tirePressureRear,
                tirePressureLoaded,
                tirePressureUnladen,
            )
        }
        return VehicleData(
            vehicleId = vehicleId,
            licensePlate = licensePlate,
            vin = vin,
            firstRegistration = firstRegistration?.let(DateParser::parseOrNull),
            purchasedAt = purchasedAt?.let(DateParser::parseOrNull),
            purchasedKm = purchasedKm,
            tireSets = sets,
            powerKw = powerKw,
            powerPs = powerPs,
            displacementCc = displacementCc,
            engineOil = engineOil,
            brakeFluid = brakeFluid,
        )
    }
}
