package com.dokodemo.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokodemo.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Animation states mapping exactly to the 2.0s timeline
    val dotScale = remember { Animatable(0f) }
    val bloomProgress = remember { Animatable(0f) }
    val blinkAlpha = remember { Animatable(0f) }

    // Colors
    val bgColor = Color(0xFFF0F4F7) // Pure Ice White
    val dotColor = Color(0xFF607D8B) // Blue-Grey
    val lineColor = Color(0xFFA0C4E3) // Sky Blue
    val blinkColor = Color(0xFFB7D5C7) // Soft Mint Green

    LaunchedEffect(Unit) {
        // 0.0s - 0.5s: The Dawn (Central dot appears and expands)
        dotScale.animateTo(1f, animationSpec = tween(500, easing = EaseOutCubic))
        
        // 0.5s - 1.2s: The Bloom (Network lines flow organically)
        bloomProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        
        // 1.2s - 1.5s: Stabilization
        delay(300)
        
        // 1.5s - 2.0s: The Signal (Nodes blink with halo)
        launch {
            blinkAlpha.animateTo(1f, animationSpec = tween(250, easing = EaseOut))
            blinkAlpha.animateTo(0f, animationSpec = tween(250, easing = EaseIn))
        }
        delay(500)
        
        // Buffer before transitioning
        delay(100)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Network Bloom Animation Canvas
            Canvas(modifier = Modifier.size(160.dp)) {
                val R = size.minDimension / 2 * 0.8f
                val center = Offset(size.width / 2, size.height / 2)
                
                // Define the abstract D-shaped network nodes
                val n0 = center // Central Dot
                val n1 = center + Offset(-0.4f * R, -0.8f * R) // Top Left
                val n2 = center + Offset(-0.4f * R, 0.8f * R)  // Bottom Left
                val n3 = center + Offset(0.3f * R, -0.8f * R)  // Top Mid
                val n4 = center + Offset(0.3f * R, 0.8f * R)   // Bottom Mid
                val n5 = center + Offset(0.8f * R, -0.3f * R)  // Right Curve 1
                val n6 = center + Offset(0.8f * R, 0.3f * R)   // Right Curve 2

                // 2. Draw Network Lines (The Bloom)
                if (bloomProgress.value > 0f) {
                    val strokeWidth = 4.dp.toPx()
                    
                    fun drawProgressLine(start: Offset, end: Offset, progress: Float) {
                        if (progress <= 0f) return
                        val currentEnd = Offset(
                            start.x + (end.x - start.x) * progress,
                            start.y + (end.y - start.y) * progress
                        )
                        drawLine(
                            color = lineColor,
                            start = start,
                            end = currentEnd,
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // Phase 1: Center to outer nodes
                    val p1 = (bloomProgress.value / 0.6f).coerceIn(0f, 1f)
                    drawProgressLine(n0, n1, p1)
                    drawProgressLine(n0, n2, p1)
                    drawProgressLine(n0, n3, p1)
                    drawProgressLine(n0, n4, p1)
                    drawProgressLine(n0, n5, p1)
                    drawProgressLine(n0, n6, p1)

                    // Phase 2: Connect outer nodes to form 'D'
                    val p2 = ((bloomProgress.value - 0.4f) / 0.6f).coerceIn(0f, 1f)
                    drawProgressLine(n1, n2, p2) // Left spine
                    drawProgressLine(n1, n3, p2) // Top
                    drawProgressLine(n2, n4, p2) // Bottom
                    drawProgressLine(n3, n5, p2) // Top curve
                    drawProgressLine(n5, n6, p2) // Right curve
                    drawProgressLine(n6, n4, p2) // Bottom curve
                }

                // 1. Draw Central Dot (The Dawn)
                if (dotScale.value > 0f) {
                    drawCircle(
                        color = dotColor,
                        radius = 8.dp.toPx() * dotScale.value,
                        center = n0
                    )
                }

                // Draw Outer Nodes fading in
                val nodeRadius = 5.dp.toPx()
                if (bloomProgress.value > 0.5f) {
                    val nodeAlpha = ((bloomProgress.value - 0.5f) * 2).coerceIn(0f, 1f)
                    val nColor = lineColor.copy(alpha = nodeAlpha)
                    listOf(n1, n2, n3, n4, n5, n6).forEach { node ->
                        drawCircle(color = nColor, radius = nodeRadius, center = node)
                    }
                }

                // 3. Draw The Signal (Mint Green Blink)
                if (blinkAlpha.value > 0f) {
                    val haloRadius = 18.dp.toPx()
                    // Blink on key strategic nodes
                    listOf(n0, n3, n5, n2).forEach { node ->
                        drawCircle(
                            color = blinkColor.copy(alpha = blinkAlpha.value * 0.4f),
                            radius = haloRadius,
                            center = node
                        )
                        drawCircle(
                            color = blinkColor.copy(alpha = blinkAlpha.value),
                            radius = nodeRadius * 1.5f,
                            center = node
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(56.dp))
            
            // App Title Text
            Text(
                text = "DokoDemo",
                color = dotColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline Text
            Text(
                text = stringResource(id = R.string.splash_tagline),
                color = dotColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp
            )
        }
    }
}
