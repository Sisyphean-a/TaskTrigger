package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class CommandExecutor {
    fun execute(task: TaskEntity): ExecutionLogEntity {
        val startedAt = System.currentTimeMillis()
        return runCatching {
            val process = ProcessBuilder(command(task)).redirectErrorStream(true).start()
            if (!process.waitFor(MAX_RECEIVER_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return timeoutLog(task, startedAt)
            }
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val exitCode = process.exitValue()
            resultLog(task, startedAt, exitCode == 0, output.ifBlank { "exit code: $exitCode" })
        }.getOrElse { error ->
            resultLog(task, startedAt, false, error.message ?: error.toString())
        }
    }

    private fun timeoutLog(task: TaskEntity, startedAt: Long) = resultLog(
        task, startedAt, false, "命令超过 ${MAX_RECEIVER_SECONDS} 秒，已终止以避免系统结束定时接收器。",
    )

    private fun resultLog(task: TaskEntity, startedAt: Long, success: Boolean, output: String) = ExecutionLogEntity(
        taskId = task.id, executedAt = startedAt, success = success,
        durationMs = System.currentTimeMillis() - startedAt, output = output,
    )

    private fun command(task: TaskEntity): List<String> = if (task.useRoot) {
        listOf("su", "-c", task.command)
    } else {
        listOf("sh", "-c", task.command)
    }

    private companion object { const val MAX_RECEIVER_SECONDS = 8L }
}
