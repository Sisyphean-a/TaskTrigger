package com.tasktrigger.data

import com.tasktrigger.domain.LogReason
import com.tasktrigger.domain.LogSource
import com.tasktrigger.domain.LogStage
import com.tasktrigger.domain.LogStatus

data class ExecutionLogSummary(
    val id: Long,
    val taskId: Long,
    val taskName: String,
    val executedAt: Long,
    val durationMs: Long,
    val output: String,
    val source: LogSource,
    val stage: LogStage,
    val status: LogStatus,
    val reasonCode: LogReason,
    val exitCode: Int?,
    val taskNameSnapshot: String?,
    val commandSnapshot: String?,
) {
    val success: Boolean
        get() = status == LogStatus.SUCCEEDED
}
