package com.tasktrigger.domain

import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.TaskEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskOperationsTest {
    @Test
    fun `saving enabled countdown calculates an absolute trigger and schedules it`() = runBlocking {
        val store = FakeTaskStore()
        val scheduler = FakeTaskScheduler()
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 1_000L })
        val task = TaskEntity(
            name = "countdown",
            command = "echo ok",
            triggerAt = 0,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownDurationMillis = 60_000,
        )

        val result = operations.save(task)

        assertEquals(61_000L, result.value?.triggerAt)
        assertEquals(61_000L, store.tasks.single().triggerAt)
        assertEquals(61_000L, scheduler.scheduled.single().triggerAt)
    }

    @Test
    fun `deleting disables the task before cancelling and removing it`() = runBlocking {
        val events = mutableListOf<String>()
        val task = TaskEntity(id = 7, name = "old", command = "echo ok", triggerAt = 10_000)
        val store = FakeTaskStore(events).apply { tasks += task }
        val scheduler = FakeTaskScheduler(events)
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 1_000L })

        operations.delete(task.id)

        assertEquals(listOf("save:false", "cancel", "delete"), events)
        assertEquals(emptyList<TaskEntity>(), store.tasks)
    }

    @Test
    fun `executing now records the command result through the store`() = runBlocking {
        val task = TaskEntity(id = 9, name = "manual", command = "echo ok", triggerAt = 10_000)
        val store = FakeTaskStore().apply { tasks += task }
        val operations = TaskOperations(store, FakeTaskScheduler(), FakeCommandRunner(), Clock { 2_000L })

        operations.executeNow(task.id)

        assertEquals(true, store.logs.single().success)
        assertEquals("ok", store.logs.single().output)
    }

    @Test
    fun `countdown overflow is rejected with an explicit reason`() = runBlocking {
        val task = TaskEntity(
            name = "overflow",
            command = "echo ok",
            triggerAt = 0,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownDurationMillis = 60_000,
        )
        val operations = TaskOperations(
            FakeTaskStore(),
            FakeTaskScheduler(),
            FakeCommandRunner(),
            Clock { Long.MAX_VALUE - 1 },
        )

        val result = operations.save(task)

        assertEquals(true, result.error?.contains("溢出"))
    }

    @Test
    fun `schedule failure disables task and records the reason`() = runBlocking {
        val store = FakeTaskStore()
        val scheduler = FakeTaskScheduler(
            scheduleResult = ScheduleResult.Failure(
                LogReason.EXACT_ALARM_DENIED,
                "denied",
            ),
        )
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 1_000L })
        val task = TaskEntity(name = "fixed", command = "echo ok", triggerAt = 10_000)

        operations.save(task)

        assertEquals(false, store.tasks.single().enabled)
        assertEquals(LogReason.EXACT_ALARM_DENIED, store.logs.single().reasonCode)
        assertEquals(LogStage.SCHEDULING, store.logs.single().stage)
    }

    @Test
    fun `duplicate alarm delivery is skipped without running the command`() = runBlocking {
        val task = TaskEntity(id = 5, name = "once", command = "echo ok", triggerAt = 10_000)
        val store = FakeTaskStore().apply {
            tasks += task
            nextClaim = ClaimResult.Duplicate
        }
        val runner = FakeCommandRunner()
        val operations = TaskOperations(store, FakeTaskScheduler(), runner, Clock { 2_000L })

        operations.handleAlarm(task.id)

        assertEquals(0, runner.executionCount)
        assertEquals(LogReason.DUPLICATE_DELIVERY, store.logs.single().reasonCode)
        assertEquals(LogStatus.SKIPPED, store.logs.single().status)
        assertEquals(LogStage.DISPATCH, store.logs.single().stage)
    }

    @Test
    fun `boot recovery disables an expired one shot and records a skip`() = runBlocking {
        val task = TaskEntity(
            id = 6,
            name = "expired",
            command = "echo ok",
            triggerAt = 999,
            scheduleMode = ScheduleMode.COUNTDOWN,
            countdownDurationMillis = 60_000,
        )
        val store = FakeTaskStore().apply { tasks += task }
        val scheduler = FakeTaskScheduler()
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 1_000L })

        operations.restoreOnBoot()

        assertEquals(false, store.tasks.single().enabled)
        assertEquals(emptyList<TaskEntity>(), scheduler.scheduled)
        assertEquals(LogReason.EXPIRED_BEFORE_RECOVERY, store.logs.single().reasonCode)
        assertEquals(LogSource.BOOT_RECOVERY, store.logs.single().source)
    }

    @Test
    fun `weekly task reschedules even when execution log persistence fails`() = runBlocking {
        val task = TaskEntity(
            id = 8,
            name = "weekly",
            command = "echo ok",
            triggerAt = 10_000,
            repeatDays = "1",
        )
        val store = FakeTaskStore().apply {
            tasks += task
            recordFailure = IllegalStateException("log unavailable")
        }
        val scheduler = FakeTaskScheduler()
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 2_000L })

        val result = operations.handleAlarm(task.id)

        assertEquals(task, scheduler.scheduled.single())
        assertEquals("log unavailable", result.warnings.single())
        assertEquals(0, result.value?.exitCode)
    }

    @Test
    fun `cancel failure is logged without preventing deletion`() = runBlocking {
        val task = TaskEntity(id = 12, name = "delete", command = "echo ok", triggerAt = 10_000)
        val store = FakeTaskStore().apply { tasks += task }
        val scheduler = FakeTaskScheduler(
            cancelResult = ScheduleResult.Failure(LogReason.CANCEL_FAILED, "cancel failed"),
        )
        val operations = TaskOperations(store, scheduler, FakeCommandRunner(), Clock { 2_000L })

        operations.delete(task.id)

        assertEquals(emptyList<TaskEntity>(), store.tasks)
        assertEquals(LogReason.CANCEL_FAILED, store.logs.single().reasonCode)
        assertEquals(LogSource.USER_ACTION, store.logs.single().source)
    }

    @Test
    fun `enabling an expired fixed one shot is rejected and stays disabled`() = runBlocking {
        val task = TaskEntity(
            id = 13,
            name = "expired fixed",
            command = "echo ok",
            triggerAt = 999,
            enabled = false,
        )
        val store = FakeTaskStore().apply { tasks += task }
        val operations = TaskOperations(store, FakeTaskScheduler(), FakeCommandRunner(), Clock { 1_000L })

        val result = operations.setEnabled(task.id, true)

        assertEquals("单次固定时间必须晚于当前时间", result.error)
        assertEquals(false, store.tasks.single().enabled)
    }
}

