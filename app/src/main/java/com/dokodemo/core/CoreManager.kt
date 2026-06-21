package com.dokodemo.core

import android.content.Context
import android.util.Log
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.preferences.CustomRoutingRule
import com.dokodemo.data.preferences.CustomRuleAction
import com.dokodemo.data.preferences.CustomRuleMatchType
import com.dokodemo.data.preferences.RoutingMode
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import libv2ray.Libv2ray
import libv2ray.CoreController
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.dokodemo.core.config.OutboundGenerator
import com.dokodemo.core.config.ShadowsocksGenerator
import com.dokodemo.core.config.TrojanGenerator
import com.dokodemo.core.config.VlessGenerator
import com.dokodemo.core.config.VmessGenerator

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
        const val DNS_INBOUND_PORT = 10853
        private const val MAX_PORT_RETRIES = 5
    }
    
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var isInitialized = false
    private var currentConfig: String? = null
    /** Holds the running CoreController */
    private var coreController: libv2ray.CoreController? = null
    
    /**
     * Initialize V2Ray environment
     */
    fun initialize() {
        if (isInitialized) return
        
        try {
            go.Seq.setContext(context)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize go.Seq context", e)
        }
        
        try {
            val filesDir = context.filesDir.absolutePath
            Libv2ray.initCoreEnv(filesDir, "")
            isInitialized = true
            Log.i(TAG, "V2Ray environment initialized. Version: ${getVersion()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize V2Ray: ${e.message}")
            isInitialized = true // Prevent crash loops
        }
    }
    
    /**
     * Get V2Ray core version
     */
    fun getVersion(): String {
        return try {
            Libv2ray.checkVersionX()
        } catch (e: Throwable) {
            "Version Check Error"
        }
    }
    
    /**
     * Generate V2Ray JSON configuration from ServerProfile
     */
    fun generateConfig(
        profile: ServerProfile,
        routingMode: RoutingMode = RoutingMode.GLOBAL,
        muxEnabled: Boolean = false,
        allowInsecure: Boolean = false,
        udpEnabled: Boolean = true,
        customRules: List<CustomRoutingRule> = emptyList(),
        dnsPort: Int = DNS_INBOUND_PORT
    ): String {
        val config = buildMap<String, Any> {
            // Log settings
            put("log", mapOf(
                "access" to "",
                "error" to "",
                "loglevel" to "warning"
            ))
            
            // DNS settings - 使用远程 DNS 解析
            put("dns", mapOf<String, Any>(
                "servers" to listOf(
                    mapOf(
                        "tag" to "remote",
                        "address" to "8.8.8.8",
                        "detour" to "proxy"
                    ),
                    mapOf(
                        "tag" to "local",
                        "address" to "223.5.5.5",
                        "detour" to "direct",
                        "domains" to listOf("geosite:cn")
                    )
                ),
                "queryStrategy" to "UseIPv4",
                "disableCache" to false
            ))
            
            // Inbounds - Local SOCKS5 proxy
            // Stats and Policy
            put("stats", emptyMap<String, Any>())
            put("policy", mapOf(
                "levels" to mapOf(
                    "0" to mapOf(
                        "statsUserUplink" to true,
                        "statsUserDownlink" to true
                    ),
                    "8" to mapOf(
                        "statsUserUplink" to true,
                        "statsUserDownlink" to true
                    )
                ),
                "system" to mapOf(
                    "statsInboundUplink" to true,
                    "statsInboundDownlink" to true,
                    "statsOutboundUplink" to true,
                    "statsOutboundDownlink" to true
                )
            ))

            put("inbounds", listOf(
                mapOf(
                    "tag" to "socks",
                    "port" to SOCKS_PORT,
                    "listen" to "127.0.0.1",
                    "protocol" to "socks",
                    "sniffing" to mapOf(
                        "enabled" to true,
                        "destOverride" to listOf("http", "tls"),
                        "routeOnly" to false,
                        "domainsExcluded" to listOf("courier.push.apple.com", "api.jpush.cn")
                    ),
                    "settings" to mapOf(
                        "auth" to "noauth",
                        "udp" to udpEnabled,
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
                        "destOverride" to listOf("http", "tls"),
                        "routeOnly" to false
                    ),
                    "settings" to mapOf(
                        "allowTransparent" to false
                    )
                ),
                mapOf(
                    "tag" to "dns-in",
                    "port" to dnsPort,
                    "listen" to "127.0.0.1",
                    "protocol" to "dokodemo-door",
                    "settings" to mapOf(
                        "address" to "1.1.1.1",
                        "port" to 53,
                        "network" to "udp"
                    )
                )
            ))
            
            // Outbounds
            put("outbounds", listOf(
                generateOutbound(profile, muxEnabled, allowInsecure),
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
                "rules" to buildList {
                    // DNS 查询通过代理
                    add(mapOf(
                        "type" to "field",
                        "inboundTag" to listOf("dns-in"),
                        "outboundTag" to "proxy"
                    ))
                    
                    // Direct for private IPs
                    add(mapOf(
                        "type" to "field",
                        "ip" to listOf("geoip:private"),
                        "outboundTag" to "direct"
                    ))

                    if (!udpEnabled) {
                        add(mapOf(
                            "type" to "field",
                            "network" to "udp",
                            "outboundTag" to "block"
                        ))
                    }

                    customRules
                        .filter { it.enabled }
                        .mapNotNull(::buildCustomRoutingRule)
                        .forEach(::add)
                    
                    // Direct for CN sites only in BYPASS_CN mode
                    if (routingMode == RoutingMode.BYPASS_CN) {
                        add(mapOf(
                            "type" to "field",
                            "domain" to listOf("geosite:cn"),
                            "outboundTag" to "direct"
                        ))
                        add(mapOf(
                            "type" to "field",
                            "ip" to listOf("geoip:cn"),
                            "outboundTag" to "direct"
                        ))
                    }
                    
                    // Google.com through proxy
                    add(mapOf(
                        "type" to "field",
                        "domain" to listOf("domain:google.com"),
                        "outboundTag" to "proxy"
                    ))
                    
                    // Everything else goes through proxy
                    add(mapOf(
                        "type" to "field",
                        "port" to "0-65535",
                        "outboundTag" to "proxy"
                    ))
                }
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
    
    private fun generateOutbound(
        profile: ServerProfile,
        muxEnabled: Boolean,
        allowInsecure: Boolean
    ): Map<String, Any> {
        val generator: OutboundGenerator = when (profile.protocol) {
            Protocol.VLESS -> VlessGenerator()
            Protocol.VMESS -> VmessGenerator()
            Protocol.TROJAN -> TrojanGenerator()
            Protocol.SHADOWSOCKS -> ShadowsocksGenerator()
            else -> VlessGenerator()
        }
        return generator.generate(profile, muxEnabled, allowInsecure)
    }

    private fun buildCustomRoutingRule(rule: CustomRoutingRule): Map<String, Any>? {
        val value = rule.value.trim()
        if (value.isEmpty()) {
            return null
        }

        val outboundTag = when (rule.action) {
            CustomRuleAction.PROXY -> "proxy"
            CustomRuleAction.DIRECT -> "direct"
            CustomRuleAction.BLOCK -> "block"
        }

        return when (rule.matchType) {
            CustomRuleMatchType.DOMAIN_FULL -> mapOf(
                "type" to "field",
                "domain" to listOf("full:$value"),
                "outboundTag" to outboundTag
            )
            CustomRuleMatchType.DOMAIN_SUFFIX -> mapOf(
                "type" to "field",
                "domain" to listOf("domain:$value"),
                "outboundTag" to outboundTag
            )
            CustomRuleMatchType.DOMAIN_KEYWORD -> mapOf(
                "type" to "field",
                "domain" to listOf("keyword:$value"),
                "outboundTag" to outboundTag
            )
            CustomRuleMatchType.IP_CIDR -> mapOf(
                "type" to "field",
                "ip" to listOf(value),
                "outboundTag" to outboundTag
            )
            CustomRuleMatchType.GEOSITE -> mapOf(
                "type" to "field",
                "domain" to listOf("geosite:$value"),
                "outboundTag" to outboundTag
            )
            CustomRuleMatchType.GEOIP -> mapOf(
                "type" to "field",
                "ip" to listOf("geoip:$value"),
                "outboundTag" to outboundTag
            )
        }
    }
    
    /**
     * Test configuration validity
     */
    fun testConfig(configJson: String): String {
        return ""
    }
    
    /**
     * Start V2Ray core with automatic port retry.
     * If a port binding conflict occurs, automatically retries with different DNS inbound ports.
     * @return null if successful, or an error message string on failure.
     */
    fun startCore(configJson: String): String? {
        initialize()
        return startCoreInternal(configJson)
    }
    
    /**
     * Start V2Ray core with a profile, with automatic port retry on bind failure.
     * @return null if successful, or an error message string on failure.
     */
    fun startCoreWithRetry(
        profile: ServerProfile,
        routingMode: RoutingMode = RoutingMode.GLOBAL,
        muxEnabled: Boolean = false,
        allowInsecure: Boolean = false,
        udpEnabled: Boolean = true,
        customRules: List<CustomRoutingRule> = emptyList()
    ): String? {
        initialize()
        
        for (attempt in 0 until MAX_PORT_RETRIES) {
            val dnsPort = DNS_INBOUND_PORT + attempt
            val config = generateConfig(
                profile = profile,
                routingMode = routingMode,
                muxEnabled = muxEnabled,
                allowInsecure = allowInsecure,
                udpEnabled = udpEnabled,
                customRules = customRules,
                dnsPort = dnsPort
            )
            Log.i(TAG, "Attempting core start (attempt ${attempt + 1}/$MAX_PORT_RETRIES, dnsPort=$dnsPort)")
            
            val error = startCoreInternal(config)
            if (error == null) {
                if (attempt > 0) {
                    Log.i(TAG, "Successfully started on alternate DNS port $dnsPort after $attempt retries")
                }
                return null // success
            }
            
            // Only retry if it's a port binding error
            if (!error.contains("address already in use", ignoreCase = true)) {
                return error // non-retryable error
            }
            
            Log.w(TAG, "Port conflict on attempt ${attempt + 1}, retrying with port ${dnsPort + 1}...")
        }
        return "所有备用端口均被占用，请重启设备后重试"
    }
    
    private fun startCoreInternal(configJson: String): String? {
        Log.d(TAG, "Generated config (saved to file)")
        try {
            val configFile = File(context.cacheDir, "xray_config.json")
            configFile.writeText(configJson)
            Log.i(TAG, "Config saved to: ${configFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save config: ${e.message}")
        }
        return try {
            try { coreController?.stopLoop() } catch (_: Throwable) {}
            val callback = object : libv2ray.CoreCallbackHandler {
                override fun onEmitStatus(status: Long, msg: String?): Long {
                    Log.i(TAG, "Core status [$status]: $msg")
                    return 0L
                }
                
                override fun shutdown(): Long {
                    Log.i(TAG, "Core shutdown callback")
                    return 0L
                }
                
                override fun startup(): Long {
                    Log.i(TAG, "Core startup callback")
                    return 0L
                }
            }
            
            val ctrl = Libv2ray.newCoreController(callback)
            if (ctrl == null) return "初始化 V2Ray 核心失败"
            
            ctrl.startLoop(configJson, 0)
            
            coreController = ctrl
            currentConfig = configJson
            Log.i(TAG, "V2Ray core started successfully")
            null // success
        } catch (e: Throwable) {
            Log.e(TAG, "Native library error: ${e.message}")
            val rawMsg = e.message ?: "Unknown error"
            when {
                rawMsg.contains("address already in use", ignoreCase = true) ->
                    "端口被占用: $rawMsg"
                rawMsg.contains("timeout") -> "连接超时"
                else -> "核心启动失败: $rawMsg"
            }
        }
    }
    
    /**
     * Stop V2Ray core
     */
    fun stopCore(): Boolean {
        return try {
            coreController?.stopLoop()
            coreController = null
            currentConfig = null
            Log.i(TAG, "V2Ray core stopped")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "Error stopping V2Ray: ${e.message}")
            coreController = null
            currentConfig = null
            true
        }
    }
    
    /**
     * Check if core is running
     */
    fun isRunning(): Boolean {
        return try {
            coreController?.isRunning ?: false
        } catch (e: Throwable) {
            currentConfig != null
        }
    }
    
    /**
     * Get traffic statistics
     */
    fun getUploadBytes(): Long {
        return try {
            coreController?.queryStats("proxy", "uplink") ?: 0L
        } catch (e: Throwable) {
            0L
        }
    }
    
    fun getDownloadBytes(): Long {
        return try {
            coreController?.queryStats("proxy", "downlink") ?: 0L
        } catch (e: Throwable) {
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
