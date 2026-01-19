package com.dokodemo.ui.screens.serverlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.core.ServerPinger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerItem(
    val id: Long,
    val name: String,
    val countryCode: String,
    val protocol: String,
    val ping: Int? = null,
    val isConnected: Boolean = false
)

data class ServerListUiState(
    val servers: List<ServerItem> = emptyList(),
    val selectedServerId: Long? = null,
    val searchQuery: String = "",
    val isPinging: Boolean = false,
    val systemStatus: String = "SYSTEM_ONLINE",
    val version: String = "V.2.0.4.1",
    val currentUplink: String = "TOKYO_CORE_01"
)

@HiltViewModel
class ServerListViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val serverPinger: ServerPinger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerListUiState())
    val uiState: StateFlow<ServerListUiState> = _uiState.asStateFlow()

    init {
        loadServers()
    }

    private fun loadServers() {
        viewModelScope.launch {
            serverRepository.getAllServers().collect { servers ->
                // Convert DB entities to UI models
                val serverItems = servers.map { server ->
                    ServerItem(
                        id = server.id,
                        name = server.name,
                        countryCode = server.countryCode,
                        protocol = server.protocol.name,
                        ping = server.latency,
                        isConnected = server.isSelected
                    )
                }

                _uiState.update { 
                    it.copy(
                        servers = serverItems,
                        // Update current uplink name if a selected server exists
                        currentUplink = serverItems.find { item -> item.isConnected }?.name ?: "NO_CONNECTION"
                    ) 
                }
            }
        }
    }

    fun selectServer(serverId: Long) {
        viewModelScope.launch {
            serverRepository.selectServer(serverId)
            // UI update will happen via flow collection
        }
    }

    fun deleteServer(server: ServerItem) {
        viewModelScope.launch {
            serverRepository.deleteServerById(server.id)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun pingAllServers() {
        if (_uiState.value.isPinging) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }
            
            // val entities = serverRepository.getAllServers().first()

            // entities.forEach { entity ->
            //     launch(Dispatchers.IO) {
            //         val latency = serverPinger.ping(entity.address, entity.port)
            //         // Update in DB
            //         serverRepository.updateLatency(entity.id, latency?.toInt())
            //     }
            // }

            delay(2000) // Give some time for pings to update

            _uiState.update { it.copy(isPinging = false) }
        }
    }

    fun refreshServers() {
        pingAllServers()
    }

    val filteredServers: List<ServerItem>
        get() {
            val query = _uiState.value.searchQuery.lowercase()
            return if (query.isEmpty()) {
                _uiState.value.servers
            } else {
                _uiState.value.servers.filter {
                    it.name.lowercase().contains(query) ||
                    it.countryCode.lowercase().contains(query)
                }
            }
        }
}
