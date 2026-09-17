package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidatorTest {
    private val valid = ServerProfile(
        name = "Example", address = "example.com", port = 443,
        uuid = "11111111-1111-4111-8111-111111111111", protocol = Protocol.VLESS
    )

    @Test fun acceptsBothPortBoundaries() {
        listOf(1, 65535).forEach { assertNull(ProfileValidator.validate(valid.copy(port = it))) }
    }

    @Test fun rejectsPortsOutsideValidRange() {
        listOf(-1, 0, 65536).forEach {
            assertEquals(ProfileValidator.Error.PORT, ProfileValidator.validate(valid.copy(port = it)))
        }
    }

    @Test fun rejectsEmptyWhitespaceAndUrlAddresses() {
        listOf("", " ", "bad host", "example.com\n", "https://example.com", "example.com/path").forEach {
            assertEquals("Address: $it", ProfileValidator.Error.ADDRESS, ProfileValidator.validate(valid.copy(address = it)))
        }
    }

    @Test fun rejectsUriDelimitersInServerAddress() {
        listOf("example.com?x=1", "example.com#tag", "user@example.com").forEach {
            assertEquals("Address: $it", ProfileValidator.Error.ADDRESS, ProfileValidator.validate(valid.copy(address = it)))
        }
    }

    @Test fun acceptsDomainIpv4AndIpv6Addresses() {
        listOf("example.com", "192.0.2.1", "2001:db8::1").forEach {
            assertNull(ProfileValidator.validate(valid.copy(address = it)))
        }
    }

    @Test fun vlessAndVmessRequireUuidCredentials() {
        listOf(Protocol.VLESS, Protocol.VMESS).forEach { protocol ->
            listOf("", "secret", "11111111-1111-4111-8111-11111111111Z").forEach { credential ->
                assertEquals(ProfileValidator.Error.CREDENTIAL, ProfileValidator.validate(valid.copy(protocol = protocol, uuid = credential)))
            }
            assertNull(ProfileValidator.validate(valid.copy(protocol = protocol, uuid = "ABCDEF01-2345-6789-ABCD-0123456789AB")))
        }
    }

    @Test fun trojanAndShadowsocksDoNotAcceptLegacyUuidAsPassword() {
        listOf(Protocol.TROJAN, Protocol.SHADOWSOCKS).forEach {
            assertEquals(ProfileValidator.Error.CREDENTIAL,
                ProfileValidator.validate(valid.copy(protocol = it, password = "", uuid = "legacy-password")))
        }
    }

    @Test fun trojanAndShadowsocksAcceptPasswordWithoutUuid() {
        listOf(Protocol.TROJAN, Protocol.SHADOWSOCKS).forEach {
            assertNull(ProfileValidator.validate(valid.copy(protocol = it, password = "p:a@ss+#%", uuid = "")))
        }
    }

    @Test fun realityRequiresPublicKeyAndServerName() {
        val reality = valid.copy(useReality = true, realityPublicKey = "public-key", serverName = "example.com")
        assertEquals(ProfileValidator.Error.REALITY, ProfileValidator.validate(reality.copy(realityPublicKey = " ")))
        assertEquals(ProfileValidator.Error.REALITY, ProfileValidator.validate(reality.copy(serverName = "")))
    }

    @Test fun realityAcceptsEmptyOrEvenHexShortIdsUpToEightBytes() {
        listOf("", "ab", "0123456789ABCDEF").forEach {
            assertNull(ProfileValidator.validate(valid.copy(useReality = true, realityPublicKey = "public-key",
                serverName = "example.com", realityShortId = it)))
        }
    }

    @Test fun realityRejectsOddNonHexAndOverlongShortIds() {
        listOf("a", "abc", "gg", "0123456789abcdef00", "ab cd").forEach {
            assertEquals(ProfileValidator.Error.REALITY, ProfileValidator.validate(valid.copy(useReality = true,
                realityPublicKey = "public-key", serverName = "example.com", realityShortId = it)))
        }
    }

    @Test fun unsupportedWireguardCannotBeSavedAsConnectable() {
        assertEquals(ProfileValidator.Error.PROTOCOL, ProfileValidator.validate(valid.copy(protocol = Protocol.WIREGUARD)))
    }
}
