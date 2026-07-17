package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity

enum class ScheduleMode { FIXED, COUNTDOWN }

fun interface Clock {
    fun now(): Long
}

interface TaskStore {
    suspend fun save(task: TaskEntity): TaskEntity
    suspend fun delete(task: TaskEntity)
    suspend fun findTask(id: Long): TaskEntity?
    suspend fun enabledTasks(): List<TaskEntity>
    suspend fun claimOneShot(id: Long): ClaimResult
    suspend fun record(log: ExecutionLogEntity)
}

sealed interface ClaimResult {
    data class Claimed(val task: TaskEntity) : ClaimResult
    data object Missing : ClaimResult
    data object Disabled : ClaimResult
    data object Duplicate : ClaimResult
}

interface TaskScheduler {
    fun schedule(task: TaskEntity): ScheduleResult
    fun cancel(task: TaskEntity): ScheduleResult
}

interface CommandRunner {
    fun execute(task: TaskEntity): CommandResult
    fun rootStatus(): String
}

data class CommandResult(
    val durationMs: Long,
    val output: String,
    val exitCode: Int?,
    val failure: CommandFailure,
)

enum class CommandFailure { NONE, NON_ZERO_EXIT, PROCESS_START_FAILED, TIMEOUT }

data class OperationResult<T>(
    val value: T? = null,
    val error: String? = null,
    val warnings: List<String> = emptyList(),
)
