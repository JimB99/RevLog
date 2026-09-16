package com.revlog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.revlog.data.local.entity.ServiceLogEntryEntity
import com.revlog.data.local.entity.VehicleDataEntity
import com.revlog.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    fun observeById(id: Long): Flow<VehicleEntity?>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): VehicleEntity?

    @Query("SELECT * FROM vehicles WHERE exportId = :exportId")
    suspend fun getByExportId(exportId: String): VehicleEntity?

    @Query("SELECT * FROM vehicles ORDER BY sortOrder ASC")
    suspend fun getAll(): List<VehicleEntity>

    @Query("SELECT * FROM vehicles WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<VehicleEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM vehicles")
    suspend fun maxSortOrder(): Int
}

@Dao
interface VehicleDataDao {
    @Query("SELECT * FROM vehicle_data WHERE vehicleId = :vehicleId")
    fun observeByVehicleId(vehicleId: Long): Flow<VehicleDataEntity?>

    @Query("SELECT * FROM vehicle_data WHERE vehicleId = :vehicleId")
    suspend fun getByVehicleId(vehicleId: Long): VehicleDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: VehicleDataEntity)
}

@Dao
interface ServiceLogDao {
    @Query("SELECT * FROM service_log_entries WHERE vehicleId = :vehicleId ORDER BY performedAt DESC")
    fun observeByVehicleId(vehicleId: Long): Flow<List<ServiceLogEntryEntity>>

    @Query(
        """
        SELECT * FROM service_log_entries
        WHERE vehicleId = :vehicleId AND type = :type
        ORDER BY performedAt DESC
        """,
    )
    fun observeByVehicleAndType(vehicleId: Long, type: String): Flow<List<ServiceLogEntryEntity>>

    @Query("SELECT * FROM service_log_entries WHERE vehicleId = :vehicleId")
    suspend fun getByVehicleId(vehicleId: Long): List<ServiceLogEntryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: ServiceLogEntryEntity): Long

    @Query("DELETE FROM service_log_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM service_log_entries WHERE vehicleId = :vehicleId")
    suspend fun deleteByVehicleId(vehicleId: Long)
}

@Dao
interface VehicleBundleDao {
    @Transaction
    @Query("SELECT * FROM vehicles WHERE id = :vehicleId")
    suspend fun getVehicle(vehicleId: Long): VehicleEntity?
}
