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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.core.view.WindowCompat

internal fun TextStyle.scaleSpecifiedTextUnits(scale: Float): TextStyle = copy(
    fontSize = fontSize.scaleIfSpecified(scale),
    lineHeight = lineHeight.scaleIfSpecified(scale)
)

private fun TextUnit.scaleIfSpecified(scale: Float): TextUnit =
    if (this == TextUnit.Unspecified) this else this * scale

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF355F7B),
    onPrimary = Color.White,
    primaryContainer = PrimaryAction,
    onPrimaryContainer = TextPrimaryBtn,
    secondary = AccentState,
    onSecondary = Color(0xFF294C3D),
    secondaryContainer = Color(0xFFD9E8F5),
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
    secondaryContainer = Color(0xFF293E50),
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
        headlineLarge = AppTypography.headlineLarge.scaleSpecifiedTextUnits(fontScale),
        headlineMedium = AppTypography.headlineMedium.scaleSpecifiedTextUnits(fontScale),
        headlineSmall = AppTypography.headlineSmall.scaleSpecifiedTextUnits(fontScale),
        titleLarge = AppTypography.titleLarge.scaleSpecifiedTextUnits(fontScale),
        titleMedium = AppTypography.titleMedium.scaleSpecifiedTextUnits(fontScale),
        titleSmall = AppTypography.titleSmall.scaleSpecifiedTextUnits(fontScale),
        bodyLarge = AppTypography.bodyLarge.scaleSpecifiedTextUnits(fontScale),
        bodyMedium = AppTypography.bodyMedium.scaleSpecifiedTextUnits(fontScale),
        bodySmall = AppTypography.bodySmall.scaleSpecifiedTextUnits(fontScale),
        labelLarge = AppTypography.labelLarge.scaleSpecifiedTextUnits(fontScale),
        labelMedium = AppTypography.labelMedium.scaleSpecifiedTextUnits(fontScale),
        labelSmall = AppTypography.labelSmall.scaleSpecifiedTextUnits(fontScale)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
