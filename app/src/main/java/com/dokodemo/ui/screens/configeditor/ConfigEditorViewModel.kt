package com.dokodemo.ui.screens.configeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.ui.screens.serverlist.ServerRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

data class ConfigEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val address: String = "",
    val port: String = "",
    val uuid: String = "",
    val protocol: Protocol = Protocol.VMESS,
    val security: String = "auto",
    val network: String = "tcp",
    val wsPath: String = "",
    val wsHost: String = "",
    val useTls: Boolean = false,
    val allowInsecure: Boolean = false,
    val isOnline: Boolean = false,
    val ping: String = "N/A",
    val addressError: String? = null,
    val portError: String? = null,
    val uuidError: String? = null
)

@HiltViewModel
class ConfigEditorViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigEditorUiState())
    val uiState: StateFlow<ConfigEditorUiState> = _uiState.asStateFlow()

    fun loadServer(serverId: Long) {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId)
            if (server != null) {
                _uiState.update {
                    it.copy(
                        name = server.name,
                        address = server.address,
                        port = server.port.toString(),
                        uuid = server.uuid,
                        protocol = server.protocol,
                        security = server.encryption,
                        network = server.network,
                        wsPath = server.wsPath,
                        wsHost = server.wsHost,
                        useTls = server.useTls,
                        allowInsecure = server.allowInsecure,
                    )
                }
            }
        }
    }

    fun parseUri(uri: String) {
        viewModelScope.launch {
            val decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString())
            if (decodedUri.startsWith("vmess://")) {
                val vmessData = decodedUri.substring("vmess://".length)
                try {
                    val json = String(android.util.Base64.decode(vmessData, android.util.Base64.DEFAULT))
                    val vmessProfile = Gson().fromJson(json, VmessProfile::class.java)
                    _uiState.update {
                        it.copy(
                            name = vmessProfile.ps,
                            address = vmessProfile.add,
                            port = vmessProfile.port.toString(),
                            uuid = vmessProfile.id,
                            protocol = Protocol.VMESS,
                            security = vmessProfile.scy,
                            network = vmessProfile.net,
                            wsPath = vmessProfile.path,
                            wsHost = vmessProfile.host,
                            useTls = vmessProfile.tls == "tls",
                        )
                    }
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateAddress(address: String) {
        _uiState.update { it.copy(address = address) }
    }

    fun updatePort(port: String) {
        _uiState.update { it.copy(port = port) }
    }

    fun updateUuid(uuid: String) {
        _uiState.update { it.copy(uuid = uuid) }
    }

    fun updateProtocol(protocol: Protocol) {
        _uiState.update { it.copy(protocol = protocol) }
    }

    fun updateAllowInsecure(allowInsecure: Boolean) {
        _uiState.update { it.copy(allowInsecure = allowInsecure) }
    }

    fun updateUseTls(useTls: Boolean) {
        _uiState.update { it.copy(useTls = useTls) }
    }

    fun saveConfig(onNavigateBack: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val serverProfile = ServerProfile(
                id = currentState.id ?: 0,
                name = currentState.name,
                address = currentState.address,
                port = currentState.port.toIntOrNull() ?: 0,
                uuid = currentState.uuid,
                protocol = currentState.protocol,
                encryption = currentState.security,
                network = currentState.network,
                wsPath = currentState.wsPath,
                wsHost = currentState.wsHost,
                useTls = currentState.useTls,
                allowInsecure = currentState.allowInsecure,
            )
            serverRepository.insert(serverProfile)
            onNavigateBack()
        }
    }
}

data class VmessProfile(
    val v: String,
    val ps: String,
    val add: String,
    val port: Int,
    val id: String,
    val aid: Int,
    val scy: String,
    val net: String,
    val type: String,
    val host: String,
    val path: String,
    val tls: String,
    val sni: String
)
