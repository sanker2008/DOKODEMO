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

private val LightColorScheme = lightColorScheme(
    primary = PrimaryAction,
    onPrimary = TextPrimaryBtn,
    primaryContainer = PrimaryAction,
    onPrimaryContainer = TextPrimaryBtn,
    secondary = AccentState,
    onSecondary = BaseBackground,
    secondaryContainer = SurfaceGlass,
    onSecondaryContainer = TextIconography,
    background = BaseBackground,
    onBackground = TextIconography,
    surface = SurfaceGlass,
    surfaceDim = BaseBackground,
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceGlass,
    surfaceContainer = SurfaceGlass,
    surfaceContainerHigh = SurfaceGlass,
    surfaceContainerHighest = SurfaceGlass,
    onSurface = TextIconography,
    surfaceVariant = SurfaceGlass,
    onSurfaceVariant = TextBody,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorder,
    error = IcyLemon,
    onError = BaseBackground,
    tertiary = AccentState,
    onTertiary = BaseBackground
)

// Defaulting to the same scheme for dark mode as per the new specific aesthetic goal
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryAction,
    onPrimary = DarkTextPrimaryBtn,
    primaryContainer = DarkPrimaryAction,
    onPrimaryContainer = DarkTextPrimaryBtn,
    secondary = DarkAccentState,
    onSecondary = DarkBaseBackground,
    secondaryContainer = DarkSurfaceGlass,
    onSecondaryContainer = DarkTextIconography,
    background = DarkBaseBackground,
    onBackground = DarkTextIconography,
    surface = DarkSurfaceGlass,
    surfaceDim = DarkBaseBackground,
    surfaceBright = DarkSurfaceGlass,
    surfaceContainerLowest = DarkSurfaceGlass,
    surfaceContainerLow = DarkSurfaceGlass,
    surfaceContainer = DarkSurfaceGlass,
    surfaceContainerHigh = DarkSurfaceGlass,
    surfaceContainerHighest = DarkSurfaceGlass,
    onSurface = DarkTextIconography,
    surfaceVariant = DarkSurfaceGlass,
    onSurfaceVariant = DarkTextBody,
    outline = DarkSurfaceBorder,
    outlineVariant = DarkSurfaceBorder,
    error = IcyLemon,
    onError = DarkBaseBackground,
    tertiary = DarkAccentState,
    onTertiary = DarkBaseBackground
)

@Composable
fun DokoDemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
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

    val scaledTypography = androidx.compose.material3.Typography(
        headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * fontScale),
        headlineMedium = AppTypography.headlineMedium.copy(fontSize = AppTypography.headlineMedium.fontSize * fontScale),
        headlineSmall = AppTypography.headlineSmall.copy(fontSize = AppTypography.headlineSmall.fontSize * fontScale),
        titleLarge = AppTypography.titleLarge.copy(fontSize = AppTypography.titleLarge.fontSize * fontScale),
        titleMedium = AppTypography.titleMedium.copy(fontSize = AppTypography.titleMedium.fontSize * fontScale),
        titleSmall = AppTypography.titleSmall.copy(fontSize = AppTypography.titleSmall.fontSize * fontScale),
        bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * fontScale),
        bodyMedium = AppTypography.bodyMedium.copy(fontSize = AppTypography.bodyMedium.fontSize * fontScale),
        bodySmall = AppTypography.bodySmall.copy(fontSize = AppTypography.bodySmall.fontSize * fontScale),
        labelLarge = AppTypography.labelLarge.copy(fontSize = AppTypography.labelLarge.fontSize * fontScale),
        labelMedium = AppTypography.labelMedium.copy(fontSize = AppTypography.labelMedium.fontSize * fontScale),
        labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * fontScale)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