private class FakeTaskStore(private val events: MutableList<String>? = null) : TaskStore {
    val tasks = mutableListOf<TaskEntity>()
    val logs = mutableListOf<ExecutionLogEntity>()
    var nextClaim: ClaimResult? = null
    var recordFailure: RuntimeException? = null

    override suspend fun save(task: TaskEntity): TaskEntity {
        events?.add("save:${task.enabled}")
        val saved = task.copy(id = if (task.id == 0L) 1 else task.id)
        tasks.removeAll { it.id == saved.id }
        tasks += saved
        return saved
    }

    override suspend fun delete(task: TaskEntity) {
        events?.add("delete")
        tasks.removeAll { it.id == task.id }
    }

    override suspend fun findTask(id: Long): TaskEntity? = tasks.find { it.id == id }

    override suspend fun enabledTasks(): List<TaskEntity> = tasks.filter(TaskEntity::enabled)

    override suspend fun claimOneShot(id: Long): ClaimResult =
        nextClaim ?: tasks.find { it.id == id }?.let(ClaimResult::Claimed) ?: ClaimResult.Missing

    override suspend fun record(log: ExecutionLogEntity) {
        recordFailure?.let { throw it }
        logs += log
    }
}

private class FakeTaskScheduler(
    private val events: MutableList<String>? = null,
    private val scheduleResult: ScheduleResult = ScheduleResult.Scheduled,
    private val cancelResult: ScheduleResult = ScheduleResult.Scheduled,
) : TaskScheduler {
    val scheduled = mutableListOf<TaskEntity>()

    override fun schedule(task: TaskEntity): ScheduleResult {
        scheduled += task
        return scheduleResult
    }

    override fun cancel(task: TaskEntity): ScheduleResult = cancelResult
        .also { events?.add("cancel") }
}

private class FakeCommandRunner : CommandRunner {
    var executionCount = 0

    override fun execute(task: TaskEntity): CommandResult = CommandResult(
        durationMs = 1,
        output = "ok",
        exitCode = 0,
        failure = CommandFailure.NONE,
    ).also { executionCount += 1 }

    override fun rootStatus(): String = "Root 状态：已授权"
}
