package com.dokodemo.ui.screens.qrscanner

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dokodemo.ui.components.IndustrialButton
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey
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
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IndustrialBlack)
    ) {
        if (hasCameraPermission) {
            // Camera Preview
            CameraPreview(
                onQrCodeDetected = { code ->
                    if (scannedCode == null) {
                        scannedCode = code
                        scanStatus = "TARGET ACQUIRED"
                        onQrCodeScanned(code)
                    }
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
            onGalleryClick = { /* TODO: Pick from gallery */ }
        )
    }
}

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                
                // Note: Real implementation would use ML Kit or ZXing for QR detection
                // imageAnalysis.setAnalyzer(cameraExecutor, QrCodeAnalyzer(onQrCodeDetected))
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
private fun ScannerOverlay(
    scanStatus: String,
    isFlashOn: Boolean,
    onClose: () -> Unit,
    onFlashToggle: () -> Unit,
    onGalleryClick: () -> Unit
) {
    // Scan line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLineOffset"
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialBlack.copy(alpha = 0.8f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AcidLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYS.READY",
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "NET_SECURE // PROTOCOL_V2",
                    color = TextGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            
            // Close button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, IndustrialGrey)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = IndustrialWhite,
                    fontSize = 20.sp
                )
            }
        }
        
        // Range/ISO info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RANGE: 0.4M",
                color = AcidLime,
                fontFamily = MonospaceFont,
                fontSize = 10.sp
            )
            Text(
                text = "ISO: AUTO",
                color = AcidLime,
                fontFamily = MonospaceFont,
                fontSize = 10.sp
            )
        }
        
        // Center viewfinder area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Viewfinder brackets
            ViewfinderBrackets(
                scanLineOffset = scanLineOffset,
                modifier = Modifier.size(250.dp)
            )
            
            // Center crosshair
            Canvas(modifier = Modifier.size(20.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val length = 10.dp.toPx()
                
                // Horizontal line
                drawLine(
                    color = AcidLime.copy(alpha = 0.5f),
                    start = Offset(center.x - length, center.y),
                    end = Offset(center.x + length, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                // Vertical line
                drawLine(
                    color = AcidLime.copy(alpha = 0.5f),
                    start = Offset(center.x, center.y - length),
                    end = Offset(center.x, center.y + length),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
        
        // Align QR Code text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, AcidLime)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "ALIGN QR CODE",
                    color = AcidLime,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
            }
        }
        
        // Status section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(IndustrialBlack.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "STATUS",
                        color = TextGrey,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp
                    )
                    Text(
                        text = scanStatus,
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                Text(
                    text = "CAM_01 [ACTIVE]",
                    color = TextGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndustrialButton(
                    text = "⚡ FLASH",
                    onClick = onFlashToggle,
                    isActive = isFlashOn,
                    modifier = Modifier.weight(1f)
                )
                IndustrialButton(
                    text = "🖼 GALLERY",
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer
            Text(
                text = "DOKODEMO VPN // V.2.0",
                color = TextGrey,
                fontFamily = MonospaceFont,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun ViewfinderBrackets(
    scanLineOffset: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx()
        val bracketLength = 40.dp.toPx()
        val cornerOffset = 8.dp.toPx()
        
        val rect = Rect(
            left = cornerOffset,
            top = cornerOffset,
            right = size.width - cornerOffset,
            bottom = size.height - cornerOffset
        )
        
        // Draw corner brackets
        val corners = listOf(
            // Top-left
            Pair(Offset(rect.left, rect.top + bracketLength), Offset(rect.left, rect.top)),
            Pair(Offset(rect.left, rect.top), Offset(rect.left + bracketLength, rect.top)),
            // Top-right
            Pair(Offset(rect.right - bracketLength, rect.top), Offset(rect.right, rect.top)),
            Pair(Offset(rect.right, rect.top), Offset(rect.right, rect.top + bracketLength)),
            // Bottom-left
            Pair(Offset(rect.left, rect.bottom - bracketLength), Offset(rect.left, rect.bottom)),
            Pair(Offset(rect.left, rect.bottom), Offset(rect.left + bracketLength, rect.bottom)),
            // Bottom-right
            Pair(Offset(rect.right - bracketLength, rect.bottom), Offset(rect.right, rect.bottom)),
            Pair(Offset(rect.right, rect.bottom - bracketLength), Offset(rect.right, rect.bottom))
        )
        
        corners.forEach { (start, end) ->
            drawLine(
                color = AcidLime,
                start = start,
                end = end,
                strokeWidth = strokeWidth
            )
        }
        
        // Draw scanning line
        val scanY = rect.top + (rect.height * scanLineOffset)
        drawLine(
            color = AcidLime.copy(alpha = 0.7f),
            start = Offset(rect.left + 20.dp.toPx(), scanY),
            end = Offset(rect.right - 20.dp.toPx(), scanY),
            strokeWidth = 2.dp.toPx()
        )
    }
}
