package com.tasktrigger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasktrigger.data.AppContainer
import com.tasktrigger.data.TaskEntity
import com.tasktrigger.domain.ScheduleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskViewModel(private val container: AppContainer) : ViewModel() {
    val tasks = container.repository.observeTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val statusMessage = MutableStateFlow<String?>(null)

    fun logs(taskId: Long) = container.repository.observeLogs(taskId)

    fun allLogs() = container.repository.observeAllLogs()

    fun save(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        val saved = container.repository.save(task)
        if (saved.enabled) publishScheduleResult(container.scheduler.schedule(saved)) else container.scheduler.cancel(saved)
    }

    fun delete(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        container.scheduler.cancel(task)
        container.repository.delete(task)
    }

    fun setEnabled(task: TaskEntity, enabled: Boolean) = save(task.copy(enabled = enabled))

    fun executeNow(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        container.repository.record(container.executor.execute(task))
    }

    suspend fun rootStatus(): String = withContext(Dispatchers.IO) { container.executor.rootStatus() }

    private fun publishScheduleResult(result: ScheduleResult) {
        statusMessage.value = (result as? ScheduleResult.Failure)?.message
    }
}
