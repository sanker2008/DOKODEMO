package com.dokodemo.ui.screens.qrscanner

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QrBitmapDecoderTest {
    private val content = "vmess://transparent-qr-test"
    private val size = 256
    private val modules = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)

    @Test fun transparentBlackCodeDecodesOnWhite() {
        assertEquals(content, decodeQrPixels(size, size, pixels(0xFF000000.toInt(), 0x00000000)))
    }

    @Test fun opaqueBlackCodeDecodesOnWhite() {
        assertEquals(content, decodeQrPixels(size, size, pixels(0xFF000000.toInt(), 0xFFFFFFFF.toInt())))
    }

    @Test fun whiteCodeOnBlackUsesInvertedDecode() {
        assertEquals(content, decodeQrPixels(size, size, pixels(0xFFFFFFFF.toInt(), 0xFF000000.toInt())))
    }

    @Test fun partlyTransparentBlackCodeDecodesAfterCompositing() {
        assertEquals(content, decodeQrPixels(size, size, pixels(0xC0000000.toInt(), 0x00000000)))
    }

    @Test fun noQrCodeReturnsNull() {
        assertNull(decodeQrPixels(size, size, IntArray(size * size) { 0xFFFFFFFF.toInt() }))
    }

    @Test fun fullyTransparentHiddenQrIsInvisible() {
        assertNull(decodeQrPixels(size, size, pixels(0x00000000, 0x00FFFFFF)))
    }

    private fun pixels(dark: Int, light: Int): IntArray =
        IntArray(size * size) { index ->
            if (modules[index % size, index / size]) dark else light
        }
}
