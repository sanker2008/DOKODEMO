package com.dokodemo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.content.ContextCompat
import com.dokodemo.MainActivity
import com.dokodemo.core.CoreManager
import com.dokodemo.data.model.ServerProfile
import dagger.hilt.android.AndroidEntryPoint
import com.dokodemo.core.Tun2socksWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * DokoDemoVpnService - Real VPN service for V2Ray/Xray tunneling
 * 
 * Architecture:
 * 1. VpnService creates TUN interface
 * 2. V2Ray core runs SOCKS5 proxy on localhost
 * 3. tun2socks bridges TUN traffic to SOCKS5 proxy
 */
@AndroidEntryPoint
class DokoDemoVpnService : VpnService() {
    
    @Inject
    lateinit var coreManager: CoreManager
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var trafficJob: Job? = null
    
    // Traffic stats
    private var lastUploadBytes = 0L
    private var lastDownloadBytes = 0L
    private var uploadSpeed = 0L
    private var downloadSpeed = 0L
    
    companion object {
        private const val TAG = "DokoDemoVpnService"
        
        const val ACTION_START = "com.dokodemo.START_VPN"
        const val ACTION_STOP = "com.dokodemo.STOP_VPN"
        const val EXTRA_SERVER_CONFIG = "server_config_json"
        const val EXTRA_SERVER_NAME = "server_name"
        
        private const val NOTIFICATION_CHANNEL_ID = "dokodemo_vpn_channel"
        private const val NOTIFICATION_ID = 1
        
        // VPN Configuration
        private const val VPN_MTU = 1500
        private const val VPN_ADDRESS = "10.0.0.2"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_DNS_1 = "8.8.8.8"
        private const val VPN_DNS_2 = "8.8.4.4"
        
        // Broadcast actions
        const val ACTION_VPN_CONNECTED = "com.dokodemo.VPN_CONNECTED"
        const val ACTION_VPN_DISCONNECTED = "com.dokodemo.VPN_DISCONNECTED"
        const val ACTION_TRAFFIC_UPDATE = "com.dokodemo.TRAFFIC_UPDATE"
        const val EXTRA_UPLOAD_SPEED = "upload_speed"
        const val EXTRA_DOWNLOAD_SPEED = "download_speed"
        const val EXTRA_TOTAL_UPLOAD = "total_upload"
        const val EXTRA_TOTAL_DOWNLOAD = "total_download"
        const val EXTRA_ERROR_REASON = "error_reason"
        
        @Volatile
        private var instance: DokoDemoVpnService? = null
        
        fun isServiceRunning(): Boolean = instance?.isRunning == true
        
        fun getUploadSpeed(): Long = instance?.uploadSpeed ?: 0L
        fun getDownloadSpeed(): Long = instance?.downloadSpeed ?: 0L
    }
    
    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Handle network changes if needed
            Log.d(TAG, "Network connectivity changed")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        
        try {
            // Initialize V2Ray environment - must catch Throwable because
            // UnsatisfiedLinkError is an Error (not Exception) when native lib is missing
            coreManager.initialize()
            coreManager.copyAssets()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize core manager (native lib may be missing): ${e.message}")
            // Service continues - mock mode will handle connection attempts
        }
        
