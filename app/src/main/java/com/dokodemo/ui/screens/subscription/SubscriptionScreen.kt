package com.dokodemo.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.data.model.Subscription
import com.dokodemo.ui.components.DokoInput
import com.dokodemo.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSub by remember { mutableStateOf<com.dokodemo.data.model.Subscription?>(null) }

    // 错误提示
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            // 在实际应用中可以用 Snackbar，这里用 LaunchedEffect 简单模拟
            // Snackbar.show(uiState.errorMessage!!)
            // 稍后清除
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("节点订阅", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回") }
                },
                actions = {
                    IconButton(onClick = { viewModel.updateAllSubscriptions() }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.Refresh, "全部更新", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, "添加订阅") },
                text = { Text("添加订阅") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.subscriptions.isEmpty()) {
                EmptySubState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.subscriptions) { sub ->
                        SubscriptionCard(
                            subscription = sub,
                            isRefreshing = uiState.refreshingId == sub.id,
                            onRefresh = { viewModel.updateSubscription(sub) },
                            onDelete = { viewModel.deleteSubscription(sub) },
                            onEdit = { editingSub = sub }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, url ->
                viewModel.addSubscription(name, url)
                showAddDialog = false
            }
        )
    }

    // 编辑订阅对话框
    editingSub?.let { sub ->
        EditSubscriptionDialog(
            subscription = sub,
            onDismiss = { editingSub = null },
            onSave = { name, url ->
                viewModel.editSubscription(sub, name, url)
                editingSub = null
            }
        )
    }

    // 简易错误弹窗
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = MaterialTheme.colorScheme.background,
            title = { Text("提示") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) { Text("确定") }
            }
        )
    }
}

@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val timeFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    com.dokodemo.ui.components.DokoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                
                val lastUpdateStr = if (subscription.lastUpdated != null) {
                    "上次更新: " + timeFormat.format(Date(subscription.lastUpdated))
                } else {
                    "从未更新"
                }

                Text(
                    text = "$lastUpdateStr • ${subscription.serverCount} 个节点",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (subscription.total > 0) {
                    Spacer(Modifier.height(4.dp))
                    val used = subscription.upload + subscription.download
                    val usedStr = formatBytes(used)
                    val totalStr = formatBytes(subscription.total)
                    val remainStr = formatBytes((subscription.total - used).coerceAtLeast(0))
                    
                    val expireStr = if (subscription.expire > 0) {
                        // expire 可能是秒级别的时间戳
                        val expireDate = if (subscription.expire > 9999999999L) Date(subscription.expire) else Date(subscription.expire * 1000)
                        timeFormat.format(expireDate)
                    } else {
                        "无限制"
                    }
                    
                    Text(
                        text = "流量: $usedStr / $totalStr (剩余 $remainStr)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (subscription.total > 0) (used.toFloat() / subscription.total.toFloat()).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "过期时间: $expireStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(end = 8.dp),
                    color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, "刷新", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Rounded.MoreVert, "更多", tint = MaterialTheme.colorScheme.onSurface)
                }
                DropdownMenu(
                    expanded = showMenu, 
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("删除订阅", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditSubscriptionDialog(
    subscription: Subscription,
    onDismiss: () -> Unit,
    onSave: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf(subscription.name) }
    var url by remember { mutableStateOf(subscription.url) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("编辑订阅") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DokoInput(value = name, onValueChange = { name = it }, label = "名称（备注）")
                DokoInput(value = url, onValueChange = { url = it }, label = "订阅链接")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, url) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) { Text("保存", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// Placeholder to avoid duplicate definition
private fun _placeholder_SubscriptionCard_old() {}

@Composable
private fun EmptySubState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text("暂无订阅", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "通过订阅链接一次性导入多个节点。\n支持 Clash 格式、Base64 格式等常见订阅链接。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text("添加订阅") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DokoInput(
                    value = name, onValueChange = { name = it },
                    label = "名称（备注）", placeholder = "如: 代理机场"
                )
                DokoInput(
                    value = url, onValueChange = { url = it },
                    label = "订阅链接", placeholder = "https://"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, url) },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text("确定", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0.00 B"
    var b = bytes.toDouble()
    var i = 0
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    while (b >= 1024 && i < units.size - 1) {
        b /= 1024
        i++
    }
    return String.format(Locale.getDefault(), "%.2f %s", b, units[i])
}
