package com.tasktrigger.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
internal fun EditorFields(draft: TaskDraft, onDraftChange: (TaskDraft) -> Unit) {
    EditorNameRow(draft.name) { onDraftChange(draft.copy(name = it)) }
    EditorTimeRow(draft.time) { onDraftChange(draft.copy(time = it)) }
    EditorPeriodRow(draft.repeatDays) { onDraftChange(draft.copy(repeatDays = it)) }
    CommandEditor(draft.command) { onDraftChange(draft.copy(command = it)) }
    RootModeRow(draft.useRoot) { onDraftChange(draft.copy(useRoot = it)) }
}

@Composable
private fun EditorNameRow(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("任务名称")
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = TaskText, fontSize = 20.sp),
            cursorBrush = SolidColor(TaskAccent),
            modifier = Modifier.weight(1f),
            decorationBox = { input ->
                if (value.isBlank()) Text("请输入任务名称", color = TaskMutedText, fontSize = 17.sp)
                input()
            },
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun EditorTimeRow(time: String, onTimeChange: (String) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable { showTimePicker(context, time, onTimeChange) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("执行时间")
        Text(time.takeLast(5), color = TaskText, fontSize = 20.sp, modifier = Modifier.weight(1f))
        TaskIcon(Icons.Outlined.AccessTime, "选择时间", tint = TaskAccent, modifier = Modifier.width(30.dp))
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun EditorPeriodRow(repeatDays: String, onRepeatDaysChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).clickable { showPicker = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("执行周期")
        Text(repeatDaysDisplay(repeatDays), color = TaskText, fontSize = 20.sp, modifier = Modifier.weight(1f))
        TaskIcon(Icons.Outlined.ChevronRight, "选择周期", tint = TaskAccent, modifier = Modifier.width(30.dp))
    }
    HorizontalDivider(color = TaskDivider)
    if (showPicker) PeriodPicker(repeatDays, onDismiss = { showPicker = false }) {
        onRepeatDaysChange(it)
        showPicker = false
    }
}

@Composable
private fun EditorLabel(text: String) {
    Text(text, color = TaskMutedText, fontSize = 19.sp, modifier = Modifier.width(150.dp))
}

@Composable
private fun CommandEditor(command: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 12.dp)) {
        Text("执行命令", color = TaskMutedText, fontSize = 19.sp)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Top) {
            TerminalPrompt(modifier = Modifier.padding(top = 12.dp).width(50.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(150.dp)
                    .border(BorderStroke(1.dp, TaskDivider), TaskCorner)
                    .padding(horizontal = 14.dp, vertical = 16.dp),
            ) {
                Text("1\n2", color = TaskMutedText.copy(alpha = 0.55f), fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                Spacer(Modifier.width(18.dp))
                BasicTextField(
                    value = command,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TaskText, fontFamily = FontFamily.Monospace, fontSize = 15.sp, lineHeight = 22.sp),
                    cursorBrush = SolidColor(TaskAccent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { input ->
                        if (command.isBlank()) Text("请输入 Shell 命令", color = TaskMutedText, fontFamily = FontFamily.Monospace, fontSize = 15.sp)
                        input()
                    },
                )
            }
        }
        Text("Shell command", color = TaskMutedText, fontSize = 15.sp, modifier = Modifier.padding(start = 50.dp, top = 8.dp))
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun RootModeRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Root 模式", color = TaskText, fontSize = 20.sp)
        Spacer(Modifier.width(26.dp))
        Text("使用 su -c 执行", color = TaskMutedText, fontSize = 15.sp, modifier = Modifier.weight(1f))
        TaskSwitch(checked, onCheckedChange)
    }
}

private fun repeatDaysDisplay(repeatDays: String): String {
    val days = repeatDays.split(',').mapNotNull(String::toIntOrNull).sorted()
    return when (days) {
        emptyList<Int>() -> "单次"
        listOf(1, 2, 3, 4, 5) -> "周一 至 周五"
        listOf(1, 2, 3, 4, 5, 6, 7) -> "每天"
        else -> days.joinToString("、") { "周$it" }
    }
}

private fun showTimePicker(context: android.content.Context, value: String, onTimeChange: (String) -> Unit) {
    val parsed = parseEditorTime(value) ?: LocalDateTime.now().plusHours(1)
    TimePickerDialog(context, { _, hour, minute ->
        onTimeChange(parsed.withHour(hour).withMinute(minute).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
    }, parsed.hour, parsed.minute, true).show()
}

private fun parseEditorTime(value: String): LocalDateTime? = try {
    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
} catch (_: DateTimeParseException) {
    null
}
