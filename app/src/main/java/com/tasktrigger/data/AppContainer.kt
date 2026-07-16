package com.tasktrigger.data

import android.content.Context
import androidx.room.Room
import com.tasktrigger.domain.AlarmTaskScheduler
import com.tasktrigger.domain.CommandExecutor

class AppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        TaskTriggerDatabase::class.java,
        "task-trigger.db",
    ).build()

    val repository = TaskRepository(database.taskDao(), database.executionLogDao())
    val scheduler = AlarmTaskScheduler(context.applicationContext)
    val executor = CommandExecutor()
}
