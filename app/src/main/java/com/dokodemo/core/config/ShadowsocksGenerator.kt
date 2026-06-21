package com.dokodemo.core.config

import com.dokodemo.data.model.ServerProfile

class ShadowsocksGenerator : OutboundGenerator {
    override fun generate(
        profile: ServerProfile,
        muxEnabled: Boolean,
        allowInsecure: Boolean
    ): Map<String, Any> {
        // Shadowsocks doesn't typically use the common streamSettings in this core's implementation
        // But if it did, it would be included here.
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
}
