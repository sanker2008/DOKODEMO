package com.dokodemo.ui.screens.splittunneling

import android.content.pm.ApplicationInfo
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.data.preferences.AppPreferences
import com.dokodemo.data.preferences.SplitTunnelingMode
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialToggle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
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
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("搜索应用名称或包名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            SplitModeSection(
                currentMode = uiState.splitMode,
                onModeChange = viewModel::setSplitMode
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummarySection(
                selectedCount = uiState.selectedCount,
                showSystemApps = uiState.showSystemApps,
                onToggleSystemApps = viewModel::setShowSystemApps,
                onSelectVisibleApps = viewModel::selectVisibleApps,
                onClearSelection = viewModel::clearSelection
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    item {
                        Text(
                            text = if (uiState.splitMode == SplitTunnelingMode.PROXY_SELECTED) {
                                "仅你选中的应用会进入代理，其他应用保持直连。"
                            } else {
                                "除你选中的应用外，其余应用都会进入代理。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
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
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (app.isSystemApp) {
                    Text(
                        text = "系统应用",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IndustrialToggle(
                checked = app.isProxied,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun SplitModeSection(
    currentMode: SplitTunnelingMode,
    onModeChange: (SplitTunnelingMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "分应用模式",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = currentMode == SplitTunnelingMode.PROXY_SELECTED,
                onClick = { onModeChange(SplitTunnelingMode.PROXY_SELECTED) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                label = { Text("仅选中走代理") }
            )
            SegmentedButton(
                selected = currentMode == SplitTunnelingMode.BYPASS_SELECTED,
                onClick = { onModeChange(SplitTunnelingMode.BYPASS_SELECTED) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                label = { Text("仅选中直连") }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummarySection(
    selectedCount: Int,
    showSystemApps: Boolean,
    onToggleSystemApps: (Boolean) -> Unit,
    onSelectVisibleApps: () -> Unit,
    onClearSelection: () -> Unit
) {
    IndustrialCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "已选应用 $selectedCount 个",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "显示系统应用",
                    style = MaterialTheme.typography.bodyMedium
                )
                IndustrialToggle(
                    checked = showSystemApps,
                    onCheckedChange = onToggleSystemApps
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onSelectVisibleApps) {
                    Icon(Icons.Rounded.Checklist, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("选择当前列表")
                }
                OutlinedButton(onClick = onClearSelection) {
                    Icon(Icons.Rounded.ClearAll, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("清空选择")
                }
            }
        }
    }
}

data class AppInfo(
    val appName: String,
    val packageName: String,
    val isProxied: Boolean = false,
    val isSystemApp: Boolean = false
)

data class SplitTunnelingUiState(
    val allApps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val splitMode: SplitTunnelingMode = SplitTunnelingMode.PROXY_SELECTED,
    val showSystemApps: Boolean = false,
    val selectedCount: Int = 0
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
            combine(
                appPreferences.proxiedApps,
                appPreferences.splitTunnelingMode
            ) { proxiedSet, splitMode -> proxiedSet to splitMode }
                .collect { (proxiedSet, splitMode) ->
                    _uiState.update { state ->
                        val updatedApps = state.allApps.map { app ->
                            app.copy(isProxied = proxiedSet.contains(app.packageName))
                        }
                        state.copy(
                            allApps = updatedApps,
                            filteredApps = filterApps(
                                apps = updatedApps,
                                query = state.searchQuery,
                                showSystemApps = state.showSystemApps
                            ),
                            selectedCount = proxiedSet.size,
                            splitMode = splitMode
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
            val apps = try {
                pm.getInstalledApplications(0)
                    .mapNotNull { applicationInfo ->
                    try {
                        val packageName = applicationInfo.packageName
                        val label = applicationInfo.loadLabel(pm)?.toString().orEmpty()

                        if (packageName == context.packageName || label.isBlank()) {
                            null
                        } else {
                            AppInfo(
                                appName = label,
                                packageName = packageName,
                                isSystemApp = applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                    .distinctBy { it.packageName }
                    .sortedBy { it.appName.lowercase() }
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    state.copy(
                        allApps = apps,
                        filteredApps = filterApps(
                            apps = apps,
                            query = state.searchQuery,
                            showSystemApps = state.showSystemApps
                        ),
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
                filteredApps = filterApps(
                    apps = it.allApps,
                    query = query,
                    showSystemApps = it.showSystemApps
                )
            )
        }
    }

    fun setSplitMode(mode: SplitTunnelingMode) {
        viewModelScope.launch {
            appPreferences.setSplitTunnelingMode(mode)
        }
    }

    fun setShowSystemApps(show: Boolean) {
        _uiState.update {
            it.copy(
                showSystemApps = show,
                filteredApps = filterApps(
                    apps = it.allApps,
                    query = it.searchQuery,
                    showSystemApps = show
                )
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

    fun selectVisibleApps() {
        viewModelScope.launch {
            val packages = _uiState.value.filteredApps.map { it.packageName }.toSet()
            appPreferences.setProxiedApps(_uiState.value.allApps.filter {
                it.isProxied || packages.contains(it.packageName)
            }.map { it.packageName }.toSet())
        }
    }

    fun clearSelection() {
        viewModelScope.launch {
            appPreferences.setProxiedApps(emptySet())
        }
    }

    private fun filterApps(
        apps: List<AppInfo>,
        query: String,
        showSystemApps: Boolean
    ): List<AppInfo> {
        return apps.filter { app ->
            (showSystemApps || !app.isSystemApp) && (
                query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
                )
        }
    }
}
