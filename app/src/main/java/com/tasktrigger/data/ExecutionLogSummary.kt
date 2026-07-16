package com.tasktrigger.data

data class ExecutionLogSummary(
    val id: Long,
    val taskId: Long,
    val taskName: String,
    val executedAt: Long,
    val success: Boolean,
    val durationMs: Long,
    val output: String,
)
