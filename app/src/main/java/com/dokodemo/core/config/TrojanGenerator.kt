package com.dokodemo.core.config

import com.dokodemo.data.model.ServerProfile

class TrojanGenerator : OutboundGenerator {
    override fun generate(
        profile: ServerProfile,
        muxEnabled: Boolean,
        allowInsecure: Boolean
    ): Map<String, Any> {
        val streamSettings = StreamSettingsBuilder.build(profile, allowInsecure)
        
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
                "enabled" to muxEnabled,
                "concurrency" to if (muxEnabled) 8 else -1
            )
        )
    }
}
