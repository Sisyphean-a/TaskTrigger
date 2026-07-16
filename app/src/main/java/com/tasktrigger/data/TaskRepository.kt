package com.tasktrigger.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val logDao: ExecutionLogDao,
) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

    fun observeLogs(taskId: Long): Flow<List<ExecutionLogEntity>> = logDao.observeForTask(taskId)

    fun observeAllLogs(): Flow<List<ExecutionLogSummary>> = logDao.observeAll()

    suspend fun save(task: TaskEntity): TaskEntity {
        val id = if (task.id == 0L) taskDao.insert(task) else {
            taskDao.update(task)
            task.id
        }
        return task.copy(id = id)
    }

    suspend fun delete(task: TaskEntity) = taskDao.delete(task)

    suspend fun findTask(id: Long): TaskEntity? = taskDao.findById(id)

    suspend fun enabledTasks(): List<TaskEntity> = taskDao.enabledTasks()

    suspend fun record(log: ExecutionLogEntity) = logDao.insert(log)
}
