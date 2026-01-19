package com.dokodemo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite

/**
 * Industrial Toggle - Neubrutalism style toggle switch
 * 
 * Features:
 * - Square box (not rounded)
 * - Empty = OFF
 * - Filled with Acid Lime = ON
 * - Sharp corners
 */
@Composable
fun IndustrialToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) AcidLime else MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 100),
        label = "toggleBackground"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.outline
            checked -> AcidLime
            else -> AcidLime
        },
        animationSpec = tween(durationMillis = 100),
        label = "toggleBorder"
    )
    
    Surface(
        modifier = modifier
            .size(24.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        // Empty box - the fill color indicates the state
    }
}

/**
 * Industrial Toggle with Label - For settings items
 */
@Composable
fun IndustrialToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label section
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = IndustrialGrey,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Toggle
        IndustrialToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

/**
 * Industrial Checkbox - Square checkbox for lists
 */
@Composable
fun IndustrialCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) AcidLime else IndustrialBlack,
        animationSpec = tween(durationMillis = 100),
        label = "checkboxBackground"
    )
    
    Surface(
        modifier = modifier
            .size(20.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, if (checked) AcidLime else IndustrialGrey),
        shadowElevation = 0.dp
    ) {
        if (checked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                // Checkmark representation
                Text(
                    text = "✓",
                    color = IndustrialBlack,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Industrial Tab Button - For protocol selection (VMESS / VLESS / TROJAN)
 */
@Composable
fun IndustrialTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AcidLime else IndustrialBlack,
        animationSpec = tween(durationMillis = 100),
        label = "tabBackground"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isSelected) IndustrialBlack else IndustrialWhite,
        animationSpec = tween(durationMillis = 100),
        label = "tabTextColor"
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
        border = BorderStroke(1.dp, AcidLime),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
