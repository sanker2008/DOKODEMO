package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import org.junit.Assert.*
import org.junit.Test
import java.util.Base64

class ShareLinkParserTest {
    private val parser = ShareLinkParser()
    private val uuid = "11111111-1111-4111-8111-111111111111"

    @Test fun vlessSupportsBracketedIpv6() {
        val profile = requireNotNull(parser.parse("vless://$uuid@[2001:db8::1]:8443?security=tls#IPv6"))
        assertEquals("2001:db8::1", profile.address)
        assertEquals(8443, profile.port)
    }

    @Test fun trojanSupportsBracketedIpv6() {
        val profile = requireNotNull(parser.parse("trojan://secret@[2001:db8::2]:9443?sni=example.com"))
        assertEquals("2001:db8::2", profile.address)
        assertEquals(9443, profile.port)
    }

    @Test fun trojanDecodesPasswordWithoutConvertingLiteralPlusToSpace() {
        val profile = requireNotNull(parser.parse("trojan://a%40b%3Ac+%25@example.com:443"))
        assertEquals("a@b:c+%", profile.password)
    }

    @Test fun trojanPreservesWebSocketParameters() {
        val profile = requireNotNull(parser.parse("trojan://secret@example.com:443?type=ws&path=%2Fproxy&host=cdn.example.com"))
        assertEquals("ws", profile.network)
        assertEquals("/proxy", profile.wsPath)
        assertEquals("cdn.example.com", profile.wsHost)
    }

    @Test fun trojanPreservesGrpcServiceName() {
        val profile = requireNotNull(parser.parse("trojan://secret@example.com:443?type=grpc&serviceName=my-service"))
        assertEquals("my-service", profile.wsPath)
    }

    @Test fun vlessPreservesGrpcServiceName() {
        val profile = requireNotNull(parser.parse("vless://$uuid@example.com:443?type=grpc&serviceName=my-service"))
        assertEquals("my-service", profile.wsPath)
    }

    @Test fun shadowsocksParsesSip002UrlSafeUserInfo() {
        val userInfo = Base64.getUrlEncoder().withoutPadding().encodeToString("chacha20-ietf-poly1305:secret:with:colons".toByteArray())
        val profile = requireNotNull(parser.parse("ss://$userInfo@example.com:8388#My%20SS"))
        assertEquals(Protocol.SHADOWSOCKS, profile.protocol)
        assertEquals("chacha20-ietf-poly1305", profile.ssMethod)
        assertEquals("secret:with:colons", profile.password)
        assertEquals("My SS", profile.name)
        assertEquals(8388, profile.port)
    }

    @Test fun shadowsocksParsesLegacyWholePayloadBase64() {
        val payload = Base64.getEncoder().encodeToString("aes-256-gcm:secret@example.com:8388".toByteArray())
        val profile = requireNotNull(parser.parse("ss://$payload#Legacy"))
        assertEquals("example.com", profile.address)
        assertEquals("aes-256-gcm", profile.ssMethod)
        assertEquals("secret", profile.password)
    }

    @Test fun vmessPreservesUnicodeAndNumericPort() {
        val json = """{"v":"2","ps":"东京节点","add":"example.com","port":8443,"id":"$uuid","net":"ws","path":"/proxy","host":"cdn.example.com","tls":"tls"}"""
        val profile = requireNotNull(parser.parse("vmess://" + Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))))
        assertEquals(Protocol.VMESS, profile.protocol)
        assertEquals("东京节点", profile.name)
        assertEquals(8443, profile.port)
        assertEquals("/proxy", profile.wsPath)
    }

    @Test fun rejectsOutOfRangePortsInsteadOfSavingUnusableProfiles() {
        assertNull(parser.parse("vless://$uuid@example.com:65536"))
        assertNull(parser.parse("trojan://secret@example.com:0"))
    }

    @Test fun scannedUriPreservesEncodedDelimitersAndUnicode() {
        val profile = requireNotNull(parser.parse("  trojan://p%23%25%40%3A+%E5%AF%86@example.com:443?type=ws&path=%2Fproxy%3Fa%3D1%26b%3D%2B#%E4%B8%9C%E4%BA%AC+%23%25  "))
        assertEquals("p#%@:+密", profile.password)
        assertEquals("/proxy?a=1&b=+", profile.wsPath)
        assertEquals("东京+#%", profile.name)
    }

    @Test fun scannedVmessAcceptsUnpaddedUrlSafeBase64() {
        // The repeated '?' guarantees this payload exercises the URL-safe '_' alphabet.
        val json = """{"ps":"???节点","add":"example.com","port":443,"id":"$uuid"}"""
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray(Charsets.UTF_8))
        assertTrue("Fixture must exercise URL-safe Base64", '-' in encoded || '_' in encoded)
        val profile = requireNotNull(parser.parse("vmess://$encoded"))
        assertEquals("???节点", profile.name)
        assertEquals(uuid, profile.uuid)
    }

    @Test fun shadowsocksPlainCredentialsPreserveReservedCharactersAndIpv6() {
        val profile = requireNotNull(parser.parse("ss://aes-256-gcm:p%3Aa%40b%23%25+@[2001:db8::5]:8388#%E8%8A%82%E7%82%B9"))
        assertEquals("p:a@b#%+", profile.password)
        assertEquals("2001:db8::5", profile.address)
        assertEquals("节点", profile.name)
    }

    @Test fun unsupportedShadowsocksPluginIsNotSilentlyDiscarded() {
        val userInfo = Base64.getUrlEncoder().withoutPadding().encodeToString("aes-256-gcm:secret".toByteArray())
        assertNull(parser.parse("ss://$userInfo@example.com:8388/?plugin=obfs-local%3Bobfs%3Dhttp"))
    }

    @Test fun malformedScannedTextReturnsNull() {
        listOf("", "https://example.com", "trojan://secret@", "trojan://bad%ZZ@example.com:443", "ss://not-base64", "vmess://not-base64").forEach {
            assertNull("Unexpected profile for $it", parser.parse(it))
        }
    }
}
