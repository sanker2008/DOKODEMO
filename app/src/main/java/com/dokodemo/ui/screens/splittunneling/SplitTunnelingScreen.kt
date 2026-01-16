package com.dokodemo.ui.screens.splittunneling

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialInput
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun SplitTunnelingScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitTunnelingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        containerColor = IndustrialBlack,
        topBar = {
            SplitTunnelingTopBar(onNavigateBack)
        }
    ) { paddingValues ->
        Column(
             modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            
            // Mode Selector
            IndustrialCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ROUTING MODE",
                        color = TextGrey,
                        fontFamily = MonospaceFont,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                         ModeButton(
                             text = "GLOBAL",
                             selected = uiState.mode == RoutingMode.GLOBAL,
                             onClick = { viewModel.setMode(RoutingMode.GLOBAL) },
                             modifier = Modifier.weight(1f)
                         )
                        ModeButton(
                             text = "BYPASS CN",
                             selected = uiState.mode == RoutingMode.BYPASS_CN,
                             onClick = { viewModel.setMode(RoutingMode.BYPASS_CN) },
                             modifier = Modifier.weight(1f)
                         )
                        ModeButton(
                             text = "CUSTOM",
                             selected = uiState.mode == RoutingMode.CUSTOM,
                             onClick = { viewModel.setMode(RoutingMode.CUSTOM) },
                             modifier = Modifier.weight(1f)
                         )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.mode == RoutingMode.CUSTOM) {
                // App List
                IndustrialInput(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.search(it) },
                    label = "SEARCH APPS",
                    placeholder = "chrome..."
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.apps) { app ->
                        AppItem(
                            app = app,
                            onToggle = { viewModel.toggleApp(app.packageName) }
                        )
                    }
                }
            } else {
                // Description for modes
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when(uiState.mode) {
                            RoutingMode.GLOBAL -> "ALL TRAFFIC PROXIED"
                            RoutingMode.BYPASS_CN -> "DIRECT FOR CN, PROXY OTHERS"
                            else -> ""
                        },
                        color = IndustrialGrey,
                        fontFamily = MonospaceFont,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (selected) AcidLime else Color.Transparent)
            .clickable { onClick() }
            .then(
                if (!selected) Modifier.background(IndustrialBlack) 
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Border if not selected
        if (!selected) {
            Box(
                 modifier = Modifier
                     .fillMaxSize()
                     .background(IndustrialGrey)
                     .padding(1.dp)
                     .background(IndustrialBlack)
            )
        }
        
        Text(
            text = text,
            color = if (selected) IndustrialBlack else TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AppItem(app: AppInfo, onToggle: () -> Unit) {
    IndustrialCard {
        Column(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = app.appName,
                    color = Color.White,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(if (app.isProxied) AcidLime else IndustrialGrey)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = app.packageName,
                color = TextGrey,
                fontFamily = MonospaceFont,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SplitTunnelingTopBar(onBack: () -> Unit) {
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
            text = "SPLIT_TUNNEL.EXE",
            color = AcidLime,
            fontFamily = MonospaceFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

enum class RoutingMode {
    GLOBAL, BYPASS_CN, CUSTOM
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isProxied: Boolean = false
)

data class SplitTunnelingUiState(
    val mode: RoutingMode = RoutingMode.BYPASS_CN,
    val apps: List<AppInfo> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class SplitTunnelingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SplitTunnelingUiState())
    val uiState = _uiState.asStateFlow()
    
    // Mock apps
    private val allApps = listOf(
        AppInfo("Chrome", "com.android.chrome"),
        AppInfo("YouTube", "com.google.android.youtube"),
        AppInfo("Play Store", "com.android.vending"),
        AppInfo("Telegram", "org.telegram.messenger"),
        AppInfo("Twitter", "com.twitter.android")
    )
    
    init {
        _uiState.update { it.copy(apps = allApps) }
    }
    
    fun setMode(mode: RoutingMode) {
        _uiState.update { it.copy(mode = mode) }
    }
    
    fun search(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                apps = if (query.isEmpty()) allApps else allApps.filter { app ->
                    app.appName.contains(query, ignoreCase = true)
                }
            ) 
        }
    }
    
    fun toggleApp(packageName: String) {
        // Toggle logic
        val newApps = _uiState.value.apps.map { 
            if (it.packageName == packageName) it.copy(isProxied = !it.isProxied) else it 
        }
        _uiState.update { it.copy(apps = newApps) }
    }
}
