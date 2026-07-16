package com.tasktrigger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

internal val TaskBackground = Color(0xFF0A0A0A)
internal val TaskSurface = Color(0xFF1E1E1E)
internal val TaskRaisedSurface = Color(0xFF2A2A2A)
internal val TaskCodeSurface = Color(0xFF0D0D0D)
internal val TaskText = Color.White
internal val TaskMutedText = Color(0xFF888888)
internal val TaskSubtleText = Color(0xFF555555)
internal val TaskDivider = Color(0xFF333333)
internal val TaskAccent = Color(0xFFF5692A)
internal val TaskButtonText = Color.White
internal val TaskStatusGreen = Color(0xFF4CAF50)
internal val TaskSwitchOff = Color(0xFF3A3A3A)
internal val TaskError = Color(0xFFE57373)

@Composable
internal fun TaskTriggerTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = TaskAccent,
        onPrimary = TaskButtonText,
        background = TaskBackground,
        onBackground = TaskText,
        surface = TaskSurface,
        onSurface = TaskText,
        error = TaskError,
    )
    val typography = Typography(
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal),
        labelMedium = TextStyle(fontWeight = FontWeight.SemiBold),
        bodySmall = TextStyle(fontFamily = FontFamily.Monospace),
    )
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
