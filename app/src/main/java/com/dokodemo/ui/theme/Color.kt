package com.dokodemo.ui.theme

import androidx.compose.ui.graphics.Color

// ─── 主色调（蓝紫系） ───────────────────────────────────────────────────────
val Primary = Color(0xFF6C63FF)
val PrimaryLight = Color(0xFF9D97FF)
val PrimaryDark = Color(0xFF4A42CC)

// ─── 强调色（状态/延迟显示） ────────────────────────────────────────────────
val Accent = Color(0xFF4FC3F7)          // 青蓝：延迟数字、已连接状态
val AccentGreen = Color(0xFF4CAF50)     // 绿：低延迟 / 已连接指示点
val AccentOrange = Color(0xFFFF9800)    // 橙：中等延迟
val AccentRed = Color(0xFFEF5350)       // 红：高延迟 / 错误

// ─── 深色主题背景层次 ─────────────────────────────────────────────────────
val Background = Color(0xFF12121F)      // 最底层深蓝灰
val Surface = Color(0xFF1C1C2E)         // 卡片/面板背景
val SurfaceVariant = Color(0xFF252540)  // 次级卡片、输入框背景
val Outline = Color(0xFF3A3A5C)         // 边框/分割线
val OutlineVariant = Color(0xFF2A2A45)  // 更浅的边框

// ─── 文字颜色 ──────────────────────────────────────────────────────────────
val OnBackground = Color(0xFFECECFF)    // 主文字：接近白的蓝白
val OnSurface = Color(0xFFDDDDFF)       // 卡片上的文字
val OnSurfaceVariant = Color(0xFF9090B0)// 次要文字、标签
val TextDisabled = Color(0xFF505070)    // 禁用状态文字

// ─── 浅色主题（备用，目前以深色为主） ─────────────────────────────────────
val BackgroundLight = Color(0xFFF5F5FF)
val SurfaceLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF1A1A2E)
val OnSurfaceLight = Color(0xFF2A2A40)
