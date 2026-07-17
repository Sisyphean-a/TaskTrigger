package com.tasktrigger.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, ExecutionLogEntity::class], version = 2)
abstract class TaskTriggerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun executionLogDao(): ExecutionLogDao
}
