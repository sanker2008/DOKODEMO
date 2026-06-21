package com.dokodemo.core.config

import com.dokodemo.data.model.ServerProfile

/**
 * Strategy interface for generating V2Ray outbound configurations based on protocols.
 */
interface OutboundGenerator {
    /**
     * Generates the outbound JSON map for the given server profile.
     */
    fun generate(
        profile: ServerProfile,
        muxEnabled: Boolean,
        allowInsecure: Boolean
    ): Map<String, Any>
}
