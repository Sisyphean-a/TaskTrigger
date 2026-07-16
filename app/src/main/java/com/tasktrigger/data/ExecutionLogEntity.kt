package com.tasktrigger.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val executedAt: Long,
    val success: Boolean,
    val durationMs: Long,
    val output: String,
)
