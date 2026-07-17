package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val onCopy: (TaskEntity) -> Unit,
    val onDelete: (TaskEntity) -> Unit,
)

@Composable
internal fun TaskHomeScreen(state: HomeUiState, callbacks: HomeCallbacks) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        HomeHeader(onLogs = callbacks.onLogs)
        RootStatusLine(state.rootStatus)
        state.statusMessage?.let { StatusMessage(it) }
        TaskList(
            tasks = state.tasks,
            onEdit = callbacks.onEdit,
            onToggle = callbacks.onToggle,
            onCopy = callbacks.onCopy,
            onDelete = callbacks.onDelete,
            modifier = Modifier.weight(1f),
        )
        TaskFooterActions(onLogs = callbacks.onLogs, onCreate = callbacks.onCreate)
    }
}

@Composable
private fun HomeHeader(onLogs: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("任务", color = TaskText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onLogs, modifier = Modifier.size(22.dp)) {
            TaskIcon(Icons.AutoMirrored.Outlined.Article, "日志", modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        IconButton(onClick = {}, modifier = Modifier.size(22.dp)) {
            TaskIcon(Icons.Outlined.Settings, "设置", tint = TaskMutedText, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun RootStatusLine(rootStatus: String) {
    val granted = rootStatus.contains("已授权") || rootStatus.contains("uid=0")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(if (granted) TaskStatusGreen else TaskError, RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (granted) "ROOT 已授权 uid=0" else "ROOT 未授权",
            color = TaskMutedText,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun StatusMessage(message: String) {
    Text(
        text = message,
        color = TaskAccent,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun TaskList(
    tasks: List<TaskEntity>,
    onEdit: (TaskEntity) -> Unit,
    onToggle: (TaskEntity, Boolean) -> Unit,
    onCopy: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    modifier: Modifier,
) {
    if (tasks.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("暂无任务", color = TaskMutedText, fontSize = 14.sp)
        }
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tasks, key = TaskEntity::id) { task ->
            TaskListItem(
                task = task,
                onEdit = { onEdit(task) },
                onToggle = { onToggle(task, it) },
                onCopy = { onCopy(task) },
                onDelete = { onDelete(task) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskListItem(
    task: TaskEntity,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(shape)
                .background(TaskSurface)
                .border(1.dp, TaskDivider, shape)
                .combinedClickable(onClick = onEdit, onLongClick = { showMenu = true })
                .padding(horizontal = 17.dp, vertical = 15.dp),
        ) {
            TaskCardHeader(task, onToggle)
            TaskCardSchedule(task)
            TaskCardCommand(task)
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("复制") },
                leadingIcon = { TaskIcon(Icons.Outlined.ContentCopy, null) },
                onClick = { showMenu = false; onCopy() },
            )
            DropdownMenuItem(
                text = { Text("删除", color = TaskError) },
                leadingIcon = { TaskIcon(Icons.Outlined.DeleteOutline, null, tint = TaskError) },
                onClick = { showMenu = false; confirmDelete = true },
            )
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除任务？") },
            text = { Text("该任务会被删除，已有执行日志会保留。") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("删除", color = TaskError)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun TaskCardHeader(task: TaskEntity, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(26.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TerminalPrompt(task.enabled, modifier = Modifier.width(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            task.name,
            color = TaskText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TaskSwitch(task.enabled, onToggle)
    }
}

@Composable
private fun TaskCardSchedule(task: TaskEntity) {
    Row(
        modifier = Modifier.height(30.dp).padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskIcon(Icons.Outlined.CalendarMonth, null, tint = TaskMutedText, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(taskScheduleText(task), color = TaskMutedText, fontSize = 12.sp)
    }
}

@Composable
private fun TaskCardCommand(task: TaskEntity) {
    Text(
        text = commandSummary(task.command),
        color = TaskSubtleText,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.height(24.dp).padding(top = 6.dp),
    )
}
