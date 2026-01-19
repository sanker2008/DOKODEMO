package com.dokodemo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont

/**
 * Industrial Button - Neubrutalism style button
 * 
 * Features:
 * - Sharp corners (0dp radius)
 * - Thick 2dp border
 * - Color inversion on press/active state
 * - No shadows or elevation
 */
@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true,
    borderWidth: Dp = 2.dp,
    minHeight: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Determine colors based on state
    val shouldInvert = isActive || isPressed
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surface
            shouldInvert -> AcidLime
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 100),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Gray
            shouldInvert -> IndustrialBlack
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 100),
        label = "contentColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Gray
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 100),
        label = "borderColor"
    )
    
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // No ripple - industrial style
                enabled = enabled,
                onClick = onClick
            ),
        shape = RectangleShape, // Sharp corners - 0dp
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp // NO shadows
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = text.uppercase(),
                color = contentColor,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Large Industrial Button - For main actions like CONNECT/DISCONNECT
 */
@Composable
fun LargeIndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    subText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val shouldInvert = isActive || isPressed
    
    val backgroundColor by animateColorAsState(
        targetValue = if (shouldInvert) AcidLime else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 100),
        label = "backgroundColor"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (shouldInvert) IndustrialBlack else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 100),
        label = "contentColor"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (subText != null) {
                // Two lines
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text.uppercase(),
                        color = contentColor,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        letterSpacing = 6.sp
                    )
                    Text(
                        text = subText,
                        color = if (shouldInvert) IndustrialBlack.copy(alpha = 0.7f) else AcidLime.copy(alpha = 0.7f),
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                }
            } else {
                Text(
                    text = text.uppercase(),
                    color = contentColor,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 6.sp
                )
            }
        }
    }
}

/**
 * Square FAB Button - For actions like "+" or "PING"
 */
@Composable
fun SquareFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) AcidLime else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 100),
        label = "backgroundColor"
    )
    
    Surface(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}
