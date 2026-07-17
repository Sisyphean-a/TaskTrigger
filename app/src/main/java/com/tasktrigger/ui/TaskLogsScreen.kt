package com.tasktrigger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktrigger.data.ExecutionLogEntity
import com.tasktrigger.data.ExecutionLogSummary

@Composable
internal fun GlobalLogsScreen(
    logs: List<ExecutionLogSummary>,
    onBack: () -> Unit,
    onCreate: () -> Unit,
) {
    LogScreenFrame(title = "执行日志", onBack = onBack, showActions = true) {
        Text(
            "最近执行",
            color = TaskMutedText,
            fontSize = 13.sp,
            modifier = Modifier.height(32.dp).padding(start = 20.dp),
        )
        GlobalLogList(logs, modifier = Modifier.weight(1f))
        TaskFooterActions(onLogs = {}, onCreate = onCreate)
    }
}

@Composable
internal fun TaskLogsScreen(
    task: com.tasktrigger.data.TaskEntity,
    logs: List<ExecutionLogEntity>,
    onBack: () -> Unit,
) {
    LogScreenFrame(title = task.name, onBack = onBack) {
        Text(
            "任务日志",
            color = TaskMutedText,
            fontSize = 13.sp,
            modifier = Modifier.height(32.dp).padding(start = 20.dp),
        )
        TaskLogList(logs, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LogScreenFrame(
    title: String,
    onBack: () -> Unit,
    showActions: Boolean = false,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(22.dp)) {
                TaskIcon(Icons.AutoMirrored.Outlined.ArrowBack, "返回", modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(12.dp))
            Text(
                title,
                color = TaskText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (showActions) {
                IconButton(onClick = {}, modifier = Modifier.size(20.dp)) {
                    TaskIcon(Icons.Outlined.MoreVert, "更多", tint = TaskMutedText, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.size(12.dp))
                IconButton(onClick = onBack, modifier = Modifier.size(20.dp)) {
                    TaskIcon(Icons.Outlined.Close, "关闭", tint = TaskMutedText, modifier = Modifier.size(20.dp))
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
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        items(logs, key = ExecutionLogSummary::id) { log ->
            LogItem(
                LogRow(
                    log.taskName,
                    log.executedAt,
                    log.durationMs,
                    log.output,
                    log.source,
                    log.stage,
                    log.status,
                    log.reasonCode,
                    log.exitCode,
                    log.commandSnapshot,
                ),
            )
        }
    }
}

@Composable
private fun TaskLogList(logs: List<ExecutionLogEntity>, modifier: Modifier) {
    if (logs.isEmpty()) {
        EmptyLogs(modifier)
        return
    }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        items(logs, key = ExecutionLogEntity::id) { log ->
            LogItem(
                LogRow(
                    log.taskNameSnapshot,
                    log.executedAt,
                    log.durationMs,
                    log.output,
                    log.source,
                    log.stage,
                    log.status,
                    log.reasonCode,
                    log.exitCode,
                    log.commandSnapshot,
                ),
            )
        }
    }
}

@Composable
private fun EmptyLogs(modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("暂无执行记录", color = TaskMutedText, fontSize = 14.sp)
    }
}
