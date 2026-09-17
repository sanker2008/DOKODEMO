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
    primary = Color(0xFF355F7B),
    onPrimary = Color.White,
    primaryContainer = PrimaryAction,
    onPrimaryContainer = TextPrimaryBtn,
    secondary = AccentState,
    onSecondary = Color(0xFF294C3D),
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
    error = Color(0xFFB33B35),
    onError = BaseBackground,
    tertiary = AccentState,
    onTertiary = Color(0xFF294C3D)
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
    error = Color(0xFFFFB4AB),
    onError = DarkBaseBackground,
    tertiary = DarkAccentState,
    onTertiary = DarkBaseBackground
)

@Composable
fun DokoDemoTheme(
    darkTheme: Boolean = true, // Force Dark Mode as requested
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
        headlineLarge = AppTypography.headlineLarge.copy(fontSize = AppTypography.headlineLarge.fontSize * fontScale, lineHeight = AppTypography.headlineLarge.lineHeight * fontScale),
        headlineMedium = AppTypography.headlineMedium.copy(fontSize = AppTypography.headlineMedium.fontSize * fontScale, lineHeight = AppTypography.headlineMedium.lineHeight * fontScale),
        headlineSmall = AppTypography.headlineSmall.copy(fontSize = AppTypography.headlineSmall.fontSize * fontScale, lineHeight = AppTypography.headlineSmall.lineHeight * fontScale),
        titleLarge = AppTypography.titleLarge.copy(fontSize = AppTypography.titleLarge.fontSize * fontScale, lineHeight = AppTypography.titleLarge.lineHeight * fontScale),
        titleMedium = AppTypography.titleMedium.copy(fontSize = AppTypography.titleMedium.fontSize * fontScale, lineHeight = AppTypography.titleMedium.lineHeight * fontScale),
        titleSmall = AppTypography.titleSmall.copy(fontSize = AppTypography.titleSmall.fontSize * fontScale, lineHeight = AppTypography.titleSmall.lineHeight * fontScale),
        bodyLarge = AppTypography.bodyLarge.copy(fontSize = AppTypography.bodyLarge.fontSize * fontScale, lineHeight = AppTypography.bodyLarge.lineHeight * fontScale),
        bodyMedium = AppTypography.bodyMedium.copy(fontSize = AppTypography.bodyMedium.fontSize * fontScale, lineHeight = AppTypography.bodyMedium.lineHeight * fontScale),
        bodySmall = AppTypography.bodySmall.copy(fontSize = AppTypography.bodySmall.fontSize * fontScale, lineHeight = AppTypography.bodySmall.lineHeight * fontScale),
        labelLarge = AppTypography.labelLarge.copy(fontSize = AppTypography.labelLarge.fontSize * fontScale, lineHeight = AppTypography.labelLarge.lineHeight * fontScale),
        labelMedium = AppTypography.labelMedium.copy(fontSize = AppTypography.labelMedium.fontSize * fontScale, lineHeight = AppTypography.labelMedium.lineHeight * fontScale),
        labelSmall = AppTypography.labelSmall.copy(fontSize = AppTypography.labelSmall.fontSize * fontScale, lineHeight = AppTypography.labelSmall.lineHeight * fontScale)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
