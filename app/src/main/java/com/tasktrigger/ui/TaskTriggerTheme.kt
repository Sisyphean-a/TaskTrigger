package com.tasktrigger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

internal val TaskBackground = Color(0xFF0C1211)
internal val TaskSurface = Color(0xFF171E1C)
internal val TaskText = Color(0xFFE8ECE7)
internal val TaskMutedText = Color(0xFF9CA6A1)
internal val TaskDivider = Color(0xFF34403C)
internal val TaskAccent = Color(0xFFFF6B35)
internal val TaskStatusGreen = Color(0xFF31C56A)

@Composable
internal fun TaskTriggerTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = TaskAccent,
        onPrimary = Color(0xFF17110D),
        background = TaskBackground,
        onBackground = TaskText,
        surface = TaskSurface,
        onSurface = TaskText,
        error = TaskAccent,
    )
    val typography = Typography(
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontWeight = FontWeight.Medium),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium),
        bodySmall = TextStyle(fontFamily = FontFamily.Monospace),
    )
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
