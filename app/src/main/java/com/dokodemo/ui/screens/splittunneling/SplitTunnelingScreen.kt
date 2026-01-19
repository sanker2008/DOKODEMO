package com.dokodemo.ui.screens.splittunneling

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialInput
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Composable
fun SplitTunnelingScreen(
    onNavigateBack: () -> Unit,
    viewModel: SplitTunnelingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps()
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                         Text(
                             text = "LOADING APPLICATIONS...",
                             color = IndustrialGrey,
                             fontFamily = MonospaceFont
                         )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.filteredApps) { app ->
                            AppItem(
                                app = app,
                                onToggle = { viewModel.toggleApp(app.packageName) }
                            )
                        }
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
                if (!selected) Modifier.background(MaterialTheme.colorScheme.surface) 
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
            color = if (selected) IndustrialBlack else MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(if (app.isProxied) AcidLime else MaterialTheme.colorScheme.outline)
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MonospaceFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "SPLIT_TUNNEL.EXE",
            color = MaterialTheme.colorScheme.primary,
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
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class SplitTunnelingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitTunnelingUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        // Observe preference changes to update checkbox states
        viewModelScope.launch {
            appPreferences.proxiedApps.collect { proxiedSet ->
                _uiState.update { state ->
                    val updatedApps = state.allApps.map { app ->
                        app.copy(isProxied = proxiedSet.contains(app.packageName))
                    }
                    state.copy(
                        allApps = updatedApps,
                        filteredApps = filterApps(updatedApps, state.searchQuery)
                    )
                }
            }
        }
    }
    
    fun setMode(mode: RoutingMode) {
        _uiState.update { it.copy(mode = mode) }
    }
    
    fun loadInstalledApps() {
        if (_uiState.value.allApps.isNotEmpty()) return
        
        _uiState.update { it.copy(isLoading = true) }
        
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            
            val apps = try {
                 pm.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
                    try {
                        val appInfo = resolveInfo.activityInfo.applicationInfo
                        val packageName = appInfo.packageName
                        val label = appInfo.loadLabel(pm).toString()
                        
                        // Exclude self
                        if (packageName == context.packageName) null
                        else AppInfo(label, packageName)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.appName.lowercase() }
            } catch (e: Exception) {
                emptyList()
            }
            
            // Sync with current preferences
            // We can't access flow directly here easily, but the init block handles the update.
            // We just set the base list for now.
            
            withContext(Dispatchers.Main) {
                _uiState.update { 
                    it.copy(
                        allApps = apps,
                        // Filter will be updated by the preference flow collector triggered shortly
                        // or we trigger it manually here if flow is already emitted
                        isLoading = false
                    ) 
                }
                // Trigger a re-sync with preferences
                // (Preference flow is hot, it will pick up the new allApps in the combine logic strictly speaking? 
                // No, the collect block updates allApps based on existing allApps. 
                // We need to ensure consistency. 
                // Better approach: combine flows.)
             }
        }
    }
    
    fun search(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredApps = filterApps(it.allApps, query)
            ) 
        }
    }
    
    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            val currentApps = _uiState.value.allApps
            val app = currentApps.find { it.packageName == packageName } ?: return@launch
            
            if (app.isProxied) {
                appPreferences.removeProxiedApp(packageName)
            } else {
                appPreferences.addProxiedApp(packageName)
            }
        }
    }
    
    private fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        return if (query.isEmpty()) apps else apps.filter { 
            it.appName.contains(query, ignoreCase = true) || 
            it.packageName.contains(query, ignoreCase = true)
        }
    }
}
