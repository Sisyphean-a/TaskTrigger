package com.tasktrigger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tasktrigger.domain.ClaimResult
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY triggerAt")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun findById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE enabled = 1")
    suspend fun enabledTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET enabled = 0 WHERE id = :id AND enabled = 1 AND repeatDays = ''")
    suspend fun claimEnabledOneShot(id: Long): Int

    @Transaction
    suspend fun claimOneShot(id: Long): ClaimResult {
        val task = findById(id) ?: return ClaimResult.Missing
        if (!task.enabled) return ClaimResult.Disabled
        if (task.repeatDays.isNotBlank()) return ClaimResult.Claimed(task)
        return if (claimEnabledOneShot(id) == 1) {
            ClaimResult.Claimed(task.copy(enabled = false))
        } else {
            ClaimResult.Duplicate
        }
    }
}
