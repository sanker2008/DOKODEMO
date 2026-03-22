package com.dokodemo.ui.screens.configeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Group
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.GroupRepository
import com.dokodemo.data.repository.ServerRepository
import com.dokodemo.core.ShareLinkParser
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

data class ConfigEditorUiState(
    val id: Long? = null,
    val name: String = "",
    val address: String = "",
    val port: String = "",
    val uuid: String = "",
    val password: String = "",
    val protocol: Protocol = Protocol.VMESS,
    
    // Transport & Security
    val network: String = "tcp",
    val security: String = "auto", // VMess encryption or none
    val useTls: Boolean = false,
    val allowInsecure: Boolean = false,
    val serverName: String = "",
    
    // WebSocket / gRPC
    val wsHost: String = "",
    val wsPath: String = "",
    
    // KCP
    val kcpHeader: String = "none",
    val kcpSeed: String = "",
    
    // Shadowsocks
    val ssMethod: String = "aes-256-gcm",
    
    // Grouping
    val groupId: Long? = null,
    val availableGroups: List<Group> = emptyList(),

    // UI State
    val addressError: String? = null,
    val portError: String? = null,
    val uuidError: String? = null
)

@HiltViewModel
class ConfigEditorViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val groupRepository: GroupRepository,
    private val shareLinkParser: ShareLinkParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigEditorUiState())
    val uiState: StateFlow<ConfigEditorUiState> = _uiState.asStateFlow()

    init {
        // 加载可用的分组
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(availableGroups = groups) }
            }
        }
    }

    fun loadServer(serverId: Long) {
        viewModelScope.launch {
            val server = serverRepository.getServerById(serverId).firstOrNull()
            if (server != null) {
                _uiState.update {
                    it.copy(
                        id = server.id,
                        name = server.name,
                        address = server.address,
                        port = if (server.port == 0) "" else server.port.toString(),
                        uuid = server.uuid,
                        password = server.password,
                        protocol = server.protocol,
                        security = server.encryption,
                        network = server.network,
                        wsPath = server.wsPath,
                        wsHost = server.wsHost,
                        useTls = server.useTls,
                        allowInsecure = server.allowInsecure,
                        serverName = server.serverName,
                        kcpHeader = server.kcpHeader,
                        kcpSeed = server.kcpSeed,
                        ssMethod = server.ssMethod,
                        groupId = server.groupId
                    )
                }
            }
        }
    }

    fun parseUri(uri: String) {
        viewModelScope.launch {
            try {
                val decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString())
                val profile = shareLinkParser.parse(decodedUri)
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            name = profile.name,
                            address = profile.address,
                            port = if (profile.port == 0) "" else profile.port.toString(),
                            uuid = profile.uuid,
                            protocol = profile.protocol,
                            security = profile.encryption,
                            network = profile.network,
                            wsPath = profile.wsPath,
                            wsHost = profile.wsHost,
                            useTls = profile.useTls,
                            serverName = profile.serverName
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions silently for now
            }
        }
    }

    // ─── 字段更新 ────────────────────────────────────────────────────────────
    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateAddress(address: String) = _uiState.update { it.copy(address = address, addressError = null) }
    fun updatePort(port: String) = _uiState.update { it.copy(port = port, portError = null) }
    fun updateUuid(uuid: String) = _uiState.update { it.copy(uuid = uuid, uuidError = null) }
    fun updatePassword(password: String) = _uiState.update { it.copy(password = password) }
    fun updateProtocol(protocol: Protocol) = _uiState.update { it.copy(protocol = protocol) }

    fun updateNetwork(network: String) = _uiState.update { it.copy(network = network) }
    fun updateWsPath(wsPath: String) = _uiState.update { it.copy(wsPath = wsPath) }
    fun updateWsHost(wsHost: String) = _uiState.update { it.copy(wsHost = wsHost) }
    
    fun updateSecurity(security: String) = _uiState.update { it.copy(security = security) }
    fun updateUseTls(useTls: Boolean) = _uiState.update { it.copy(useTls = useTls) }
    fun updateAllowInsecure(allowInsecure: Boolean) = _uiState.update { it.copy(allowInsecure = allowInsecure) }
    fun updateServerName(serverName: String) = _uiState.update { it.copy(serverName = serverName) }
    
    fun updateKcpHeader(kcpHeader: String) = _uiState.update { it.copy(kcpHeader = kcpHeader) }
    fun updateKcpSeed(kcpSeed: String) = _uiState.update { it.copy(kcpSeed = kcpSeed) }
    
    fun updateSsMethod(ssMethod: String) = _uiState.update { it.copy(ssMethod = ssMethod) }
    
    fun updateGroupId(groupId: Long?) = _uiState.update { it.copy(groupId = groupId) }

    // ─── 保存 ───────────────────────────────────────────────────────────────
    fun saveConfig(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            
            // 基础校验
            if (state.address.isBlank()) {
                _uiState.update { it.copy(addressError = "地址不能为空") }
                return@launch
            }
            if (state.port.toIntOrNull() == null) {
                _uiState.update { it.copy(portError = "无效的端口") }
                return@launch
            }

            val serverProfile = ServerProfile(
                id = state.id ?: 0,
                name = state.name.ifBlank { state.address },
                address = state.address,
                port = state.port.toInt(),
                uuid = state.uuid,
                password = state.password,
                protocol = state.protocol,
                encryption = state.security,
                network = state.network,
                wsPath = state.wsPath,
                wsHost = state.wsHost,
                useTls = state.useTls,
                allowInsecure = state.allowInsecure,
                serverName = state.serverName,
                kcpHeader = state.kcpHeader,
                kcpSeed = state.kcpSeed,
                ssMethod = state.ssMethod,
                groupId = state.groupId
            )

            if (state.id != null && state.id > 0) {
                serverRepository.updateServer(serverProfile)
            } else {
                serverRepository.addServer(serverProfile)
            }
            onSuccess()
        }
    }
}

data class VmessProfile(
    val v: String?,
    val ps: String?,
    val add: String?,
    val port: Int?,
    val id: String?,
    val aid: Int?,
    val scy: String?,
    val net: String?,
    val type: String?,
    val host: String?,
    val path: String?,
    val tls: String?,
    val sni: String?
)
