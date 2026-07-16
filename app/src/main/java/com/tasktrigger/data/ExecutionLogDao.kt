package com.tasktrigger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutionLogDao {
    @Insert
    suspend fun insert(log: ExecutionLogEntity)

    @Query("SELECT * FROM execution_logs WHERE taskId = :taskId ORDER BY executedAt DESC")
    fun observeForTask(taskId: Long): Flow<List<ExecutionLogEntity>>

    @Query(
        """
        SELECT execution_logs.id, execution_logs.taskId, execution_logs.executedAt,
               execution_logs.success, execution_logs.durationMs, execution_logs.output,
               COALESCE(tasks.name, '已删除任务') AS taskName
        FROM execution_logs
        LEFT JOIN tasks ON tasks.id = execution_logs.taskId
        ORDER BY execution_logs.executedAt DESC
        """,
    )
    fun observeAll(): Flow<List<ExecutionLogSummary>>
}
