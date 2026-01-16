package com.dokodemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

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

@Composable
fun DokoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DokoDarkColorScheme,
        typography = DokoTypography,
        content = content
    )
}
