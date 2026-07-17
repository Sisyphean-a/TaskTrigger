package com.tasktrigger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasktrigger.domain.ScheduleMode

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val command: String,
    val triggerAt: Long,
    val repeatDays: String = "",
    val enabled: Boolean = true,
    val useRoot: Boolean = false,
    val scheduleMode: ScheduleMode = ScheduleMode.FIXED,
    val countdownDurationMillis: Long = 0,
)
