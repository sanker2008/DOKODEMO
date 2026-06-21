package com.dokodemo.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import com.dokodemo.data.repository.ServerRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DokoDemoTileService : TileService() {

    @Inject
    lateinit var vpnController: VpnController

    @Inject
    lateinit var serverRepository: ServerRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var vpnStateReceiver: BroadcastReceiver? = null

    override fun onStartListening() {
        super.onStartListening()
        registerReceiver()
        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
        unregisterReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        
        if (vpnController.isConnected()) {
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
            vpnController.disconnect()
        } else {
            if (!vpnController.isVpnPermissionGranted()) {
                openMainActivity()
                return
            }
            
            // Set state immediately for better UX
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
            
            serviceScope.launch {
                val server = serverRepository.getSelectedServer().firstOrNull()
                if (server != null) {
                    vpnController.connect(server)
                } else {
                    // Open main activity to select a server if no server is selected
                    openMainActivity()
                }
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, com.dokodemo.MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun registerReceiver() {
        if (vpnStateReceiver == null) {
            vpnStateReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    updateTileState()
                }
            }
            val filter = IntentFilter().apply {
                addAction(DokoDemoVpnService.ACTION_VPN_CONNECTED)
                addAction(DokoDemoVpnService.ACTION_VPN_DISCONNECTED)
            }
            ContextCompat.registerReceiver(
                this,
                vpnStateReceiver!!,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    private fun unregisterReceiver() {
        vpnStateReceiver?.let {
            unregisterReceiver(it)
            vpnStateReceiver = null
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isConnected = vpnController.isConnected()
        
        tile.state = if (isConnected) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
