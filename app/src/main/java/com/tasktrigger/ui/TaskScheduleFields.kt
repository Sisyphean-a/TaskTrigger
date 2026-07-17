package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktrigger.domain.ScheduleMode

@Composable
internal fun EditorScheduleFields(
    draft: TaskDraft,
    onDraftChange: (TaskDraft) -> Unit,
) {
    EditorScheduleMode(draft.scheduleMode) { mode ->
        val repeatDays = if (mode == ScheduleMode.COUNTDOWN) "" else draft.repeatDays
        onDraftChange(draft.copy(scheduleMode = mode, repeatDays = repeatDays))
    }
    if (draft.scheduleMode == ScheduleMode.FIXED) {
        EditorTimeRow(draft.time) { onDraftChange(draft.copy(time = it)) }
        EditorPeriodRow(draft.repeatDays) { onDraftChange(draft.copy(repeatDays = it)) }
    } else {
        EditorCountdownRow(draft, onDraftChange)
    }
}

@Composable
private fun EditorScheduleMode(
    selected: ScheduleMode,
    onSelected: (ScheduleMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("时间模式", color = TaskMutedText, fontSize = 15.sp, modifier = Modifier.weight(1f))
        ScheduleModeSelector(selected, onSelected)
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun ScheduleModeSelector(selected: ScheduleMode, onSelected: (ScheduleMode) -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier.width(164.dp).height(48.dp).selectableGroup(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(shape)
                .border(1.dp, TaskDivider, shape),
        ) {
            ScheduleMode.entries.forEachIndexed { index, mode ->
                if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(TaskDivider))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(if (selected == mode) TaskRaisedSurface else Color.Transparent),
                )
            }
        }
        Row(modifier = Modifier.fillMaxSize()) {
            ScheduleMode.entries.forEach { mode ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected == mode,
                            onClick = { onSelected(mode) },
                            role = Role.RadioButton,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (mode == ScheduleMode.FIXED) "固定" else "倒计时",
                        color = if (selected == mode) TaskAccent else TaskMutedText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun EditorCountdownRow(
    draft: TaskDraft,
    onDraftChange: (TaskDraft) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text("倒计时", color = TaskMutedText, fontSize = 15.sp, modifier = Modifier.weight(1f))
        CountdownInput(draft.countdownHours) {
            onDraftChange(draft.copy(countdownHours = it.filter(Char::isDigit)))
        }
        Text("小时", color = TaskMutedText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
        CountdownInput(draft.countdownMinutes) {
            onDraftChange(draft.copy(countdownMinutes = it.filter(Char::isDigit)))
        }
        Text("分钟", color = TaskMutedText, fontSize = 13.sp, modifier = Modifier.padding(start = 6.dp))
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun CountdownInput(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = TaskText, fontSize = 15.sp, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(TaskAccent),
        modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(0.12f),
    )
}
