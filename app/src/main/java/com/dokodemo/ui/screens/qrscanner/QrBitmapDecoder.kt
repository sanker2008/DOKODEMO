package com.dokodemo.ui.screens.qrscanner

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer

/** Decode ARGB pixels over white. The supplied pixel array is composited in place. */
internal fun decodeQrPixels(width: Int, height: Int, pixels: IntArray): String? {
    require(width > 0 && height > 0)
    val count = Math.multiplyExact(width, height)
    require(pixels.size >= count)

    for (index in 0 until count) {
        val color = pixels[index]
        val alpha = color ushr 24
        if (alpha != 255) {
            val inverseAlpha = 255 - alpha
            val red = (((color ushr 16) and 0xFF) * alpha + 255 * inverseAlpha + 127) / 255
            val green = (((color ushr 8) and 0xFF) * alpha + 255 * inverseAlpha + 127) / 255
            val blue = ((color and 0xFF) * alpha + 255 * inverseAlpha + 127) / 255
            pixels[index] = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
        }
    }
    val source = RGBLuminanceSource(width, height, pixels)
    val reader = MultiFormatReader()
    val hints = mapOf(
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
    )
    return try {
        try {
            reader.decode(BinaryBitmap(HybridBinarizer(source)), hints).text
        } catch (_: ReaderException) {
            reader.reset()
            reader.decode(BinaryBitmap(HybridBinarizer(source.invert())), hints).text
        }
    } catch (_: ReaderException) {
        null
    } finally {
        reader.reset()
    }
}
