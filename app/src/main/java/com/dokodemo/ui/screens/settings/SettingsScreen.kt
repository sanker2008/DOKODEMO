package com.dokodemo.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.data.preferences.RoutingMode
import com.dokodemo.ui.components.IndustrialToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSplitTunneling: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToLogs: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRoutingDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, "返回")
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
            SettingsSection(title = "节点管理") {
                SettingsClickRow(
                    title = "订阅设置",
                    subtitle = "批量导入并自动更新节点",
                    onClick = onNavigateToSubscriptions
                )
            }

            // ─── 代理模式 ─────────────────────────────────────────────────
            SettingsSection(title = "代理路由") {
                SettingsClickRow(
                    title = "路由模式",
                    subtitle = uiState.routingMode.displayName,
                    onClick = { showRoutingDialog = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsClickRow(
                    title = "分应用代理",
                    subtitle = "选择哪些 App 走代理",
                    onClick = onNavigateToSplitTunneling
                )
            }

            // ─── 连接设置 ─────────────────────────────────────────────────
            SettingsSection(title = "连接") {
                IndustrialToggleRow(
                    label = "Mux 多路复用",
                    subtitle = "合并多条流量通道（降延迟）",
                    checked = uiState.muxEnabled,
                    onCheckedChange = { viewModel.setMuxEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                IndustrialToggleRow(
                    label = "允许不安全证书",
                    subtitle = "跳过 TLS 证书验证（不推荐）",
                    checked = uiState.allowInsecure,
                    onCheckedChange = { viewModel.setAllowInsecure(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                IndustrialToggleRow(
                    label = "UDP 代理",
                    subtitle = "转发 UDP 流量（游戏/语音通话）",
                    checked = uiState.udpEnabled,
                    onCheckedChange = { viewModel.setUdpEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                IndustrialToggleRow(
                    label = "广告过滤",
                    subtitle = "屏蔽常见广告域名",
                    checked = uiState.adBlockEnabled,
                    onCheckedChange = { viewModel.setAdBlockEnabled(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── 外观 ─────────────────────────────────────────────────────
            SettingsSection(title = "外观") {
                IndustrialToggleRow(
                    label = "深色模式",
                    subtitle = "开启深色主题",
                    checked = uiState.darkModeEnabled,
                    onCheckedChange = { viewModel.setDarkMode(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ─── 调试 ─────────────────────────────────────────────────────
            SettingsSection(title = "调试") {
                SettingsClickRow(
                    title = "查看日志",
                    subtitle = "Xray 核心运行日志",
                    onClick = { onNavigateToLogs?.invoke() }
                )
            }

            // ─── 关于 ─────────────────────────────────────────────────────
            SettingsSection(title = "关于") {
                SettingsInfoRow("核心版本", uiState.coreVersion)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                SettingsInfoRow("App 版本", uiState.appVersion)
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
        title = { Text("路由模式", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                // 模式说明
                Text(
                    "选择流量的路由方式：",
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
                            Text(mode.displayName, fontWeight = FontWeight.Medium)
                            Text(
                                text = when (mode) {
                                    RoutingMode.GLOBAL   -> "所有流量走代理，TikTok 等境外应用必选此项"
                                    RoutingMode.BYPASS_CN-> "国内直连，境外走代理，平衡模式"
                                    RoutingMode.SPLIT    -> "仅勾选的 App 走代理，需在分应用代理里配置"
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
            TextButton(onClick = onDismiss) { Text("取消") }
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
        Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
