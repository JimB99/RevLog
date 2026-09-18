package com.revlog.data.local

import android.content.Context
import androidx.room.Room

object DatabaseBootstrap {
    private const val DB_NAME = "revlog.db"

    fun open(context: Context): RevLogDatabase =
        Room.databaseBuilder(context, RevLogDatabase::class.java, DB_NAME)
            .addMigrations(RevLogMigrations.MIGRATION_1_2)
            .build()
}
