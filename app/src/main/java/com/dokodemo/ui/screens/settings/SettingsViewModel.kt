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
    val udpEnabled: Boolean = true,
    val bypassLan: Boolean = true,
    val allowLanConnection: Boolean = false,
    val autoUpdateSubscription: Boolean = false,
    val allowInsecure: Boolean = false,
    val darkModeEnabled: Boolean = true,
    val fontSizeScale: Float = 1.0f,
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
        viewModelScope.launch {
            combine(
                combine(
                    appPreferences.isDarkMode,
                    appPreferences.routingMode,
                    appPreferences.fontSizeScale
                ) { darkMode, routing, fontScale ->
                    Triple(darkMode, routing, fontScale)
                },
                combine(
                    appPreferences.muxEnabled,
                    appPreferences.allowInsecure,
                    appPreferences.udpEnabled
                ) { muxEnabled, allowInsecure, udpEnabled ->
                    Triple(muxEnabled, allowInsecure, udpEnabled)
                },
                combine(
                    appPreferences.bypassLan,
                    appPreferences.autoUpdateSubscription,
                    appPreferences.allowLanConnection
                ) { bypass, autoUpdate, allowLan -> Triple(bypass, autoUpdate, allowLan) }
            ) { primaryState, connectionState, miscState ->
                val (darkMode, routing, fontScale) = primaryState
                val (muxEnabled, allowInsecure, udpEnabled) = connectionState
                val (bypass, autoUpdate) = miscState
                _uiState.update {
                    it.copy(
                        darkModeEnabled = darkMode,
                        routingMode = routing,
                        fontSizeScale = fontScale,
                        muxEnabled = muxEnabled,
                        allowInsecure = allowInsecure,
                        udpEnabled = udpEnabled,
                        bypassLan = bypass,
                        autoUpdateSubscription = autoUpdate
                    )
                }
            }.collect()
        }
        _uiState.update { it.copy(coreVersion = vpnController.getCoreVersion()) }
    }

    fun setRoutingMode(mode: RoutingMode) {
        viewModelScope.launch { appPreferences.setRoutingMode(mode) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setDarkMode(enabled) }
    }

    fun setFontSizeScale(scale: Float) {
        viewModelScope.launch { appPreferences.setFontSizeScale(scale) }
    }

    fun setMuxEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setMuxEnabled(enabled) }
    }

    fun setAllowInsecure(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setAllowInsecure(enabled) }
    }

    fun setUdpEnabled(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setUdpEnabled(enabled) }
    }

    fun setBypassLan(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setBypassLan(enabled) }
    }

    fun setAutoUpdateSubscription(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setAutoUpdateSubscription(enabled) }
    }

    fun setAllowLanConnection(enabled: Boolean) {
        viewModelScope.launch { appPreferences.setAllowLanConnection(enabled) }
    }
}
