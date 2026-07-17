package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity

enum class LogSource { MANUAL, ALARM, BOOT_RECOVERY, USER_ACTION, LEGACY }

enum class LogStage { SCHEDULING, DISPATCH, EXECUTION }

enum class LogStatus { SUCCEEDED, FAILED, SKIPPED }

enum class LogReason {
    NONE,
    NON_ZERO_EXIT,
    PROCESS_START_FAILED,
    TIMEOUT,
    EXACT_ALARM_DENIED,
    ALARM_MANAGER_UNAVAILABLE,
    SCHEDULE_EXCEPTION,
    CANCEL_FAILED,
    TASK_MISSING,
    TASK_DISABLED,
    DUPLICATE_DELIVERY,
    EXPIRED_BEFORE_RECOVERY,
}

object ExecutionEvent {
    fun command(
        task: TaskEntity,
        source: LogSource,
        executedAt: Long,
        result: CommandResult,
    ): ExecutionLogEntity = ExecutionLogEntity(
        taskId = task.id,
        executedAt = executedAt,
        durationMs = result.durationMs,
        output = result.output,
        source = source,
        stage = LogStage.EXECUTION,
        status = if (result.failure == CommandFailure.NONE) LogStatus.SUCCEEDED else LogStatus.FAILED,
        reasonCode = result.failure.toReason(),
        exitCode = result.exitCode,
        taskNameSnapshot = task.name,
        commandSnapshot = task.command,
    )

    fun schedulingFailure(
        task: TaskEntity,
        source: LogSource,
        executedAt: Long,
        reason: LogReason,
        message: String,
    ): ExecutionLogEntity = ExecutionLogEntity(
        taskId = task.id,
        executedAt = executedAt,
        durationMs = 0,
        output = message,
        source = source,
        stage = LogStage.SCHEDULING,
        status = LogStatus.FAILED,
        reasonCode = reason,
        taskNameSnapshot = task.name,
        commandSnapshot = task.command,
    )

    fun skipped(
        taskId: Long,
        task: TaskEntity?,
        source: LogSource,
        executedAt: Long,
        reason: LogReason,
        message: String,
    ): ExecutionLogEntity = ExecutionLogEntity(
        taskId = taskId,
        executedAt = executedAt,
        durationMs = 0,
        output = message,
        source = source,
        stage = LogStage.DISPATCH,
        status = LogStatus.SKIPPED,
        reasonCode = reason,
        taskNameSnapshot = task?.name,
        commandSnapshot = task?.command,
    )

    private fun CommandFailure.toReason(): LogReason = when (this) {
        CommandFailure.NONE -> LogReason.NONE
        CommandFailure.NON_ZERO_EXIT -> LogReason.NON_ZERO_EXIT
        CommandFailure.PROCESS_START_FAILED -> LogReason.PROCESS_START_FAILED
        CommandFailure.TIMEOUT -> LogReason.TIMEOUT
    }
}
