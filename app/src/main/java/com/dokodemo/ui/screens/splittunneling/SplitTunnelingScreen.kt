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
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.size
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokodemo.R
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
                title = { Text(stringResource(R.string.split_tunneling_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Rounded.ArrowBack, stringResource(R.string.back)) }
                },
                actions = {
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.setShowSystemApps(!uiState.showSystemApps) }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.system_apps),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = uiState.showSystemApps,
                            onCheckedChange = { viewModel.setShowSystemApps(it) }
                        )
                    }
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
                placeholder = { 
                    Text(
                        text = stringResource(R.string.search_apps),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            SplitModeSection(
                currentMode = uiState.splitMode,
                onModeChange = viewModel::setSplitMode
            )

            SummarySection(
                selectedCount = uiState.selectedCount,
                onSelectVisibleApps = viewModel::selectVisibleApps,
                onClearSelection = viewModel::clearSelection
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val context = LocalContext.current
        val iconBitmap = remember(app.packageName) {
            try {
                context.packageManager.getApplicationIcon(app.packageName)
                    .toBitmap(config = android.graphics.Bitmap.Config.ARGB_8888)
                    .asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        if (iconBitmap != null) {
            Image(
                bitmap = iconBitmap,
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Android,
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (app.isSystemApp) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.system_apps),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = app.isProxied,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun SplitModeSection(
    currentMode: SplitTunnelingMode,
    onModeChange: (SplitTunnelingMode) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SegmentedButton(
            selected = currentMode == SplitTunnelingMode.PROXY_SELECTED,
            onClick = { onModeChange(SplitTunnelingMode.PROXY_SELECTED) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            label = { Text(stringResource(R.string.proxy_selected)) }
        )
        SegmentedButton(
            selected = currentMode == SplitTunnelingMode.BYPASS_SELECTED,
            onClick = { onModeChange(SplitTunnelingMode.BYPASS_SELECTED) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            label = { Text(stringResource(R.string.bypass_selected)) }
        )
    }
}

@Composable
private fun SummarySection(
    selectedCount: Int,
    onSelectVisibleApps: () -> Unit,
    onClearSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.selected_count, selectedCount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_visible),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSelectVisibleApps() }
            )
            Text(
                text = stringResource(R.string.clear_selection),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onClearSelection() }
            )
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
