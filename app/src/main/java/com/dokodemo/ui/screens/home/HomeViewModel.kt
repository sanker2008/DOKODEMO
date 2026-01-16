package com.dokodemo.ui.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.ServerRepository
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
    val coreVersion: String = "---"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vpnController: VpnController,
    private val serverRepository: ServerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var trafficReceiver: BroadcastReceiver? = null
    private var vpnStateReceiver: BroadcastReceiver? = null
    
    init {
        loadSelectedServer()
        registerReceivers()
        updateCoreVersion()
        checkVpnState()
    }
    
    private fun loadSelectedServer() {
        viewModelScope.launch {
            // Try to get selected server from database
            serverRepository.getSelectedServer().collect { server ->
                if (server != null) {
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
                } else {
                    // Seed mock data if empty
                    serverRepository.seedMockData()
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
                        _uiState.update { state ->
                            state.copy(
                                isConnected = true,
                                isConnecting = false,
                                ipAddress = "PROTECTED"
                            )
                        }
                    }
                    DokoDemoVpnService.ACTION_VPN_DISCONNECTED -> {
                        _uiState.update { state ->
                            state.copy(
                                isConnected = false,
                                isConnecting = false,
                                ipAddress = "UNPROTECTED",
                                uploadSpeed = "0 KB/s",
                                downloadSpeed = "0 KB/s",
                                speedHistory = List(50) { 0f }
                            )
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
        
        context.registerReceiver(trafficReceiver, trafficFilter, Context.RECEIVER_NOT_EXPORTED)
        context.registerReceiver(vpnStateReceiver, vpnStateFilter, Context.RECEIVER_NOT_EXPORTED)
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
