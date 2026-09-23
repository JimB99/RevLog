package com.revlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.revlog.data.local.dao.ReminderDao
import com.revlog.data.local.dao.ServiceLogDao
import com.revlog.data.local.dao.VehicleBundleDao
import com.revlog.data.local.dao.VehicleDao
import com.revlog.data.local.dao.VehicleDataDao
import com.revlog.data.local.entity.ReminderRuleEntity
import com.revlog.data.local.entity.ServiceLogEntryEntity
import com.revlog.data.local.entity.VehicleDataEntity
import com.revlog.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        VehicleDataEntity::class,
        ServiceLogEntryEntity::class,
        ReminderRuleEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class RevLogDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun vehicleDataDao(): VehicleDataDao
    abstract fun serviceLogDao(): ServiceLogDao
    abstract fun vehicleBundleDao(): VehicleBundleDao
    abstract fun reminderDao(): ReminderDao
}
