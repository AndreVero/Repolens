package com.vero.repolens.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkInkPrimary,
    onPrimary = InkText,
    primaryContainer = DarkInkPrimaryContainer,
    onPrimaryContainer = DarkInkText,
    secondary = DarkInkSecondary,
    onSecondary = InkText,
    secondaryContainer = DarkInkSecondaryContainer,
    onSecondaryContainer = DarkInkText,
    tertiary = DarkInkTertiary,
    onTertiary = InkText,
    tertiaryContainer = DarkInkTertiaryContainer,
    onTertiaryContainer = DarkInkText,
    background = DarkSandBackground,
    onBackground = DarkInkText,
    surface = DarkSandSurface,
    onSurface = DarkInkText,
    surfaceVariant = DarkSandSurfaceVariant,
    onSurfaceVariant = DarkInkTextMuted,
    outline = DarkSandOutline
)

private val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = InkOnPrimary,
    primaryContainer = InkPrimaryContainer,
    onPrimaryContainer = InkText,
    secondary = InkSecondary,
    onSecondary = InkOnPrimary,
    secondaryContainer = InkSecondaryContainer,
    onSecondaryContainer = InkText,
    tertiary = InkTertiary,
    onTertiary = InkOnPrimary,
    tertiaryContainer = InkTertiaryContainer,
    onTertiaryContainer = InkText,
    background = SandBackground,
    onBackground = InkText,
    surface = SandSurface,
    onSurface = InkText,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = InkTextMuted,
    outline = SandOutline
)

@Composable
fun RepolensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
