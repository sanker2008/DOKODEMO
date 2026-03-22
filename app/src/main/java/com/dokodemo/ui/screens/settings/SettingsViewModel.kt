package com.dokodemo.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.data.preferences.RoutingMode
import com.dokodemo.service.VpnController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val routingMode: RoutingMode = RoutingMode.GLOBAL,
    val muxEnabled: Boolean = false,
    val sniffingEnabled: Boolean = true,
    val udpEnabled: Boolean = true,
    val adBlockEnabled: Boolean = false,
    val allowInsecure: Boolean = false,
    val darkModeEnabled: Boolean = true,
    val coreVersion: String = "---",
    val appVersion: String = "1.0.0"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val vpnController: VpnController
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // 监听 DataStore 中保存的设置
        viewModelScope.launch {
            combine(
                appPreferences.isDarkMode,
                appPreferences.routingMode
            ) { darkMode, routing ->
                _uiState.update {
                    it.copy(
                        darkModeEnabled = darkMode,
                        routingMode = routing
                    )
                }
            }.collect()
        }
        // 获取 Xray-core 版本
        _uiState.update { it.copy(coreVersion = vpnController.getCoreVersion()) }
    }

    fun setRoutingMode(mode: RoutingMode) {
        viewModelScope.launch { appPreferences.setRoutingMode(mode) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDarkMode(enabled) }
    }

    fun setMuxEnabled(enabled: Boolean) {
        _uiState.update { it.copy(muxEnabled = enabled) }
        // TODO: 保存到 DataStore（后续迭代）
    }

    fun setAllowInsecure(enabled: Boolean) {
        _uiState.update { it.copy(allowInsecure = enabled) }
    }

    fun setUdpEnabled(enabled: Boolean) {
        _uiState.update { it.copy(udpEnabled = enabled) }
    }

    fun setAdBlockEnabled(enabled: Boolean) {
        _uiState.update { it.copy(adBlockEnabled = enabled) }
    }
}
