package com.dokodemo.ui.screens.qrscanner

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()
    private var frameCount = 0

    override fun analyze(image: ImageProxy) {
        frameCount++
        
        try {
            if (frameCount % 60 == 0) {
                Log.d("QrCodeAnalyzer", "Processing frame $frameCount, size: ${image.width}x${image.height}, rotation: ${image.imageInfo.rotationDegrees}")
            }
            
            val yPlane = image.planes[0]
            val buffer = yPlane.buffer
            val rowStride = yPlane.rowStride
            val data = toByteArray(buffer)
            
            val rotation = image.imageInfo.rotationDegrees
            val width = image.width
            val height = image.height
            
            // If the image is rotated, we need to rotate the data, also dropping the row padding
            val processedData = rotateAndCleanYUV420(data, width, height, rowStride, rotation)
            
            // Swap dimensions if rotated
            val finalWidth = if (rotation == 90 || rotation == 270) height else width
            val finalHeight = if (rotation == 90 || rotation == 270) width else height
            
            val source = PlanarYUVLuminanceSource(
                processedData,
                finalWidth,
                finalHeight,
                0,
                0,
                finalWidth,
                finalHeight,
                false
            )
            
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val hints = mapOf(
                DecodeHintType.TRY_HARDER to true,
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
            val result = reader.decode(binaryBitmap, hints)
            Log.d("QrCodeAnalyzer", "QR Code detected successfully: ${result.text.take(20)}...")
            onQrCodeDetected(result.text)
            
        } catch (e: Exception) {
            // QR code not found, this is normal
            if (frameCount % 120 == 0) {
                Log.d("QrCodeAnalyzer", "Still scanning... frame $frameCount, error: ${e.message}")
            }
        } finally {
            image.close()
        }
    }

    private fun toByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }
    
    private fun rotateAndCleanYUV420(data: ByteArray, imageWidth: Int, imageHeight: Int, rowStride: Int, rotation: Int): ByteArray {
        val yuv = ByteArray(imageWidth * imageHeight)
        var i = 0
        
        when (rotation) {
            90 -> {
                for (x in 0 until imageWidth) {
                    for (y in imageHeight - 1 downTo 0) {
                        yuv[i++] = data[y * rowStride + x]
                    }
                }
            }
            270 -> {
                for (x in imageWidth - 1 downTo 0) {
                    for (y in 0 until imageHeight) {
                        yuv[i++] = data[y * rowStride + x]
                    }
                }
            }
            180 -> {
                for (y in imageHeight - 1 downTo 0) {
                    for (x in imageWidth - 1 downTo 0) {
                        yuv[i++] = data[y * rowStride + x]
                    }
                }
            }
            else -> {
                // Remove padding
                if (imageWidth == rowStride) {
                    return data
                } else {
                    for (y in 0 until imageHeight) {
                        System.arraycopy(data, y * rowStride, yuv, y * imageWidth, imageWidth)
                    }
                }
            }
        }
        
        return yuv
    }
}