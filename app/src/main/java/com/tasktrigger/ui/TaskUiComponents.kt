package com.tasktrigger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val TaskCorner = RoundedCornerShape(8.dp)

@Composable
internal fun TaskScreenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawTaskBackground() },
    ) { content() }
}

private fun DrawScope.drawTaskBackground() {
    drawRect(TaskBackground)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(TaskGlow, Color.Transparent),
            center = center.copy(x = size.width * 0.72f, y = size.height * 0.18f),
            radius = size.maxDimension * 0.72f,
        ),
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(TaskGlow.copy(alpha = 0.58f), Color.Transparent),
            center = center.copy(x = size.width * 0.12f, y = size.height * 0.82f),
            radius = size.maxDimension * 0.68f,
        ),
    )
}

@Composable
internal fun TaskIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    tint: Color = TaskText,
    modifier: Modifier = Modifier,
) {
    Icon(imageVector, contentDescription, modifier = modifier.size(24.dp), tint = tint)
}

@Composable
internal fun TerminalPrompt(
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Text(
        text = ">_",
        color = if (enabled) TaskAccent else TaskMutedText,
        style = TextStyle(fontSize = 24.sp, letterSpacing = (-2).sp),
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
            .background(if (checked) TaskAccent.copy(alpha = 0.78f) else TaskSwitchOff)
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
                .padding(horizontal = 2.dp)
                .size(22.dp)
                .background(if (checked) Color(0xFFFFF7F2) else Color(0xFFB6BDB9), CircleShape),
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
        modifier = modifier
            .height(48.dp)
            .clip(TaskCorner)
            .background(Brush.horizontalGradient(listOf(TaskAccent, TaskAccentDeep))),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TaskButtonText,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        if (icon != null) {
            Icon(icon, null, modifier = Modifier.padding(end = 12.dp), tint = TaskButtonText)
        }
        Text(text, fontSize = 18.sp, maxLines = 1)
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
        modifier = modifier.height(56.dp),
        shape = TaskCorner,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) TaskText else TaskMutedText,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = TaskMutedText,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) TaskAccent else TaskDivider,
        ),
    ) {
        if (icon != null) Icon(icon, null, modifier = Modifier.padding(end = 12.dp))
        Text(text, fontSize = 19.sp)
    }
}
