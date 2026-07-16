package com.tasktrigger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.ExecutionLogSummary

private data class LogRow(
    val taskName: String?,
    val executedAt: Long,
    val success: Boolean,
    val durationMs: Long,
    val output: String,
)

@Composable
internal fun GlobalLogsScreen(
    logs: List<ExecutionLogSummary>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
) {
    LogScreenFrame(title = "执行日志", onBack = onBack) {
        Text("最近执行", color = TaskMutedText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 16.dp))
        GlobalLogList(logs = logs, modifier = Modifier.weight(1f))
        LogsBottomBar(onCreate = onCreate)
    }
}

@Composable
internal fun TaskLogsScreen(task: com.tasktrigger.data.TaskEntity, logs: List<ExecutionLogEntity>, onBack: () -> Unit) {
    LogScreenFrame(title = task.name, onBack = onBack) {
        Text("任务日志", color = TaskMutedText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 16.dp))
        TaskLogList(logs = logs, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LogScreenFrame(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) { Text("‹", color = TaskText, style = MaterialTheme.typography.headlineLarge) }
            Spacer(Modifier.padding(start = 12.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        HorizontalDivider(color = TaskDivider)
        content()
    }
}

@Composable
private fun GlobalLogList(logs: List<ExecutionLogSummary>, modifier: Modifier = Modifier) {
    if (logs.isEmpty()) {
        EmptyLogs(modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 12.dp)) {
        items(logs, key = ExecutionLogSummary::id) { log ->
            LogItem(LogRow(log.taskName, log.executedAt, log.success, log.durationMs, log.output))
        }
    }
}

@Composable
private fun TaskLogList(logs: List<ExecutionLogEntity>, modifier: Modifier = Modifier) {
    if (logs.isEmpty()) {
        EmptyLogs(modifier)
        return
    }
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 12.dp)) {
        items(logs, key = ExecutionLogEntity::id) { log ->
            LogItem(LogRow(null, log.executedAt, log.success, log.durationMs, log.output))
        }
    }
}

@Composable
private fun EmptyLogs(modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("暂无执行记录", color = TaskMutedText, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LogItem(log: LogRow) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("●", color = TaskAccent, modifier = Modifier.padding(end = 14.dp))
            Column(modifier = Modifier.weight(1f)) {
                log.taskName?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
                Text(logTime(log.executedAt), color = TaskMutedText, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = "${if (log.success) "成功" else "失败"} · ${log.durationMs}ms",
                color = TaskText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = log.output,
            color = TaskMutedText,
            fontFamily = FontFamily.Monospace,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 29.dp, top = 12.dp),
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun LogsBottomBar(onCreate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("▤  日志", color = TaskAccent, style = MaterialTheme.typography.titleMedium)
        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(containerColor = TaskAccent, contentColor = TaskBackground),
        ) {
            Text("＋  新建任务", style = MaterialTheme.typography.titleMedium)
        }
    }
}
