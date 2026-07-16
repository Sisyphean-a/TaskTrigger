package com.tasktrigger.domain

import com.tasktrigger.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TaskScheduleTest {
    @Test
    fun `single task in the future keeps its trigger time`() {
        val now = System.currentTimeMillis()
        val task = TaskEntity(name = "once", command = "echo ok", triggerAt = now + 60_000)

        assertEquals(task.triggerAt, TaskSchedule.nextTrigger(task, now))
    }

    @Test
    fun `repeating task selects the requested weekday`() {
        val zone = ZoneId.systemDefault()
        val mondayNine = LocalDateTime.of(2026, 7, 20, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val sundayNoon = LocalDateTime.of(2026, 7, 19, 12, 0).atZone(zone).toInstant().toEpochMilli()
        val task = TaskEntity(name = "weekly", command = "echo ok", triggerAt = mondayNine, repeatDays = "1")

        assertEquals(mondayNine, TaskSchedule.nextTrigger(task, sundayNoon))
    }
}
