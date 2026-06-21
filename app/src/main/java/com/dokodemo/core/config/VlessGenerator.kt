package com.dokodemo.core.config

import com.dokodemo.data.model.ServerProfile

class VlessGenerator : OutboundGenerator {
    override fun generate(
        profile: ServerProfile,
        muxEnabled: Boolean,
        allowInsecure: Boolean
    ): Map<String, Any> {
        val streamSettings = StreamSettingsBuilder.build(profile, allowInsecure)
        
        val effectiveFlow = when {
            profile.useReality && profile.flow.isEmpty() -> "xtls-rprx-vision"
            profile.flow.isNotEmpty() -> profile.flow
            else -> null
        }
        
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
                                if (effectiveFlow != null) {
                                    put("flow", effectiveFlow)
                                }
                                put("level", 0)
                            }
                        )
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
