package com.dokodemo.ui.screens.serverlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

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
class ServerListViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(ServerListUiState())
    val uiState: StateFlow<ServerListUiState> = _uiState.asStateFlow()
    
    init {
        loadMockServers()
    }
    
    private fun loadMockServers() {
        val mockServers = listOf(
            ServerItem(1, "TOKYO_CORE_01", "JP", "WIREGUARD", 12, true),
            ServerItem(2, "NEW_YORK_EAST", "US", "OPENVPN", 45),
            ServerItem(3, "FRANKFURT_HUB", "DE", "WIREGUARD", 102),
            ServerItem(4, "SINGAPORE_DIRECT", "SG", "WIREGUARD", 18),
            ServerItem(5, "LONDON_BRIDGE", "UK", "IKEV2", 88),
            ServerItem(6, "SAO_PAULO_S1", "BR", "WIREGUARD", 156),
            ServerItem(7, "TORONTO_NORTH", "CA", "OPENVPN", 52),
            ServerItem(8, "SYDNEY_PRIME", "AU", "WIREGUARD", 180),
            ServerItem(9, "AMSTERDAM_VPN", "NL", "WIREGUARD", 95),
            ServerItem(10, "HONG_KONG_PRO", "HK", "VLESS", 28)
        )
        
        _uiState.update { 
            it.copy(
                servers = mockServers,
                selectedServerId = 1
            ) 
        }
    }
    
    fun selectServer(serverId: Long) {
        _uiState.update { state ->
            state.copy(
                selectedServerId = serverId,
                servers = state.servers.map { server ->
                    server.copy(isConnected = server.id == serverId)
                },
                currentUplink = state.servers.find { it.id == serverId }?.name ?: state.currentUplink
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun pingAllServers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }
            
            // Simulate pinging each server
            val updatedServers = _uiState.value.servers.map { server ->
                delay(100) // Simulate network delay
                server.copy(ping = Random.nextInt(10, 200))
            }
            
            _uiState.update { 
                it.copy(
                    servers = updatedServers,
                    isPinging = false
                ) 
            }
        }
    }
    
    fun refreshServers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPinging = true) }
            delay(500)
            loadMockServers()
            _uiState.update { it.copy(isPinging = false) }
        }
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
