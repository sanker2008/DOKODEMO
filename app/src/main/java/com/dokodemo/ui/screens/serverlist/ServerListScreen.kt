package com.dokodemo.ui.screens.serverlist

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.ui.components.IndustrialSearchInput
import com.dokodemo.ui.components.SquareFab
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey
import androidx.compose.material3.MaterialTheme

@Composable
fun ServerListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    onNavigateToConfigEditor: (Long?) -> Unit,
    viewModel: ServerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            SquareFab(
                onClick = onNavigateToAddProfile
            ) {
                Text(
                    text = "+",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            ServerListTopBar(
                onNavigateBack = onNavigateBack,
                onRefresh = { viewModel.refreshServers() }
            )
            
            // Search Bar
            IndustrialSearchInput(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = "SEARCH_LOCALE...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Server List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
            ) {
                items(
                    items = viewModel.filteredServers,
                    key = { it.id }
                ) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = server.id == uiState.selectedServerId,
                        onClick = { viewModel.selectServer(server.id) },
                        onDelete = { viewModel.deleteServer(server) }
                    )
                }
            }
            
            // Bottom Status Bar
            BottomStatusBar(
                systemStatus = uiState.systemStatus,
                version = uiState.version,
                currentUplink = uiState.currentUplink
            )
        }
    }
}

@Composable
private fun ServerListTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .clickable { onNavigateBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
        }
        
        // Title
        Text(
            text = "SERVER LIST",
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = 2.sp
        )
        
        // Refresh button
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↻",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun ServerListItem(
    server: ServerItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
    val borderColor = if (server.isConnected) AcidLime else MaterialTheme.colorScheme.outline
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, borderColor, RectangleShape)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section: Country code + Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Country code box
                Box(
                    modifier = Modifier
                        .border(1.dp, if (server.isConnected) AcidLime else MaterialTheme.colorScheme.outline)
                        .background(if (server.isConnected) AcidLime else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = server.countryCode,
                        color = if (server.isConnected) IndustrialBlack else MaterialTheme.colorScheme.onSurface,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Server info
                Column {
                    Text(
                        text = server.name,
                        color = if (server.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "PROTOCOL: ${server.protocol}",
                        color = TextGrey,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp
                    )
                }
            }
            
            // Right section: Ping + Signal + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ping value
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = server.ping?.let { "${it}ms" } ?: "--ms",
                        color = MaterialTheme.colorScheme.primary,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (server.isConnected) {
                        Text(
                            text = "CONNECTED",
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = MonospaceFont,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Signal bars
                SignalBars(
                    strength = when {
                        server.ping == null -> 0
                        server.ping < 50 -> 4
                        server.ping < 100 -> 3
                        server.ping < 150 -> 2
                        else -> 1
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Delete Button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalBars(strength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 1..4) {
            val height = (8 + i * 4).dp
            val isActive = i <= strength
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .background(if (isActive) AcidLime else MaterialTheme.colorScheme.outline)
            )
        }
    }
}

@Composable
private fun BottomStatusBar(
    systemStatus: String,
    version: String,
    currentUplink: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AcidLime)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = systemStatus,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = MonospaceFont,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
            
            Text(
                text = version,
                color = TextGrey,
                fontFamily = MonospaceFont,
                fontSize = 10.sp
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row {
            Text(
                text = "CURRENT UPLINK:",
                color = TextGrey,
                fontFamily = MonospaceFont,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currentUplink,
                color = MaterialTheme.colorScheme.primary,
                fontFamily = MonospaceFont,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
