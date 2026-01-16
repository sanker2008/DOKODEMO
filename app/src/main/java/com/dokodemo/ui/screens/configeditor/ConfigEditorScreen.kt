package com.dokodemo.ui.screens.configeditor

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokodemo.data.model.Protocol
import com.dokodemo.ui.components.IndustrialButton
import com.dokodemo.ui.components.IndustrialInput
import com.dokodemo.ui.components.IndustrialTabButton
import com.dokodemo.ui.components.IndustrialToggleRow
import com.dokodemo.ui.theme.AcidLime
import com.dokodemo.ui.theme.IndustrialBlack
import com.dokodemo.ui.theme.IndustrialGrey
import com.dokodemo.ui.theme.IndustrialWhite
import com.dokodemo.ui.theme.MonospaceFont
import com.dokodemo.ui.theme.TextGrey

@Composable
fun ConfigEditorScreen(
    serverId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: ConfigEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(serverId) {
        serverId?.let { viewModel.loadServer(it) }
    }
    
    Scaffold(
        containerColor = IndustrialBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            ConfigEditorTopBar(
                onCancel = onNavigateBack
            )
            
            // Status indicator
            StatusIndicator(
                isOnline = uiState.isOnline,
                ping = uiState.ping
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Protocol selection
            ProtocolSelector(
                selectedProtocol = uiState.protocol,
                onProtocolSelected = { viewModel.updateProtocol(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form fields
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Remarks/Name
                IndustrialInput(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = "REMARKS:",
                    placeholder = "OSAKA_NODE_01",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Address and Port row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IndustrialInput(
                        value = uiState.address,
                        onValueChange = { viewModel.updateAddress(it) },
                        label = "ADDRESS:",
                        placeholder = "192.168.1.45",
                        modifier = Modifier.weight(1f)
                    )
                    
                    IndustrialInput(
                        value = uiState.port,
                        onValueChange = { viewModel.updatePort(it) },
                        label = "PORT:",
                        placeholder = "443",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.width(100.dp)
                    )
                }
                
                // Error messages
                uiState.addressError?.let {
                    Text(
                        text = it,
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                uiState.portError?.let {
                    Text(
                        text = it,
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // UUID/Password
                IndustrialInput(
                    value = uiState.uuid,
                    onValueChange = { viewModel.updateUuid(it) },
                    label = "UUID / PASSWORD:",
                    placeholder = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        Row {
                            // Copy button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, IndustrialGrey)
                                    .clickable { /* Copy */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⎘",
                                    color = TextGrey,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            // Refresh button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .border(1.dp, IndustrialGrey)
                                    .clickable { /* Generate */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "↻",
                                    color = TextGrey,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                )
                
                uiState.uuidError?.let {
                    Text(
                        text = it,
                        color = AcidLime,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // TLS Settings
                IndustrialToggleRow(
                    label = "ALLOW INSECURE",
                    subtitle = "Skip certificate verification",
                    checked = uiState.allowInsecure,
                    onCheckedChange = { viewModel.updateAllowInsecure(it) }
                )
                
                IndustrialToggleRow(
                    label = "TLS",
                    subtitle = "Transport Layer Security",
                    checked = uiState.useTls,
                    onCheckedChange = { viewModel.updateUseTls(it) }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button
            IndustrialButton(
                text = "⬇ SAVE CONFIG",
                onClick = { viewModel.saveConfig(onNavigateBack) },
                isActive = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ConfigEditorTopBar(
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "CANCEL",
            color = TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onCancel() }
        )
        
        Text(
            text = "SERVER CONFIG",
            color = IndustrialWhite,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        )
        
        // Placeholder for symmetry
        Spacer(modifier = Modifier.width(60.dp))
    }
}

@Composable
private fun StatusIndicator(
    isOnline: Boolean,
    ping: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Online status badge
        Box(
            modifier = Modifier
                .border(1.dp, if (isOnline) AcidLime else IndustrialGrey)
                .background(IndustrialBlack)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isOnline) AcidLime else TextGrey)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) "ONLINE" else "OFFLINE",
                    color = if (isOnline) AcidLime else TextGrey,
                    fontFamily = MonospaceFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Ping
        Text(
            text = "PING: ",
            color = TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 11.sp
        )
        Text(
            text = ping,
            color = AcidLime,
            fontFamily = MonospaceFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProtocolSelector(
    selectedProtocol: Protocol,
    onProtocolSelected: (Protocol) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "PROTOCOL:",
            color = TextGrey,
            fontFamily = MonospaceFont,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            IndustrialTabButton(
                text = "VMESS",
                isSelected = selectedProtocol == Protocol.VMESS,
                onClick = { onProtocolSelected(Protocol.VMESS) }
            )
            IndustrialTabButton(
                text = "VLESS",
                isSelected = selectedProtocol == Protocol.VLESS,
                onClick = { onProtocolSelected(Protocol.VLESS) }
            )
            IndustrialTabButton(
                text = "TROJAN",
                isSelected = selectedProtocol == Protocol.TROJAN,
                onClick = { onProtocolSelected(Protocol.TROJAN) }
            )
        }
    }
}
