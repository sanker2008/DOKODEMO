package com.dokodemo.ui.screens.qrscanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dokodemo.ui.components.DokoButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.runtime.rememberUpdatedState
import java.util.concurrent.Executors

import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.dokodemo.R

@Composable
fun QrScannerScreen(onNavigateBack: () -> Unit, onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    fun permissionGranted() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var hasCameraPermission by remember { mutableStateOf(permissionGranted()) }
    var isFlashOn by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraFailed by remember { mutableStateOf(false) }
    var decoding by remember { mutableStateOf(false) }
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var scanStatus by remember { mutableStateOf<Int?>(null) }
    fun acceptCode(code: String?) {
        if (scannedCode != null) return
        val accepted = acceptedQrCode(code)
        if (accepted != null) {
            scannedCode = accepted
            scanStatus = R.string.qr_success
        } else {
            scanStatus = R.string.qr_invalid
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && !decoding) scope.launch {
            decoding = true
            try { acceptCode(withContext(Dispatchers.IO) { decodeQrFromUri(context, uri) }) }
            finally { decoding = false }
        }
    }
    LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) hasCameraPermission = permissionGranted()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(isFlashOn, camera, hasCameraPermission) {
        if (hasCameraPermission) camera?.cameraControl?.enableTorch(isFlashOn)
    }
    LaunchedEffect(scannedCode) { scannedCode?.let(onQrCodeScanned) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding()) {
        if (hasCameraPermission && !cameraFailed) {
            ScannerOverlay(
                scanStatus = stringResource(if (decoding) R.string.qr_decoding else scanStatus ?: R.string.qr_prompt),
                isFlashOn = isFlashOn,
                flashEnabled = camera?.cameraInfo?.hasFlashUnit() == true,
                onClose = onNavigateBack,
                onFlashToggle = { isFlashOn = !isFlashOn },
                onGalleryClick = { if (!decoding) galleryLauncher.launch("image/*") },
                preview = {
                    CameraPreview(
                        onQrCodeDetected = { if (!decoding) acceptCode(it) },
                        onCameraBound = { camera = it },
                        onCameraError = { cameraFailed = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.qr_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(if (cameraFailed) R.string.qr_camera_failed else R.string.qr_permission_required))
                scanStatus?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
                if (!hasCameraPermission) {
                    DokoButton(stringResource(R.string.qr_grant_permission), { permissionLauncher.launch(Manifest.permission.CAMERA) })
                    TextButton(onClick = {
                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                    }) { Text(stringResource(R.string.qr_open_settings)) }
                } else {
                    DokoButton(stringResource(R.string.qr_retry), { cameraFailed = false })
                }
                DokoButton(stringResource(R.string.qr_gallery), { galleryLauncher.launch("image/*") }, enabled = !decoding)
                TextButton(onClick = onNavigateBack) { Text(stringResource(R.string.back)) }
            }
        }
    }
}

private fun decodeQrFromUri(context: Context, uri: Uri): String? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0)
    val options = BitmapFactory.Options().apply {
        inSampleSize = 1
        while (bounds.outWidth / inSampleSize > 2048 || bounds.outHeight / inSampleSize > 2048) inSampleSize *= 2
    }
    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?: return@runCatching null
    try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        decodeQrPixels(bitmap.width, bitmap.height, pixels)
    } finally { bitmap.recycle() }
}.getOrNull()

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String) -> Unit,
    onCameraBound: (Camera) -> Unit,
    onCameraError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    val currentDetected by rememberUpdatedState(onQrCodeDetected)
    val currentBound by rememberUpdatedState(onCameraBound)
    val currentError by rememberUpdatedState(onCameraError)
    AndroidView(factory = { previewView }, modifier = modifier)
    DisposableEffect(lifecycleOwner, previewView) {
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        val disposed = java.util.concurrent.atomic.AtomicBoolean(false)
        var provider: ProcessCameraProvider? = null
        analysis.setAnalyzer(executor, QrCodeAnalyzer { code ->
            mainExecutor.execute { if (!disposed.get()) currentDetected(code) }
        })
        providerFuture.addListener({
            if (!disposed.get()) try {
                provider = providerFuture.get()
                currentBound(provider!!.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis))
            } catch (_: Exception) { currentError() }
        }, mainExecutor)
        onDispose {
            disposed.set(true)
            analysis.clearAnalyzer()
            provider?.unbind(preview, analysis)
            executor.shutdown()
        }
    }
}

@Composable
private fun ScannerOverlay(
    scanStatus: String,
    isFlashOn: Boolean,
    flashEnabled: Boolean,
    onClose: () -> Unit,
    onFlashToggle: () -> Unit,
    onGalleryClick: () -> Unit,
    preview: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部栏 (Mist & Dawn - Transparent + Blur feel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                .padding(vertical = 12.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.qr_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // 关闭按钮
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Show the entire camera frame between the controls, without a crop guide.
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) { preview() }
        
        // 底部控制操作区
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = scanStatus,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 闪光灯按钮
                DokoButton(
                    text = stringResource(R.string.qr_flash),
                    enabled = flashEnabled,
                    icon = if (isFlashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    isActive = isFlashOn,
                    onClick = onFlashToggle,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 相册按钮
                DokoButton(
                    text = stringResource(R.string.qr_gallery),
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
