package com.revlog.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RevLogMigration2To3Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RevLogDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate2To3_preservesPowerPsAsReal() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                INSERT INTO vehicles (id, exportId, name, type, sortOrder, createdAt)
                VALUES (1, 'export-1', 'Test Car', 'CAR', 0, 0)
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO vehicle_data (
                    vehicleId, licensePlate, powerKw, powerPs
                ) VALUES (1, 'W-12345', 100, 136)
                """.trimIndent(),
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 3, true, RevLogMigrations.MIGRATION_2_3)

        helper.openDatabase(TEST_DB).apply {
            query("SELECT powerPs FROM vehicle_data WHERE vehicleId = 1").use { cursor ->
                assertEquals(true, cursor.moveToFirst())
                assertEquals(136.0, cursor.getDouble(0), 0.001)
            }
            close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
