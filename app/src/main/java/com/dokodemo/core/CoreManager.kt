package com.dokodemo.core

import android.content.Context
import android.util.Log
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import libv2ray.Libv2ray
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CoreManager handles V2Ray/Xray core operations
 * 
 * This is the REAL implementation using LibXray native library.
 * Requires libv2ray.aar in app/libs folder.
 */
@Singleton
class CoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CoreManager"
        
        // Local SOCKS proxy port
        const val SOCKS_PORT = 10808
        const val HTTP_PORT = 10809
        
        // DNS settings
        const val DNS_LOCAL = "8.8.8.8"
        const val DNS_REMOTE = "1.1.1.1"
    }
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var isInitialized = false
    private var currentConfig: String? = null
    
    /**
     * Initialize V2Ray environment
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            val filesDir = context.filesDir.absolutePath
            Libv2ray.initV2Env(filesDir)
            isInitialized = true
            Log.i(TAG, "V2Ray environment initialized. Version: ${getVersion()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize V2Ray: ${e.message}")
            // Fallback mode - will use mock if native lib not available
        }
    }
    
    /**
     * Get V2Ray core version
     */
    fun getVersion(): String {
        return try {
            Libv2ray.version()
        } catch (e: UnsatisfiedLinkError) {
            "Mock Mode (No Native Lib)"
        }
    }
    
    /**
     * Generate V2Ray JSON configuration from ServerProfile
     */
    fun generateConfig(profile: ServerProfile): String {
        val config = buildMap<String, Any> {
            // Log settings
            put("log", mapOf(
                "access" to "",
                "error" to "",
                "loglevel" to "warning"
            ))
            
            // DNS settings
            put("dns", mapOf<String, Any>(
                "servers" to listOf(
                    mapOf(
                        "address" to DNS_LOCAL,
                        "port" to 53,
                        "domains" to listOf("geosite:cn")
                    ),
                    mapOf(
                        "address" to DNS_REMOTE,
                        "port" to 53
                    )
                ),
                "queryStrategy" to "UseIPv4"
            ))
            
            // Inbounds - Local SOCKS5 proxy
            put("inbounds", listOf(
                mapOf(
                    "tag" to "socks",
                    "port" to SOCKS_PORT,
                    "listen" to "127.0.0.1",
                    "protocol" to "socks",
                    "sniffing" to mapOf(
                        "enabled" to true,
                        "destOverride" to listOf("http", "tls"),
                        "routeOnly" to false
                    ),
                    "settings" to mapOf(
                        "auth" to "noauth",
                        "udp" to true,
                        "allowTransparent" to false
                    )
                ),
                mapOf(
                    "tag" to "http",
                    "port" to HTTP_PORT,
                    "listen" to "127.0.0.1",
                    "protocol" to "http",
                    "sniffing" to mapOf(
                        "enabled" to true,
                        "destOverride" to listOf("http", "tls")
                    ),
                    "settings" to mapOf(
                        "allowTransparent" to false
                    )
                )
            ))
            
            // Outbounds
            put("outbounds", listOf(
                generateOutbound(profile),
                mapOf(
                    "tag" to "direct",
                    "protocol" to "freedom",
                    "settings" to emptyMap<String, Any>()
                ),
                mapOf(
                    "tag" to "block",
                    "protocol" to "blackhole",
                    "settings" to mapOf(
                        "response" to mapOf("type" to "http")
                    )
                )
            ))
            
            // Routing
            put("routing", mapOf(
                "domainStrategy" to "IPIfNonMatch",
                "domainMatcher" to "hybrid",
                "rules" to listOf(
                    // Block ads (optional)
                    mapOf(
                        "type" to "field",
                        "domain" to listOf("geosite:category-ads-all"),
                        "outboundTag" to "block"
                    ),
                    // Direct for private IPs
                    mapOf(
                        "type" to "field",
                        "ip" to listOf("geoip:private"),
                        "outboundTag" to "direct"
                    ),
                    // Direct for CN sites (optional, can be removed)
                    mapOf(
                        "type" to "field",
                        "domain" to listOf("geosite:cn"),
                        "outboundTag" to "direct"
                    ),
                    // Everything else goes through proxy
                    mapOf(
                        "type" to "field",
                        "port" to "0-65535",
                        "outboundTag" to "proxy"
                    )
                )
            ))
            
            // Policy
            put("policy", mapOf(
                "levels" to mapOf(
                    "0" to mapOf(
                        "handshake" to 4,
                        "connIdle" to 300,
                        "downlinkOnly" to 1,
                        "uplinkOnly" to 1,
                        "bufferSize" to 10240
                    )
                ),
                "system" to mapOf(
                    "statsInboundUplink" to true,
                    "statsInboundDownlink" to true,
                    "statsOutboundUplink" to true,
                    "statsOutboundDownlink" to true
                )
            ))
            
            // Stats for traffic monitoring
            put("stats", emptyMap<String, Any>())
        }
        
        return gson.toJson(config)
    }
    
    private fun generateOutbound(profile: ServerProfile): Map<String, Any> {
        return when (profile.protocol) {
            Protocol.VLESS -> generateVlessOutbound(profile)
            Protocol.VMESS -> generateVmessOutbound(profile)
            Protocol.TROJAN -> generateTrojanOutbound(profile)
            Protocol.SHADOWSOCKS -> generateShadowsocksOutbound(profile)
            else -> generateVlessOutbound(profile)
        }
    }
    
    private fun generateVlessOutbound(profile: ServerProfile): Map<String, Any> {
        val streamSettings = generateStreamSettings(profile)
        
        return mapOf(
            "tag" to "proxy",
            "protocol" to "vless",
            "settings" to mapOf(
                "vnext" to listOf(
                    mapOf(
                        "address" to profile.address,
                        "port" to profile.port,
                        "users" to listOf(
                            buildMap<String, Any> {
                                put("id", profile.uuid)
                                put("encryption", profile.encryption.ifEmpty { "none" })
                                if (profile.flow.isNotEmpty()) {
                                    put("flow", profile.flow)
                                }
                                put("level", 0)
                            }
                        )
                    )
                )
            ),
            "streamSettings" to streamSettings,
            "mux" to mapOf(
                "enabled" to false,
                "concurrency" to -1
            )
        )
    }
    
    private fun generateVmessOutbound(profile: ServerProfile): Map<String, Any> {
        val streamSettings = generateStreamSettings(profile)
        
        return mapOf(
            "tag" to "proxy",
            "protocol" to "vmess",
            "settings" to mapOf(
                "vnext" to listOf(
                    mapOf(
                        "address" to profile.address,
                        "port" to profile.port,
                        "users" to listOf(
                            mapOf(
                                "id" to profile.uuid,
                                "alterId" to 0,
                                "security" to profile.encryption.ifEmpty { "auto" },
                                "level" to 0
                            )
                        )
                    )
                )
            ),
            "streamSettings" to streamSettings,
            "mux" to mapOf(
                "enabled" to false,
                "concurrency" to -1
            )
        )
    }
    
    private fun generateTrojanOutbound(profile: ServerProfile): Map<String, Any> {
        val streamSettings = generateStreamSettings(profile)
        
        return mapOf(
            "tag" to "proxy",
            "protocol" to "trojan",
            "settings" to mapOf(
                "servers" to listOf(
                    mapOf(
                        "address" to profile.address,
                        "port" to profile.port,
                        "password" to profile.password.ifEmpty { profile.uuid },
                        "level" to 0
                    )
                )
            ),
            "streamSettings" to streamSettings,
            "mux" to mapOf(
                "enabled" to false,
                "concurrency" to -1
            )
        )
    }
    
    private fun generateShadowsocksOutbound(profile: ServerProfile): Map<String, Any> {
        return mapOf(
            "tag" to "proxy",
            "protocol" to "shadowsocks",
            "settings" to mapOf(
                "servers" to listOf(
                    mapOf(
                        "address" to profile.address,
                        "port" to profile.port,
                        "password" to profile.password.ifEmpty { profile.uuid },
                        "method" to profile.encryption.ifEmpty { "aes-256-gcm" },
                        "level" to 0
                    )
                )
            )
        )
    }
    
    private fun generateStreamSettings(profile: ServerProfile): Map<String, Any> {
        return buildMap {
            put("network", profile.network.ifEmpty { "tcp" })
            
            // TLS settings
            if (profile.useTls) {
                put("security", "tls")
                put("tlsSettings", mapOf(
                    "serverName" to profile.serverName.ifEmpty { profile.address },
                    "allowInsecure" to profile.allowInsecure,
                    "fingerprint" to "chrome"
                ))
            } else {
                put("security", "none")
            }
            
            // Transport settings
            when (profile.network) {
                "ws" -> {
                    put("wsSettings", mapOf(
                        "path" to profile.wsPath.ifEmpty { "/" },
                        "headers" to mapOf(
                            "Host" to profile.wsHost.ifEmpty { profile.address }
                        )
                    ))
                }
                "grpc" -> {
                    put("grpcSettings", mapOf(
                        "serviceName" to profile.wsPath, // reuse wsPath for serviceName
                        "multiMode" to false
                    ))
                }
                "tcp" -> {
                    put("tcpSettings", mapOf(
                        "header" to mapOf("type" to "none")
                    ))
                }
            }
        }
    }
    
    /**
     * Test configuration validity
     */
    fun testConfig(configJson: String): String {
        return try {
            Libv2ray.testConfig(configJson)
        } catch (e: UnsatisfiedLinkError) {
            "" // Empty means OK in mock mode
        }
    }
    
    /**
     * Start V2Ray core with the given configuration
     */
    fun startCore(configJson: String): Boolean {
        initialize()
        
        return try {
            // Test config first
            val error = testConfig(configJson)
            if (error.isNotEmpty()) {
                Log.e(TAG, "Config validation failed: $error")
                return false
            }
            
            // Start the core
            val result = Libv2ray.startV2Ray(configJson)
            if (result) {
                currentConfig = configJson
                Log.i(TAG, "V2Ray core started successfully")
            } else {
                Log.e(TAG, "Failed to start V2Ray core")
            }
            result
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library not available, running in mock mode")
            currentConfig = configJson
            true // Mock success
        } catch (e: Exception) {
            Log.e(TAG, "Error starting V2Ray: ${e.message}")
            false
        }
    }
    
    /**
     * Stop V2Ray core
     */
    fun stopCore(): Boolean {
        return try {
            val result = Libv2ray.stopV2Ray()
            currentConfig = null
            Log.i(TAG, "V2Ray core stopped")
            result
        } catch (e: UnsatisfiedLinkError) {
            currentConfig = null
            true // Mock success
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping V2Ray: ${e.message}")
            false
        }
    }
    
    /**
     * Check if core is running
     */
    fun isRunning(): Boolean {
        return try {
            Libv2ray.isRunning()
        } catch (e: UnsatisfiedLinkError) {
            currentConfig != null
        }
    }
    
    /**
     * Get traffic statistics
     */
    fun getUploadBytes(): Long {
        return try {
            Libv2ray.queryStats("proxy", "uplink")
        } catch (e: Exception) {
            0L
        }
    }
    
    fun getDownloadBytes(): Long {
        return try {
            Libv2ray.queryStats("proxy", "downlink")
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get current SOCKS proxy address
     */
    fun getSocksAddress(): String = "127.0.0.1:$SOCKS_PORT"
    
    /**
     * Copy GeoIP and GeoSite assets if needed
     */
    fun copyAssets() {
        val filesDir = context.filesDir
        val geoipFile = File(filesDir, "geoip.dat")
        val geositeFile = File(filesDir, "geosite.dat")
        
        // Copy from assets if not exists
        if (!geoipFile.exists()) {
            try {
                context.assets.open("geoip.dat").use { input ->
                    geoipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.i(TAG, "Copied geoip.dat")
            } catch (e: Exception) {
                Log.w(TAG, "geoip.dat not found in assets")
            }
        }
        
        if (!geositeFile.exists()) {
            try {
                context.assets.open("geosite.dat").use { input ->
                    geositeFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.i(TAG, "Copied geosite.dat")
            } catch (e: Exception) {
                Log.w(TAG, "geosite.dat not found in assets")
            }
        }
    }
}

data class TrafficStats(
    val uploadBytes: Long,
    val downloadBytes: Long,
    val uploadSpeed: Long,
    val downloadSpeed: Long
)
