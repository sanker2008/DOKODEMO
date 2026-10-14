package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.preferences.RoutingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ConnectionConfigurationTest {
    private val server = ServerProfile(
        id = 17,
        name = "Original",
        address = "vpn.example.com",
        port = 443,
        uuid = "credential",
        isSelected = true
    )

    private fun key(profile: ServerProfile = server, mode: RoutingMode = RoutingMode.GLOBAL) =
        ConnectionConfiguration.from(profile, mode)

    @Test
    fun `ping rename and subscription bookkeeping do not restart the connection`() {
        val updated = server.copy(
            name = "Renamed", latency = 42, updatedAt = 10, createdAt = 11,
            countryCode = "JP", countryName = "Japan", lastConnected = 12,
            subscriptionId = 13, groupId = 14, isSelected = false
        )
        assertEquals(key(), key(updated))
    }

    @Test
    fun `same ID endpoint and credential edits require a new connection`() {
        listOf(
            server.copy(address = "new.example.com"),
            server.copy(port = 8443),
            server.copy(uuid = "new-credential"),
            server.copy(password = "new-password"),
            server.copy(protocol = Protocol.TROJAN),
            server.copy(encryption = "aes-128-gcm"),
            server.copy(ssMethod = "chacha20-ietf-poly1305")
        ).forEach { changed ->
            assertEquals(server.id, changed.id)
            assertNotEquals(key(), key(changed))
        }
    }

    @Test
    fun `subscription refresh that retains ID detects transport and security changes`() {
        listOf(
            server.copy(network = "ws"),
            server.copy(wsPath = "/new-path"),
            server.copy(wsHost = "cdn.example.com"),
            server.copy(useTls = false),
            server.copy(allowInsecure = true),
            server.copy(serverName = "sni.example.com"),
            server.copy(useReality = true),
            server.copy(realityPublicKey = "key"),
            server.copy(realityShortId = "ab"),
            server.copy(realitySpiderX = "/spider"),
            server.copy(fingerprint = "firefox"),
            server.copy(flow = "xtls-rprx-vision"),
            server.copy(kcpHeader = "dtls"),
            server.copy(kcpSeed = "seed")
        ).forEach { changed -> assertNotEquals(key(), key(changed)) }
    }

    @Test
    fun `duplicate nodes with identical connection settings do not restart VPN`() {
        assertEquals(key(), key(server.copy(id = 99, name = "Duplicate")))
    }

    @Test
    fun `each routing mode has a distinct connection identity`() {
        assertEquals(RoutingMode.entries.size, RoutingMode.entries.map { key(mode = it) }.toSet().size)
    }
}
