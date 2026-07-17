package com.tasktrigger.ui

import com.tasktrigger.data.TaskEntity
import com.tasktrigger.domain.ScheduleMode
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskDraftModelTest {
    @Test
    fun `countdown copy is an unsaved disabled draft without the old deadline`() {
        val source = TaskEntity(
            id = 42,
            name = "backup",
            command = "echo ok",
            triggerAt = 999_000,
            enabled = true,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownDurationMillis = 120_000,
        )

        val copy = source.copySeed()

        assertEquals(0, copy.id)
        assertEquals("backup 副本", copy.name)
        assertEquals(false, copy.enabled)
        assertEquals(0, copy.triggerAt)
        assertEquals(120_000, copy.countdownDurationMillis)
    }

    @Test
    fun `countdown draft rejects minutes outside zero to fifty nine`() {
        val draft = TaskDraft(
            name = "timer",
            command = "echo ok",
            time = "2026-07-17 10:00",
            repeatDays = "",
            useRoot = false,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownHours = "0",
            countdownMinutes = "60",
        )

        val result = draft.buildTask(existing = null, isCopy = false, now = 1_000)

        assertEquals("分钟必须是 0 到 59 的整数", result.error)
    }

    @Test
    fun `countdown draft stores duration and no repeat period`() {
        val draft = TaskDraft(
            name = "timer",
            command = "echo ok",
            time = "2026-07-17 10:00",
            repeatDays = "",
            useRoot = false,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownHours = "2",
            countdownMinutes = "5",
        )

        val result = draft.buildTask(existing = null, isCopy = false, now = 1_000)

        assertEquals(7_500_000L, result.task?.countdownDurationMillis)
        assertEquals(0L, result.task?.triggerAt)
        assertEquals("", result.task?.repeatDays)
    }

    @Test
    fun `past one shot fixed time is rejected`() {
        val draft = TaskDraft(
            name = "past",
            command = "echo ok",
            time = "2020-01-01 00:00",
            repeatDays = "",
            useRoot = false,
        )

        val result = draft.buildTask(existing = null, isCopy = false, now = Long.MAX_VALUE)

        assertEquals("单次固定时间必须晚于当前时间", result.error)
    }
}
