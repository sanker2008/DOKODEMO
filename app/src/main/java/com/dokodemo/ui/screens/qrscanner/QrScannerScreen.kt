package com.dokodemo.ui.screens.qrscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dokodemo.ui.components.IndustrialButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.runtime.rememberUpdatedState
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.io.InputStream
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var isFlashOn by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf("SCANNING...") }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val result = decodeQrFromUri(context, it)
            if (result != null) {
                scannedCode = result
                scanStatus = "TARGET ACQUIRED"
                android.widget.Toast.makeText(context, "解码成功", android.widget.Toast.LENGTH_SHORT).show()
                // Navigation is handled by LaunchedEffect(scannedCode)
            } else {
                scanStatus = "SCAN FAILED"
                android.widget.Toast.makeText(context, "未识别到有效的二维码", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Toggle flashlight when isFlashOn or camera changes
    LaunchedEffect(isFlashOn, camera) {
        try {
            if (hasCameraPermission) {
                camera?.cameraControl?.enableTorch(isFlashOn)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Navigate on main thread when a QR code is detected
    // (camera callback runs on background thread, so navigate must be deferred)
    LaunchedEffect(scannedCode) {
        val code = scannedCode
        if (code != null) {
            onQrCodeScanned(code)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasCameraPermission) {
            // Camera Preview
            CameraPreview(
                onQrCodeDetected = { code ->
                    if (scannedCode == null) {
                        scannedCode = code
                        scanStatus = "TARGET ACQUIRED"
                    }
                },
                onCameraBound = { boundCamera ->
                    camera = boundCamera
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Overlay
        ScannerOverlay(
            scanStatus = scanStatus,
            isFlashOn = isFlashOn,
            onClose = onNavigateBack,
            onFlashToggle = { isFlashOn = !isFlashOn },
            onGalleryClick = { galleryLauncher.launch("image/*") }
        )
    }
}

private fun decodeQrFromUri(context: Context, uri: Uri): String? {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        
        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
        )
        return reader.decode(binaryBitmap, hints).text
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String) -> Unit,
    onCameraBound: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Use rememberUpdatedState to avoid re-binding when the callback changes
    val currentOnQrCodeDetected by rememberUpdatedState(onQrCodeDetected)
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier
    ) { previewView ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            
            imageAnalysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer(currentOnQrCodeDetected))
            
            try {
                // Only unbind and rebind if not already bound with the same use cases
                // (Simplified: unbindAll is still safe here if 'update' is called correctly)
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                onCameraBound(camera)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun ScannerOverlay(
    scanStatus: String,
    isFlashOn: Boolean,
    onClose: () -> Unit,
    onFlashToggle: () -> Unit,
    onGalleryClick: () -> Unit
) {
    // 柔和的扫码线动画
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanLineOffset"
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏 (Mist & Dawn - Transparent + Blur feel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "扫描二维码",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 关闭按钮
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // 中间扫码区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // 背景暗化
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)))
            
            // 扫码框
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(2.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .background(androidx.compose.ui.graphics.Color.Transparent)
            ) {
                // 扫描线
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val scanY = size.height * scanLineOffset
                    drawLine(
                        color = primaryColor,
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 3.dp.toPx()
                    )
                    // 渐变光晕
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(androidx.compose.ui.graphics.Color.Transparent, primaryColor.copy(alpha = 0.3f)),
                            startY = maxOf(0f, scanY - 60.dp.toPx()),
                            endY = scanY
                        ),
                        topLeft = Offset(0f, maxOf(0f, scanY - 60.dp.toPx())),
                        size = androidx.compose.ui.geometry.Size(size.width, minOf(60.dp.toPx(), scanY))
                    )
                }
            }
        }
        
        // 底部控制操作区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (scanStatus == "SCANNING...") "请将二维码放入框内" else "识别成功",
                style = MaterialTheme.typography.bodyMedium,
                color = if (scanStatus == "SCANNING...") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 闪光灯按钮
                IndustrialButton(
                    text = "照明",
                    icon = if (isFlashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    isActive = isFlashOn,
                    onClick = onFlashToggle,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 相册按钮
                IndustrialButton(
                    text = "相册",
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}