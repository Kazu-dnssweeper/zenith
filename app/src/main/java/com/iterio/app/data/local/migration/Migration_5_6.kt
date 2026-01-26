package com.iterio.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 5 to 6
 * Adds reviewEnabled column to tasks table for per-task review task generation toggle
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add reviewEnabled column with default value of 1 (true)
        database.execSQL(
            "ALTER TABLE tasks ADD COLUMN reviewEnabled INTEGER NOT NULL DEFAULT 1"
        )
    }
}
