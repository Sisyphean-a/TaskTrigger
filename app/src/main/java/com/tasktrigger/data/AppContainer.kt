package com.tasktrigger.data

import android.content.Context
import androidx.room.Room
import com.tasktrigger.domain.AlarmTaskScheduler
import com.tasktrigger.domain.Clock
import com.tasktrigger.domain.CommandExecutor
import com.tasktrigger.domain.TaskOperations

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        TaskTriggerDatabase::class.java,
        "task-trigger.db",
    ).addMigrations(MIGRATION_1_2).build()

    val repository = TaskRepository(database.taskDao(), database.executionLogDao())
    val scheduler = AlarmTaskScheduler(context.applicationContext)
    val executor = CommandExecutor()
    val operations = TaskOperations(repository, scheduler, executor, Clock(System::currentTimeMillis))
}
