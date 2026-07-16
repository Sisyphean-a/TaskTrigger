package com.tasktrigger.domain

import com.tasktrigger.data.TaskEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

object TaskSchedule {
    fun nextTrigger(task: TaskEntity, now: Long = System.currentTimeMillis()): Long? {
        if (task.repeatDays.isBlank()) return task.triggerAt.takeIf { it > now }
        val zone = ZoneId.systemDefault()
        val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(task.triggerAt), zone)
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(now), zone)
        return nextRepeated(start, current, task.repeatDays)
            .atZone(zone).toInstant().toEpochMilli()
    }

    private fun nextRepeated(start: LocalDateTime, now: LocalDateTime, days: String): LocalDateTime {
        val enabledDays = days.split(',').mapNotNull(String::toIntOrNull).toSet()
        val candidate = now.withHour(start.hour).withMinute(start.minute).withSecond(0).withNano(0)
        return (0..7).asSequence()
            .map { candidate.plusDays(it.toLong()) }
            .first { it.isAfter(now) && it.dayOfWeek.value in enabledDays }
    }

    fun description(task: TaskEntity): String = if (task.repeatDays.isBlank()) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(task.triggerAt), ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.MINUTES).toString().replace('T', ' ')
    } else {
        "每周 ${task.repeatDays.split(',').joinToString("、") { "周$it" }}"
    }
}
