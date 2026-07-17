package com.tasktrigger.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE tasks ADD COLUMN scheduleMode TEXT NOT NULL DEFAULT 'FIXED'",
        )
        database.execSQL(
            "ALTER TABLE tasks ADD COLUMN countdownDurationMillis INTEGER NOT NULL DEFAULT 0",
        )
        database.execSQL(CREATE_EXECUTION_LOGS_V2)
        database.execSQL(
            """
            INSERT INTO execution_logs_v2 (
                id, taskId, executedAt, durationMs, output, source, stage, status,
                reasonCode, exitCode, taskNameSnapshot, commandSnapshot
            )
            SELECT id, taskId, executedAt, durationMs, output, 'LEGACY', 'EXECUTION',
                CASE WHEN success = 1 THEN 'SUCCEEDED' ELSE 'FAILED' END,
                'NONE', NULL, NULL, NULL
            FROM execution_logs
            """.trimIndent(),
        )
        database.execSQL("DROP TABLE execution_logs")
        database.execSQL("ALTER TABLE execution_logs_v2 RENAME TO execution_logs")
    }
}

private const val CREATE_EXECUTION_LOGS_V2 =
    """
    CREATE TABLE IF NOT EXISTS execution_logs_v2 (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        taskId INTEGER NOT NULL,
        executedAt INTEGER NOT NULL,
        durationMs INTEGER NOT NULL,
        output TEXT NOT NULL,
        source TEXT NOT NULL,
        stage TEXT NOT NULL,
        status TEXT NOT NULL,
        reasonCode TEXT NOT NULL,
        exitCode INTEGER,
        taskNameSnapshot TEXT,
        commandSnapshot TEXT
    )
    """
