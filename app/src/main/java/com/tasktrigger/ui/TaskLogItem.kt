package com.tasktrigger.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktrigger.domain.LogReason
import com.tasktrigger.domain.LogSource
import com.tasktrigger.domain.LogStage
import com.tasktrigger.domain.LogStatus

internal data class LogRow(
    val taskName: String?,
    val executedAt: Long,
    val durationMs: Long,
    val output: String,
    val source: LogSource,
    val stage: LogStage,
    val status: LogStatus,
    val reason: LogReason,
    val exitCode: Int?,
    val commandSnapshot: String?,
)

@Composable
internal fun LogItem(log: LogRow) {
    var expanded by remember(log) { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(TaskSurface, shape)
            .border(1.dp, TaskDivider, shape)
            .clickable { expanded = !expanded }
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        LogCardHeader(log)
        LogMetadata(log)
        log.commandSnapshot?.takeIf { expanded }?.let {
            Text(
                text = "\$ $it",
                color = TaskMutedText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        Text(
            text = log.output.ifBlank { "无输出" },
            color = TaskSubtleText,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 2,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 22.dp, top = 6.dp),
        )
    }
}

@Composable
private fun LogCardHeader(log: LogRow) {
    val color = statusColor(log.status)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(5.dp)),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            log.taskName ?: "历史记录",
            color = TaskText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${statusText(log.status)} · ${log.durationMs}ms",
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LogMetadata(log: LogRow) {
    val exit = log.exitCode?.let { " · exit $it" }.orEmpty()
    Text(
        text = "${logTime(log.executedAt)} · ${sourceText(log.source)} · ${stageText(log.stage)}$exit",
        color = TaskMutedText,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 22.dp, top = 3.dp),
    )
    if (log.reason != LogReason.NONE) {
        Text(
            text = log.reason.name,
            color = TaskError,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 22.dp, top = 2.dp),
        )
    }
}

private fun statusText(status: LogStatus): String = when (status) {
    LogStatus.SUCCEEDED -> "成功"
    LogStatus.FAILED -> "失败"
    LogStatus.SKIPPED -> "已跳过"
}

private fun statusColor(status: LogStatus) = when (status) {
    LogStatus.SUCCEEDED -> TaskStatusGreen
    LogStatus.FAILED -> TaskError
    LogStatus.SKIPPED -> TaskMutedText
}

private fun sourceText(source: LogSource): String = when (source) {
    LogSource.MANUAL -> "手动"
    LogSource.ALARM -> "定时"
    LogSource.BOOT_RECOVERY -> "开机恢复"
    LogSource.USER_ACTION -> "用户操作"
    LogSource.LEGACY -> "历史"
}

private fun stageText(stage: LogStage): String = when (stage) {
    LogStage.SCHEDULING -> "调度"
    LogStage.DISPATCH -> "派发"
    LogStage.EXECUTION -> "执行"
}
