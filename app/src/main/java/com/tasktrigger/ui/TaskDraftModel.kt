package com.tasktrigger.ui

import com.tasktrigger.data.TaskEntity
import com.tasktrigger.domain.ScheduleMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal data class TaskDraft(
    val name: String,
    val command: String,
    val time: String,
    val repeatDays: String,
    val useRoot: Boolean,
    val scheduleMode: ScheduleMode = ScheduleMode.FIXED,
    val countdownHours: String = "0",
    val countdownMinutes: String = "1",
)

internal data class DraftBuildResult(
    val task: TaskEntity? = null,
    val error: String? = null,
)

internal fun TaskEntity.copySeed(): TaskEntity = copy(
    id = 0,
    name = "$name 副本",
    triggerAt = if (scheduleMode == ScheduleMode.COUNTDOWN) 0 else triggerAt,
    enabled = false,
)

internal fun TaskEntity?.toDraft(): TaskDraft {
    val duration = this?.countdownDurationMillis ?: 60_000
    return TaskDraft(
        name = this?.name.orEmpty(),
        command = this?.command.orEmpty(),
        time = editorTime(this),
        repeatDays = this?.repeatDays.orEmpty(),
        useRoot = this?.useRoot ?: false,
        scheduleMode = this?.scheduleMode ?: ScheduleMode.FIXED,
        countdownHours = (duration / HOUR_MILLIS).toString(),
        countdownMinutes = ((duration % HOUR_MILLIS) / MINUTE_MILLIS).toString(),
    )
}

internal fun TaskDraft.buildTask(
    existing: TaskEntity?,
    isCopy: Boolean,
    now: Long,
): DraftBuildResult {
    commonError()?.let { return DraftBuildResult(error = it) }
    val schedule = scheduleValues(now)
    schedule.error?.let { return DraftBuildResult(error = it) }
    return DraftBuildResult(
        task = TaskEntity(
            id = existing?.id ?: 0,
            name = name.trim(),
            command = command.trim(),
            triggerAt = schedule.triggerAt,
            repeatDays = schedule.repeatDays,
            enabled = if (isCopy) false else existing?.enabled ?: true,
            useRoot = useRoot,
            scheduleMode = scheduleMode,
            countdownDurationMillis = schedule.durationMillis,
        ),
    )
}

private data class ScheduleValues(
    val triggerAt: Long = 0,
    val repeatDays: String = "",
    val durationMillis: Long = 0,
    val error: String? = null,
)

private fun TaskDraft.commonError(): String? = when {
    name.isBlank() -> "请输入任务名称"
    command.isBlank() -> "请输入执行命令"
    else -> null
}

private fun TaskDraft.scheduleValues(now: Long): ScheduleValues = when (scheduleMode) {
    ScheduleMode.FIXED -> fixedValues(now)
    ScheduleMode.COUNTDOWN -> countdownValues()
}

private fun TaskDraft.fixedValues(now: Long): ScheduleValues {
    val triggerAt = parseTriggerAt(time) ?: return ScheduleValues(error = "执行时间格式无效")
    if (repeatDays.isNotBlank() && repeatDays.split(',').any { it.toIntOrNull() !in 1..7 }) {
        return ScheduleValues(error = "周期只能填写 1 到 7")
    }
    if (repeatDays.isBlank() && triggerAt <= now) {
        return ScheduleValues(error = "单次固定时间必须晚于当前时间")
    }
    return ScheduleValues(triggerAt = triggerAt, repeatDays = repeatDays.trim())
}

private fun TaskDraft.countdownValues(): ScheduleValues {
    if (repeatDays.isNotBlank()) return ScheduleValues(error = "倒计时不能设置执行周期")
    val hours = countdownHours.ifBlank { "0" }.toLongOrNull()
        ?: return ScheduleValues(error = "小时必须是非负整数")
    val minutes = countdownMinutes.ifBlank { "0" }.toLongOrNull()
        ?: return ScheduleValues(error = "分钟必须是 0 到 59 的整数")
    if (hours < 0) return ScheduleValues(error = "小时必须是非负整数")
    if (minutes !in 0..59) return ScheduleValues(error = "分钟必须是 0 到 59 的整数")
    val duration = runCatching {
        Math.addExact(Math.multiplyExact(hours, HOUR_MILLIS), minutes * MINUTE_MILLIS)
    }.getOrNull() ?: return ScheduleValues(error = "倒计时时长溢出")
    if (duration < MINUTE_MILLIS) return ScheduleValues(error = "倒计时至少需要 1 分钟")
    return ScheduleValues(durationMillis = duration)
}

private fun parseTriggerAt(time: String): Long? = try {
    LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    null
}

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 3_600_000L
