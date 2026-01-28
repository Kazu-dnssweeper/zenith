package com.iterio.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 6 to 7
 * Adds performance indexes for frequently queried columns
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_isActive_scheduleType ON tasks(isActive, scheduleType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_deadlineDate ON tasks(deadlineDate)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_specificDate ON tasks(specificDate)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_study_sessions_startedAt ON study_sessions(startedAt)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_subject_groups_displayOrder ON subject_groups(displayOrder)")
    }
}
