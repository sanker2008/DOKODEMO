package com.dokodemo.ui.screens.serverlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.R
import com.dokodemo.ui.components.DokoSearchInput
import com.dokodemo.ui.components.SquareFab
import com.dokodemo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToConfigEditor: (Long?) -> Unit,
    onNavigateToConfigEditorWithUri: (String) -> Unit,
    viewModel: ServerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // 直接从 uiState 中派生过滤列表，uiState 是响应式的 StateFlow，节点增删后会自动更新
    val filteredServers = remember(uiState.servers, uiState.searchQuery, uiState.selectedGroupId) {
        val query = uiState.searchQuery.lowercase()
        var result = uiState.servers
        if (uiState.selectedGroupId != null) {
            result = result.filter { it.groupId == uiState.selectedGroupId }
        }
        if (query.isNotEmpty()) {
            result = result.filter {
                it.name.lowercase().contains(query) ||
                it.countryCode.lowercase().contains(query)
            }
        }
        result
    }

    val listState = rememberLazyListState()
    var hasScrolledToSelected by remember { mutableStateOf(false) }

    LaunchedEffect(filteredServers, uiState.selectedServerId) {
        if (!hasScrolledToSelected && filteredServers.isNotEmpty() && uiState.selectedServerId != null) {
            val index = filteredServers.indexOfFirst { it.id == uiState.selectedServerId }
            if (index != -1) {
                listState.scrollToItem(index)
                hasScrolledToSelected = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.node_list), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    // 排序菜单
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.AutoMirrored.Rounded.Sort, stringResource(R.string.sort_nodes))
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_default), color = if(uiState.sortOption == SortOption.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                onClick = { viewModel.setSortOption(SortOption.DEFAULT); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_latency), color = if(uiState.sortOption == SortOption.LATENCY_ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                onClick = { viewModel.setSortOption(SortOption.LATENCY_ASC); sortMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.sort_name), color = if(uiState.sortOption == SortOption.NAME_ASC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                                onClick = { viewModel.setSortOption(SortOption.NAME_ASC); sortMenuExpanded = false }
                            )
                        }
                    }

                    // 测速按钮
                    TextButton(onClick = { viewModel.refreshServers() }, enabled = !uiState.isPinging) {
                        if (uiState.isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.pinging), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.Refresh, stringResource(R.string.ping_all), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.ping_all), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }
            Box {
                ExtendedFloatingActionButton(
                    onClick = { expanded = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Rounded.Add, stringResource(R.string.add_node)) },
                    text = { Text(stringResource(R.string.add_node)) }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.import_from_clipboard), color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            expanded = false
                            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clipData = clipboardManager.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val text = clipData.getItemAt(0).text?.toString()
                                if (!text.isNullOrBlank()) {
                                    onNavigateToConfigEditorWithUri(text)
                                } else {
                                    android.widget.Toast.makeText(context, context.getString(R.string.clipboard_empty), android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, context.getString(R.string.clipboard_empty), android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.scan_qr), color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            expanded = false
                            onNavigateToAddProfile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.manual_config), color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            expanded = false
                            onNavigateToConfigEditor(null)
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ─── 搜索框 ────────────────────────────────────────────────────
            DokoSearchInput(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = stringResource(R.string.search_nodes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ─── 分组标签 ─────────────────────────────────────────────────
            // 仅当有多个分组时才显示
            if (uiState.groups.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        GroupChip(
                            label = stringResource(R.string.all),
                            selected = uiState.selectedGroupId == null,
                            onClick = { viewModel.selectGroup(null) }
                        )
                    }
                    items(uiState.groups) { group ->
                        GroupChip(
                            label = group.name,
                            selected = uiState.selectedGroupId == group.id,
                            onClick = { viewModel.selectGroup(group.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ─── 节点列表 ─────────────────────────────────────────────────
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredServers,
                    key = { it.id }
                ) { server ->
                    NodeCard(
                        server = server,
                        isSelected = server.id == uiState.selectedServerId,
                        onClick = {
                            viewModel.selectServer(server.id)
                            onNavigateBack()
                        },
                        onEdit = { onNavigateToConfigEditor(server.id) },
                        onDelete = { viewModel.deleteServer(server) },
                        onPing = { viewModel.pingSingleServer(server) }
                    )
                }

                if (filteredServers.isEmpty()) {
                    item {
                        EmptyState()
                    }
                }
            }
        }
    }
}

// ─── 分组芯片 ─────────────────────────────────────────────────────────────
@Composable
private fun GroupChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary,
            selectedLabelColor = Color.White
        )
    )
}

// ─── 节点卡片 ─────────────────────────────────────────────────────────────
@Composable
private fun NodeCard(
    server: ServerItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPing: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    com.dokodemo.ui.components.DokoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        containerColor = if (isSelected)
                Primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 连接状态圆点
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            server.isConnected -> AccentGreen
                            isSelected -> Primary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
            )

            Spacer(Modifier.width(12.dp))

            // 节点信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = server.protocol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 延迟
            Column(horizontalAlignment = Alignment.End) {
                val ping = server.ping
                Text(
                    text = if (ping != null) "${ping}ms" else "--",
                    fontFamily = MonoFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = when {
                        ping == null -> MaterialTheme.colorScheme.onSurfaceVariant
                        ping < 100   -> AccentGreen
                        ping < 200   -> AccentOrange
                        else         -> AccentRed
                    }
                )
                // 信号格（简化版 4 格）
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
                    val bars = when {
                        ping == null -> 0
                        ping < 50   -> 4
                        ping < 100  -> 3
                        ping < 200  -> 2
                        else        -> 1
                    }
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((4 + i * 3).dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (i <= bars) Primary else MaterialTheme.colorScheme.outline.copy(0.4f))
                        )
                    }
                }
            }

            // 更多操作菜单
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, stringResource(R.string.more), tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ping_all)) },
                                leadingIcon = { Icon(Icons.Rounded.Refresh, null) },
                                onClick = { showMenu = false; onPing() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.edit)) },
                                leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                                onClick = { showMenu = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onDelete() }
                            )
                        }
            }
        }
    }
}

// ─── 空状态 ────────────────────────────────────────────────────────────────
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.List,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.no_nodes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.click_to_add_node),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
