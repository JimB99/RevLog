package com.revlog.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exportId: String,
    val name: String,
    val type: String,
    val sortOrder: Int,
    val createdAt: Long,
)

@Entity(
    tableName = "vehicle_data",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("vehicleId", unique = true)],
)
data class VehicleDataEntity(
    @PrimaryKey val vehicleId: Long,
    val licensePlate: String?,
    val vin: String?,
    val firstRegistration: String?,
    val purchasedAt: String?,
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

@Entity(
    tableName = "service_log_entries",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("vehicleId"),
        Index(value = ["vehicleId", "type", "performedAt"]),
    ],
)
data class ServiceLogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val type: String,
    val performedAt: String,
    val odometerKm: Int?,
    val note: String?,
)
