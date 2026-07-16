package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterListOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    LogScreenFrame(title = "执行日志", onBack = onBack, showClear = true) {
        Text("最近执行", color = TaskMutedText, fontSize = 17.sp, modifier = Modifier.padding(top = 22.dp, bottom = 18.dp))
        HorizontalDivider(color = TaskDivider)
        GlobalLogList(logs, modifier = Modifier.weight(1f))
        LogsBottomBar(onCreate)
    }
}

@Composable
internal fun TaskLogsScreen(
    task: com.tasktrigger.data.TaskEntity,
    logs: List<ExecutionLogEntity>,
    onBack: () -> Unit,
) {
    LogScreenFrame(title = task.name, onBack = onBack) {
        Text("任务日志", color = TaskMutedText, fontSize = 17.sp, modifier = Modifier.padding(top = 22.dp, bottom = 18.dp))
        HorizontalDivider(color = TaskDivider)
        TaskLogList(logs, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LogScreenFrame(
    title: String,
    onBack: () -> Unit,
    showClear: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                TaskIcon(Icons.AutoMirrored.Outlined.ArrowBack, "返回", modifier = Modifier.size(33.dp))
            }
            Spacer(Modifier.width(22.dp))
            Text(title, color = TaskText, fontSize = 26.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            if (showClear) {
                IconButton(onClick = {}, modifier = Modifier.size(48.dp)) {
                    TaskIcon(Icons.Outlined.FilterListOff, "清除筛选", modifier = Modifier.size(30.dp))
                }
            }
        }
        content()
    }
}

@Composable
private fun GlobalLogList(logs: List<ExecutionLogSummary>, modifier: Modifier) {
    if (logs.isEmpty()) {
        EmptyLogs(modifier)
        return
    }
    LazyColumn(modifier = modifier) {
        items(logs, key = ExecutionLogSummary::id) { log ->
            LogItem(LogRow(log.taskName, log.executedAt, log.success, log.durationMs, log.output))
        }
    }
}

@Composable
private fun TaskLogList(logs: List<ExecutionLogEntity>, modifier: Modifier) {
    if (logs.isEmpty()) {
        EmptyLogs(modifier)
        return
    }
    LazyColumn(modifier = modifier) {
        items(logs, key = ExecutionLogEntity::id) { log ->
            LogItem(LogRow(null, log.executedAt, log.success, log.durationMs, log.output))
        }
    }
}

@Composable
private fun EmptyLogs(modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("暂无执行记录", color = TaskMutedText, fontSize = 18.sp)
    }
}

@Composable
private fun LogItem(log: LogRow) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 9.dp, end = 24.dp)
                    .size(17.dp)
                    .background(if (log.success) TaskAccent else TaskError, androidx.compose.foundation.shape.CircleShape),
            )
            Column(modifier = Modifier.weight(1f)) {
                log.taskName?.let { Text(it, color = TaskText, fontSize = 22.sp) }
                Spacer(Modifier.height(if (log.taskName == null) 0.dp else 12.dp))
                Text(logTime(log.executedAt), color = TaskMutedText, fontSize = 16.sp)
                Text(
                    text = log.output,
                    color = TaskMutedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            Text(
                text = "${if (log.success) "成功" else "失败"} · ${log.durationMs}ms",
                color = TaskText,
                fontSize = 16.sp,
            )
        }
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun LogsBottomBar(onCreate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.width(90.dp).height(3.dp).background(TaskAccent, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TaskIcon(Icons.AutoMirrored.Outlined.Article, null, tint = TaskAccent, modifier = Modifier.size(27.dp))
                Spacer(Modifier.width(9.dp))
                Text("日志", color = TaskAccent, fontSize = 18.sp)
            }
        }
        TaskPrimaryButton(
            text = "新建任务",
            onClick = onCreate,
            modifier = Modifier.width(154.dp),
            icon = Icons.Outlined.Add,
        )
    }
}
