package com.tasktrigger.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktrigger.data.TaskEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal data class TaskDraft(
    val name: String,
    val command: String,
    val time: String,
    val repeatDays: String,
    val useRoot: Boolean,
)

internal data class EditorCallbacks(
    val onBack: () -> Unit,
    val onSave: (TaskEntity) -> Unit,
    val onExecute: (TaskEntity) -> Unit,
    val onLogs: (TaskEntity) -> Unit,
    val onDelete: (TaskEntity) -> Unit,
)

private data class EditorContentState(
    val task: TaskEntity?,
    val draft: TaskDraft,
    val error: String?,
    val savedTask: TaskEntity?,
    val onSaveDraft: (TaskDraft) -> Unit,
)

@Composable
internal fun TaskEditorScreen(task: TaskEntity?, callbacks: EditorCallbacks) {
    val taskKey = task?.id
    var draft by remember(taskKey) { mutableStateOf(task.toDraft()) }
    var error by remember(taskKey) { mutableStateOf<String?>(null) }
    var confirmDelete by remember(taskKey) { mutableStateOf(false) }
    val savedTask = task?.takeIf { it.id > 0 }
    val state = EditorContentState(task, draft, error, savedTask) { value ->
        val triggerAt = parseTriggerAt(value.time)
        error = validateTaskInput(value, triggerAt)
        if (error == null) callbacks.onSave(taskEntity(task, value, triggerAt!!))
    }
    EditorContent(state, callbacks.copy(onDelete = { confirmDelete = true })) { draft = it }
    if (confirmDelete && savedTask != null) {
        DeleteConfirmation(onDismiss = { confirmDelete = false }) { callbacks.onDelete(savedTask) }
    }
}

@Composable
private fun EditorContent(
    state: EditorContentState,
    callbacks: EditorCallbacks,
    onDraftChange: (TaskDraft) -> Unit,
) {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        EditorTopBar(state.task == null, callbacks.onBack) { state.onSaveDraft(state.draft) }
        EditorFields(state.draft, isNew = state.task == null, onDraftChange = onDraftChange)
        if (state.task == null) {
            EditorRootMode(state.draft.useRoot, isNew = true) { onDraftChange(state.draft.copy(useRoot = it)) }
            EditorImmediateAction(task = null, callbacks = callbacks)
        } else {
            EditorImmediateAction(task = state.savedTask, callbacks = callbacks)
            EditorRootMode(state.draft.useRoot, isNew = false) { onDraftChange(state.draft.copy(useRoot = it)) }
            EditorTaskActions(state.savedTask, callbacks)
        }
        state.error?.let {
            Text(it, color = TaskError, fontSize = 12.sp, modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
        }
        EditorSaveButton(state)
    }
}

@Composable
private fun EditorTopBar(isNew: Boolean, onBack: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.width(22.dp)) {
            TaskIcon(Icons.AutoMirrored.Outlined.ArrowBack, "返回", modifier = Modifier.width(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(if (isNew) "新建任务" else "编辑任务", color = TaskText, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Text("保存", color = TaskAccent, fontSize = 16.sp, modifier = Modifier.clickable(onClick = onSave))
    }
}

@Composable
private fun EditorImmediateAction(task: TaskEntity?, callbacks: EditorCallbacks) {
    Box(modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp, vertical = 12.dp)) {
        TaskExecuteButton(
            onClick = { task?.let(callbacks.onExecute) },
            enabled = task != null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EditorTaskActions(task: TaskEntity?, callbacks: EditorCallbacks) {
    task?.let {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TaskOutlineButton(
                text = "查看日志",
                onClick = { callbacks.onLogs(it) },
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Outlined.Article,
            )
            TaskDangerButton(
                text = "删除任务",
                onClick = { callbacks.onDelete(it) },
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.DeleteOutline,
            )
        }
    }
}

@Composable
private fun EditorSaveButton(state: EditorContentState) {
    Box(modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 16.dp, vertical = 12.dp)) {
        TaskSaveButton(
            text = if (state.task == null) "保存并启用" else "保存修改",
            onClick = { state.onSaveDraft(state.draft) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}

@Composable
internal fun PeriodPicker(value: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var selected by remember(value) { mutableStateOf(value.split(',').mapNotNull(String::toIntOrNull).toSet()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("执行周期") },
        text = {
            Column {
                val days = listOf("一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5, "六" to 6, "日" to 7)
                days.chunked(4).forEach { row ->
                    Row {
                        row.forEach { (label, day) ->
                            TextButton(onClick = { selected = selected.toggle(day) }) {
                                Text("周$label", color = if (day in selected) TaskAccent else TaskText)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(selected.sorted().joinToString(",")) }) { Text("确定", color = TaskAccent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun DeleteConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除任务？") },
        text = { Text("该任务会被删除，已有执行日志会保留。") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("删除", color = TaskError) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private fun Set<Int>.toggle(day: Int): Set<Int> = if (day in this) this - day else this + day

private fun TaskEntity?.toDraft(): TaskDraft = TaskDraft(
    name = this?.name.orEmpty(),
    command = this?.command.orEmpty(),
    time = editorTime(this),
    repeatDays = this?.repeatDays.orEmpty(),
    useRoot = this?.useRoot ?: false,
)

private fun taskEntity(task: TaskEntity?, draft: TaskDraft, triggerAt: Long): TaskEntity = TaskEntity(
    id = task?.id ?: 0,
    name = draft.name.trim(),
    command = draft.command.trim(),
    triggerAt = triggerAt,
    repeatDays = draft.repeatDays.trim(),
    enabled = task?.enabled ?: true,
    useRoot = draft.useRoot,
)

private fun parseTriggerAt(time: String): Long? = try {
    LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
} catch (_: DateTimeParseException) {
    null
}

private fun validateTaskInput(draft: TaskDraft, triggerAt: Long?): String? = when {
    draft.name.isBlank() -> "请输入任务名称"
    draft.command.isBlank() -> "请输入执行命令"
    triggerAt == null -> "执行时间格式无效"
    draft.repeatDays.isNotBlank() && draft.repeatDays.split(',').any { it.toIntOrNull() !in 1..7 } -> "周期只能填写 1 到 7"
    else -> null
}
