package com.dokodemo.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
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
                .wrapContentWidth(Alignment.CenterHorizontally)
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── 顶栏 ─────────────────────────────────────────────────────
            TopBar(
                routingMode = uiState.routingMode,
                onRoutingModeChange = { viewModel.setRoutingMode(it) }
            )

            if (uiState.isApplyingConnectionChanges) {
                Text(
                    text = stringResource(R.string.connection_applying_changes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            TrafficMonitorCard(uiState.uploadSpeed, uiState.downloadSpeed)
            Spacer(Modifier.height(16.dp))

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

            uiState.connectionError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp))
                TextButton(onClick = onNavigateToServerList) { Text(stringResource(R.string.node_list)) }
            }
            // ─── 大圆形连接按钮 ───────────────────────────────────────────
            ConnectButton(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting,
                isEnabled = uiState.isConnected || uiState.isConnecting || !uiState.hasNoServers,
                onClick = {
                    if (uiState.currentServer == null && !uiState.isConnected && !uiState.isConnecting) {
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
                color = MaterialTheme.colorScheme.surface,
                shape = CircleShape,
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
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
                        contentDescription = stringResource(R.string.routing_mode),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
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
    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val dotColor = when {
        isConnecting -> MaterialTheme.colorScheme.primary
        isConnected -> AccentState
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
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
    val bgColor by animateColorAsState(
        targetValue = if (isConnected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(250), label = "connectionFill"
    )
    val buttonText = when {
        isConnecting -> stringResource(R.string.cancel)
        isConnected -> stringResource(R.string.home_disconnect_hint)
        else -> stringResource(R.string.home_connect_hint)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(180.dp).clip(CircleShape)
                .background(bgColor)
                .clickable(enabled = isEnabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(84.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 3.dp
                )
            }
            Icon(
                Icons.Rounded.PowerSettingsNew, contentDescription = buttonText,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = if (isEnabled) 1f else 0.38f),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(buttonText, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.current_node), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(serverName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(protocol, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onPingClick, enabled = !isPinging,
                        contentPadding = PaddingValues(horizontal = 8.dp)) {
                        if (isPinging) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("·  " + latency.ifEmpty { "--" }, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.Refresh, stringResource(R.string.ping_all), Modifier.size(14.dp))
                    }
                }
            }
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ChevronRight, stringResource(R.string.switch_node),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TrafficMonitorCard(uploadSpeed: String, downloadSpeed: String) {
    com.dokodemo.ui.components.DokoCard {
        Row(Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
            TrafficLabel("↑", stringResource(R.string.upload_speed), uploadSpeed, Modifier.weight(1f))
            TrafficLabel("↓", stringResource(R.string.download_speed), downloadSpeed, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TrafficLabel(arrow: String, label: String, speed: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$arrow  $label", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(speed.ifEmpty { "0 B/s" }, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0), // Scaffold handles system insets
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .height(56.dp)
            .clip(CircleShape)

    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
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
        containerColor = MaterialTheme.colorScheme.surface
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
