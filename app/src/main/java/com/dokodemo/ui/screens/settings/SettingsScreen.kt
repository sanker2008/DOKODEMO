package com.dokodemo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.dokodemo.core.CoreManager
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialToggleRow
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSplitTunneling: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        containerColor = IndustrialBlack,
        topBar = {
            SettingsTopBar(onNavigateBack)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: CORE CONFIG
            item {
                SectionHeader("CORE_CONFIG // V2RAY")
            }
            
            item {
                IndustrialCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IndustrialToggleRow(
                            label = "MUX MULTIPLEXING",
                            checked = uiState.muxEnabled,
                            onCheckedChange = { viewModel.toggleMux() }
                        )
                        IndustrialToggleRow(
                            label = "SNIFFING (HTTP/TLS)",
                            checked = uiState.sniffingEnabled,
                            onCheckedChange = { viewModel.toggleSniffing() }
                        )
                        IndustrialToggleRow(
                            label = "UDP RELAY",
                            checked = uiState.udpEnabled,
                            onCheckedChange = { viewModel.toggleUdp() }
                        )
                    }
                }
            }
            
            // Section: ROUTING
            item {
                SectionHeader("ROUTING_RULES")
            }
            
            item {
                IndustrialCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SettingsOption(
                            title = "SPLIT TUNNELING",
                            value = "CONFIGURED",
                            onClick = onNavigateToSplitTunneling
                        )
                        IndustrialToggleRow(
                            label = "BLOCK ADS (GEOSITE)",
                            checked = uiState.blockAds,
                            onCheckedChange = { viewModel.toggleBlockAds() }
                        )
                        IndustrialToggleRow(
                            label = "BYPASS LAN",
                            checked = uiState.bypassLan,
                            onCheckedChange = { viewModel.toggleBypassLan() }
                        )
                    }
                }
            }
            
             // Section: APP
            item {
                SectionHeader("SYSTEM_&_DISPLAY")
            }
            
            item {
                IndustrialCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IndustrialToggleRow(
                            label = "DARK MODE (ALWAYS)",
                            checked = true,
                            onCheckedChange = { /* Lock to dark */ }
                        )
                        SettingsOption(
                            title = "CORE VERSION",
                            value = uiState.coreVersion,
                            onClick = {}
                        )
                         SettingsOption(
                            title = "APP VERSION",
                            value = "1.0.0 (BETA)",
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(IndustrialGrey)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = AcidLime,
                fontFamily = MonospaceFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "SETTINGS.CONF",
            color = AcidLime,
            fontFamily = MonospaceFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = TextGrey,
        fontFamily = MonospaceFont,
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsOption(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontFamily = MonospaceFont,
            fontSize = 14.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = AcidLime,
                fontFamily = MonospaceFont,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = ">",
                color = IndustrialGrey,
                fontFamily = MonospaceFont,
                fontSize = 14.sp
            )
        }
    }
}

// ViewModel and State
data class SettingsUiState(
    val muxEnabled: Boolean = false,
    val sniffingEnabled: Boolean = true,
    val udpEnabled: Boolean = true,
    val blockAds: Boolean = false,
    val bypassLan: Boolean = true,
    val coreVersion: String = "---"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val coreManager: CoreManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        _uiState.update { it.copy(coreVersion = coreManager.getVersion()) }
    }
    
    fun toggleMux() {
        _uiState.update { it.copy(muxEnabled = !it.muxEnabled) }
    }
    
    fun toggleSniffing() {
        _uiState.update { it.copy(sniffingEnabled = !it.sniffingEnabled) }
    }
    
    fun toggleUdp() {
        _uiState.update { it.copy(udpEnabled = !it.udpEnabled) }
    }
    
    fun toggleBlockAds() {
        _uiState.update { it.copy(blockAds = !it.blockAds) }
    }
    
    fun toggleBypassLan() {
        _uiState.update { it.copy(bypassLan = !it.bypassLan) }
    }
}
