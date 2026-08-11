package com.dokodemo.ui.screens.configeditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.R
import com.dokodemo.data.model.Protocol
import com.dokodemo.ui.components.DokoCard
import com.dokodemo.ui.components.DokoInput
import com.dokodemo.ui.components.DokoTabButton
import com.dokodemo.ui.components.DokoToggleRow

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
                title = { Text(if (serverId != null) stringResource(R.string.edit_node) else stringResource(R.string.add_node_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back)) }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveConfig(onNavigateBack) }) {
                        Icon(Icons.Rounded.Check, stringResource(R.string.save), tint = MaterialTheme.colorScheme.primary)
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
            ConfigSection(stringResource(R.string.basic_config)) {
                DokoInput(
                    value = uiState.name, onValueChange = viewModel::updateName,
                    label = stringResource(R.string.alias), placeholder = stringResource(R.string.alias_placeholder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    DokoInput(
                        value = uiState.address, onValueChange = viewModel::updateAddress,
                        label = stringResource(R.string.address),
                        errorMessage = uiState.addressError,
                        modifier = Modifier.weight(2f).padding(end = 12.dp)
                    )
                    DokoInput(
                        value = uiState.port, onValueChange = viewModel::updatePort,
                        label = stringResource(R.string.port), keyboardType = KeyboardType.Number,
                        errorMessage = uiState.portError,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Authentication based on Protocol
                when (uiState.protocol) {
                    Protocol.VMESS, Protocol.VLESS, Protocol.TROJAN -> {
                        DokoInput(
                            value = uiState.uuid, onValueChange = viewModel::updateUuid,
                            label = if (uiState.protocol == Protocol.TROJAN) stringResource(R.string.password) else "UUID",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        if (uiState.protocol == Protocol.VMESS) {
                            InputDropdown(
                                label = stringResource(R.string.security), value = uiState.security,
                                options = listOf("auto", "aes-128-gcm", "chacha20-poly1305", "none"),
                                onValueChange = viewModel::updateSecurity,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Protocol.SHADOWSOCKS -> {
                        DokoInput(
                            value = uiState.password, onValueChange = viewModel::updatePassword,
                            label = stringResource(R.string.password), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        InputDropdown(
                            label = stringResource(R.string.encryption), value = uiState.ssMethod,
                            options = listOf("aes-256-gcm", "aes-128-gcm", "chacha20-ietf-poly1305"),
                            onValueChange = viewModel::updateSsMethod
                        )
                    }
                    else -> {
                        Text(text = stringResource(R.string.protocol_no_config), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Transport (Network)
            ConfigSection(stringResource(R.string.transport_config)) {
                InputDropdown(
                    label = stringResource(R.string.network_protocol), value = uiState.network,
                    options = listOf("tcp", "ws", "grpc", "kcp", "httpupgrade"),
                    onValueChange = viewModel::updateNetwork
                )

                Spacer(Modifier.height(12.dp))

                when (uiState.network) {
                    "ws", "httpupgrade" -> {
                        DokoInput(
                            value = uiState.wsPath, onValueChange = viewModel::updateWsPath,
                            label = stringResource(R.string.path), placeholder = "/", modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        DokoInput(
                            value = uiState.wsHost, onValueChange = viewModel::updateWsHost,
                            label = stringResource(R.string.host), modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "grpc" -> {
                        DokoInput(
                            value = uiState.wsPath, onValueChange = viewModel::updateWsPath,
                            label = stringResource(R.string.service_name), modifier = Modifier.fillMaxWidth()
                        )
                    }
                    "kcp" -> {
                        InputDropdown(
                            label = stringResource(R.string.header_type), value = uiState.kcpHeader,
                            options = listOf("none", "dtls", "utp", "srtp", "wechat-video", "wireguard"),
                            onValueChange = viewModel::updateKcpHeader,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        DokoInput(
                            value = uiState.kcpSeed, onValueChange = viewModel::updateKcpSeed,
                            label = stringResource(R.string.seed), modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // TLS / Security
            ConfigSection(stringResource(R.string.tls_config)) {
                DokoToggleRow(
                    label = stringResource(R.string.enable_tls), checked = uiState.useTls,
                    onCheckedChange = viewModel::updateUseTls
                )
                if (uiState.useTls) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    DokoInput(
                        value = uiState.serverName, onValueChange = viewModel::updateServerName,
                        label = stringResource(R.string.sni), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    DokoToggleRow(
                        label = stringResource(R.string.allow_insecure_label), checked = uiState.allowInsecure,
                        onCheckedChange = viewModel::updateAllowInsecure
                    )
                }
                if (uiState.protocol == Protocol.VLESS) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    DokoToggleRow(
                        label = stringResource(R.string.use_reality), checked = uiState.useReality,
                        onCheckedChange = viewModel::updateUseReality
                    )
                    if (uiState.useReality) {
                        DokoInput(
                            value = uiState.flow, onValueChange = viewModel::updateFlow,
                            label = stringResource(R.string.flow), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        DokoInput(
                            value = uiState.realityPublicKey, onValueChange = viewModel::updateRealityPublicKey,
                            label = stringResource(R.string.reality_public_key), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        DokoInput(
                            value = uiState.realityShortId, onValueChange = viewModel::updateRealityShortId,
                            label = stringResource(R.string.reality_short_id), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        DokoInput(
                            value = uiState.realitySpiderX, onValueChange = viewModel::updateRealitySpiderX,
                            label = stringResource(R.string.reality_spider_x), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        )
                        InputDropdown(
                            label = stringResource(R.string.fingerprint), value = uiState.fingerprint,
                            options = listOf("chrome", "firefox", "safari", "edge", "ios", "android", "random"),
                            onValueChange = viewModel::updateFingerprint,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Group Assignment
            ConfigSection(stringResource(R.string.group_settings)) {
                InputGroupDropdown(
                    label = stringResource(R.string.group),
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
    DokoCard {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun ProtocolSelector(selected: Protocol, onSelected: (Protocol) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(Protocol.entries) { proto ->
            DokoTabButton(
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
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
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
    val selectedText = groups.find { it.id == selectedId }?.name ?: stringResource(R.string.no_group)
    
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedText, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            DropdownMenuItem(text = { Text(stringResource(R.string.no_group)) }, onClick = { onSelected(null); expanded = false })
            groups.forEach { g ->
                DropdownMenuItem(text = { Text(g.name) }, onClick = { onSelected(g.id); expanded = false })
            }
        }
    }
}
