package com.dokodemo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dokodemo.ui.theme.DokoDemoTheme

@Composable
fun IndustrialLogo(
    modifier: Modifier = Modifier
) {
    val logoGray = Color(0xFF607D8B)
    val logoGreen = Color(0xFFB7D5C7)
    val logoBlue = Color(0xFFA0C4E3)

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = width * 0.085f

            // 1. Layered Green Background (Soft Rings)
            drawCircle(
                color = logoGreen.copy(alpha = 0.25f),
                radius = width * 0.45f,
                center = center
            )
            drawCircle(
                color = logoGreen.copy(alpha = 0.45f),
                radius = width * 0.33f,
                center = center
            )
            drawCircle(
                color = logoGreen.copy(alpha = 0.65f),
                radius = width * 0.22f,
                center = center
            )

            // 2. Central Blue Node
            drawCircle(
                color = logoBlue,
                radius = width * 0.12f,
                center = center
            )

            val grayStroke = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )

            // 3. The "D" Structure (Peeling Paths)
            
            // Upper Branch: Peels from diagonal, goes up, curves right, then into center
            val upperBranch = Path().apply {
                moveTo(width * 0.3f, height * 0.62f) // Fork point on diagonal
                quadraticBezierTo(width * 0.25f, height * 0.45f, width * 0.25f, height * 0.35f)
                quadraticBezierTo(width * 0.25f, height * 0.18f, width * 0.45f, height * 0.18f)
                lineTo(width * 0.65f, height * 0.18f)
                quadraticBezierTo(width * 0.82f, height * 0.18f, width * 0.82f, height * 0.35f)
                quadraticBezierTo(width * 0.82f, height * 0.48f, width * 0.68f, height * 0.48f)
                lineTo(width * 0.5f, height * 0.48f)
            }
            drawPath(path = upperBranch, color = logoGray, style = grayStroke)

            // Lower Branch: Peels from diagonal, goes down, curves right, then ends top-right
            val lowerBranch = Path().apply {
                moveTo(width * 0.3f, height * 0.62f) // Same fork point
                quadraticBezierTo(width * 0.35f, height * 0.82f, width * 0.55f, height * 0.82f)
                lineTo(width * 0.72f, height * 0.82f)
                quadraticBezierTo(width * 0.95f, height * 0.82f, width * 0.95f, height * 0.6f)
                quadraticBezierTo(width * 0.95f, height * 0.45f, width * 0.85f, height * 0.35f)
            }
            drawPath(path = lowerBranch, color = logoGray, style = grayStroke)

            // 4. Main Diagonal Connector
            val diagonal = Path().apply {
                moveTo(width * 0.12f, height * 0.82f) // Bottom-left node
                lineTo(width * 0.88f, height * 0.18f) // Top-right node
            }
            drawPath(path = diagonal, color = logoGray, style = grayStroke)

            // Terminal Nodes
            drawCircle(
                color = logoGray,
                radius = width * 0.065f,
                center = Offset(width * 0.12f, height * 0.82f)
            )
            drawCircle(
                color = logoGray,
                radius = width * 0.065f,
                center = Offset(width * 0.88f, height * 0.18f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IndustrialLogoPreview() {
    DokoDemoTheme {
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            IndustrialLogo(modifier = Modifier.size(320.dp))
        }
    }
}
