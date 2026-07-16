package com.tasktrigger.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.tasktrigger.data.TaskEntity

private sealed class TaskScreen {
    data object Home : TaskScreen()
    data object Create : TaskScreen()
    data object Logs : TaskScreen()
    data class Edit(val task: TaskEntity) : TaskScreen()
    data class TaskLogs(val task: TaskEntity) : TaskScreen()
}

@Composable
internal fun TaskTriggerApp(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var rootStatus by remember { mutableStateOf("Root 状态：检测中…") }
    var screen by remember { mutableStateOf<TaskScreen>(TaskScreen.Home) }
    LaunchedEffect(Unit) { rootStatus = viewModel.rootStatus() }

    Surface(modifier = Modifier.fillMaxSize(), color = TaskBackground) {
        when (val current = screen) {
            TaskScreen.Home -> TaskHomeScreen(
                state = HomeUiState(tasks, rootStatus, statusMessage),
                callbacks = HomeCallbacks(
                    onCreate = { screen = TaskScreen.Create },
                    onEdit = { screen = TaskScreen.Edit(it) },
                    onLogs = { screen = TaskScreen.Logs },
                    onToggle = viewModel::setEnabled,
                ),
            )
            TaskScreen.Create -> TaskEditorScreen(
                task = null,
                callbacks = EditorCallbacks(
                    onBack = { screen = TaskScreen.Home },
                    onSave = { viewModel.save(it); screen = TaskScreen.Home },
                    onExecute = {},
                    onLogs = {},
                    onDelete = {},
                ),
            )
            is TaskScreen.Edit -> TaskEditorScreen(
                task = current.task,
                callbacks = EditorCallbacks(
                    onBack = { screen = TaskScreen.Home },
                    onSave = { viewModel.save(it); screen = TaskScreen.Home },
                    onExecute = { viewModel.executeNow(it); screen = TaskScreen.TaskLogs(it) },
                    onLogs = { screen = TaskScreen.TaskLogs(it) },
                    onDelete = { viewModel.delete(it); screen = TaskScreen.Home },
                ),
            )
            TaskScreen.Logs -> GlobalLogsScreen(
                logs = viewModel.allLogs().collectAsState(initial = emptyList()).value,
                onBack = { screen = TaskScreen.Home },
                onCreate = { screen = TaskScreen.Create },
            )
            is TaskScreen.TaskLogs -> TaskLogsScreen(
                task = current.task,
                logs = viewModel.logs(current.task.id).collectAsState(initial = emptyList()).value,
                onBack = { screen = TaskScreen.Edit(current.task) },
            )
        }
    }
}
