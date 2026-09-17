package com.dokodemo.core.config

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import org.junit.Assert.*
import org.junit.Test

class OutboundGeneratorTest {
    @Test fun shadowsocksUsesUserSelectedCipherInsteadOfGenericEncryptionDefault() {
        val profile = ServerProfile(name = "SS", address = "example.com", port = 8388,
            protocol = Protocol.SHADOWSOCKS, password = "secret", ssMethod = "chacha20-ietf-poly1305", encryption = "auto")
        val outbound = ShadowsocksGenerator().generate(profile, false, false)
        val server = ((outbound["settings"] as Map<*, *>)["servers"] as List<*>).single() as Map<*, *>
        assertEquals("chacha20-ietf-poly1305", server["method"])
    }

    @Test fun realityWithoutFlowDoesNotEnableVision() {
        val profile = ServerProfile(name = "Reality", address = "example.com", port = 443,
            uuid = "11111111-1111-4111-8111-111111111111", useReality = true, flow = "", network = "grpc")
        val user = vlessUser(VlessGenerator().generate(profile, false, false))
        assertTrue("Empty flow must stay empty or be omitted", user["flow"] == null || user["flow"] == "")
    }

    @Test fun explicitlySelectedVisionIsPreserved() {
        val profile = ServerProfile(name = "Reality", address = "example.com", port = 443,
            uuid = "11111111-1111-4111-8111-111111111111", useReality = true, flow = "xtls-rprx-vision")
        assertEquals("xtls-rprx-vision", vlessUser(VlessGenerator().generate(profile, false, false))["flow"])
    }

    @Test fun trojanUsesEditedPasswordEvenWhenLegacyUuidContainsOldPassword() {
        val profile = ServerProfile(name = "Trojan", address = "example.com", port = 443,
            protocol = Protocol.TROJAN, uuid = "old-password", password = "new-password")
        val outbound = TrojanGenerator().generate(profile, false, false)
        val server = ((outbound["settings"] as Map<*, *>)["servers"] as List<*>).single() as Map<*, *>
        assertEquals("new-password", server["password"])
    }

    private fun vlessUser(outbound: Map<String, Any>): Map<*, *> {
        val server = ((outbound["settings"] as Map<*, *>)["vnext"] as List<*>).single() as Map<*, *>
        return (server["users"] as List<*>).single() as Map<*, *>
    }
}
