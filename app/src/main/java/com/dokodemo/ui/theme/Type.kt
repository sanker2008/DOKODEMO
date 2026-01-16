package com.dokodemo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================
// DOKODEMO - TYPOGRAPHY SYSTEM
// ============================================

// Monospace font for data display (IPs, ports, latency, logs)
val MonospaceFont = FontFamily.Monospace

// Sans-serif for headings (system default, bold)
val IndustrialFont = FontFamily.Default

val DokoTypography = Typography(
    // Display - Large titles like "DOKODEMO"
    displayLarge = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = 8.sp,
        color = IndustrialWhite
    ),
    displayMedium = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 4.sp,
        color = IndustrialWhite
    ),
    displaySmall = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 2.sp,
        color = IndustrialWhite
    ),
    
    // Headlines - Section headers like "CONNECT", "SERVER LIST"
    headlineLarge = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 4.sp,
        color = IndustrialWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 2.sp,
        color = IndustrialWhite
    ),
    headlineSmall = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
        color = IndustrialWhite
    ),
    
    // Title - Screen titles, card headers
    titleLarge = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 2.sp,
        color = IndustrialWhite
    ),
    titleMedium = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 1.sp,
        color = IndustrialWhite
    ),
    titleSmall = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = IndustrialWhite
    ),
    
    // Body - Regular text content
    bodyLarge = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = IndustrialWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = IndustrialWhite
    ),
    bodySmall = TextStyle(
        fontFamily = IndustrialFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextGrey
    ),
    
    // Label - Monospace for data (CRITICAL for Neubrutalism)
    labelLarge = TextStyle(
        fontFamily = MonospaceFont,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 1.sp,
        color = AcidLime
    ),
    labelMedium = TextStyle(
        fontFamily = MonospaceFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = AcidLime
    ),
    labelSmall = TextStyle(
        fontFamily = MonospaceFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = TextGrey
    )
)

// ============================================
// CUSTOM TEXT STYLES FOR DATA DISPLAY
// ============================================

// Status text (like "STATUS: DISCONNECTED")
val StatusTextStyle = TextStyle(
    fontFamily = MonospaceFont,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 1.sp,
    color = AcidLime
)

// Ping/Latency display (large monospace numbers)
val PingTextStyle = TextStyle(
    fontFamily = MonospaceFont,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    letterSpacing = 0.sp,
    color = AcidLime
)

// Terminal log text
val TerminalTextStyle = TextStyle(
    fontFamily = MonospaceFont,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp,
    color = IndustrialWhite
)

// Server name text
val ServerNameStyle = TextStyle(
    fontFamily = IndustrialFont,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    letterSpacing = 1.sp,
    color = IndustrialWhite
)

// Country code text (for the [JP] boxes)
val CountryCodeStyle = TextStyle(
    fontFamily = MonospaceFont,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    letterSpacing = 0.sp,
    color = AcidLime
)
