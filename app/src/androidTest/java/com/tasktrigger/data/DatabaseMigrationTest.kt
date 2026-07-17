package com.tasktrigger.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TaskTriggerDatabase::class.java,
    )

    @Test
    fun migrationFrom1To2PreservesTasksAndLegacyLogs() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO tasks VALUES (1, 'fixed', 'echo 1', 1000, '', 1, 0)")
            execSQL("INSERT INTO tasks VALUES (2, 'weekly', 'echo 2', 2000, '1,3', 1, 1)")
            execSQL("INSERT INTO tasks VALUES (3, 'disabled', 'echo 3', 3000, '', 0, 0)")
            execSQL("INSERT INTO execution_logs VALUES (10, 1, 4000, 1, 20, 'ok')")
            execSQL("INSERT INTO execution_logs VALUES (11, 99, 5000, 0, 30, 'failed')")
            close()
        }

        val database = helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)

        database.query("SELECT scheduleMode, countdownDurationMillis FROM tasks ORDER BY id").use {
            assertEquals(3, it.count)
            while (it.moveToNext()) {
                assertEquals("FIXED", it.getString(0))
                assertEquals(0, it.getLong(1))
            }
        }
        database.query(
            "SELECT taskId, source, stage, status, exitCode, taskNameSnapshot FROM execution_logs ORDER BY id",
        ).use {
            it.moveToFirst()
            assertEquals(1, it.getLong(0))
            assertEquals("LEGACY", it.getString(1))
            assertEquals("EXECUTION", it.getString(2))
            assertEquals("SUCCEEDED", it.getString(3))
            assertEquals(true, it.isNull(4))
            assertEquals(true, it.isNull(5))
            it.moveToNext()
            assertEquals(99, it.getLong(0))
            assertEquals("FAILED", it.getString(3))
        }
        database.close()
    }

    private companion object {
        const val DB_NAME = "migration-test"
    }
}
