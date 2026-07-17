package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity

class TaskOperations(
    private val store: TaskStore,
    private val scheduler: TaskScheduler,
    private val commandRunner: CommandRunner,
    private val clock: Clock,
) {
    fun rootStatus(): String = commandRunner.rootStatus()

    suspend fun save(task: TaskEntity): OperationResult<TaskEntity> {
        val normalization = normalizeForSave(task)
        val normalized = normalization.value ?: return normalization
        val saved = store.save(normalized)
        if (!saved.enabled) {
            val cancellation = scheduler.cancel(saved)
            return OperationResult(value = saved, warnings = cancellationWarnings(saved, cancellation))
        }
        return when (val result = scheduler.schedule(saved)) {
            ScheduleResult.Scheduled -> OperationResult(value = saved)
            is ScheduleResult.Failure -> handleScheduleFailure(saved, result, LogSource.USER_ACTION)
        }
    }

    suspend fun delete(taskId: Long): OperationResult<Unit> {
        val task = store.findTask(taskId) ?: return OperationResult(error = "任务不存在")
        val disabled = store.save(task.copy(enabled = false))
        val cancellation = scheduler.cancel(disabled)
        store.delete(disabled)
        return OperationResult(value = Unit, warnings = cancellationWarnings(disabled, cancellation))
    }

    suspend fun setEnabled(taskId: Long, enabled: Boolean): OperationResult<TaskEntity> {
        val task = store.findTask(taskId) ?: return OperationResult(error = "任务不存在")
        if (enabled) return save(task.copy(enabled = true))
        val disabled = store.save(task.copy(enabled = false, triggerAt = disabledTrigger(task)))
        val cancellation = scheduler.cancel(disabled)
        return OperationResult(value = disabled, warnings = cancellationWarnings(disabled, cancellation))
    }

    suspend fun executeNow(taskId: Long): OperationResult<CommandResult> {
        val task = store.findTask(taskId) ?: return OperationResult(error = "任务不存在")
        return executeTask(task, LogSource.MANUAL)
    }

    suspend fun handleAlarm(taskId: Long): OperationResult<CommandResult> {
        val claim = store.claimOneShot(taskId)
        val task = (claim as? ClaimResult.Claimed)?.task
            ?: return skipAlarm(taskId, claim)
        val execution = executeTask(task, LogSource.ALARM)
        if (task.repeatDays.isBlank()) return execution
        return rescheduleWeekly(task, execution)
    }

    suspend fun restoreOnBoot(): List<OperationResult<TaskEntity>> =
        store.enabledTasks().map { restoreTask(it) }

    private fun normalizeForSave(task: TaskEntity): OperationResult<TaskEntity> = when (task.scheduleMode) {
        ScheduleMode.FIXED -> normalizeFixed(task)
        ScheduleMode.COUNTDOWN -> normalizeCountdown(task)
    }

    private fun normalizeFixed(task: TaskEntity): OperationResult<TaskEntity> {
        if (task.enabled && task.repeatDays.isBlank() && task.triggerAt <= clock.now()) {
            return OperationResult(error = "单次固定时间必须晚于当前时间")
        }
        return OperationResult(value = task.copy(countdownDurationMillis = 0))
    }

    private fun normalizeCountdown(task: TaskEntity): OperationResult<TaskEntity> {
        if (task.repeatDays.isNotBlank() || task.countdownDurationMillis < MIN_COUNTDOWN_MILLIS) {
            return OperationResult(error = "倒计时至少需要 1 分钟，且不能设置执行周期")
        }
        val triggerAt = if (task.enabled) {
            runCatching { Math.addExact(clock.now(), task.countdownDurationMillis) }.getOrNull()
                ?: return OperationResult(error = "倒计时时长溢出")
        } else {
            0
        }
        return OperationResult(value = task.copy(triggerAt = triggerAt))
    }

    private suspend fun executeTask(
        task: TaskEntity,
        source: LogSource,
    ): OperationResult<CommandResult> {
        val executedAt = clock.now()
        val result = commandRunner.execute(task)
        val warning = runCatching {
            store.record(ExecutionEvent.command(task, source, executedAt, result))
        }.exceptionOrNull()?.message
        return OperationResult(value = result, warnings = listOfNotNull(warning))
    }

    private suspend fun handleScheduleFailure(
        task: TaskEntity,
        failure: ScheduleResult.Failure,
        source: LogSource,
    ): OperationResult<TaskEntity> {
        val disabled = store.save(task.copy(enabled = false, triggerAt = disabledTrigger(task)))
        val log = ExecutionEvent.schedulingFailure(
            disabled,
            source,
            clock.now(),
            failure.reason,
            failure.message,
        )
        val warning = runCatching { store.record(log) }.exceptionOrNull()?.message
        return OperationResult(error = failure.message, warnings = listOfNotNull(warning))
    }

    private suspend fun skipAlarm(
        taskId: Long,
        claim: ClaimResult,
    ): OperationResult<CommandResult> {
        val task = store.findTask(taskId)
        val reason = when (claim) {
            ClaimResult.Missing -> LogReason.TASK_MISSING
            ClaimResult.Disabled -> LogReason.TASK_DISABLED
            ClaimResult.Duplicate -> LogReason.DUPLICATE_DELIVERY
            is ClaimResult.Claimed -> error("已领取的任务不应进入跳过分支")
        }
        val message = claimError(claim)
        val log = ExecutionEvent.skipped(taskId, task, LogSource.ALARM, clock.now(), reason, message)
        val warning = runCatching { store.record(log) }.exceptionOrNull()?.message
        return OperationResult(error = message, warnings = listOfNotNull(warning))
    }

    private suspend fun restoreTask(task: TaskEntity): OperationResult<TaskEntity> {
        if (task.repeatDays.isBlank() && task.triggerAt <= clock.now()) {
            return expireOnBoot(task)
        }
        return when (val result = scheduler.schedule(task)) {
            ScheduleResult.Scheduled -> OperationResult(value = task)
            is ScheduleResult.Failure -> handleScheduleFailure(task, result, LogSource.BOOT_RECOVERY)
        }
    }

    private suspend fun expireOnBoot(task: TaskEntity): OperationResult<TaskEntity> {
        val disabled = store.save(task.copy(enabled = false, triggerAt = disabledTrigger(task)))
        val log = ExecutionEvent.skipped(
            task.id,
            disabled,
            LogSource.BOOT_RECOVERY,
            clock.now(),
            LogReason.EXPIRED_BEFORE_RECOVERY,
            "开机恢复时任务已经过期",
        )
        val warning = runCatching { store.record(log) }.exceptionOrNull()?.message
        return OperationResult(value = disabled, warnings = listOfNotNull(warning))
    }

    private suspend fun rescheduleWeekly(
        task: TaskEntity,
        execution: OperationResult<CommandResult>,
    ): OperationResult<CommandResult> = when (val result = scheduler.schedule(task)) {
        ScheduleResult.Scheduled -> execution
        is ScheduleResult.Failure -> {
            val scheduling = handleScheduleFailure(task, result, LogSource.ALARM)
            OperationResult(
                value = execution.value,
                error = scheduling.error,
                warnings = execution.warnings + scheduling.warnings,
            )
        }
    }

    private suspend fun cancellationWarnings(
        task: TaskEntity,
        result: ScheduleResult,
    ): List<String> {
        val failure = result as? ScheduleResult.Failure ?: return emptyList()
        val log = ExecutionEvent.schedulingFailure(
            task,
            LogSource.USER_ACTION,
            clock.now(),
            failure.reason,
            failure.message,
        )
        val logError = runCatching { store.record(log) }.exceptionOrNull()?.message
        return listOf(failure.message) + listOfNotNull(logError)
    }

    private fun disabledTrigger(task: TaskEntity): Long =
        if (task.scheduleMode == ScheduleMode.COUNTDOWN) 0 else task.triggerAt

    private fun claimError(result: ClaimResult): String = when (result) {
        ClaimResult.Missing -> "任务不存在"
        ClaimResult.Disabled -> "任务已停用"
        ClaimResult.Duplicate -> "任务已被处理"
        is ClaimResult.Claimed -> error("已领取的任务不应进入错误分支")
    }

    private companion object {
        const val MIN_COUNTDOWN_MILLIS = 60_000L
    }
}
