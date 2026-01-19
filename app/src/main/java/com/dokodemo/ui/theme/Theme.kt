package com.dokodemo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================
// DOKODEMO - NEUBRUTALISM THEME
// ============================================

private val DokoDarkColorScheme = darkColorScheme(
    // Primary - Acid Lime for all interactive elements
    primary = AcidLime,
    onPrimary = IndustrialBlack,
    primaryContainer = AcidLime,
    onPrimaryContainer = IndustrialBlack,
    
    // Secondary - Same as primary for monochromatic look
    secondary = AcidLime,
    onSecondary = IndustrialBlack,
    secondaryContainer = IndustrialGrey,
    onSecondaryContainer = AcidLime,
    
    // Tertiary
    tertiary = AcidLime,
    onTertiary = IndustrialBlack,
    
    // Background - Pure Black
    background = IndustrialBlack,
    onBackground = IndustrialWhite,
    
    // Surface - Pure Black (NO elevation)
    surface = IndustrialBlack,
    onSurface = IndustrialWhite,
    surfaceVariant = IndustrialBlack,
    onSurfaceVariant = TextGrey,
    
    // Inverse
    inverseSurface = IndustrialWhite,
    inverseOnSurface = IndustrialBlack,
    inversePrimary = IndustrialBlack,
    
    // Error
    error = ErrorRed,
    onError = IndustrialWhite,
    errorContainer = ErrorRed,
    onErrorContainer = IndustrialWhite,
    
    // Outline - Grey borders
    outline = IndustrialGrey,
    outlineVariant = BorderGrey,
    
    // Scrim
    scrim = IndustrialBlack
)

private val DokoLightColorScheme = lightColorScheme(
    primary = IndustrialBlack,       // Black: Used for borders, text, icons
    onPrimary = IndustrialWhite,     // White: Text on black buttons (if any)
    primaryContainer = AcidLime,     // Acid Lime: Used for Button Fills / Highlights
    onPrimaryContainer = IndustrialBlack, // Black: Text on Lime buttons
    background = IndustrialWhite,    // White: App Background
    surface = IndustrialWhite,       // White: Card Background
    onSurface = IndustrialBlack,     // Black: Text on cards
    surfaceVariant = IndustrialWhite,
    onSurfaceVariant = IndustrialBlack,
    outline = IndustrialBlack,       // Black: All borders are black
    error = ErrorRed,                // Red: For strict errors
    
    // Secondary
    secondary = IndustrialBlack,
    onSecondary = IndustrialWhite,
    secondaryContainer = AcidLime,
    onSecondaryContainer = IndustrialBlack,

    // Tertiary
    tertiary = IndustrialBlack,
    onTertiary = IndustrialWhite
)

@Composable
fun DokoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DokoDarkColorScheme else DokoLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use the background color for the status bar
            window.statusBarColor = colorScheme.background.toArgb()
            
            // Controller for status bar icons
            WindowCompat.getInsetsController(window, view).apply {
                // If it's NOT dark theme (Light Mode), we want Dark Icons (true).
                // If it IS dark theme (Dark Mode), we want Light Icons (false).
                isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DokoTypography,
        content = content
    )
}
