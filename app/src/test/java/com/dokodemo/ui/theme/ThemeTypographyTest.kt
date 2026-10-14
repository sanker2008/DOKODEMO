package com.dokodemo.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTypographyTest {
    @Test fun unspecifiedUnitsStayUnspecifiedWhenScaling() {
        val style = TextStyle(fontSize = 14.sp, lineHeight = TextUnit.Unspecified)

        val scaled = style.scaleSpecifiedTextUnits(1.2f)

        assertEquals(16.8f, scaled.fontSize.value, 0.0001f)
        assertEquals(TextUnit.Unspecified, scaled.lineHeight)
        assertEquals(TextStyle(), TextStyle().scaleSpecifiedTextUnits(1.2f))
    }

    @Test fun specifiedLineHeightScalesAndDefaultScalePreservesStyle() {
        val style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)

        assertEquals(24f, style.scaleSpecifiedTextUnits(1.2f).lineHeight.value, 0.0001f)
        assertEquals(style, style.scaleSpecifiedTextUnits(1f))
    }
}
