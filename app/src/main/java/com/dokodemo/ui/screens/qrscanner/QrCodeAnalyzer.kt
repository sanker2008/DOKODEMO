package com.dokodemo.ui.screens.qrscanner

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
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
            if (frameCount % 30 == 0) {
                Log.d("QrCodeAnalyzer", "Processing frame $frameCount, size: ${image.width}x${image.height}, rotation: ${image.imageInfo.rotationDegrees}")
            }
            
            val buffer = image.planes[0].buffer
            val data = toByteArray(buffer)
            
            val rotation = image.imageInfo.rotationDegrees
            val width = image.width
            val height = image.height
            
            // If the image is rotated, we need to rotate the data
            val rotatedData = if (rotation == 90 || rotation == 270) {
                rotateYUV420Degree90(data, width, height, rotation)
            } else {
                data 
            }
            
            // Swap dimensions if rotated
            val finalWidth = if (rotation == 90 || rotation == 270) height else width
            val finalHeight = if (rotation == 90 || rotation == 270) width else height
            
            val source = PlanarYUVLuminanceSource(
                rotatedData,
                finalWidth,
                finalHeight,
                0,
                0,
                finalWidth,
                finalHeight,
                false
            )
            
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            
            val result = reader.decode(binaryBitmap)
            Log.d("QrCodeAnalyzer", "QR Code detected: ${result.text.take(50)}...")
            onQrCodeDetected(result.text)
            
        } catch (e: Exception) {
            // QR code not found, this is normal
            if (frameCount % 60 == 0) {
                Log.d("QrCodeAnalyzer", "No QR code found in frame $frameCount: ${e.message}")
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
    
    private fun rotateYUV420Degree90(data: ByteArray, imageWidth: Int, imageHeight: Int, rotation: Int): ByteArray {
        val yuv = ByteArray(imageWidth * imageHeight)
        var i = 0
        
        if (rotation == 90) {
            for (x in 0 until imageWidth) {
                for (y in imageHeight - 1 downTo 0) {
                    yuv[i] = data[y * imageWidth + x]
                    i++
                }
            }
        } else if (rotation == 270) {
            for (x in imageWidth - 1 downTo 0) {
                for (y in 0 until imageHeight) {
                    yuv[i] = data[y * imageWidth + x]
                    i++
                }
            }
        }
        
        return yuv
    }
}