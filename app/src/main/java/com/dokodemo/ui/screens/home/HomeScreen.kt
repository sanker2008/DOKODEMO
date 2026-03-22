package com.dokodemo.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    LaunchedEffect(uiState.needsVpnPermission, uiState.vpnPermissionIntent) {
        if (uiState.needsVpnPermission) {
            uiState.vpnPermissionIntent?.let { intent ->
                vpnPermissionLauncher.launch(intent)
            }
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
            TopBar(onNavigateToSettings)

            Spacer(Modifier.height(8.dp))

            // ─── 连接状态标签 ─────────────────────────────────────────────
            StatusBadge(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting
            )

            Spacer(Modifier.height(32.dp))

            // ─── 大圆形连接按钮 ───────────────────────────────────────────
            ConnectButton(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting,
                onClick = {
                    if (uiState.currentServer == null) {
                        Toast.makeText(context, "请先选择一个节点", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.toggleConnection()
                    }
                }
            )

            Spacer(Modifier.height(32.dp))

            // ─── 当前节点卡片 ─────────────────────────────────────────────
            CurrentNodeCard(
                serverName = uiState.currentServer?.name ?: "未选择节点",
                protocol = uiState.protocol,
                latency = uiState.ping,
                onClick = onNavigateToServerList
            )

            Spacer(Modifier.height(16.dp))

            // ─── 运行时长（已连接时显示） ─────────────────────────────────
            if (uiState.isConnected) {
                Text(
                    text = "已连接 ${uiState.ipAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
            }

            // ─── 流量监控（折线图） ────────────────────────────────────────
            TrafficMonitorCard(
                uploadSpeed = uiState.uploadSpeed,
                downloadSpeed = uiState.downloadSpeed,
                speedHistory = uiState.speedHistory,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── 顶栏 ──────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SanProxy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        IconButton(onClick = onSettings) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "设置",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        isConnecting -> "连接中…"
        isConnected  -> "已连接"
        else         -> "未连接"
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
        targetValue = if (isConnected) Primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "btnBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "btnText"
    )
    val buttonText = when {
        isConnecting -> "连接中"
        isConnected  -> "断开"
        else         -> "连接"
    }

    Box(contentAlignment = Alignment.Center) {
        // 连接时的光圈（仅已连接状态显示）
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = pulseAlpha))
            )
        }

        // 主按钮
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    brush = if (isConnected) {
                        Brush.radialGradient(listOf(PrimaryLight, Primary))
                    } else {
                        Brush.radialGradient(listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surfaceVariant
                        ))
                    }
                )
                .clickable(enabled = !isConnecting, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isConnected) "⏹" else "▶",
                    fontSize = 28.sp,
                    color = textColor
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "当前节点",
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
                if (protocol.isNotEmpty()) {
                    Text(
                        text = protocol,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
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
                Text(
                    text = "切换 →",
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "流量监控",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Text(arrow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
        Text(speed.ifEmpty { "0 B/s" }, fontFamily = MonoFont, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SpeedGraph(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline
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
        drawPath(path, lineColor, style = Stroke(width = 2.dp.toPx()))
        drawPath(path, lineColor.copy(alpha = 0.2f), style = Stroke(width = 6.dp.toPx()))
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onNavigateToHome,
            icon = { Text("🏠", fontSize = 20.sp) },
            label = { Text("主页") }
        )
        NavigationBarItem(
            selected = currentRoute == "nodes",
            onClick = onNavigateToServerList,
            icon = { Text("📋", fontSize = 20.sp) },
            label = { Text("节点") }
        )
        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = onNavigateToSettings,
            icon = { Text("⚙️", fontSize = 20.sp) },
            label = { Text("设置") }
        )
    }
}
