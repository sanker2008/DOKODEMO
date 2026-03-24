package com.dokodemo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

/**
 * 主操作按钮（替代旧 IndustrialButton）
 * 圆角16dp，支持 active 状态
 */
@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    enabled: Boolean = true,
    minHeight: Dp = 52.dp,
    // 以下参数保留兼容性但不使用
    borderWidth: Dp = 0.dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surface
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
            isActive -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "contentColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = minHeight),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp),
        interactionSource = interactionSource
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 大连接按钮（主页用，替代旧 LargeIndustrialButton）
 * 保持同名兼容
 */
@Composable
fun LargeIndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    subText: String? = null
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "largeContainerColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "largeContentColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
            if (subText != null) {
                Text(
                    text = subText,
                    fontSize = 11.sp,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 浮动操作按钮（方形→圆形）
 */
@Composable
fun SquareFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        content()
    }
}

/**
 * 标签页按钮（协议选择用，替代旧 IndustrialTabButton）
 */
@Composable
fun IndustrialTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(150),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(150),
        label = "tabText"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
