package com.dokodemo.ui.screens.serverlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.ui.components.IndustrialSearchInput
import com.dokodemo.ui.components.SquareFab
import com.dokodemo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToConfigEditor: (Long?) -> Unit,
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("节点列表", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 测速按钮
                    TextButton(onClick = { viewModel.refreshServers() }, enabled = !uiState.isPinging) {
                        if (uiState.isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("测速中...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Rounded.Refresh, "全部测速", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("测速", fontWeight = FontWeight.Bold)
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
                    icon = { Icon(Icons.Rounded.Add, "添加节点") },
                    text = { Text("添加节点") }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("扫码添加", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            expanded = false
                            onNavigateToAddProfile()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("手动配置", color = MaterialTheme.colorScheme.onSurface) },
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
            IndustrialSearchInput(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = "搜索节点…",
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
                            label = "全部",
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
                        onClick = { viewModel.selectServer(server.id) },
                        onEdit = { onNavigateToConfigEditor(server.id) },
                        onDelete = { viewModel.deleteServer(server) }
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
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder().run {
                androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.5f))
            }
        else null,
        elevation = CardDefaults.cardElevation(0.dp)
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
                    Icon(Icons.Rounded.MoreVert, "更多", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("删除", color = MaterialTheme.colorScheme.error) },
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
            Text("📋", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "还没有节点",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "点击右下角的按钮添加节点",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
