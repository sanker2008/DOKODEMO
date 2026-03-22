package com.dokodemo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnBackground,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = OnBackground,
    secondary = Accent,
    onSecondary = Background,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = AccentRed,
    onError = OnBackground,
    tertiary = AccentGreen,
    onTertiary = Background
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnBackground,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = OnBackgroundLight,
    secondary = Accent,
    onSecondary = BackgroundLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFF0F0FF),
    onSurfaceVariant = Color(0xFF606080),
    outline = Color(0xFFCCCCDD),
    error = AccentRed,
    onError = OnBackground
)

@Composable
fun SanProxyTheme(
    darkTheme: Boolean = true,  // 默认深色
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
