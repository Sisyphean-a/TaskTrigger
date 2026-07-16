package com.tasktrigger.ui

import android.os.Bundle
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tasktrigger.TaskTriggerApplication
import com.tasktrigger.data.TaskEntity
import com.tasktrigger.domain.TaskSchedule
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as TaskTriggerApplication).container
        setContent { TaskTriggerTheme { TaskTriggerScreen(TaskViewModel(container)) } }
    }
}

@Composable
private fun TaskTriggerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF7CFF6B), background = Color(0xFF111111), surface = Color(0xFF1A1A1A),
    ), content = content)
}

@Composable
private fun TaskTriggerScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    var editing by remember { mutableStateOf<TaskEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var logTask by remember { mutableStateOf<TaskEntity?>(null) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("TASKTRIGGER", style = MaterialTheme.typography.headlineMedium)
            Text("定时执行系统指令", color = Color.LightGray)
            statusMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            RootStatus(viewModel)
            ExactAlarmPermission()
            Spacer(Modifier.height(16.dp))
            Button(onClick = { creating = true }) { Text("+ 新任务") }
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    TaskRow(task, onEdit = { editing = task }, onToggle = { viewModel.setEnabled(task, it) },
                        onRun = { viewModel.executeNow(task) }, onDelete = { viewModel.delete(task) }, onLogs = { logTask = task })
                }
            }
        }
    }
    if (creating || editing != null) TaskEditor(editing, onDismiss = { creating = false; editing = null }, onSave = viewModel::save)
    logTask?.let { TaskLogs(it, viewModel, onDismiss = { logTask = null }) }
}

@Composable
private fun RootStatus(viewModel: TaskViewModel) {
    var available by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) { available = viewModel.rootAvailable() }
    Text(when (available) { true -> "Root 状态：已检测到 su"; false -> "Root 状态：未检测到 su"; null -> "Root 状态：检测中…" })
}

@Composable
private fun ExactAlarmPermission() {
    val context = LocalContext.current
    val manager = context.getSystemService(AlarmManager::class.java)
    if (android.os.Build.VERSION.SDK_INT >= 31 && manager?.canScheduleExactAlarms() == false) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("精确定时权限未开启", color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { requestExactAlarmPermission(context) }) { Text("授权") }
        }
    }
}

private fun requestExactAlarmPermission(context: Context) {
    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    })
}

@Composable
private fun TaskRow(task: TaskEntity, onEdit: () -> Unit, onToggle: (Boolean) -> Unit, onRun: () -> Unit, onDelete: () -> Unit, onLogs: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(task.name, style = MaterialTheme.typography.titleMedium); Text(TaskSchedule.description(task)) }
            Switch(task.enabled, onCheckedChange = onToggle)
        }
        Text(task.command.lineSequence().firstOrNull().orEmpty(), color = Color.LightGray, maxLines = 1)
        Row { TextButton(onClick = onRun) { Text("立即执行") }; TextButton(onClick = onLogs) { Text("日志") }; TextButton(onClick = onEdit) { Text("编辑") }; TextButton(onClick = onDelete) { Text("删除") } }
        HorizontalDivider()
    }
}

@Composable
private fun TaskLogs(task: TaskEntity, viewModel: TaskViewModel, onDismiss: () -> Unit) {
    val logs by viewModel.logs(task.id).collectAsState(initial = emptyList())
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("${task.name} 的执行日志") }, text = {
        LazyColumn { items(logs, key = { it.id }) { log ->
            Card(Modifier.padding(vertical = 4.dp)) { Column(Modifier.padding(8.dp)) {
                Text(if (log.success) "成功 · ${log.durationMs}ms" else "失败 · ${log.durationMs}ms")
                Text(java.time.Instant.ofEpochMilli(log.executedAt).atZone(ZoneId.systemDefault()).format(formatter))
                Text(log.output, color = Color.LightGray)
            } }
        } }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}

@Composable
private fun TaskEditor(task: TaskEntity?, onDismiss: () -> Unit, onSave: (TaskEntity) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    var name by remember { mutableStateOf(task?.name.orEmpty()) }
    var command by remember { mutableStateOf(task?.command.orEmpty()) }
    var time by remember { mutableStateOf(task?.let { LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it.triggerAt), ZoneId.systemDefault()).format(formatter) } ?: LocalDateTime.now().plusHours(1).format(formatter)) }
    var root by remember { mutableStateOf(task?.useRoot ?: false) }
    var repeat by remember { mutableStateOf(task?.repeatDays ?: "") }
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (task == null) "新任务" else "编辑任务") }, text = {
        Column {
            OutlinedTextField(name, { name = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(time, { time = it }, label = { Text("时间：yyyy-MM-dd HH:mm") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(repeat, { repeat = it }, label = { Text("周期：留空为单次，1-7 用逗号分隔") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(command, { command = it }, label = { Text("Shell 命令") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Text("为遵守 Android 广播生命周期，定时命令最长执行 8 秒。", color = Color.LightGray)
            Row { Checkbox(root, { root = it }); Text("使用 Root（su -c）") }
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = {
        Button(onClick = {
            val parsed = runCatching { LocalDateTime.parse(time, formatter).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
            error = validate(name, command, parsed, repeat)
            if (error.isBlank()) onSave(TaskEntity(id = task?.id ?: 0, name = name.trim(), command = command.trim(), triggerAt = parsed!!, repeatDays = repeat.trim(), enabled = task?.enabled ?: true, useRoot = root))
            if (error.isBlank()) onDismiss()
        }) { Text("保存") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}

private fun validate(name: String, command: String, time: Long?, repeat: String): String = when {
    name.isBlank() -> "请输入任务名称"
    command.isBlank() -> "请输入执行命令"
    time == null -> "时间格式无效"
    repeat.isNotBlank() && repeat.split(',').any { it.toIntOrNull() !in 1..7 } -> "周期只能填写 1 到 7"
    else -> ""
}
