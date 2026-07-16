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
}
