package com.dokodemo.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Core Color Palette ───────────────────────────────────────────────────────
val BaseBackground = Color(0xFFF0F4F7)       // Ultra-light Ice White/Grey
val PrimaryAction = Color(0xFFA0C4E3)        // Clear Sky Blue
val TextIconography = Color(0xFF607D8B)      // Cool Blue-Grey
val AccentState = Color(0xFFB7D5C7)          // Soft Mint Green

// ─── Semantic Colors ────────────────────────────────────────────────────────
val SurfaceGlass = Color(0xB3FFFFFF)         // 70% opacity White for Cards
val SurfaceBorder = Color(0x1A607D8B)        // 10% opacity #607D8B for Card borders
val TextPrimaryBtn = Color(0xFF546E7A)       // Slightly darker shade for text on Primary Action
val TextBody = Color(0xB3607D8B)             // 70% opacity #607D8B for Body Text & Subtitles

// ─── Warning / Error color (replacing red for Mist&Dawn) ───────────────
val IcyLemon = Color(0xFFE3E1C0)             // Warning / High latency

// ─── Dark Mode Core Color Palette ─────────────────────────────────────────────
val DarkBaseBackground = Color(0xFF121417)
val DarkPrimaryAction = Color(0xFF7CAEE0)
val DarkTextIconography = Color(0xFFE2E8F0)
val DarkAccentState = Color(0xFFB7D5C7)          // Mint Green glow

// ─── Dark Mode Semantic Colors ──────────────────────────────────────────────
val DarkSurfaceGlass = Color(0x991E2429)         // 60% opacity Dark Glass
val DarkSurfaceBorder = Color(0x33E2E8F0)        // 20% opacity white border
val DarkTextPrimaryBtn = Color(0xFF0F172A)       // Dark text on Light Primary Action
val DarkTextBody = Color(0xB3E2E8F0)             // 70% opacity for Body Text & Subtitles

// Map these back to MaterialTheme names if needed
val Background = BaseBackground
val Surface = SurfaceGlass
val Primary = PrimaryAction
val OnBackground = TextIconography
val OnSurface = TextIconography
val Outline = SurfaceBorder

