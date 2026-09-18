package com.revlog.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.revlog.data.local.entity.ReminderRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_rules WHERE vehicleId = :vehicleId")
    fun observeByVehicleId(vehicleId: Long): Flow<List<ReminderRuleEntity>>

    @Query("SELECT * FROM reminder_rules WHERE vehicleId = :vehicleId AND serviceType = :serviceType LIMIT 1")
    suspend fun getByVehicleAndType(vehicleId: Long, serviceType: String): ReminderRuleEntity?

    @Query("SELECT * FROM reminder_rules WHERE enabled = 1")
    suspend fun getAllEnabled(): List<ReminderRuleEntity>

    @Upsert
    suspend fun upsert(entity: ReminderRuleEntity): Long

    @Query("UPDATE reminder_rules SET lastNotifiedDueDate = :dueDate WHERE id = :id")
    suspend fun updateLastNotifiedDueDate(id: Long, dueDate: String?)

    @Query("UPDATE reminder_rules SET lastNotifiedDueDate = NULL WHERE vehicleId = :vehicleId AND serviceType = :serviceType")
    suspend fun clearLastNotified(vehicleId: Long, serviceType: String)
}
