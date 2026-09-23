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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE reminder_rules ADD COLUMN notifyTimeMinutes INTEGER")
            migrateVehicleDataPowerPsToReal(db)
        }
    }

    private fun migrateVehicleDataPowerPsToReal(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vehicle_data_new (
                vehicleId INTEGER NOT NULL,
                licensePlate TEXT,
                vin TEXT,
                firstRegistration TEXT,
                purchasedAt TEXT,
                purchasedKm INTEGER,
                tireDimensions TEXT,
                tirePressureFront TEXT,
                tirePressureRear TEXT,
                tirePressureLoaded TEXT,
                tirePressureUnladen TEXT,
                powerKw INTEGER,
                powerPs REAL,
                displacementCc INTEGER,
                engineOil TEXT,
                brakeFluid TEXT,
                tireSetsJson TEXT,
                PRIMARY KEY(vehicleId),
                FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO vehicle_data_new (
                vehicleId,
                licensePlate,
                vin,
                firstRegistration,
                purchasedAt,
                purchasedKm,
                tireDimensions,
                tirePressureFront,
                tirePressureRear,
                tirePressureLoaded,
                tirePressureUnladen,
                powerKw,
                powerPs,
                displacementCc,
                engineOil,
                brakeFluid,
                tireSetsJson
            )
            SELECT
                vehicleId,
                licensePlate,
                vin,
                firstRegistration,
                purchasedAt,
                purchasedKm,
                tireDimensions,
                tirePressureFront,
                tirePressureRear,
                tirePressureLoaded,
                tirePressureUnladen,
                powerKw,
                CAST(powerPs AS REAL),
                displacementCc,
                engineOil,
                brakeFluid,
                tireSetsJson
            FROM vehicle_data
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE vehicle_data")
        db.execSQL("ALTER TABLE vehicle_data_new RENAME TO vehicle_data")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_vehicle_data_vehicleId ON vehicle_data (vehicleId)",
        )
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            migrateVehicleDataPowerKwToReal(db)
        }
    }

    private fun migrateVehicleDataPowerKwToReal(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS vehicle_data_new (
                vehicleId INTEGER NOT NULL,
                licensePlate TEXT,
                vin TEXT,
                firstRegistration TEXT,
                purchasedAt TEXT,
                purchasedKm INTEGER,
                tireDimensions TEXT,
                tirePressureFront TEXT,
                tirePressureRear TEXT,
                tirePressureLoaded TEXT,
                tirePressureUnladen TEXT,
                powerKw REAL,
                powerPs REAL,
                displacementCc INTEGER,
                engineOil TEXT,
                brakeFluid TEXT,
                tireSetsJson TEXT,
                PRIMARY KEY(vehicleId),
                FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO vehicle_data_new (
                vehicleId,
                licensePlate,
                vin,
                firstRegistration,
                purchasedAt,
                purchasedKm,
                tireDimensions,
                tirePressureFront,
                tirePressureRear,
                tirePressureLoaded,
                tirePressureUnladen,
                powerKw,
                powerPs,
                displacementCc,
                engineOil,
                brakeFluid,
                tireSetsJson
            )
            SELECT
                vehicleId,
                licensePlate,
                vin,
                firstRegistration,
                purchasedAt,
                purchasedKm,
                tireDimensions,
                tirePressureFront,
                tirePressureRear,
                tirePressureLoaded,
                tirePressureUnladen,
                CAST(powerKw AS REAL),
                powerPs,
                displacementCc,
                engineOil,
                brakeFluid,
                tireSetsJson
            FROM vehicle_data
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE vehicle_data")
        db.execSQL("ALTER TABLE vehicle_data_new RENAME TO vehicle_data")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_vehicle_data_vehicleId ON vehicle_data (vehicleId)",
        )
    }
}
