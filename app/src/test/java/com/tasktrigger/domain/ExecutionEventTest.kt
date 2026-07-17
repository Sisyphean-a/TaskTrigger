package com.tasktrigger.domain

import com.tasktrigger.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExecutionEventTest {
    @Test
    fun `successful command event keeps source exit code and snapshots`() {
        val task = TaskEntity(id = 4, name = "backup", command = "echo ok", triggerAt = 2_000)
        val result = CommandResult(25, "ok", 0, CommandFailure.NONE)

        val log = ExecutionEvent.command(task, LogSource.MANUAL, 1_000, result)

        assertEquals(LogStatus.SUCCEEDED, log.status)
        assertEquals(LogStage.EXECUTION, log.stage)
        assertEquals(LogReason.NONE, log.reasonCode)
        assertEquals(LogSource.MANUAL, log.source)
        assertEquals(0, log.exitCode)
        assertEquals("backup", log.taskNameSnapshot)
        assertEquals("echo ok", log.commandSnapshot)
    }
}
