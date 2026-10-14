package com.dokodemo.ui.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.core.ConnectionConfiguration
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.ServerRepository
import com.dokodemo.data.repository.SubscriptionRepository
import com.dokodemo.service.DokoDemoVpnService
import com.dokodemo.service.VpnController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isApplyingConnectionChanges: Boolean = false,
    val currentServer: ServerProfile? = null,
    val currentServerName: String = "SELECT SERVER",
    val currentServerRegion: String = "--",
    val protocol: String = "---",
    val encryption: String = "---",
    val uploadSpeed: String = "0 KB/s",
    val downloadSpeed: String = "0 KB/s",
    val ping: String = "--ms",
    val ipAddress: String = "UNPROTECTED",
    val connectionError: String? = null,
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
    private val subscriptionSyncManager: com.dokodemo.core.SubscriptionSyncManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var trafficReceiver: BroadcastReceiver? = null
    private var vpnStateReceiver: BroadcastReceiver? = null
    private var connectionJob: Job? = null
    private var wantsConnection = vpnController.isConnected()
    private var isStopping = false
    private var requestedConfiguration = vpnController.lastRequestedConnection
    private var configurationLoaded = false
    
    init {
        registerReceivers()
        updateCoreVersion()
        checkVpnState()
        loadSelectedServer()
        checkAutoUpdateSubscriptions()
    }
    
    private fun checkAutoUpdateSubscriptions() {
        viewModelScope.launch {
            val autoUpdate = appPreferences.autoUpdateSubscription.first()
            if (autoUpdate) {
                try {
                    val subs = subscriptionRepository.getAllSubscriptions().first()
                    for (sub in subs) {
                        if (sub.isActive) {
                            subscriptionSyncManager.refresh(sub.id)
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
            _uiState.update { it.copy(isPinging = true, toastMessage = context.getString(com.dokodemo.R.string.pinging)) }
            try {
                val latency = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    serverPinger.ping(server.address, server.port)
                }
                serverRepository.updateLatency(server.id, latency?.toInt())
                if (latency != null) {
                    _uiState.update { it.copy(ping = "${latency}ms", toastMessage = context.getString(com.dokodemo.R.string.review_ping_ok, latency)) }
                } else {
                    _uiState.update { it.copy(ping = "--ms", toastMessage = context.getString(com.dokodemo.R.string.review_ping_failed)) }
                }
            } catch (e: Exception) {
                serverRepository.updateLatency(server.id, null)
                _uiState.update { it.copy(ping = "--ms", toastMessage = context.getString(com.dokodemo.R.string.review_ping_failed)) }
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
            // Read the node and mode together so one configuration produces one service request.
            combine(serverRepository.getSelectedServer(), appPreferences.routingMode) { server, mode ->
                server to mode
            }.collect { (server, mode) ->
                _uiState.update { state ->
                    state.copy(
                        routingMode = mode,
                        currentServer = server,
                        currentServerName = server?.name?.replace("_", " ") ?: "",
                        currentServerRegion = server?.countryCode ?: "",
                        protocol = server?.protocol?.name ?: "",
                        encryption = if (server?.useTls == true) "TLS" else "NONE",
                        ping = server?.latency?.let { "${it}ms" } ?: "--ms"
                    )
                }
                configurationLoaded = true
                reconcileConnection()
            }
        }
    }

    private fun reconcileConnection() {
        if (!wantsConnection || !configurationLoaded) return
        val state = _uiState.value
        val server = state.currentServer
        if (server == null) {
            // A deleted selection must not leave an unmanageable connection running.
            disconnect()
        } else if (ConnectionConfiguration.from(server, state.routingMode) != requestedConfiguration) {
            connect(applyingChanges = true)
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
                        if (isStopping || connectionJob?.isActive == true) return
                        wantsConnection = true
                        requestedConfiguration = vpnController.lastRequestedConnection
                        _uiState.update { state ->
                            state.copy(
                                isConnected = true,
                                connectionError = null,
                                isConnecting = false,
                                isApplyingConnectionChanges = false,
                                ipAddress = "PROTECTED"
                            )
                        }
                        // Also reconcile a service connection that completed during VM recreation.
                        reconcileConnection()
                    }
                    DokoDemoVpnService.ACTION_VPN_DISCONNECTED -> {
                        // A previous service request may finish while its replacement is preparing.
                        if (connectionJob?.isActive == true) return
                        connectionJob?.cancel()
                        wantsConnection = false
                        isStopping = false
                        requestedConfiguration = null
                        val errorReason = intent?.getStringExtra(DokoDemoVpnService.EXTRA_ERROR_REASON)
                        _uiState.update { it.copy(isConnected = false, isConnecting = false,
                            isApplyingConnectionChanges = false, ipAddress = "UNPROTECTED",
                            needsVpnPermission = false, vpnPermissionIntent = null,
                            uploadSpeed = "0 KB/s", downloadSpeed = "0 KB/s", speedHistory = List(50) { 0f },
                            connectionError = errorReason) }

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
        val normalizedSpeed = downloadBytes.toFloat().coerceAtLeast(0f)
        
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
        if (wantsConnection || _uiState.value.isConnected || _uiState.value.isConnecting) {
            disconnect()
        } else {
            wantsConnection = true
            connect()
        }
    }

    private fun connect(applyingChanges: Boolean = false) {
        val state = _uiState.value
        val server = state.currentServer ?: return
        connectionJob?.cancel()
        isStopping = false

        if (!vpnController.isVpnPermissionGranted()) {
            if (_uiState.value.needsVpnPermission) return
            _uiState.update {
                it.copy(needsVpnPermission = true, vpnPermissionIntent = vpnController.getVpnPermissionIntent())
            }
            return
        }

        // Claim before launching: latency/name emissions during config preparation cannot duplicate it.
        requestedConfiguration = ConnectionConfiguration.from(server, state.routingMode)
        _uiState.update {
            it.copy(isConnected = false, isConnecting = true, connectionError = null,
                isApplyingConnectionChanges = applyingChanges)
        }
        connectionJob = viewModelScope.launch {
            try {
                vpnController.connect(server, state.routingMode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Validation/config preparation can fail before START, leaving the old VPN alive.
                val stillConnected = vpnController.isConnected()
                wantsConnection = stillConnected
                requestedConfiguration = if (stillConnected) vpnController.lastRequestedConnection else null
                val reason = e.message ?: context.getString(com.dokodemo.R.string.validation_config)
                val error = if (stillConnected) {
                    context.getString(com.dokodemo.R.string.connection_changes_failed_keep_previous, reason)
                } else reason
                _uiState.update {
                    it.copy(isConnected = stillConnected, isConnecting = false,
                        isApplyingConnectionChanges = false, connectionError = error)
                }
            }
        }
    }

    private fun disconnect() {
        // Cancel config preparation before sending STOP, otherwise a suspended START can revive VPN.
        wantsConnection = false
        isStopping = true
        requestedConfiguration = null
        connectionJob?.cancel()
        _uiState.update {
            it.copy(isConnecting = true, isApplyingConnectionChanges = false, connectionError = null,
                needsVpnPermission = false, vpnPermissionIntent = null)
        }
        vpnController.disconnect()
    }

    fun onVpnPermissionResult(granted: Boolean) {
        val wasWaiting = _uiState.value.needsVpnPermission
        _uiState.update { it.copy(needsVpnPermission = false, vpnPermissionIntent = null) }
        if (!wasWaiting || !wantsConnection) return
        if (granted) {
            connect()
        } else {
            wantsConnection = false
            requestedConfiguration = null
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
