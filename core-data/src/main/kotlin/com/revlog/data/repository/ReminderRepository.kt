package com.revlog.data.repository

import com.revlog.data.local.RevLogDatabase
import com.revlog.data.mapper.toDomain
import com.revlog.data.mapper.toEntity
import com.revlog.domain.model.ReminderRule
import com.revlog.domain.model.ServiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val database: RevLogDatabase,
) {
    private val reminderDao = database.reminderDao()

    fun observeRules(vehicleId: Long): Flow<List<ReminderRule>> =
        reminderDao.observeByVehicleId(vehicleId).map { list -> list.map { it.toDomain() } }

    suspend fun getRule(vehicleId: Long, serviceType: ServiceType): ReminderRule? =
        reminderDao.getByVehicleAndType(vehicleId, serviceType.name)?.toDomain()

    suspend fun upsertRule(rule: ReminderRule) {
        reminderDao.upsert(rule.toEntity())
    }

    suspend fun getAllEnabledRules(): List<ReminderRule> =
        reminderDao.getAllEnabled().map { it.toDomain() }

    suspend fun markNotified(ruleId: Long, dueDate: String) {
        reminderDao.updateLastNotifiedDueDate(ruleId, dueDate)
    }

    suspend fun clearLastNotified(vehicleId: Long, serviceType: ServiceType) {
        reminderDao.clearLastNotified(vehicleId, serviceType.name)
    }
}
