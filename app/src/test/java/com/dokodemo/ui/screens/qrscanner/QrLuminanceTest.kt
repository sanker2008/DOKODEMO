package com.dokodemo.ui.screens.qrscanner

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class QrLuminanceTest {
    @Test fun compactPlaneAtZeroRotationPreservesPixels() {
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5, 6),
            normalizeQrLuminance(byteArrayOf(1, 2, 3, 4, 5, 6), 2, 3, 2, 1, 0))
    }

    @Test fun rowPaddingIsNotTreatedAsImagePixels() {
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5, 6),
            normalizeQrLuminance(byteArrayOf(1, 2, 99, 99, 3, 4, 99, 99, 5, 6, 99, 99), 2, 3, 4, 1, 0))
    }

    @Test fun pixelStrideSkipsInterleavedBytesAndAllowsMissingFinalRowPadding() {
        // Camera plane buffers can end immediately after the final valid sample.
        assertArrayEquals(byteArrayOf(1, 2, 3, 4),
            normalizeQrLuminance(byteArrayOf(1, 99, 2, 99, 99, 99, 3, 99, 4), 2, 2, 6, 2, 0))
    }

    @Test fun ninetyDegreesRotatesNonSquarePlaneClockwise() {
        assertArrayEquals(byteArrayOf(5, 3, 1, 6, 4, 2),
            normalizeQrLuminance(byteArrayOf(1, 2, 3, 4, 5, 6), 2, 3, 2, 1, 90))
    }

    @Test fun oneHundredEightyDegreesReversesBothAxes() {
        assertArrayEquals(byteArrayOf(6, 5, 4, 3, 2, 1),
            normalizeQrLuminance(byteArrayOf(1, 2, 3, 4, 5, 6), 2, 3, 2, 1, 180))
    }

    @Test fun twoHundredSeventyDegreesRotatesNonSquarePlaneClockwise() {
        assertArrayEquals(byteArrayOf(2, 4, 6, 1, 3, 5),
            normalizeQrLuminance(byteArrayOf(1, 2, 3, 4, 5, 6), 2, 3, 2, 1, 270))
    }

    @Test fun rotationUsesVisiblePixelsAfterRemovingBothStrides() {
        assertArrayEquals(byteArrayOf(5, 3, 1, 6, 4, 2),
            normalizeQrLuminance(byteArrayOf(1, 99, 2, 99, 99, 3, 99, 4, 99, 99, 5, 99, 6), 2, 3, 5, 2, 90))
    }

    @Test fun luminanceRetainsUnsignedByteBitPatterns() {
        assertArrayEquals(byteArrayOf(-1, -128, 0, 127),
            normalizeQrLuminance(byteArrayOf(-1, -128, 0, 127), 2, 2, 2, 1, 0))
    }
}
