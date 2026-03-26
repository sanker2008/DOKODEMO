package com.dokodemo.service

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.dokodemo.core.CoreManager
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VpnController - High-level API for VPN operations
 * 
 * Use this class from ViewModels to control VPN connections.
 */
@Singleton
class VpnController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coreManager: CoreManager,
    private val appPreferences: AppPreferences
) {
    
    /**
     * Check if VPN permission is granted
     */
    fun isVpnPermissionGranted(): Boolean {
        return VpnService.prepare(context) == null
    }
    
    /**
     * Get intent to request VPN permission
     */
    fun getVpnPermissionIntent(): Intent? {
        return VpnService.prepare(context)
    }
    
    /**
     * Start VPN connection with the given server profile
     */
    suspend fun connect(profile: ServerProfile) {
        val routingMode = appPreferences.routingMode.first()
        val proxiedApps = appPreferences.proxiedApps.first()
        
        // Generate initial config (service will handle retries via CoreManager)
        val configJson = coreManager.generateConfig(profile, routingMode)
        
        val intent = Intent(context, DokoDemoVpnService::class.java).apply {
            action = DokoDemoVpnService.ACTION_START
            putExtra(DokoDemoVpnService.EXTRA_SERVER_CONFIG, configJson)
            putExtra(DokoDemoVpnService.EXTRA_SERVER_NAME, profile.name)
            putExtra("routing_mode", routingMode.name)
            putExtra("server_address", profile.address)
            putExtra("server_port", profile.port)
            putStringArrayListExtra("proxied_apps", ArrayList(proxiedApps.toList()))
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    /**
     * Start VPN with raw JSON config
     */
    fun connectWithConfig(configJson: String, serverName: String = "Custom") {
        val intent = Intent(context, DokoDemoVpnService::class.java).apply {
            action = DokoDemoVpnService.ACTION_START
            putExtra(DokoDemoVpnService.EXTRA_SERVER_CONFIG, configJson)
            putExtra(DokoDemoVpnService.EXTRA_SERVER_NAME, serverName)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
    
    /**
     * Disconnect VPN
     */
    fun disconnect() {
        val intent = Intent(context, DokoDemoVpnService::class.java).apply {
            action = DokoDemoVpnService.ACTION_STOP
        }
        
        context.startService(intent)
    }
    
    /**
     * Check if VPN is currently connected
     */
    fun isConnected(): Boolean {
        return DokoDemoVpnService.isServiceRunning()
    }
    
    /**
     * Get current upload speed (bytes per second)
     */
    fun getUploadSpeed(): Long {
        return DokoDemoVpnService.getUploadSpeed()
    }
    
    /**
     * Get current download speed (bytes per second)
     */
    fun getDownloadSpeed(): Long {
        return DokoDemoVpnService.getDownloadSpeed()
    }
    
    /**
     * Get V2Ray core version
     */
    fun getCoreVersion(): String {
        return coreManager.getVersion()
    }
}