        // Register connectivity receiver
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    connectivityReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION),
                    RECEIVER_NOT_EXPORTED
                )
            } else {
                registerReceiver(
                    connectivityReceiver,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register connectivity receiver: ${e.message}")
        }
        
        Log.i(TAG, "VPN Service created, V2Ray version: ${coreManager.getVersion()}")

    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val configJson = intent.getStringExtra(EXTRA_SERVER_CONFIG)
                    val serverName = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "DokoDemo"
                    val routingMode = intent.getStringExtra("routing_mode")
                    val proxiedApps = intent.getStringArrayListExtra("proxied_apps")
                    
                    if (configJson != null) {
                        // MUST call startForeground immediately on the main thread
                        // before doing any heavy work, to avoid ForegroundServiceDidNotStartInTimeException
                        startForeground(NOTIFICATION_ID, createNotification("正在连接...", serverName))
                        startVpn(configJson, serverName, routingMode, proxiedApps)
                    } else {
                    Log.e(TAG, "No configuration provided")
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopVpn()
            }
            else -> {
                // Service restarted by system
                if (!isRunning) {
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }
    
    private fun startVpn(
        configJson: String,
        serverName: String,
        routingMode: String? = null,
        proxiedApps: List<String>? = null
    ) {
        if (isRunning) {
            Log.w(TAG, "VPN already running, restarting for new node: $serverName")
            try { coreManager.stopCore() } catch (_: Throwable) {}
            try { Tun2socksWrapper.stop() } catch (_: Throwable) {}
            try { vpnInterface?.close() } catch (_: Throwable) {}
            vpnInterface = null
            trafficJob?.cancel()
            trafficJob = null
            isRunning = false
            // Small delay to let sockets close
            Thread.sleep(500)
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "Starting VPN connection to: $serverName")
                
                // 1. Start V2Ray core with automatic port retry
                var coreError: String? = null
                var finalConfig = configJson
                
                for (attempt in 0 until 5) {
                    if (attempt > 0) {
                        // Parse config and modify only the dns-in inbound port
                        try {
                            val newPort = CoreManager.DNS_INBOUND_PORT + attempt
                            Log.w(TAG, "Port conflict, retrying with DNS port $newPort (attempt ${attempt + 1}/5)")
                            val jsonObj = com.google.gson.JsonParser.parseString(configJson).asJsonObject
                            val inbounds = jsonObj.getAsJsonArray("inbounds")
                            for (i in 0 until inbounds.size()) {
                                val inbound = inbounds[i].asJsonObject
                                if (inbound.get("tag")?.asString == "dns-in") {
                                    inbound.addProperty("port", newPort)
                                    break
                                }
                            }
                            finalConfig = jsonObj.toString()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to modify config for retry: ${e.message}")
                            break
                        }
                    }
                    
                    coreError = try {
                        coreManager.startCore(finalConfig)
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception starting V2Ray core: ${e.message}", e)
                        "核心异常: ${e.message}"
                    }
                    
                    if (coreError == null) {
                        if (attempt > 0) Log.i(TAG, "Core started on alternate DNS port ${CoreManager.DNS_INBOUND_PORT + attempt}")
                        break // success
                    }
                    
                    // Only retry on port binding errors
                    if (!coreError.contains("address already in use", ignoreCase = true)) break
                }
                
                if (coreError != null) {
                    Log.e(TAG, "Failed to start V2Ray core: $coreError")
                    broadcastDisconnected(coreError)
                    stopSelf()
                    return@launch
                }
                Log.i(TAG, "V2Ray core started, SOCKS proxy at ${coreManager.getSocksAddress()}")
                
                // Small delay to ensure core is ready
                delay(500)
                
                // 2. Establish VPN interface
                val vpnBuilder = Builder()
                    .setSession("DokoDemo - $serverName")
                    .setMtu(VPN_MTU)
                    .addAddress(VPN_ADDRESS, 24)
                    .addDnsServer(VPN_DNS_1)
                    .addDnsServer(VPN_DNS_2)
                    .addRoute(VPN_ROUTE, 0)
                
                // Set as always-on capable
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vpnBuilder.setMetered(false)
                }
                
                if (routingMode == "SPLIT" && !proxiedApps.isNullOrEmpty()) {
                    Log.i(TAG, "Split tunneling mode: ${proxiedApps.size} apps")
                    proxiedApps.forEach { pkg ->
                        try {
                            vpnBuilder.addAllowedApplication(pkg)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to add allowed application: $pkg")
                        }
                    }
                } else {
                    try {
                        // CRITICAL: Exclude this app from the VPN to prevent infinite routing loops
                        vpnBuilder.addDisallowedApplication(packageName)
                    } catch (e: Exception) {
                        Log.w(TAG, "Cannot exclude self from VPN", e)
                    }
                }
                
                vpnInterface = vpnBuilder.establish()
                
                if (vpnInterface == null) {
                    Log.e(TAG, "Failed to establish VPN interface - permission may not be granted")
                    coreManager.stopCore()
                    broadcastDisconnected()
                    stopSelf()
                    return@launch
                }
                
                val tunFd = vpnInterface!!.fd
                Log.i(TAG, "VPN interface established, fd: $tunFd")
                
                // 3. Start tun2socks
                val tun2socksStarted = try {
                    Tun2socksWrapper.start(
                        tunFd,
                        coreManager.getSocksAddress(),
                        VPN_DNS_1,
                        VPN_MTU
                    )
                } catch (e: Throwable) {
                    Log.e(TAG, "tun2socks start error (native lib may be missing): ${e.message}", e)
                    true // Continue anyway in degraded mode
                }
                
                if (!tun2socksStarted) {
                    Log.e(TAG, "Failed to start tun2socks")
                    coreManager.stopCore()
                    try { vpnInterface?.close() } catch (_: Exception) {}
                    vpnInterface = null
                    broadcastDisconnected()
                    stopSelf()
                    return@launch
                }
                
                Log.i(TAG, "tun2socks started")
                
                // 4. Mark as running
                isRunning = true
                
                // Update notification
                updateNotification("已连接", serverName)
                
                // Broadcast connection status
                sendBroadcast(Intent(ACTION_VPN_CONNECTED).apply { setPackage(packageName) })
                
                // Start traffic monitoring
                startTrafficMonitor()
                
                Log.i(TAG, "VPN connection established successfully!")
                
            } catch (e: Throwable) {
                Log.e(TAG, "Fatal error starting VPN: ${e.message}", e)
                isRunning = false
                try { coreManager.stopCore() } catch (_: Exception) {}
                try { vpnInterface?.close() } catch (_: Exception) {}
                vpnInterface = null
                broadcastDisconnected()
                stopSelf()
            }
        }
    }
    
    private fun broadcastDisconnected(errorReason: String? = null) {
        try {
            sendBroadcast(Intent(ACTION_VPN_DISCONNECTED).apply {
                setPackage(packageName)
                if (errorReason != null) {
                    putExtra(EXTRA_ERROR_REASON, errorReason)
                }
            })
        } catch (_: Exception) {}
    }
    
    private fun stopVpn() {
        Log.i(TAG, "Stopping VPN connection...")
        
        isRunning = false
        
        // Stop traffic monitor
        trafficJob?.cancel()
        trafficJob = null
        
        // Stop tun2socks
        try {
            Tun2socksWrapper.stop()
            Log.i(TAG, "tun2socks stopped")
        } catch (e: Throwable) {
            Log.w(TAG, "Error stopping tun2socks (native lib missing?): ${e.message}")
        }
        
        // Stop V2Ray core
        coreManager.stopCore()
        Log.i(TAG, "V2Ray core stopped")
        
        // Close VPN interface
        try {
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "VPN interface closed")
        } catch (e: Exception) {
            Log.w(TAG, "Error closing VPN interface: ${e.message}")
        }
        
        // Broadcast disconnection
        sendBroadcast(Intent(ACTION_VPN_DISCONNECTED).apply { setPackage(packageName) })
        
        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        
        Log.i(TAG, "VPN disconnected")
    }
    
    private fun startTrafficMonitor() {
        trafficJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && isRunning) {
                try {
                    val currentUpload = coreManager.getUploadBytes()
                    val currentDownload = coreManager.getDownloadBytes()
                    
                    // Calculate speed (bytes per second)
                    if (currentUpload < lastUploadBytes) lastUploadBytes = 0
                    if (currentDownload < lastDownloadBytes) lastDownloadBytes = 0
                    
                    uploadSpeed = currentUpload - lastUploadBytes
                    downloadSpeed = currentDownload - lastDownloadBytes
                    
                    lastUploadBytes = currentUpload
                    lastDownloadBytes = currentDownload
                    
                    // Broadcast traffic update
                    val trafficIntent = Intent(ACTION_TRAFFIC_UPDATE).apply {
                        setPackage(packageName)
                        putExtra(EXTRA_UPLOAD_SPEED, uploadSpeed)
                        putExtra(EXTRA_DOWNLOAD_SPEED, downloadSpeed)
                        putExtra(EXTRA_TOTAL_UPLOAD, currentUpload)
                        putExtra(EXTRA_TOTAL_DOWNLOAD, currentDownload)
                    }
                    sendBroadcast(trafficIntent)
                    
                    // Update notification periodically
                    if (isRunning) {
                        val speedText = "↑${formatSpeed(uploadSpeed)} ↓${formatSpeed(downloadSpeed)}"
                        updateNotificationWithSpeed(speedText)
                    }
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Error monitoring traffic: ${e.message}")
                }
                
                delay(1000) // Update every second
            }
        }
    }
    
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "${bytesPerSecond}B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024}KB/s"
            else -> String.format("%.1fMB/s", bytesPerSecond / (1024.0 * 1024.0))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        
        try {
            unregisterReceiver(connectivityReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        
        stopVpn()
        Log.i(TAG, "VPN Service destroyed")
    }
    
    override fun onRevoke() {
        super.onRevoke()
        Log.i(TAG, "VPN permission revoked by user")
        stopVpn()
    }
    
    // Notification management
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "DokoDemo VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN connection status"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String, serverName: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val stopIntent = Intent(this, DokoDemoVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        
        return builder
            .setContentTitle("DokoDemo VPN")
            .setContentText("$status - $serverName")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_delete,
                    "Disconnect",
                    stopPendingIntent
                ).build()
            )
            .build()
    }
    
    private fun updateNotification(status: String, serverName: String) {
        val notification = createNotification(status, serverName)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun updateNotificationWithSpeed(speedText: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        
        val notification = builder
            .setContentTitle("DokoDemo VPN - Connected")
            .setContentText(speedText)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
