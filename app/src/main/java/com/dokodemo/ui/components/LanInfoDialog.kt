package com.dokodemo.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dokodemo.R

@Composable
fun LanInfoDialog(
    ipAddress: String,
    httpPort: Int,
    socksPort: Int,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.lan_proxy_info_title))
        },
        text = {
            Text(stringResource(R.string.lan_proxy_info_desc, ipAddress, httpPort, socksPort))
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}
