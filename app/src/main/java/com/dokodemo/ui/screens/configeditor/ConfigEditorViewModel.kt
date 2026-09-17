package com.dokodemo.ui.screens.configeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.model.Group
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.repository.GroupRepository
import com.dokodemo.data.repository.ServerRepository
import com.dokodemo.core.ShareLinkParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigEditorUiState(
    val id: Long? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val address: String = "",
    val port: String = "",
    val uuid: String = "",
    val password: String = "",
    val protocol: Protocol = Protocol.VMESS,
    
    // Transport & Security
    val network: String = "tcp",
    val security: String = "auto", // VMess encryption or none
    val useReality: Boolean = false,
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val realitySpiderX: String = "",
    val fingerprint: String = "chrome",
    val flow: String = "",
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
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val shareLinkParser: ShareLinkParser
) : ViewModel() {

    private var loadedSource: String? = null
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
        if (loadedSource == "id:$serverId") return
        loadedSource = "id:$serverId"
        _uiState.update { it.copy(id = serverId, isLoading = true) }
        viewModelScope.launch {
            try {
            val server = serverRepository.getServerById(serverId).firstOrNull()
            if (server != null) {
                _uiState.update {
                    it.copy(
                        id = server.id,
                        name = server.name,
                        address = server.address,
                        port = if (server.port == 0) "" else server.port.toString(),
                        uuid = server.uuid,
                        password = server.password.ifEmpty { if (server.protocol == Protocol.TROJAN) server.uuid else "" },
                        protocol = server.protocol,
                        security = server.encryption,
                        network = server.network,
                        wsPath = server.wsPath,
                        wsHost = server.wsHost,
                        useReality = server.useReality,
                        realityPublicKey = server.realityPublicKey,
                        realityShortId = server.realityShortId,
                        realitySpiderX = server.realitySpiderX,
                        fingerprint = server.fingerprint,
                        flow = server.flow,
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
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                _uiState.update { it.copy(addressError = context.getString(com.dokodemo.R.string.save_failed)) }
            } finally { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun parseUri(uri: String) {
        if (loadedSource == uri) return
        loadedSource = uri
        viewModelScope.launch {
            try {
                // Navigation arguments are already decoded, and raw scanned URIs shouldn't be fully decoded here
                // as it breaks base64 (+ replaced with space).
                val profile = shareLinkParser.parse(uri.trim())
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            name = profile.name,
                            address = profile.address,
                            port = if (profile.port == 0) "" else profile.port.toString(),
                            uuid = profile.uuid,
                            password = profile.password,
                            protocol = profile.protocol,
                            security = profile.encryption,
                            network = profile.network,
                            wsPath = profile.wsPath,
                            wsHost = profile.wsHost,
                            useReality = profile.useReality,
                            realityPublicKey = profile.realityPublicKey,
                            realityShortId = profile.realityShortId,
                            realitySpiderX = profile.realitySpiderX,
                            fingerprint = profile.fingerprint,
                            flow = profile.flow,
                            useTls = profile.useTls,
                            serverName = profile.serverName,
                            kcpHeader = profile.kcpHeader,
                            kcpSeed = profile.kcpSeed,
                            ssMethod = profile.ssMethod
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(addressError = context.getString(com.dokodemo.R.string.import_invalid))
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
    fun updateUseReality(useReality: Boolean) = _uiState.update { it.copy(useReality = useReality) }
    fun updateRealityPublicKey(realityPublicKey: String) = _uiState.update { it.copy(realityPublicKey = realityPublicKey) }
    fun updateRealityShortId(realityShortId: String) = _uiState.update { it.copy(realityShortId = realityShortId) }
    fun updateRealitySpiderX(realitySpiderX: String) = _uiState.update { it.copy(realitySpiderX = realitySpiderX) }
    fun updateFingerprint(fingerprint: String) = _uiState.update { it.copy(fingerprint = fingerprint) }
    fun updateFlow(flow: String) = _uiState.update { it.copy(flow = flow) }
    fun updateAllowInsecure(allowInsecure: Boolean) = _uiState.update { it.copy(allowInsecure = allowInsecure) }
    fun updateServerName(serverName: String) = _uiState.update { it.copy(serverName = serverName) }
    
    fun updateKcpHeader(kcpHeader: String) = _uiState.update { it.copy(kcpHeader = kcpHeader) }
    fun updateKcpSeed(kcpSeed: String) = _uiState.update { it.copy(kcpSeed = kcpSeed) }
    
    fun updateSsMethod(ssMethod: String) = _uiState.update { it.copy(ssMethod = ssMethod) }
    
    fun updateGroupId(groupId: Long?) = _uiState.update { it.copy(groupId = groupId) }

    // ─── 保存 ───────────────────────────────────────────────────────────────
    fun saveConfig(onSuccess: () -> Unit) {
        if (_uiState.value.isSaving || _uiState.value.isLoading) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
            val state = _uiState.value
            
            // 基础校验
            if (state.address.isBlank()) {
                _uiState.update { it.copy(addressError = context.getString(com.dokodemo.R.string.validation_address)) }
                return@launch
            }
            if (state.port.toIntOrNull() !in 1..65535) {
                _uiState.update { it.copy(portError = context.getString(com.dokodemo.R.string.validation_port)) }
                return@launch
            }

            val original = state.id?.let { serverRepository.getServerById(it).firstOrNull() }
            if (state.id != null && original == null) {
                _uiState.update { it.copy(addressError = context.getString(com.dokodemo.R.string.node_missing)) }
                return@launch
            }
            val serverProfile = (original ?: ServerProfile(name = "", address = "", port = 443)).copy(
                id = state.id ?: 0,
                name = state.name.ifBlank { state.address },
                address = state.address.trim(),
                port = state.port.toInt(),
                uuid = state.uuid.trim(),
                password = state.password,
                protocol = state.protocol,
                encryption = if (state.protocol == Protocol.SHADOWSOCKS) state.ssMethod else if (state.protocol == Protocol.VLESS) "none" else state.security,
                updatedAt = System.currentTimeMillis(),
                network = state.network,
                wsPath = state.wsPath,
                wsHost = state.wsHost,
                useReality = state.useReality,
                realityPublicKey = state.realityPublicKey,
                realityShortId = state.realityShortId,
                realitySpiderX = state.realitySpiderX,
                fingerprint = state.fingerprint,
                flow = state.flow,
                useTls = state.useTls,
                allowInsecure = state.allowInsecure,
                serverName = state.serverName,
                kcpHeader = state.kcpHeader,
                kcpSeed = state.kcpSeed,
                ssMethod = state.ssMethod,
                groupId = state.groupId
            )

            val validation = com.dokodemo.core.ProfileValidator.validate(serverProfile)
            if (validation != null) {
                val error = context.getString(when (validation) {
                    com.dokodemo.core.ProfileValidator.Error.ADDRESS -> com.dokodemo.R.string.validation_address
                    com.dokodemo.core.ProfileValidator.Error.PORT -> com.dokodemo.R.string.validation_port
                    com.dokodemo.core.ProfileValidator.Error.CREDENTIAL -> com.dokodemo.R.string.validation_credential
                    com.dokodemo.core.ProfileValidator.Error.REALITY -> com.dokodemo.R.string.validation_reality
                    com.dokodemo.core.ProfileValidator.Error.PROTOCOL -> com.dokodemo.R.string.protocol_no_config
                })
                _uiState.update { it.copy(addressError = error) }
                return@launch
            }
            if (state.id != null && state.id > 0) {
                serverRepository.updateServer(serverProfile)
            } else {
                serverRepository.addAndSelectServer(serverProfile)
            }
            onSuccess()
            } catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (_: Exception) {
                _uiState.update { it.copy(addressError = context.getString(com.dokodemo.R.string.save_failed)) }
            } finally { _uiState.update { it.copy(isSaving = false) } }
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
