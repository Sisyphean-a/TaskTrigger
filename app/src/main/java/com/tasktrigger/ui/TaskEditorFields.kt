package com.tasktrigger.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
internal fun EditorFields(
    draft: TaskDraft,
    isNew: Boolean,
    onDraftChange: (TaskDraft) -> Unit,
) {
    EditorNameRow(draft.name) { onDraftChange(draft.copy(name = it)) }
    EditorScheduleFields(draft, onDraftChange)
    CommandEditor(draft.command, isNew) { onDraftChange(draft.copy(command = it)) }
}

@Composable
private fun EditorNameRow(value: String, onValueChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("任务名称")
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = TaskText, fontSize = 15.sp, textAlign = TextAlign.End),
            cursorBrush = SolidColor(TaskAccent),
            modifier = Modifier.weight(1f),
            decorationBox = { input ->
                Box(contentAlignment = Alignment.CenterEnd) {
                    if (value.isBlank()) Text("请输入任务名称", color = TaskSubtleText, fontSize = 15.sp)
                    input()
                }
            },
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
internal fun EditorTimeRow(time: String, onTimeChange: (String) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { showTimePicker(context, time, onTimeChange) }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("执行时间")
        Text(time.takeLast(5), color = TaskText, fontSize = 15.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        TaskIcon(Icons.Outlined.AccessTime, "选择时间", tint = TaskMutedText, modifier = Modifier.width(18.dp))
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
internal fun EditorPeriodRow(repeatDays: String, onRepeatDaysChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { showPicker = true }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EditorLabel("执行周期")
        Text(
            repeatDaysDisplay(repeatDays),
            color = TaskText,
            fontSize = 15.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TaskIcon(Icons.Outlined.ChevronRight, "选择周期", tint = TaskMutedText, modifier = Modifier.width(18.dp))
    }
    HorizontalDivider(color = TaskDivider)
    if (showPicker) PeriodPicker(repeatDays, onDismiss = { showPicker = false }) {
        onRepeatDaysChange(it)
        showPicker = false
    }
}

@Composable
private fun EditorLabel(text: String) {
    Text(text, color = TaskMutedText, fontSize = 15.sp, modifier = Modifier.width(80.dp))
}

@Composable
private fun CommandEditor(command: String, isNew: Boolean, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "执行命令",
            color = TaskMutedText,
            fontSize = 15.sp,
            modifier = Modifier.height(47.dp).padding(start = 20.dp, top = 16.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isNew) 185.dp else 120.dp)
                .padding(horizontal = 16.dp),
        ) {
            CommandInput(command, onValueChange, if (isNew) 160.dp else 120.dp)
        }
        if (!isNew) {
            Text(
                text = "${command.length}/2000",
                color = TaskSubtleText,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().height(39.dp).padding(start = 20.dp, end = 20.dp, top = 8.dp),
            )
        } else {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CommandInput(command: String, onValueChange: (String) -> Unit, height: androidx.compose.ui.unit.Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(TaskCodeSurface, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text("1\n2", color = TaskSubtleText, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = command,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TaskText, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp),
            cursorBrush = SolidColor(TaskAccent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { input ->
                if (command.isBlank()) Text("请输入 Shell 命令", color = TaskSubtleText, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                input()
            },
        )
    }
}

@Composable
internal fun EditorRootMode(checked: Boolean, isNew: Boolean, onCheckedChange: (Boolean) -> Unit) {
    HorizontalDivider(color = TaskDivider)
    Row(
        modifier = Modifier.fillMaxWidth().height(if (isNew) 77.dp else 59.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Root 模式", color = TaskText, fontSize = 15.sp)
            if (isNew) Text("带有 su -c 执行", color = TaskMutedText, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
        }
        TaskSwitch(checked, onCheckedChange)
    }
    HorizontalDivider(color = TaskDivider)
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
