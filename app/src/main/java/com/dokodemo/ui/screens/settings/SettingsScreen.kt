package com.dokodemo.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.R
import com.dokodemo.data.preferences.RoutingMode
import com.dokodemo.ui.components.DokoToggleRow
import com.dokodemo.ui.components.LanguageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSplitTunneling: () -> Unit,
    onNavigateToCustomRules: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToLogs: (() -> Unit)? = null,
    onNavigateToTrafficHistory: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRoutingDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ─── 节点管理 ─────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.node_management)) {
                SettingsClickRow(
                    title = stringResource(R.string.subscription_settings),
                    subtitle = stringResource(R.string.subscription_settings_desc),
                    onClick = onNavigateToSubscriptions
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                DokoToggleRow(
                    label = stringResource(R.string.auto_update_sub),
                    subtitle = stringResource(R.string.auto_update_sub_desc),
                    checked = uiState.autoUpdateSubscription,
                    onCheckedChange = { viewModel.setAutoUpdateSubscription(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── 代理模式 ─────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.proxy_routing)) {
                SettingsClickRow(
                    title = stringResource(R.string.routing_mode),
                    subtitle = getRoutingModeTitle(uiState.routingMode),
                    onClick = { showRoutingDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = stringResource(R.string.split_tunneling),
                    subtitle = stringResource(R.string.split_tunneling_desc),
                    onClick = onNavigateToSplitTunneling
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = stringResource(R.string.custom_routing_rules),
                    subtitle = stringResource(R.string.custom_routing_rules_desc),
                    onClick = onNavigateToCustomRules
                )
            }

            // ─── 连接设置 ─────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.connection)) {
                DokoToggleRow(
                    label = stringResource(R.string.mux_multiplexing),
                    subtitle = stringResource(R.string.mux_desc),
                    checked = uiState.muxEnabled,
                    onCheckedChange = { viewModel.setMuxEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                DokoToggleRow(
                    label = stringResource(R.string.allow_insecure),
                    subtitle = stringResource(R.string.allow_insecure_desc),
                    checked = uiState.allowInsecure,
                    onCheckedChange = { viewModel.setAllowInsecure(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                DokoToggleRow(
                    label = stringResource(R.string.udp_proxy),
                    subtitle = stringResource(R.string.udp_proxy_desc),
                    checked = uiState.udpEnabled,
                    onCheckedChange = { viewModel.setUdpEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                DokoToggleRow(
                    label = stringResource(R.string.bypass_lan),
                    subtitle = stringResource(R.string.bypass_lan_desc),
                    checked = uiState.bypassLan,
                    onCheckedChange = { viewModel.setBypassLan(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── 外观 ─────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.appearance)) {
                DokoToggleRow(
                    label = stringResource(R.string.dark_mode),
                    subtitle = stringResource(R.string.dark_mode_desc),
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = stringResource(R.string.font_size),
                    subtitle = stringResource(R.string.font_size_desc),
                    onClick = { showFontSizeDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.language_desc),
                    onClick = { showLanguageDialog = true }
                )
            }

            // ─── 安全提示 ─────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.security_tips)) {
                SettingsInfoRow(
                    label = stringResource(R.string.timezone_tip_title),
                    value = stringResource(R.string.timezone_tip_desc)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsInfoRow(
                    label = stringResource(R.string.language_tip_title),
                    value = stringResource(R.string.language_tip_desc)
                )
            }

            // ─── 调试 ─────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.debugging)) {
                SettingsClickRow(
                    title = stringResource(R.string.view_logs),
                    subtitle = stringResource(R.string.view_logs_desc),
                    onClick = { onNavigateToLogs?.invoke() }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = stringResource(R.string.traffic_history),
                    subtitle = stringResource(R.string.traffic_history_desc),
                    onClick = { onNavigateToTrafficHistory?.invoke() }
                )
            }

            // ─── 关于 ─────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.about)) {
                SettingsInfoRow(stringResource(R.string.core_version), uiState.coreVersion)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsInfoRow(stringResource(R.string.app_version), uiState.appVersion)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // 路由模式选择弹窗
    if (showRoutingDialog) {
        RoutingModeDialog(
            currentMode = uiState.routingMode,
            onSelect = {
                viewModel.setRoutingMode(it)
                showRoutingDialog = false
            },
            onDismiss = { showRoutingDialog = false }
        )
    }

    // 语言选择弹窗
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }

    // 字体大小选择弹窗
    if (showFontSizeDialog) {
        FontSizeDialog(
            currentScale = uiState.fontSizeScale,
            onSelect = {
                viewModel.setFontSizeScale(it)
                showFontSizeDialog = false
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }
}

@Composable
private fun getRoutingModeTitle(mode: RoutingMode): String {
    return when (mode) {
        RoutingMode.GLOBAL -> stringResource(R.string.routing_global_title)
        RoutingMode.BYPASS_CN -> stringResource(R.string.routing_bypass_cn_title)
        RoutingMode.SPLIT -> stringResource(R.string.routing_split_title)
    }
}

// ─── 字体大小选择弹窗 ─────────────────────────────────────────────────────
@Composable
private fun FontSizeDialog(
    currentScale: Float,
    onSelect: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0.85f to stringResource(R.string.font_size_small),
        1.0f to stringResource(R.string.font_size_normal),
        1.15f to stringResource(R.string.font_size_large)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.font_size), fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                options.forEach { (scale, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(scale) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = scale == currentScale,
                            onClick = { onSelect(scale) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// ─── 路由模式选择弹窗 ─────────────────────────────────────────────────────
@Composable
private fun RoutingModeDialog(
    currentMode: RoutingMode,
    onSelect: (RoutingMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.routing_mode), fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                // 模式说明
                Text(
                    stringResource(R.string.choose_routing_mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                RoutingMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = mode == currentMode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(getRoutingModeTitle(mode), fontWeight = FontWeight.Medium)
                            Text(
                                text = when (mode) {
                                    RoutingMode.GLOBAL   -> stringResource(R.string.routing_global_desc)
                                    RoutingMode.BYPASS_CN-> stringResource(R.string.routing_bypass_cn_desc)
                                    RoutingMode.SPLIT    -> stringResource(R.string.routing_split_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// ─── 可复用子组件 ──────────────────────────────────────────────────────────
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
    )
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsClickRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
