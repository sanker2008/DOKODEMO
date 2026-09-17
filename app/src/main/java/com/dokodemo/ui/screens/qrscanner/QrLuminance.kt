package com.dokodemo.ui.screens.qrscanner

/** Pack a camera Y plane, respecting both pixel stride and row padding. */
internal fun normalizeQrLuminance(
    data: ByteArray, width: Int, height: Int, rowStride: Int, pixelStride: Int, rotation: Int
): ByteArray {
    require(width > 0 && height > 0 && rowStride > 0 && pixelStride > 0)
    require(rotation in listOf(0, 90, 180, 270))
    require(data.size.toLong() > (height - 1L) * rowStride + (width - 1L) * pixelStride)
    val output = ByteArray(Math.multiplyExact(width, height))
    for (y in 0 until height) for (x in 0 until width) {
        val index = when (rotation) {
            90 -> x * height + height - 1 - y
            180 -> (height - 1 - y) * width + width - 1 - x
            270 -> (width - 1 - x) * height + y
            else -> y * width + x
        }
        output[index] = data[y * rowStride + x * pixelStride]
    }
    return output
}
