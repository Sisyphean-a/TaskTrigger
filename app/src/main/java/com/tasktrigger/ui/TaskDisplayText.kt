package com.tasktrigger.ui

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity
import com.tasktrigger.domain.ScheduleMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun taskScheduleText(task: TaskEntity): String {
    if (task.scheduleMode == ScheduleMode.COUNTDOWN) {
        val totalMinutes = task.countdownDurationMillis / 60_000
        return "倒计时 · ${totalMinutes / 60}小时${totalMinutes % 60}分钟"
    }
    val time = Instant.ofEpochMilli(task.triggerAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    if (task.repeatDays.isBlank()) return "单次 · $time"
    val days = task.repeatDays.split(',').mapNotNull(String::toIntOrNull).sorted()
    val period = when (days) {
        listOf(1, 2, 3, 4, 5) -> "工作日"
        listOf(1, 2, 3, 4, 5, 6, 7) -> "每天"
        else -> days.joinToString("、") { "周$it" }
    }
    return "$period · $time"
}

internal fun commandSummary(command: String): String = command.lineSequence().firstOrNull().orEmpty()

internal fun editorTime(task: TaskEntity?): String = task?.let {
    LocalDateTime.ofInstant(Instant.ofEpochMilli(it.triggerAt), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
} ?: LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

internal fun logTime(executedAt: Long): String = Instant.ofEpochMilli(executedAt)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

internal fun logStatus(log: ExecutionLogEntity): String = if (log.success) "成功" else "失败"
