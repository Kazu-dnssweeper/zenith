package com.iterio.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 4 to 5
 * Adds reviewCount column to tasks table for custom review count per task
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 復習回数カラムを追加（null = プランのデフォルト使用）
        database.execSQL(
            "ALTER TABLE tasks ADD COLUMN reviewCount INTEGER DEFAULT NULL"
        )
    }
}
