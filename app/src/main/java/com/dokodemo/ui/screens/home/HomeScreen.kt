package com.dokodemo.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.ui.components.LargeIndustrialButton
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.SolidColor

@Composable
fun HomeScreen(
    onNavigateToServerList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentRoute = "home",
                onNavigateToHome = { },
                onNavigateToServerList = onNavigateToServerList,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Top bar with logo and title
            TopBar()
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Status bar
            StatusBar(
                isConnected = uiState.isConnected,
                isConnecting = uiState.isConnecting
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Target Server Card
            TargetServerCard(
                serverName = uiState.currentServer?.name ?: "No Server Selected",
                region = uiState.currentServerRegion,
                ping = uiState.ping,
                onClick = onNavigateToServerList
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main Connect Button
            LargeIndustrialButton(
                text = if (uiState.isConnecting) "CONNECTING" else if (uiState.isConnected) "DISCONNECT" else "CONNECT",
                onClick = { viewModel.toggleConnection() },
                isActive = uiState.isConnected,
                subText = if (uiState.isConnecting) "[ PLEASE WAIT ]" else "[ INIT_HANDSHAKE_V4 ]",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Connection info row
            ConnectionInfoRow(
                ipAddress = uiState.ipAddress,
                protocol = uiState.protocol,
                encryption = uiState.encryption
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Traffic Monitor
            TrafficMonitor(
                uploadSpeed = uiState.uploadSpeed,
                downloadSpeed = uiState.downloadSpeed,
                speedHistory = uiState.speedHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo icon (simplified)
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◈",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
        }
        
        // Title
        Text(
            text = "DOKODEMO // VPN",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        
        // Profile icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StatusBar(
    isConnected: Boolean,
    isConnecting: Boolean
) {
    val isLight = MaterialTheme.colorScheme.background == IndustrialWhite
    
    val statusText = when {
        isConnecting -> "CONNECTING"
        isConnected -> "CONNECTED"
        else -> "DISCONNECTED"
    }
    
    val securityText = when {
        isConnected -> ":: SECURED ::"
        else -> ":: UNSECURED ::"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "STATUS: ",
                color = TextGrey,
                fontFamily = MonospaceFont,
                fontSize = 11.sp
            )
            Text(
                modifier = if (isLight && (isConnected || isConnecting)) Modifier.background(AcidLime).padding(horizontal = 4.dp) else Modifier,
                text = statusText,
                color = if (isLight) IndustrialBlack else if (isConnected) AcidLime else if (isConnecting) AcidLime.copy(alpha = 0.6f) else IndustrialWhite,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }
        
        Text(
            text = securityText,
            color = if (isConnected) AcidLime else TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TargetServerCard(
    serverName: String,
    region: String,
    ping: String,
    onClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineColor)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
    ) {
        // Radar/map background visual
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp)
        ) {
            // Draw grid lines
            val gridSpacing = 20.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = outlineColor.copy(alpha = 0.2f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.5f
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = outlineColor.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.5f
                )
                y += gridSpacing
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AcidLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TARGET SERVER",
                        color = IndustrialBlack,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = serverName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "//$region",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = MonospaceFont,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "◉ PING: $ping",
                    color = TextGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 11.sp
                )
            }
            
            // Dropdown arrow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, outlineColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▽",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ConnectionInfoRow(
    ipAddress: String,
    protocol: String,
    encryption: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoItem(label = "IP_ADDR", value = ipAddress, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        InfoItem(label = "PROTOCOL", value = protocol, modifier = Modifier.weight(0.7f))
        Spacer(modifier = Modifier.width(8.dp))
        InfoItem(label = "ENCRYPTION", value = encryption, modifier = Modifier.weight(0.7f))
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TrafficMonitor(
    uploadSpeed: String,
    downloadSpeed: String,
    speedHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRAFFIC_MONITOR",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            
            Row {
                Text(
                    text = "UP: ",
                    color = TextGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp
                )
                Text(
                    text = uploadSpeed,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "DL: ",
                    color = TextGrey,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp
                )
                Text(
                    text = downloadSpeed,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Graph
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            OscilloscopeGraph(
                dataPoints = speedHistory,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun OscilloscopeGraph(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val isLight = MaterialTheme.colorScheme.background == IndustrialWhite
    val lineColor = if (isLight) IndustrialBlack else AcidLime
    
    Canvas(modifier = modifier) {
        if (dataPoints.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
        
        // Draw grid
        val gridLines = 4
        for (i in 1 until gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = outlineColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.5f
            )
        }
        
        // Draw waveform
        val path = Path().apply {
            dataPoints.forEachIndexed { index, value ->
                val x = index * stepX
                val y = height - (value * height * 0.8f) - (height * 0.1f)
                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }
        
        // Draw the line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw glow effect (thicker, semi-transparent)
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.3f),
            style = Stroke(width = 6.dp.toPx())
        )
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String,
    onNavigateToHome: () -> Unit,
    onNavigateToServerList: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = "⌂",
            label = "HOME",
            isSelected = currentRoute == "home",
            onClick = onNavigateToHome
        )
        BottomNavItem(
            icon = "⊞",
            label = "SERVERS",
            isSelected = currentRoute == "servers",
            onClick = onNavigateToServerList
        )
        BottomNavItem(
            icon = "⚙",
            label = "SETTINGS",
            isSelected = currentRoute == "settings",
            onClick = onNavigateToSettings
        )
    }
}

@Composable
private fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background == IndustrialWhite
    val selectedBg = if (isSelected && isLight) AcidLime else Color.Transparent
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .background(selectedBg)
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        val contentColor = if (isSelected) {
            if (isLight) IndustrialBlack else AcidLime 
        } else TextGrey

        Text(
            text = icon,
            color = contentColor,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = contentColor,
            fontFamily = MonospaceFont,
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
    }
}
