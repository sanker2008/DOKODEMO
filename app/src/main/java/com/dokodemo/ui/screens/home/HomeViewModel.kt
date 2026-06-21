package com.dokodemo.ui.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.ServerRepository
import com.dokodemo.data.repository.SubscriptionRepository
import com.dokodemo.data.repository.GroupRepository
import com.dokodemo.core.SubscriptionFetcher
import com.dokodemo.service.DokoDemoVpnService
import com.dokodemo.service.VpnController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class HomeUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val currentServer: ServerProfile? = null,
    val currentServerName: String = "SELECT SERVER",
    val currentServerRegion: String = "--",
    val protocol: String = "---",
    val encryption: String = "---",
    val uploadSpeed: String = "0 KB/s",
    val downloadSpeed: String = "0 KB/s",
    val ping: String = "--ms",
    val ipAddress: String = "UNPROTECTED",
    val speedHistory: List<Float> = List(50) { 0f },
    
    // VPN Permission
    val needsVpnPermission: Boolean = false,
    val vpnPermissionIntent: Intent? = null,
    
    // Core info
    val coreVersion: String = "---",
    
    // Routing Mode
    val routingMode: com.dokodemo.data.preferences.RoutingMode = com.dokodemo.data.preferences.RoutingMode.GLOBAL,
    
    // Ping
    val isPinging: Boolean = false,
    val toastMessage: String? = null,
    
    // Empty state
    val hasNoServers: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vpnController: VpnController,
    private val serverRepository: ServerRepository,
    private val appPreferences: com.dokodemo.data.preferences.AppPreferences,
    private val serverPinger: com.dokodemo.core.ServerPinger,
    private val subscriptionRepository: SubscriptionRepository,
    private val groupRepository: GroupRepository,
    private val subscriptionFetcher: SubscriptionFetcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var trafficReceiver: BroadcastReceiver? = null
    private var vpnStateReceiver: BroadcastReceiver? = null
    private var fallbackCount = 0
    private val MAX_FALLBACK = 3
    
    init {
        loadSelectedServer()
        registerReceivers()
        updateCoreVersion()
        checkVpnState()
        observeRoutingMode()
        checkAutoUpdateSubscriptions()
    }
    
    private fun observeRoutingMode() {
        viewModelScope.launch {
            appPreferences.routingMode.collect { mode ->
                _uiState.update { it.copy(routingMode = mode) }
            }
        }
    }
    
    private fun checkAutoUpdateSubscriptions() {
        viewModelScope.launch {
            val autoUpdate = appPreferences.autoUpdateSubscription.first()
            if (autoUpdate) {
                try {
                    val subs = subscriptionRepository.getAllSubscriptions().first()
                    for (sub in subs) {
                        if (sub.isActive) {
                            var group = groupRepository.getGroupBySubscriptionId(sub.id)
                            if (group == null) {
                                val newGroupId = groupRepository.insertGroup(com.dokodemo.data.model.Group(name = sub.name, subscriptionId = sub.id))
                                group = groupRepository.getGroupById(newGroupId)
                            }
                            val result = subscriptionFetcher.fetchAndParse(sub.url, group?.id)
                            result.onSuccess { (nodes, info) ->
                                serverRepository.replaceServersForSubscription(sub.id, nodes)
                                subscriptionRepository.updateSyncStatus(
                                    id = sub.id, 
                                    timestamp = System.currentTimeMillis(), 
                                    count = nodes.size,
                                    upload = info?.upload ?: sub.upload,
                                    download = info?.download ?: sub.download,
                                    total = info?.total ?: sub.total,
                                    expire = info?.expire ?: sub.expire
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore auto-update errors on background
                }
            }
        }
    }
    
    fun setRoutingMode(mode: com.dokodemo.data.preferences.RoutingMode) {
        viewModelScope.launch {
            appPreferences.setRoutingMode(mode)
        }
    }
    
    fun pingCurrentServer() {
        val server = _uiState.value.currentServer ?: return
        if (_uiState.value.isPinging) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true, toastMessage = "正在测试...") }
            try {
                val latency = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    serverPinger.ping(server.address, server.port)
                }
                serverRepository.updateLatency(server.id, latency?.toInt())
                if (latency != null) {
                    _uiState.update { it.copy(ping = "${latency}ms", toastMessage = "测试成功：${latency}ms") }
                } else {
                    _uiState.update { it.copy(ping = "--ms", toastMessage = "测试失败：节点不可达") }
                }
            } catch (e: Exception) {
                serverRepository.updateLatency(server.id, null)
                _uiState.update { it.copy(ping = "--ms", toastMessage = "测试异常: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isPinging = false) }
            }
        }
    }
    
    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun loadSelectedServer() {
        viewModelScope.launch {
            serverRepository.getAllServers().collect { servers ->
                _uiState.update { it.copy(hasNoServers = servers.isEmpty()) }
            }
        }
        viewModelScope.launch {
            // Try to get selected server from database
            serverRepository.getSelectedServer().collect { server ->
                if (server != null) {
                    val currentState = _uiState.value
                    // Check if we are connected and the server changed
                    val needsReconnect = currentState.isConnected && 
                            currentState.currentServer != null && 
                            currentState.currentServer.id != server.id
                    
                    _uiState.update { state ->
                        state.copy(
                            currentServer = server,
                            currentServerName = server.name.replace("_", " "),
                            currentServerRegion = server.countryCode,
                            protocol = server.protocol.name,
                            encryption = if (server.useTls) "TLS" else "NONE",
                            ping = server.latency?.let { "${it}ms" } ?: "--ms"
                        )
                    }
                    
                    if (needsReconnect) {
                        connect()
                    }
                } else {
                    // Do nothing if empty, let user add server
                }
            }
        }
    }
    
    private fun registerReceivers() {
        // Traffic update receiver
        trafficReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    val uploadSpeed = it.getLongExtra(DokoDemoVpnService.EXTRA_UPLOAD_SPEED, 0)
                    val downloadSpeed = it.getLongExtra(DokoDemoVpnService.EXTRA_DOWNLOAD_SPEED, 0)
                    
                    updateTrafficStats(uploadSpeed, downloadSpeed)
                }
            }
        }
        
        // VPN state receiver
        vpnStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    DokoDemoVpnService.ACTION_VPN_CONNECTED -> {
                        fallbackCount = 0 // Reset on success
                        _uiState.update { state ->
                            state.copy(
                                isConnected = true,
                                isConnecting = false,
                                ipAddress = "PROTECTED"
                            )
                        }
                    }
                    DokoDemoVpnService.ACTION_VPN_DISCONNECTED -> {
                        val errorReason = intent?.getStringExtra(DokoDemoVpnService.EXTRA_ERROR_REASON)
                        if (errorReason != null && fallbackCount < MAX_FALLBACK) {
                            fallbackCount++
                            tryFallbackNextNode()
                        } else {
                            fallbackCount = 0
                            _uiState.update { state ->
                                state.copy(
                                    isConnected = false,
                                    isConnecting = false,
                                    ipAddress = "UNPROTECTED",
                                    uploadSpeed = "0 KB/s",
                                    downloadSpeed = "0 KB/s",
                                    speedHistory = List(50) { 0f },
                                    toastMessage = errorReason?.let { "连接失败：$it" }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Register receivers
        val trafficFilter = IntentFilter(DokoDemoVpnService.ACTION_TRAFFIC_UPDATE)
        val vpnStateFilter = IntentFilter().apply {
            addAction(DokoDemoVpnService.ACTION_VPN_CONNECTED)
            addAction(DokoDemoVpnService.ACTION_VPN_DISCONNECTED)
        }
        
        trafficReceiver?.let { receiver ->
            ContextCompat.registerReceiver(
                context,
                receiver,
                trafficFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
        
        vpnStateReceiver?.let { receiver ->
            ContextCompat.registerReceiver(
                context,
                receiver,
                vpnStateFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }
    
    private fun updateTrafficStats(uploadBytes: Long, downloadBytes: Long) {
        val uploadStr = formatSpeed(uploadBytes)
        val downloadStr = formatSpeed(downloadBytes)
        
        // Update speed history for graph (normalized 0-1)
        val normalizedSpeed = (downloadBytes.toFloat() / (1024 * 1024)).coerceIn(0f, 1f)
        
        _uiState.update { state ->
            val newHistory = state.speedHistory.toMutableList()
            newHistory.removeAt(0)
            newHistory.add(normalizedSpeed)
            
            state.copy(
                uploadSpeed = uploadStr,
                downloadSpeed = downloadStr,
                speedHistory = newHistory
            )
        }
    }
    
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond < 1024 -> "$bytesPerSecond B/s"
            bytesPerSecond < 1024 * 1024 -> "${bytesPerSecond / 1024} KB/s"
            else -> String.format("%.1f MB/s", bytesPerSecond / (1024.0 * 1024.0))
        }
    }
    
    private fun updateCoreVersion() {
        _uiState.update { it.copy(coreVersion = vpnController.getCoreVersion()) }
    }
    
    private fun checkVpnState() {
        val isConnected = vpnController.isConnected()
        _uiState.update { state ->
            state.copy(
                isConnected = isConnected,
                ipAddress = if (isConnected) "PROTECTED" else "UNPROTECTED"
            )
        }
    }
    
    fun toggleConnection() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.isConnected) {
                // Disconnect
                disconnect()
            } else {
                // Connect
                connect()
            }
        }
    }
    
    private suspend fun connect() {
        val server = _uiState.value.currentServer
        if (server == null) {
            // No server selected
            return
        }
        
        // Check VPN permission
        if (!vpnController.isVpnPermissionGranted()) {
            _uiState.update { state ->
                state.copy(
                    needsVpnPermission = true,
                    vpnPermissionIntent = vpnController.getVpnPermissionIntent()
                )
            }
            return
        }
        
        // Start connecting
        _uiState.update { it.copy(isConnecting = true) }
        
        // Start VPN
        vpnController.connect(server)
    }
    
    private fun disconnect() {
        _uiState.update { it.copy(isConnecting = true) }
        vpnController.disconnect()
    }
    
    fun onVpnPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(needsVpnPermission = false, vpnPermissionIntent = null) }
        
        if (granted) {
            viewModelScope.launch {
                connect()
            }
        }
    }
    
    fun selectServer(serverId: Long) {
        viewModelScope.launch {
            serverRepository.selectServer(serverId)
        }
    }
    
    private fun tryFallbackNextNode() {
        viewModelScope.launch {
            val servers = serverRepository.getAllServers().first()
            if (servers.isEmpty()) return@launch
            
            val current = _uiState.value.currentServer
            if (current != null) {
                val currentIndex = servers.indexOfFirst { it.id == current.id }
                val nextServer = if (currentIndex != -1 && currentIndex + 1 < servers.size) {
                    servers[currentIndex + 1]
                } else {
                    servers.first()
                }
                
                _uiState.update { it.copy(toastMessage = "节点异常，正在尝试切换到: ${nextServer.name}") }
                
                // Select and connect
                serverRepository.selectServer(nextServer.id)
                _uiState.update { it.copy(currentServer = nextServer) }
                connect()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        
        try {
            trafficReceiver?.let { context.unregisterReceiver(it) }
            vpnStateReceiver?.let { context.unregisterReceiver(it) }
        } catch (e: Exception) {
            // Receiver not registered
        }
    }
}
