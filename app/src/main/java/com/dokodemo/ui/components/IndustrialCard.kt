package com.dokodemo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont

/**
 * Industrial Card - Neubrutalism style container
 * 
 * Features:
 * - Pure black background
 * - 1dp grey border
 * - No shadows
 * - Sharp corners (0dp radius)
 * - 16dp padding
 */
@Composable
fun IndustrialCard(
    modifier: Modifier = Modifier,
    borderColor: Color = IndustrialGrey,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = IndustrialBlack,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RectangleShape, // Sharp corners
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 0.dp // NO shadows
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * Industrial Card with Header - For sections with labels
 */
@Composable
fun IndustrialCardWithHeader(
    header: String,
    modifier: Modifier = Modifier,
    headerColor: Color = AcidLime,
    borderColor: Color = IndustrialGrey,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        // Header label
        Text(
            text = "[ $header ]",
            color = headerColor,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Card content
        IndustrialCard(
            borderColor = borderColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * Server List Item Card - Specialized for server list
 */
@Composable
fun ServerListItemCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable () -> Unit
) {
    val backgroundColor = if (isSelected) AcidLime else IndustrialBlack
    val borderColor = if (isSelected) AcidLime else IndustrialGrey
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            content()
        }
    }
}

/**
 * Status Badge - For showing connection status, protocol tags, etc.
 */
@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = IndustrialBlack,
    textColor: Color = AcidLime,
    borderColor: Color = AcidLime
) {
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Country Code Box - For [JP], [US], etc.
 */
@Composable
fun CountryCodeBox(
    code: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = if (isSelected) IndustrialBlack else IndustrialBlack
    val textColor = if (isSelected) IndustrialBlack else AcidLime
    val borderColor = AcidLime
    
    Surface(
        modifier = modifier,
        shape = RectangleShape,
        color = if (isSelected) AcidLime else IndustrialBlack,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Text(
            text = code.uppercase().take(2),
            color = if (isSelected) IndustrialBlack else AcidLime,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
