package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val TaskCorner = RoundedCornerShape(10.dp)

@Composable
internal fun TaskScreenBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(TaskBackground)) { content() }
}

@Composable
internal fun TaskIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color = TaskText,
    modifier: Modifier = Modifier.size(24.dp),
) {
    Icon(imageVector, contentDescription, modifier = modifier, tint = tint)
}

@Composable
internal fun TerminalPrompt(enabled: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = "›",
        color = if (enabled) TaskAccent else TaskMutedText,
        style = TextStyle(fontSize = 18.sp),
        modifier = modifier,
    )
}

@Composable
internal fun TaskSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .width(46.dp)
            .height(26.dp)
            .clip(CircleShape)
            .background(if (checked) TaskAccent else TaskSwitchOff)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { onCheckedChange(!checked) },
            )
            .semantics { role = Role.Switch },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .background(Color.White, CircleShape),
        )
    }
}

@Composable
internal fun TaskPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskAccent,
            contentColor = TaskButtonText,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        icon?.let {
            Icon(it, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 14.sp, maxLines = 1)
    }
}

@Composable
internal fun TaskSaveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskAccent,
            contentColor = TaskButtonText,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun TaskExecuteButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(47.dp),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskRaisedSurface,
            contentColor = TaskAccent,
            disabledContainerColor = TaskRaisedSurface,
            disabledContentColor = TaskAccent,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TaskAccent),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Text("立即执行", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun TaskDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(47.dp),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskRaisedSurface,
            contentColor = TaskError,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TaskDivider),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        icon?.let {
            Icon(it, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 14.sp)
    }
}

@Composable
internal fun TaskOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(47.dp),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TaskMutedText,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TaskMutedText,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TaskDivider),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        icon?.let {
            Icon(it, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 14.sp)
    }
}

@Composable
internal fun TaskSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(43.dp),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskRaisedSurface,
            contentColor = TaskMutedText,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, TaskDivider),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        icon?.let {
            Icon(it, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontSize = 14.sp)
    }
}

@Composable
internal fun TaskFooterActions(onLogs: () -> Unit, onCreate: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .height(68.dp)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskSecondaryButton(
            text = "日志",
            onClick = onLogs,
            modifier = Modifier.width(86.dp),
            icon = Icons.AutoMirrored.Outlined.Article,
        )
        Spacer(Modifier.weight(1f))
        TaskPrimaryButton(
            text = "新建任务",
            onClick = onCreate,
            modifier = Modifier.width(120.dp).height(41.dp),
            icon = Icons.Outlined.Add,
        )
    }
}
