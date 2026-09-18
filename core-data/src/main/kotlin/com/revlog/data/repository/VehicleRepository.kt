package com.revlog.data.repository

import androidx.room.withTransaction
import com.revlog.data.local.RevLogDatabase
import com.revlog.data.local.entity.VehicleEntity
import com.revlog.data.mapper.emptyVehicleData
import com.revlog.data.mapper.newExportId
import com.revlog.data.mapper.toBundle
import com.revlog.data.mapper.toDomain
import com.revlog.data.mapper.toEntity
import com.revlog.domain.model.ImportConflictMode
import com.revlog.domain.model.ServiceLogEntry
import com.revlog.domain.model.Vehicle
import com.revlog.domain.model.VehicleBundle
import com.revlog.domain.model.VehicleData
import com.revlog.domain.model.VehicleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VehicleRepository(
    private val database: RevLogDatabase,
    private val reminderRepository: ReminderRepository,
) {
    private val vehicleDao = database.vehicleDao()
    private val vehicleDataDao = database.vehicleDataDao()
    private val serviceLogDao = database.serviceLogDao()

    fun observeVehicles(): Flow<List<Vehicle>> =
        vehicleDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeVehicle(vehicleId: Long): Flow<Vehicle?> =
        vehicleDao.observeById(vehicleId).map { it?.toDomain() }

    fun observeVehicleData(vehicleId: Long): Flow<VehicleData?> =
        vehicleDataDao.observeByVehicleId(vehicleId).map { it?.toDomain() }

    fun observeServiceLogs(vehicleId: Long): Flow<List<ServiceLogEntry>> =
        serviceLogDao.observeByVehicleId(vehicleId).map { list -> list.map { it.toDomain() } }

    fun observeServiceLogsByType(vehicleId: Long, type: String): Flow<List<ServiceLogEntry>> =
        serviceLogDao.observeByVehicleAndType(vehicleId, type).map { list -> list.map { it.toDomain() } }

    suspend fun createVehicle(name: String, type: VehicleType): Long {
        val sortOrder = vehicleDao.maxSortOrder() + 1
        val now = System.currentTimeMillis()
        val vehicleId = vehicleDao.insert(
            VehicleEntity(
                exportId = newExportId(),
                name = name.trim(),
                type = type.name,
                sortOrder = sortOrder,
                createdAt = now,
            ),
        )
        vehicleDataDao.upsert(emptyVehicleData(vehicleId).toEntity())
        return vehicleId
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.update(vehicle.toEntity())
    }

    suspend fun deleteVehicle(vehicleId: Long) {
        vehicleDao.deleteById(vehicleId)
    }

    suspend fun reorderVehicles(orderedIds: List<Long>) {
        database.withTransaction {
            orderedIds.forEachIndexed { index, id ->
                val vehicle = vehicleDao.getById(id) ?: return@forEachIndexed
                vehicleDao.update(vehicle.copy(sortOrder = index))
            }
        }
    }

    suspend fun upsertVehicleData(data: VehicleData) {
        vehicleDataDao.upsert(data.toEntity())
    }

    suspend fun addServiceLog(entry: ServiceLogEntry): Long {
        val id = serviceLogDao.insert(entry.copy(id = 0).toEntity())
        reminderRepository.clearLastNotified(entry.vehicleId, entry.type)
        return id
    }

    suspend fun deleteServiceLog(id: Long) {
        serviceLogDao.deleteById(id)
    }

    suspend fun getBundles(vehicleIds: List<Long>? = null): List<VehicleBundle> {
        val vehicles = if (vehicleIds == null) {
            vehicleDao.getAll()
        } else {
            vehicleDao.getByIds(vehicleIds)
        }
        return vehicles.map { vehicle ->
            val data = vehicleDataDao.getByVehicleId(vehicle.id)
            val logs = serviceLogDao.getByVehicleId(vehicle.id)
            toBundle(vehicle, data, logs)
        }
    }

    suspend fun getBundle(vehicleId: Long): VehicleBundle? {
        val vehicle = vehicleDao.getById(vehicleId) ?: return null
        val data = vehicleDataDao.getByVehicleId(vehicleId)
        val logs = serviceLogDao.getByVehicleId(vehicleId)
        return toBundle(vehicle, data, logs)
    }

    suspend fun findByExportId(exportId: String): Vehicle? =
        vehicleDao.getByExportId(exportId)?.toDomain()

    suspend fun importBundles(
        bundles: List<VehicleBundle>,
        conflictMode: ImportConflictMode,
    ): Int {
        var imported = 0
        database.withTransaction {
            bundles.forEach { bundle ->
                val existing = vehicleDao.getByExportId(bundle.vehicle.exportId)
                if (existing != null && conflictMode == ImportConflictMode.UPDATE_EXISTING) {
                    vehicleDao.update(
                        existing.copy(
                            name = bundle.vehicle.name,
                            type = bundle.vehicle.type.name,
                        ),
                    )
                    vehicleDataDao.upsert(bundle.data.copy(vehicleId = existing.id).toEntity())
                    serviceLogDao.deleteByVehicleId(existing.id)
                    bundle.serviceLogs.forEach { log ->
                        serviceLogDao.insert(
                            log.copy(id = 0, vehicleId = existing.id).toEntity(),
                        )
                    }
                } else {
                    val exportId = if (existing != null) newExportId() else bundle.vehicle.exportId.ifBlank { newExportId() }
                    insertAsNew(bundle, exportId)
                }
                imported++
            }
        }
        return imported
    }

    private suspend fun insertAsNew(bundle: VehicleBundle, exportId: String) {
        val sortOrder = vehicleDao.maxSortOrder() + 1
        val vehicleId = vehicleDao.insert(
            VehicleEntity(
                exportId = exportId,
                name = bundle.vehicle.name,
                type = bundle.vehicle.type.name,
                sortOrder = sortOrder,
                createdAt = bundle.vehicle.createdAt.takeIf { it > 0 } ?: System.currentTimeMillis(),
            ),
        )
        vehicleDataDao.upsert(bundle.data.copy(vehicleId = vehicleId).toEntity())
        bundle.serviceLogs.forEach { log ->
            serviceLogDao.insert(log.copy(id = 0, vehicleId = vehicleId).toEntity())
        }
    }
}
