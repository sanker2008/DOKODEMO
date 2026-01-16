package com.dokodemo.ui.screens.configeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigEditorUiState(
    val isEditing: Boolean = false,
    val serverId: Long? = null,
    
    // Form fields
    val name: String = "",
    val address: String = "",
    val port: String = "",
    val uuid: String = "",
    val protocol: Protocol = Protocol.VMESS,
    
    // TLS settings
    val useTls: Boolean = true,
    val allowInsecure: Boolean = false,
    
    // Status
    val isOnline: Boolean = false,
    val ping: String = "--ms",
    
    // Validation
    val addressError: String? = null,
    val portError: String? = null,
    val uuidError: String? = null,
    
    // Actions
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class ConfigEditorViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ConfigEditorUiState())
    val uiState: StateFlow<ConfigEditorUiState> = _uiState.asStateFlow()
    
    fun loadServer(serverId: Long) {
        viewModelScope.launch {
            serverRepository.getServerById(serverId).collect { server ->
                server?.let {
                    _uiState.update { state ->
                        state.copy(
                            isEditing = true,
                            serverId = server.id,
                            name = server.name,
                            address = server.address,
                            port = server.port.toString(),
                            uuid = server.uuid,
                            protocol = server.protocol,
                            useTls = server.useTls,
                            allowInsecure = server.allowInsecure,
                            isOnline = true,
                            ping = server.latency?.let { "${it}ms" } ?: "--ms"
                        )
                    }
                }
            }
        }
    }
    
    fun updateName(value: String) {
        _uiState.update { it.copy(name = value) }
    }
    
    fun updateAddress(value: String) {
        _uiState.update { it.copy(address = value, addressError = null) }
    }
    
    fun updatePort(value: String) {
        _uiState.update { it.copy(port = value, portError = null) }
    }
    
    fun updateUuid(value: String) {
        _uiState.update { it.copy(uuid = value, uuidError = null) }
    }
    
    fun updateProtocol(protocol: Protocol) {
        _uiState.update { it.copy(protocol = protocol) }
    }
    
    fun updateUseTls(value: Boolean) {
        _uiState.update { it.copy(useTls = value) }
    }
    
    fun updateAllowInsecure(value: Boolean) {
        _uiState.update { it.copy(allowInsecure = value) }
    }
    
    fun saveConfig(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Validate
        var hasError = false
        
        if (state.address.isBlank()) {
            _uiState.update { it.copy(addressError = "Address is required") }
            hasError = true
        }
        
        val portInt = state.port.toIntOrNull()
        if (portInt == null || portInt < 1 || portInt > 65535) {
            _uiState.update { it.copy(portError = "Invalid port (1-65535)") }
            hasError = true
        }
        
        if (state.uuid.isBlank() && state.protocol != Protocol.SHADOWSOCKS) {
            _uiState.update { it.copy(uuidError = "UUID is required") }
            hasError = true
        }
        
        if (hasError) return
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val serverProfile = ServerProfile(
                    id = state.serverId ?: 0,
                    name = state.name.ifBlank { "${state.address}:${state.port}" },
                    address = state.address,
                    port = portInt!!,
                    uuid = state.uuid,
                    protocol = state.protocol,
                    useTls = state.useTls,
                    allowInsecure = state.allowInsecure
                )
                
                if (state.isEditing && state.serverId != null) {
                    serverRepository.updateServer(serverProfile)
                } else {
                    serverRepository.addServer(serverProfile)
                }
                
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        saveError = e.message ?: "Failed to save"
                    ) 
                }
            }
        }
    }
}
