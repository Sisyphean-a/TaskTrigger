package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktrigger.data.TaskEntity

internal data class HomeUiState(
    val tasks: List<TaskEntity>,
    val rootStatus: String,
    val statusMessage: String?,
)

internal data class HomeCallbacks(
    val onCreate: () -> Unit,
    val onEdit: (TaskEntity) -> Unit,
    val onLogs: () -> Unit,
    val onToggle: (TaskEntity, Boolean) -> Unit,
)

@Composable
internal fun TaskHomeScreen(state: HomeUiState, callbacks: HomeCallbacks) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        HomeHeader(onLogs = callbacks.onLogs)
        RootStatusLine(state.rootStatus)
        state.statusMessage?.let { StatusMessage(it) }
        HorizontalDivider(color = TaskDivider)
        TaskList(
            tasks = state.tasks,
            onEdit = callbacks.onEdit,
            onToggle = callbacks.onToggle,
            modifier = Modifier.weight(1f),
        )
        HomeBottomBar(onLogs = callbacks.onLogs, onCreate = callbacks.onCreate)
    }
}

@Composable
private fun HomeHeader(onLogs: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("任务", color = TaskText, fontSize = 28.sp, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onLogs) { TaskIcon(Icons.AutoMirrored.Outlined.Article, "日志") }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = {}, enabled = false) {
            TaskIcon(Icons.Outlined.Settings, "设置", tint = TaskText)
        }
    }
}

@Composable
private fun RootStatusLine(rootStatus: String) {
    val granted = rootStatus.contains("已授权") || rootStatus.contains("uid=0")
    Row(
        modifier = Modifier.padding(bottom = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (granted) TaskStatusGreen else TaskAccent, androidx.compose.foundation.shape.CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (granted) "ROOT  已授权 · uid=0" else "ROOT  未授权",
            color = TaskMutedText,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun StatusMessage(message: String) {
    Text(
        text = message,
        color = TaskAccent,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun TaskList(
    tasks: List<TaskEntity>,
    onEdit: (TaskEntity) -> Unit,
    onToggle: (TaskEntity, Boolean) -> Unit,
    modifier: Modifier,
) {
    if (tasks.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("暂无任务", color = TaskMutedText, fontSize = 18.sp)
        }
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 10.dp)) {
        items(tasks, key = TaskEntity::id) { task ->
            TaskListItem(task, onEdit = { onEdit(task) }, onToggle = { onToggle(task, it) })
        }
    }
}

@Composable
private fun TaskListItem(task: TaskEntity, onEdit: () -> Unit, onToggle: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 27.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TerminalPrompt(enabled = task.enabled, modifier = Modifier.width(54.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.name, color = TaskText, fontSize = 22.sp)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TaskIcon(
                        Icons.Outlined.CalendarMonth,
                        null,
                        tint = TaskMutedText,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(taskScheduleText(task), color = TaskMutedText, fontSize = 15.sp)
                }
            }
            TaskSwitch(task.enabled, onToggle)
        }
        Text(
            text = commandSummary(task.command),
            color = TaskMutedText,
            fontFamily = FontFamily.Monospace,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 54.dp, top = 16.dp),
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun HomeBottomBar(onLogs: () -> Unit, onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onLogs)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TaskIcon(Icons.AutoMirrored.Outlined.Article, null, tint = TaskText, modifier = Modifier.size(27.dp))
            Spacer(Modifier.width(12.dp))
            Text("日志", color = TaskText, fontSize = 18.sp)
        }
        TaskPrimaryButton(
            text = "新建任务",
            onClick = onCreate,
            modifier = Modifier.width(154.dp),
            icon = Icons.Outlined.Add,
        )
    }
}
