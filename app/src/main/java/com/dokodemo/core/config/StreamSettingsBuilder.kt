package com.dokodemo.core.config

import com.dokodemo.data.model.ServerProfile

/**
 * Utility to generate streamSettings map for V2Ray configurations.
 */
object StreamSettingsBuilder {
    fun build(profile: ServerProfile, allowInsecure: Boolean): Map<String, Any> {
        return buildMap {
            put("network", profile.network.ifEmpty { "tcp" })
            
            if (profile.useReality) {
                put("security", "reality")
                put("realitySettings", mapOf(
                    "serverName" to profile.serverName.ifEmpty { profile.address },
                    "publicKey" to profile.realityPublicKey,
                    "shortId" to profile.realityShortId,
                    "spiderX" to profile.realitySpiderX.ifEmpty { "/" },
                    "fingerprint" to profile.fingerprint.ifEmpty { "chrome" }
                ))
            } else if (profile.useTls) {
                put("security", "tls")
                put("tlsSettings", mapOf(
                    "serverName" to profile.serverName.ifEmpty { profile.address },
                    "allowInsecure" to (profile.allowInsecure || allowInsecure),
                    "fingerprint" to profile.fingerprint.ifEmpty { "chrome" }
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
                        "serviceName" to profile.wsPath,
                        "multiMode" to false
                    ))
                }
                "kcp" -> {
                    val headerType = profile.kcpHeader.ifEmpty { "none" }
                    
                    // kcpSettings: transport parameters only
                    put("kcpSettings", mapOf(
                        "mtu" to 1350,
                        "tti" to 50,
                        "uplinkCapacity" to 5,
                        "downlinkCapacity" to 20,
                        "congestion" to false,
                        "readBufferSize" to 2,
                        "writeBufferSize" to 2
                    ))
                    
                    // finalmask: obfuscation chain (sibling of kcpSettings in streamSettings)
                    // Order: first = outermost layer, last = innermost
                    put("finalmask", mapOf(
                        "udp" to buildList<Any> {
                            // Layer 1 (outer): header obfuscation
                            if (headerType != "none") {
                                add(mapOf("type" to "header-$headerType"))
                            }
                            // Layer 2 (inner): base mKCP protocol - always required
                            add(mapOf("type" to "mkcp-original"))
                            // Layer 3 (optional): encryption with seed
                            if (profile.kcpSeed.isNotEmpty()) {
                                add(mapOf(
                                    "type" to "mkcp-aes128gcm",
                                    "settings" to mapOf("key" to profile.kcpSeed)
                                ))
                            }
                        }
                    ))
                }
                "httpupgrade" -> {
                    put("httpupgradeSettings", mapOf(
                        "path" to profile.wsPath.ifEmpty { "/" },
                        "host" to profile.wsHost.ifEmpty { profile.address }
                    ))
                }
                "tcp" -> {
                    put("tcpSettings", mapOf(
                        "header" to mapOf("type" to "none")
                    ))
                }
                "xhttp", "splithttp" -> {
                    put("xhttpSettings", mapOf(
                        "path" to profile.wsPath.ifEmpty { "/" },
                        "host" to listOf(profile.wsHost.ifEmpty { profile.address })
                    ))
                }
            }
        }
    }
}
