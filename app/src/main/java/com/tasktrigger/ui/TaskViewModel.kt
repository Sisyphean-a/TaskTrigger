package com.tasktrigger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasktrigger.data.AppContainer
import com.tasktrigger.data.TaskEntity
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
        publishResult(container.operations.save(task))
    }

    fun delete(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        publishResult(container.operations.delete(task.id))
    }

    fun setEnabled(task: TaskEntity, enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        publishResult(container.operations.setEnabled(task.id, enabled))
    }

    fun executeNow(task: TaskEntity) = viewModelScope.launch(Dispatchers.IO) {
        publishResult(container.operations.executeNow(task.id))
    }

    suspend fun rootStatus(): String = withContext(Dispatchers.IO) { container.operations.rootStatus() }

    private fun publishResult(result: com.tasktrigger.domain.OperationResult<*>) {
        statusMessage.value = result.error ?: result.warnings.firstOrNull()
    }
}
