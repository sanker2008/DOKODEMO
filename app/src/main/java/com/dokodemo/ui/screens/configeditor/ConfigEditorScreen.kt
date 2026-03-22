package com.dokodemo.ui.screens.configeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.data.model.Protocol
import com.dokodemo.ui.components.IndustrialCard
import com.dokodemo.ui.components.IndustrialInput
import com.dokodemo.ui.components.IndustrialTabButton
import com.dokodemo.ui.components.IndustrialToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigEditorScreen(
    serverId: Long? = null,
    uri: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: ConfigEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(serverId, uri) {
        if (serverId != null) {
            viewModel.loadServer(serverId)
        } else if (uri != null) {
            viewModel.parseUri(uri)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (serverId != null) "编辑节点" else "添加节点", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Rounded.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveConfig(onNavigateBack) }) {
                        Icon(Icons.Rounded.Check, "保存", tint = MaterialTheme.colorScheme.primary)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Protocol Selector
            ProtocolSelector(
                selected = uiState.protocol,
                onSelected = { viewModel.updateProtocol(it) }
            )

            // Basic Info
            ConfigSection("基础配置") {
                IndustrialInput(
                    value = uiState.name, onValueChange = viewModel::updateName,
                    label = "别名/备注", placeholder = "如: US Server 01",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    IndustrialInput(
                        value = uiState.address, onValueChange = viewModel::updateAddress,
                        label = "地址(IP/域名)",
                        errorMessage = uiState.addressError,
                        modifier = Modifier.weight(2f).padding(end = 12.dp)
                    )
                    IndustrialInput(
                        value = uiState.port, onValueChange = viewModel::updatePort,
                        label = "端口", keyboardType = KeyboardType.Number,
                        errorMessage = uiState.portError,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Authentication based on Protocol
                when (uiState.protocol) {
                    Protocol.VMESS, Protocol.VLESS, Protocol.TROJAN -> {
                        IndustrialInput(
                            value = uiState.uuid, onValueChange = viewModel::updateUuid,
                            label = if (uiState.protocol == Protocol.TROJAN) "密码 (Password)" else "UUID",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Protocol.SHADOWSOCKS -> {
                        IndustrialInput(
                            value = uiState.password, onValueChange = viewModel::updatePassword,
                            label = "密码", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        InputDropdown(
                            label = "加密方式", value = uiState.ssMethod,
                            options = listOf("aes-256-gcm", "aes-128-gcm", "chacha20-ietf-poly1305"),
                            onValueChange = viewModel::updateSsMethod
                        )
                    }
                    else -> {
                        // For other protocols like Wireguard, you can handle here or simply do nothing
                        Text(text = "此协议暂无专属表单项，请继续配置传输和 TLS", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Transport (Network)
            ConfigSection("传输配置") {
                InputDropdown(
                    label = "传输协议 (Network)", value = uiState.network,
                    options = listOf("tcp", "ws", "grpc", "kcp", "httpupgrade"),
                    onValueChange = viewModel::updateNetwork
                )

                Spacer(Modifier.height(12.dp))

                when (uiState.network) {
                    "ws", "httpupgrade" -> {
                        IndustrialInput(
                            value = uiState.wsPath, onValueChange = viewModel::updateWsPath,
                            label = "路径 (Path)", placeholder = "/", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        IndustrialInput(
                            value = uiState.wsHost, onValueChange = viewModel::updateWsHost,
                            label = "伪装域名 (Host)", modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "grpc" -> {
                        IndustrialInput(
                            value = uiState.wsPath, onValueChange = viewModel::updateWsPath,
                            label = "ServiceName", modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "kcp" -> {
                        InputDropdown(
                            label = "伪装类型 (Header Type)", value = uiState.kcpHeader,
                            options = listOf("none", "dtls", "utp", "srtp", "wechat-video", "wireguard"),
                            onValueChange = viewModel::updateKcpHeader,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        IndustrialInput(
                            value = uiState.kcpSeed, onValueChange = viewModel::updateKcpSeed,
                            label = "混淆密钥 (Seed) 可选", modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // TLS / Security
            ConfigSection("TLS 配置") {
                IndustrialToggleRow(
                    label = "开启 TLS", checked = uiState.useTls,
                    onCheckedChange = viewModel::updateUseTls
                )
                if (uiState.useTls) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    IndustrialInput(
                        value = uiState.serverName, onValueChange = viewModel::updateServerName,
                        label = "SNI (Server Name)", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    IndustrialToggleRow(
                        label = "允许不安全证书 (allowInsecure)", checked = uiState.allowInsecure,
                        onCheckedChange = viewModel::updateAllowInsecure
                    )
                }
            }

            // Group Assignment
            ConfigSection("分组设置") {
                InputGroupDropdown(
                    label = "所属分组",
                    groups = uiState.availableGroups,
                    selectedId = uiState.groupId,
                    onSelected = viewModel::updateGroupId
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
    IndustrialCard {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ProtocolSelector(selected: Protocol, onSelected: (Protocol) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(Protocol.entries) { proto ->
            IndustrialTabButton(
                text = proto.name,
                isSelected = selected == proto,
                onClick = { onSelected(proto) }
            )
        }
    }
}

// 简单的 Dropdown 选择器实现（基于 OutlinedTextField + DropdownMenu）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputDropdown(
    label: String, value: String, options: List<String>, onValueChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onValueChange(opt); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputGroupDropdown(
    label: String, groups: List<com.dokodemo.data.model.Group>, selectedId: Long?, onSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = groups.find { it.id == selectedId }?.name ?: "无分组"
    
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("无分组") }, onClick = { onSelected(null); expanded = false })
            groups.forEach { g ->
                DropdownMenuItem(text = { Text(g.name) }, onClick = { onSelected(g.id); expanded = false })
            }
        }
    }
}
