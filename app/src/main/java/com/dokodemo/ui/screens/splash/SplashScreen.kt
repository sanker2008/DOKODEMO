package com.dokodemo.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit
) {
    // Animation states
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    var showCursor by remember { mutableStateOf(true) }
    var progressWidth by remember { mutableStateOf(0f) }
    
    // Cursor blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )
    
    // Animation sequence
    LaunchedEffect(Unit) {
        // Fade in logo
        logoAlpha.animateTo(1f, animationSpec = tween(800))
        delay(200)
        
        // Fade in text
        textAlpha.animateTo(1f, animationSpec = tween(500))
        delay(300)
        
        // Progress bar animation
        val steps = 20
        for (i in 1..steps) {
            progressWidth = i.toFloat() / steps
            delay(50)
        }
        
        delay(500)
        
        // Navigate to home
        onNavigateToHome()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBlack)
    ) {
        // Grid background
        GridBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top version text
            Text(
                text = "V.1.0.4-ALPHA // DOKO_CLIENT",
                color = IndustrialGrey,
                fontFamily = MonospaceFont,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(logoAlpha.value)
            ) {
                // Logo - Nested squares with checkmark
                NestedSquaresLogo(
                    modifier = Modifier.size(160.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // App name
                Text(
                    text = "DOKODEMO",
                    color = IndustrialWhite,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    letterSpacing = 8.sp,
                    modifier = Modifier.alpha(textAlpha.value)
                )
            }
            
            // Bottom content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                // Status text with blinking cursor
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle indicator
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(
                            color = AcidLime,
                            radius = size.minDimension / 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "SYSTEM INITIALIZING...",
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    
                    // Blinking cursor
                    Text(
                        text = "_",
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.alpha(cursorAlpha)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress bar with caution stripes
                CautionProgressBar(
                    progress = progressWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(horizontal = 48.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Footer text
                Text(
                    text = "SECURE CONNECTION PROTOCOL",
                    color = IndustrialGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun GridBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 40.dp.toPx()
        val strokeWidth = 0.5.dp.toPx()
        
        // Draw vertical lines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = IndustrialGrey.copy(alpha = 0.3f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = strokeWidth
            )
            x += gridSize
        }
        
        // Draw horizontal lines
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = IndustrialGrey.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
            y += gridSize
        }
    }
}

@Composable
private fun NestedSquaresLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val padding = 20.dp.toPx()
        
        // Outer square (with corner brackets)
        val outerSize = size.minDimension
        val outerOffset = Offset(
            (size.width - outerSize) / 2,
            (size.height - outerSize) / 2
        )
        
        // Draw corner brackets for outer square
        val bracketLength = outerSize * 0.15f
        val halfStroke = strokeWidth / 2
        
        // Top-left corner
        drawLine(AcidLime, Offset(outerOffset.x, outerOffset.y + bracketLength), Offset(outerOffset.x, outerOffset.y), strokeWidth)
        drawLine(AcidLime, Offset(outerOffset.x, outerOffset.y), Offset(outerOffset.x + bracketLength, outerOffset.y), strokeWidth)
        
        // Top-right corner
        drawLine(AcidLime, Offset(outerOffset.x + outerSize - bracketLength, outerOffset.y), Offset(outerOffset.x + outerSize, outerOffset.y), strokeWidth)
        drawLine(AcidLime, Offset(outerOffset.x + outerSize, outerOffset.y), Offset(outerOffset.x + outerSize, outerOffset.y + bracketLength), strokeWidth)
        
        // Bottom-left corner
        drawLine(AcidLime, Offset(outerOffset.x, outerOffset.y + outerSize - bracketLength), Offset(outerOffset.x, outerOffset.y + outerSize), strokeWidth)
        drawLine(AcidLime, Offset(outerOffset.x, outerOffset.y + outerSize), Offset(outerOffset.x + bracketLength, outerOffset.y + outerSize), strokeWidth)
        
        // Bottom-right corner
        drawLine(AcidLime, Offset(outerOffset.x + outerSize - bracketLength, outerOffset.y + outerSize), Offset(outerOffset.x + outerSize, outerOffset.y + outerSize), strokeWidth)
        drawLine(AcidLime, Offset(outerOffset.x + outerSize, outerOffset.y + outerSize - bracketLength), Offset(outerOffset.x + outerSize, outerOffset.y + outerSize), strokeWidth)
        
        // Middle square (full outline)
        val middleSize = outerSize - padding * 2
        val middleOffset = Offset(
            outerOffset.x + padding,
            outerOffset.y + padding
        )
        drawRect(
            color = AcidLime,
            topLeft = middleOffset,
            size = Size(middleSize, middleSize),
            style = Stroke(width = strokeWidth)
        )
        
        // Inner square (filled)
        val innerSize = middleSize - padding * 2
        val innerOffset = Offset(
            middleOffset.x + padding,
            middleOffset.y + padding
        )
        drawRect(
            color = AcidLime,
            topLeft = innerOffset,
            size = Size(innerSize, innerSize)
        )
        
        // Checkmark in center
        val checkStart = Offset(
            innerOffset.x + innerSize * 0.25f,
            innerOffset.y + innerSize * 0.5f
        )
        val checkMiddle = Offset(
            innerOffset.x + innerSize * 0.45f,
            innerOffset.y + innerSize * 0.7f
        )
        val checkEnd = Offset(
            innerOffset.x + innerSize * 0.75f,
            innerOffset.y + innerSize * 0.3f
        )
        
        drawLine(IndustrialBlack, checkStart, checkMiddle, strokeWidth * 1.5f)
        drawLine(IndustrialBlack, checkMiddle, checkEnd, strokeWidth * 1.5f)
    }
}

@Composable
private fun CautionProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stripeWidth = 8.dp.toPx()
        val progressWidth = size.width * progress
        
        // Draw yellow/black caution stripes
        var x = 0f
        var isYellow = true
        while (x < progressWidth) {
            val width = minOf(stripeWidth, progressWidth - x)
            drawRect(
                color = if (isYellow) AcidLime else IndustrialBlack,
                topLeft = Offset(x, 0f),
                size = Size(width, size.height)
            )
            x += stripeWidth
            isYellow = !isYellow
        }
        
        // Draw remaining empty part
        if (progressWidth < size.width) {
            drawRect(
                color = IndustrialGrey.copy(alpha = 0.3f),
                topLeft = Offset(progressWidth, 0f),
                size = Size(size.width - progressWidth, size.height)
            )
        }
    }
}
