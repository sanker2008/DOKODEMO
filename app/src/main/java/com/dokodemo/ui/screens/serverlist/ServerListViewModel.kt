package com.dokodemo.ui.screens.serverlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.core.ServerPinger
import com.dokodemo.data.model.Group
import com.dokodemo.data.repository.GroupRepository
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
    val address: String,         // Needed for pinging
    val port: Int,               // Needed for pinging
    val ping: Int? = null,
    val isConnected: Boolean = false,
    val groupId: Long? = null
)

enum class SortOption { DEFAULT, LATENCY_ASC, NAME_ASC }

data class ServerListUiState(
    val servers: List<ServerItem> = emptyList(),
    val groups: List<Group> = emptyList(),       // 分组列表（用于 Tab 筛选）
    val selectedGroupId: Long? = null,           // null = 显示全部
    val selectedServerId: Long? = null,
    val searchQuery: String = "",
    val isPinging: Boolean = false,
    val toastMessage: String? = null,
    val sortOption: SortOption = SortOption.DEFAULT
)

@HiltViewModel
class ServerListViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val groupRepository: GroupRepository,
    private val serverPinger: ServerPinger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerListUiState())
    val uiState: StateFlow<ServerListUiState> = _uiState.asStateFlow()

    init {
        loadServers()
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
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
                        address = server.address,
                        port = server.port,
                        ping = server.latency,
                        isConnected = server.isSelected,
                        groupId = server.groupId
                    )
                }

                _uiState.update {
                    it.copy(
                        servers = serverItems,
                        selectedServerId = serverItems.find { item -> item.isConnected }?.id
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

            try {
                // Ping each server concurrently
                val servers = _uiState.value.servers
                val successCount = java.util.concurrent.atomic.AtomicInteger(0)
                servers.map { server ->
                    launch(Dispatchers.IO) {
                        val latency = try {
                            serverPinger.ping(server.address, server.port)
                        } catch (e: Exception) {
                            null
                        }
                        serverRepository.updateLatency(server.id, latency?.toInt())
                        if (latency != null) {
                            successCount.incrementAndGet()
                        }
                    }
                }.forEach { it.join() }
                
                _uiState.update { it.copy(toastMessage = "测试完成：${successCount.get()}/${servers.size} 个节点可用") }
            } catch (e: Exception) {
                android.util.Log.e("ServerListVM", "Ping failed: ${e.message}")
                _uiState.update { it.copy(toastMessage = "测试失败: ${e.message}") }
            }

            _uiState.update { it.copy(isPinging = false) }
        }
    }

    fun pingSingleServer(server: ServerItem) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(toastMessage = "正在测试...") }
                val latency = kotlinx.coroutines.withContext(Dispatchers.IO) {
                    serverPinger.ping(server.address, server.port)
                }
                serverRepository.updateLatency(server.id, latency?.toInt())
                if (latency != null) {
                    _uiState.update { it.copy(toastMessage = "测试成功：${latency}ms") }
                } else {
                    _uiState.update { it.copy(toastMessage = "测试失败：节点不可达") }
                }
            } catch (e: Exception) {
                serverRepository.updateLatency(server.id, null)
                _uiState.update { it.copy(toastMessage = "测试异常: ${e.message}") }
            }
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun refreshServers() {
        pingAllServers()
    }

    fun selectGroup(groupId: Long?) {
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    val filteredServers: List<ServerItem>
        get() {
            val state = _uiState.value
            val query = state.searchQuery.lowercase()
            var result = state.servers
            // 分组筛选
            if (state.selectedGroupId != null) {
                result = result.filter { it.groupId == state.selectedGroupId }
            }
            // 搜索筛选
            if (query.isNotEmpty()) {
                result = result.filter {
                    it.name.lowercase().contains(query) ||
                    it.countryCode.lowercase().contains(query)
                }
            }
            // 排序
            when (state.sortOption) {
                SortOption.LATENCY_ASC -> result = result.sortedBy { it.ping ?: Int.MAX_VALUE }
                SortOption.NAME_ASC -> result = result.sortedBy { it.name.lowercase() }
                SortOption.DEFAULT -> {} // DB default order
            }
            return result
        }
}
