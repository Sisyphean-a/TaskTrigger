package com.tasktrigger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasktrigger.domain.LogReason
import com.tasktrigger.domain.LogSource
import com.tasktrigger.domain.LogStage
import com.tasktrigger.domain.LogStatus

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val executedAt: Long,
    val durationMs: Long,
    val output: String,
    val source: LogSource,
    val stage: LogStage,
    val status: LogStatus,
    val reasonCode: LogReason,
    val exitCode: Int? = null,
    val taskNameSnapshot: String? = null,
    val commandSnapshot: String? = null,
) {
    val success: Boolean
        get() = status == LogStatus.SUCCEEDED
}
