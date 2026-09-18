package com.revlog.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.revlog.data.tire.LegacyTireMigration
import com.revlog.data.tire.TireSetCodec

object RevLogMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE vehicle_data ADD COLUMN tireSetsJson TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reminder_rules (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    vehicleId INTEGER NOT NULL,
                    serviceType TEXT NOT NULL,
                    intervalMonths INTEGER NOT NULL,
                    leadDays INTEGER NOT NULL DEFAULT 14,
                    enabled INTEGER NOT NULL DEFAULT 1,
                    anchor TEXT NOT NULL DEFAULT 'LAST_SERVICE_DATE',
                    lastNotifiedDueDate TEXT,
                    FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_reminder_rules_vehicleId ON reminder_rules(vehicleId)")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_reminder_rules_vehicleId_serviceType ON reminder_rules(vehicleId, serviceType)",
            )

            val cursor = db.query("SELECT * FROM vehicle_data")
            while (cursor.moveToNext()) {
                val vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow("vehicleId"))
                val tireDimensions = cursor.getString(cursor.getColumnIndexOrThrow("tireDimensions"))
                val tirePressureFront = cursor.getString(cursor.getColumnIndexOrThrow("tirePressureFront"))
                val tirePressureRear = cursor.getString(cursor.getColumnIndexOrThrow("tirePressureRear"))
                val tirePressureLoaded = cursor.getString(cursor.getColumnIndexOrThrow("tirePressureLoaded"))
                val tirePressureUnladen = cursor.getString(cursor.getColumnIndexOrThrow("tirePressureUnladen"))
                val sets = LegacyTireMigration.toTireSets(
                    tireDimensions,
                    tirePressureFront,
                    tirePressureRear,
                    tirePressureLoaded,
                    tirePressureUnladen,
                )
                if (sets.isNotEmpty()) {
                    val json = TireSetCodec.encode(sets)
                    db.execSQL(
                        "UPDATE vehicle_data SET tireSetsJson = ? WHERE vehicleId = ?",
                        arrayOf(json, vehicleId),
                    )
                }
            }
            cursor.close()
        }
    }
}
