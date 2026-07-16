package com.tasktrigger.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle

internal val TaskBackground = Color(0xFF07100E)
internal val TaskSurface = Color(0xFF0E1A17)
internal val TaskText = Color(0xFFF0F2EF)
internal val TaskMutedText = Color(0xFFA7ADAA)
internal val TaskDivider = Color(0xFF31403B)
internal val TaskAccent = Color(0xFFFF5A1F)
internal val TaskAccentDeep = Color(0xFFFF4B19)
internal val TaskButtonText = Color(0xFFFFFFFF)
internal val TaskStatusGreen = Color(0xFF32C86B)
internal val TaskSwitchOff = Color(0xFF3D4643)
internal val TaskError = Color(0xFFFF4A42)
internal val TaskGlow = Color(0xFF0B2019)

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
        headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold),
        headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal),
        labelMedium = TextStyle(fontWeight = FontWeight.Normal),
        bodySmall = TextStyle(fontFamily = FontFamily.Monospace),
    )
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
