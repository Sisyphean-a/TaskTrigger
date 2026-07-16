package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity
import java.io.BufferedReader
import java.io.InputStreamReader

class CommandExecutor {
    fun execute(task: TaskEntity): ExecutionLogEntity {
        val startedAt = System.currentTimeMillis()
        return runCatching {
            val process = ProcessBuilder(command(task)).redirectErrorStream(true).start()
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ExecutionLogEntity(taskId = task.id, executedAt = startedAt, success = exitCode == 0,
                durationMs = System.currentTimeMillis() - startedAt, output = output.ifBlank { "exit code: $exitCode" })
        }.getOrElse { error ->
            ExecutionLogEntity(taskId = task.id, executedAt = startedAt, success = false,
                durationMs = System.currentTimeMillis() - startedAt, output = error.message ?: error.toString())
        }
    }

    private fun command(task: TaskEntity): List<String> = if (task.useRoot) {
        listOf("su", "-c", task.command)
    } else {
        listOf("sh", "-c", task.command)
    }
}
