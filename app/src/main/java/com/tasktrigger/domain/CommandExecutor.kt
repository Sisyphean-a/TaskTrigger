package com.tasktrigger.domain

import com.tasktrigger.data.TaskEntity
import java.util.concurrent.TimeUnit

class CommandExecutor(
    private val processStarter: (List<String>) -> Process = {
        ProcessBuilder(it).redirectErrorStream(true).start()
    },
    private val timeoutSeconds: Long = MAX_RECEIVER_SECONDS,
) : CommandRunner {
    override fun rootStatus(): String = runCatching {
        val process = processStarter(listOf("su", "-c", "id"))
        if (!process.waitFor(ROOT_CHECK_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            return "Root 状态：未授权或无响应"
        }
        val output = process.inputStream.bufferedReader().use { it.readText().trim() }
        if (process.exitValue() == 0 && output.contains("uid=0")) "Root 状态：已授权（$output）" else "Root 状态：未 Root"
    }.getOrDefault("Root 状态：未 Root")

    override fun execute(task: TaskEntity): CommandResult {
        val startedAt = System.currentTimeMillis()
        return runCatching {
            val process = processStarter(command(task))
            val output = StringBuilder()
            var readFailure: Throwable? = null
            val reader = Thread {
                runCatching {
                    process.inputStream.bufferedReader().use { output.append(it.readText()) }
                }.onFailure { readFailure = it }
            }.apply { start() }
            val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                process.waitFor()
            }
            reader.join()
            readFailure?.let { throw it }
            if (!completed) return timeoutResult(startedAt, output.toString())
            val exitCode = process.exitValue()
            commandResult(
                startedAt = startedAt,
                exitCode = exitCode,
                output = output.toString().ifBlank { "exit code: $exitCode" },
                failure = if (exitCode == 0) CommandFailure.NONE else CommandFailure.NON_ZERO_EXIT,
            )
        }.getOrElse { error ->
            commandResult(startedAt, null, error.message ?: error.toString(), CommandFailure.PROCESS_START_FAILED)
        }
    }

    private fun timeoutResult(startedAt: Long, output: String): CommandResult {
        val message = "命令超过 $timeoutSeconds 秒，已终止以避免系统结束定时接收器。"
        val combined = if (output.isBlank()) message else "$output\n$message"
        return commandResult(startedAt, null, combined, CommandFailure.TIMEOUT)
    }

    private fun commandResult(
        startedAt: Long,
        exitCode: Int?,
        output: String,
        failure: CommandFailure,
    ) = CommandResult(
        durationMs = System.currentTimeMillis() - startedAt, output = output,
        exitCode = exitCode, failure = failure,
    )

    private fun command(task: TaskEntity): List<String> = if (task.useRoot) {
        listOf("su", "-c", task.command)
    } else {
        listOf("sh", "-c", task.command)
    }

    private companion object {
        const val MAX_RECEIVER_SECONDS = 8L
        const val ROOT_CHECK_SECONDS = 3L
    }
}
