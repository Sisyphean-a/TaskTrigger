package com.tasktrigger.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
internal fun TaskHomeScreen(
    state: HomeUiState,
    callbacks: HomeCallbacks,
) {
    val exactAlarmAllowed = rememberExactAlarmPermission()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        HomeHeader()
        RootStatusLine(state.rootStatus)
        if (!exactAlarmAllowed) ExactAlarmPermissionRow()
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
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 22.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("任务", style = MaterialTheme.typography.headlineLarge)
        Text("≡", color = TaskMutedText, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun RootStatusLine(rootStatus: String) {
    val granted = rootStatus.contains("已授权")
    val label = if (granted) "ROOT  已授权 · uid=0" else "ROOT  未授权"
    Row(modifier = Modifier.padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("●", color = if (granted) TaskStatusGreen else TaskAccent)
        Spacer(Modifier.padding(start = 8.dp))
        Text(label, color = TaskMutedText, style = MaterialTheme.typography.labelMedium)
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
    modifier: Modifier = Modifier,
) {
    if (tasks.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("暂无任务", color = TaskMutedText, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 12.dp)) {
        items(tasks, key = TaskEntity::id) { task ->
            TaskListItem(task = task, onEdit = { onEdit(task) }, onToggle = { onToggle(task, it) })
        }
    }
}

@Composable
private fun TaskListItem(task: TaskEntity, onEdit: () -> Unit, onToggle: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = ">_",
                color = if (task.enabled) TaskAccent else TaskMutedText,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 18.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(task.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(taskScheduleText(task), color = TaskMutedText, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = task.enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TaskAccent,
                    checkedTrackColor = TaskAccent.copy(alpha = 0.45f),
                    uncheckedThumbColor = Color(0xFFB6BDB9),
                    uncheckedTrackColor = Color(0xFF38413D),
                ),
            )
        }
        Text(
            text = commandSummary(task.command),
            color = TaskMutedText,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 42.dp, top = 13.dp),
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun HomeBottomBar(onLogs: () -> Unit, onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onLogs) {
            Text("▤  日志", color = TaskText, style = MaterialTheme.typography.titleMedium)
        }
        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(containerColor = TaskAccent, contentColor = TaskBackground),
        ) {
            Text("＋  新建任务", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ExactAlarmPermissionRow() {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("精确定时权限未开启", color = TaskAccent, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = { requestExactAlarmPermission(context) }) { Text("授权", color = TaskAccent) }
    }
}

@Composable
private fun rememberExactAlarmPermission(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val manager = remember { context.getSystemService(AlarmManager::class.java) }
    var allowed by remember { mutableStateOf(canScheduleExactAlarms(manager)) }
    DisposableEffect(lifecycleOwner, manager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) allowed = canScheduleExactAlarms(manager)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return allowed
}

private fun canScheduleExactAlarms(manager: AlarmManager?): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager?.canScheduleExactAlarms() == true

private fun requestExactAlarmPermission(context: Context) {
    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    })
}
