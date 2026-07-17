package com.tasktrigger.data

import com.tasktrigger.domain.ClaimResult
import com.tasktrigger.domain.TaskStore
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val logDao: ExecutionLogDao,
) : TaskStore {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

    fun observeLogs(taskId: Long): Flow<List<ExecutionLogEntity>> = logDao.observeForTask(taskId)

    fun observeAllLogs(): Flow<List<ExecutionLogSummary>> = logDao.observeAll()

    override suspend fun save(task: TaskEntity): TaskEntity {
        val id = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task)
            task.id
        }
        return task.copy(id = id)
    }

    override suspend fun delete(task: TaskEntity) = taskDao.delete(task)

    override suspend fun findTask(id: Long): TaskEntity? = taskDao.findById(id)

    override suspend fun enabledTasks(): List<TaskEntity> = taskDao.enabledTasks()

    override suspend fun claimOneShot(id: Long): ClaimResult = taskDao.claimOneShot(id)

    override suspend fun record(log: ExecutionLogEntity) = logDao.insert(log)
}
