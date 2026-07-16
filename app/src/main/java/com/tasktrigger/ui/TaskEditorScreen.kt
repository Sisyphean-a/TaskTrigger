package com.tasktrigger.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.tasktrigger.data.TaskEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private data class TaskDraft(
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
internal fun TaskEditorScreen(
    task: TaskEntity?,
    callbacks: EditorCallbacks,
) {
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
    val contentCallbacks = callbacks.copy(onDelete = { confirmDelete = true })
    EditorContent(state, contentCallbacks) { draft = it }
    if (confirmDelete && savedTask != null) DeleteConfirmation({ confirmDelete = false }) { callbacks.onDelete(savedTask) }
}

@Composable
private fun EditorContent(
    state: EditorContentState,
    callbacks: EditorCallbacks,
    onDraftChange: (TaskDraft) -> Unit,
) {
    Column(
        modifier = Modifier.statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
    ) {
        EditorTopBar(state.task == null, callbacks.onBack)
        EditorFields(state.draft, onDraftChange)
        EditorActions(state.savedTask, callbacks)
        state.error?.let { Text(it, color = TaskAccent, modifier = Modifier.padding(top = 16.dp)) }
        Spacer(Modifier.height(28.dp))
        EditorSaveButton(state)
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun EditorFields(draft: TaskDraft, onDraftChange: (TaskDraft) -> Unit) {
    EditorTextRow("任务名称", draft.name) { onDraftChange(draft.copy(name = it)) }
    EditorTextRow("执行时间", draft.time, "yyyy-MM-dd HH:mm") { onDraftChange(draft.copy(time = it)) }
    EditorTextRow("执行周期", draft.repeatDays, "留空为单次；1-7 用逗号分隔") { onDraftChange(draft.copy(repeatDays = it)) }
    CommandEditor(draft.command) { onDraftChange(draft.copy(command = it)) }
    RootModeRow(draft.useRoot) { onDraftChange(draft.copy(useRoot = it)) }
}

@Composable
private fun EditorActions(
    task: TaskEntity?,
    callbacks: EditorCallbacks,
) {
    ExecuteButton(task, callbacks.onExecute)
    task?.let { ExistingTaskActions({ callbacks.onLogs(it) }, { callbacks.onDelete(it) }) }
}

@Composable
private fun EditorSaveButton(state: EditorContentState) {
    SaveButton(state.task == null) { state.onSaveDraft(state.draft) }
}

@Composable
private fun EditorTopBar(isNew: Boolean, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) { Text("‹", color = TaskText, style = MaterialTheme.typography.headlineLarge) }
        Text(if (isNew) "新建任务" else "编辑任务", style = MaterialTheme.typography.headlineSmall)
        Text("保存", color = TaskAccent, style = MaterialTheme.typography.titleLarge)
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun EditorTextRow(label: String, value: String, hint: String = "", onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 19.dp)) {
        Text(label, color = TaskMutedText, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TaskText, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
            cursorBrush = SolidColor(TaskAccent),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { input ->
                if (value.isBlank() && hint.isNotBlank()) Text(hint, color = TaskMutedText)
                input()
            },
        )
    }
    HorizontalDivider(color = TaskDivider)
}

@Composable
private fun CommandEditor(command: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 16.dp)) {
        Text("执行命令", color = TaskMutedText, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(12.dp))
        BasicTextField(
            value = command,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TaskText, fontFamily = FontFamily.Monospace, fontSize = MaterialTheme.typography.bodyMedium.fontSize),
            cursorBrush = SolidColor(TaskAccent),
            modifier = Modifier.fillMaxWidth().height(188.dp).padding(14.dp),
            decorationBox = { input ->
                if (command.isBlank()) Text("请输入 Shell 命令", color = TaskMutedText, fontFamily = FontFamily.Monospace)
                input()
            },
        )
        HorizontalDivider(color = TaskDivider)
        Text("Shell command", color = TaskMutedText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun RootModeRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Root 模式", style = MaterialTheme.typography.titleMedium)
            Text("使用 su -c 执行", color = TaskMutedText, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = TaskAccent, checkedTrackColor = TaskAccent.copy(alpha = 0.45f)),
        )
    }
}

@Composable
private fun ExecuteButton(task: TaskEntity?, onExecute: (TaskEntity) -> Unit) {
    OutlinedButton(
        onClick = { task?.let(onExecute) },
        enabled = task != null,
        border = BorderStroke(1.dp, if (task == null) TaskDivider else TaskAccent),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (task == null) TaskMutedText else TaskText),
    ) {
        Text(">_  立即执行", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 6.dp))
    }
    if (task == null) Text("保存后可执行", color = TaskMutedText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 7.dp))
}

@Composable
private fun ExistingTaskActions(onLogs: () -> Unit, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onLogs, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, TaskDivider)) { Text("▤  查看日志") }
        OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f), border = BorderStroke(1.dp, TaskDivider)) { Text("⌫  删除任务") }
    }
}

@Composable
private fun SaveButton(isNew: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = TaskAccent, contentColor = TaskBackground),
    ) {
        Text(if (isNew) "保存并启用" else "保存修改", modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun DeleteConfirmation(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除任务？") },
        text = { Text("该任务会被删除，已有执行日志会保留。") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("删除", color = TaskAccent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

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
