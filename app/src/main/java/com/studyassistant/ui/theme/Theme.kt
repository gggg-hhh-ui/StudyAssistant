package com.studyassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Green500,
    onPrimary = White,
    primaryContainer = GreenDark,
    onPrimaryContainer = White,
    secondary = Green600,
    onSecondary = White,
    tertiary = Green700,
    onTertiary = White,
    background = DarkBackground,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Gray300,
    error = Red400,
    onError = White,
    outline = Gray700
)

@Composable
fun StudyAssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
