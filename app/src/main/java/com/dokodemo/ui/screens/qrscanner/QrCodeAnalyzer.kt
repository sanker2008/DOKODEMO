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
            val processedData = normalizeQrLuminance(data, width, height, rowStride, yPlane.pixelStride, rotation)
            
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
            val result = try { reader.decode(binaryBitmap, hints) }
            catch (_: com.google.zxing.NotFoundException) {
                reader.reset()
                reader.decode(BinaryBitmap(HybridBinarizer(source.invert())), hints)
            }
            Log.d("QrCodeAnalyzer", "QR code detected")
            onQrCodeDetected(result.text)
            
        } catch (e: Exception) {
            // QR code not found, this is normal
            if (frameCount % 120 == 0) {
                Log.d("QrCodeAnalyzer", "Still scanning... frame $frameCount, error: ${e.message}")
            }
        } finally {
            reader.reset()
            image.close()
        }
    }

    private fun toByteArray(buffer: ByteBuffer): ByteArray {
        val buffer = buffer.duplicate()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }
    
}
