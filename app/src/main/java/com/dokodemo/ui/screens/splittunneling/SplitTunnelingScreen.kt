package com.dokodemo.ui.screens.splittunneling

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialSearchInput
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("分应用代理", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Rounded.ArrowBack, "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            IndustrialSearchInput(
                value = uiState.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = "搜索应用(支持包名/中文名)",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(uiState.filteredApps) { app ->
                        AppItem(
                            app = app,
                            onToggle = { viewModel.toggleApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(app: AppInfo, onToggle: () -> Unit) {
    IndustrialCard(
        modifier = Modifier.clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            com.dokodemo.ui.components.IndustrialToggle(
                checked = app.isProxied,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isProxied: Boolean = false
)

data class SplitTunnelingUiState(
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

                        if (packageName == context.packageName) null
                        else AppInfo(label, packageName)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.appName.lowercase() }
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    val updatedApps = apps.map { app ->
                        // 因为这里此时 appPreferences 还没同步完，我们可以等待它或者直接依赖 flow
                        app
                    }
                    state.copy(
                        allApps = updatedApps,
                        filteredApps = filterApps(updatedApps, state.searchQuery),
                        isLoading = false
                    )
                }
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
