package com.dokodemo.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.R
import com.dokodemo.ui.theme.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun HomeScreen(
    onNavigateToServerList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onVpnPermissionResult(result.resultCode == Activity.RESULT_OK)
    }

    val vpnIntent = uiState.vpnPermissionIntent
    LaunchedEffect(uiState.needsVpnPermission, vpnIntent) {
        if (uiState.needsVpnPermission && vpnIntent != null) {
            vpnPermissionLauncher.launch(vpnIntent)
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeBottomNav(
                currentRoute = "home",
                onNavigateToHome = {},
                onNavigateToServerList = onNavigateToServerList,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── 顶栏 ─────────────────────────────────────────────────────
            TopBar(
                routingMode = uiState.routingMode,
                onRoutingModeChange = { viewModel.setRoutingMode(it) }
            )

            Spacer(Modifier.height(8.dp))

            // ─── 流量监控（折线图） ────────────────────────────────────────
            TrafficMonitorCard(
                uploadSpeed = uiState.uploadSpeed,
                downloadSpeed = uiState.downloadSpeed,
                speedHistory = uiState.speedHistory,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.height(24.dp))

            // ─── 当前节点/空状态卡片 ─────────────────────────────────────────────
            if (uiState.hasNoServers) {
                HomeEmptyStateCard(onClick = onNavigateToServerList)
            } else {
                CurrentNodeCard(
                    serverName = uiState.currentServer?.name ?: stringResource(R.string.no_node_selected),
                    protocol = uiState.protocol,
                    latency = uiState.ping,
                    isPinging = uiState.isPinging,
                    onClick = onNavigateToServerList,
                    onPingClick = { viewModel.pingCurrentServer() }
                )
            }

            Spacer(Modifier.height(32.dp))

            // ─── 连接状态标签 ─────────────────────────────────────────────
            StatusBadge(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting
            )

            Spacer(Modifier.height(16.dp))

            // ─── 大圆形连接按钮 ───────────────────────────────────────────
            ConnectButton(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting,
                isEnabled = !uiState.hasNoServers,
                onClick = {
                    if (uiState.currentServer == null) {
                        Toast.makeText(context, context.getString(R.string.please_select_node), Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.toggleConnection()
                    }
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── 顶栏 ──────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(
    routingMode: com.dokodemo.data.preferences.RoutingMode,
    onRoutingModeChange: (com.dokodemo.data.preferences.RoutingMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            com.dokodemo.ui.components.DokoLogo(modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DokoDemo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val currentRoutingTitle = when (routingMode) {
            com.dokodemo.data.preferences.RoutingMode.GLOBAL -> stringResource(R.string.routing_global_title)
            com.dokodemo.data.preferences.RoutingMode.BYPASS_CN -> stringResource(R.string.routing_bypass_cn_title)
            com.dokodemo.data.preferences.RoutingMode.SPLIT -> stringResource(R.string.routing_split_title)
        }

        Box {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentRoutingTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = "Select Routing Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                com.dokodemo.data.preferences.RoutingMode.entries.forEach { mode ->
                    val title = when (mode) {
                        com.dokodemo.data.preferences.RoutingMode.GLOBAL -> stringResource(R.string.routing_global_title)
                        com.dokodemo.data.preferences.RoutingMode.BYPASS_CN -> stringResource(R.string.routing_bypass_cn_title)
                        com.dokodemo.data.preferences.RoutingMode.SPLIT -> stringResource(R.string.routing_split_title)
                    }
                    DropdownMenuItem(
                        text = { Text(title) },
                        onClick = {
                            onRoutingModeChange(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ─── 状态标签 ─────────────────────────────────────────────────────────────
@Composable
private fun StatusBadge(isConnected: Boolean, isConnecting: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isConnecting -> MaterialTheme.colorScheme.outline
            isConnected  -> AccentGreen.copy(alpha = 0.15f)
            else         -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(400), label = "statusBg"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            isConnecting -> MaterialTheme.colorScheme.onSurfaceVariant
            isConnected  -> AccentGreen
            else         -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(400), label = "statusText"
    )
    val dotColor by animateColorAsState(
        targetValue = when {
            isConnecting -> MaterialTheme.colorScheme.outline
            isConnected  -> AccentGreen
            else         -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(400), label = "dotColor"
    )
    val statusText = when {
        isConnecting -> stringResource(R.string.connecting)
        isConnected  -> stringResource(R.string.connected)
        else         -> stringResource(R.string.disconnected)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(text = statusText, color = textColor, style = MaterialTheme.typography.labelMedium)
    }
}

// ─── 大圆形连接按钮 ───────────────────────────────────────────────────────
@Composable
private fun ConnectButton(
    isConnected: Boolean,
    isConnecting: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    // 连接时的光圈脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    val bgColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.background,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btnBg"
    )
    val textColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        label = "btnText"
    )
    val buttonText = when {
        isConnecting -> stringResource(R.string.connecting)
        isConnected  -> stringResource(R.string.disconnect)
        else         -> stringResource(R.string.connect)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp) // 预留呼吸效果的空间，防止连接成功后页面闪动
    ) {
        // 连接时的呼吸光环 (Diffused Soft Mint Green internal glow / breathing halo)
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(AccentState.copy(alpha = pulseAlpha))
            )
        }

        // 主按钮
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable(enabled = isEnabled && !isConnecting, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Very soft diffused glow behind icon when connected
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(AccentState.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = buttonText,
                    tint = textColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

// ─── 当前节点卡片 ─────────────────────────────────────────────────────────
@Composable
private fun CurrentNodeCard(
    serverName: String,
    protocol: String,
    latency: String,
    isPinging: Boolean,
    onClick: () -> Unit,
    onPingClick: () -> Unit
) {
    com.dokodemo.ui.components.DokoCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.current_node),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = serverName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (protocol.isNotEmpty()) {
                        Text(
                            text = protocol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = latency.ifEmpty { "--" },
                        fontFamily = MonoFont,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            latency.isEmpty() || latency == "--" -> MaterialTheme.colorScheme.onSurfaceVariant
                            latency.removeSuffix("ms").toIntOrNull()?.let { it < 100 } == true -> AccentGreen
                            latency.removeSuffix("ms").toIntOrNull()?.let { it < 200 } == true -> AccentOrange
                            else -> AccentRed
                        },
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onPingClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Ping",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.switch_node),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── 流量监控卡片 ────────────────────────────────────────────────────────
@Composable
private fun TrafficMonitorCard(
    uploadSpeed: String,
    downloadSpeed: String,
    speedHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF7CAEE0), Color(0xFF5A8BB5))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.traffic_monitor),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TrafficLabel("↑", uploadSpeed)
                    TrafficLabel("↓", downloadSpeed)
                }
            }
            Spacer(Modifier.height(12.dp))
            // 折线图
            SpeedGraph(
                dataPoints = speedHistory,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}

@Composable
private fun TrafficLabel(arrow: String, speed: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(arrow, color = Color.White, style = MaterialTheme.typography.labelSmall)
        Text(speed.ifEmpty { "0 B/s" }, fontFamily = MonoFont, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SpeedGraph(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = Color.White
    val gridColor = Color.White.copy(alpha = 0.2f)
    Canvas(modifier = modifier) {
        if (dataPoints.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val step = w / (dataPoints.size - 1).coerceAtLeast(1)
        // 网格线
        for (i in 1..3) {
            val y = h * i / 4
            drawLine(gridColor.copy(alpha = 0.3f), Offset(0f, y), Offset(w, y), 0.5f)
        }
        // 折线
        val path = Path().apply {
            dataPoints.forEachIndexed { idx, v ->
                val x = idx * step
                val y = h - (v * h * 0.85f).coerceIn(0f, h)
                if (idx == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
    }
}

// ─── 底部导航 ────────────────────────────────────────────────────────────
@Composable
fun HomeBottomNav(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToServerList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    NavigationBar(
        containerColor = Color(0x991E2429), // Dark Glass
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0), // Scaffold handles system insets
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(56.dp)
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = Color(0x33E2E8F0), // 20% opacity white border
                shape = CircleShape
            )
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )

        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.home)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == "nodes",
            onClick = onNavigateToServerList,
            icon = { Icon(Icons.AutoMirrored.Rounded.List, contentDescription = stringResource(R.string.nodes)) },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = onNavigateToSettings,
            icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings)) },
            colors = navItemColors
        )
    }
}

// ─── 空状态卡片 ───────────────────────────────────────────────────────────
@Composable
fun HomeEmptyStateCard(onClick: () -> Unit) {
    com.dokodemo.ui.components.DokoCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_to_dokodemo),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.click_to_add_first_node),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
