package com.dokodemo.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class ImportSourceClassifierTest {
    @Test fun subscriptionUrlsPreserveQueryTokensAndFragments() {
        val url = "https://example.com/api/sub?token=a%2Bb%3D&format=base64#My%20subscription"
        assertEquals(ImportSource.Subscription(url), ImportSourceClassifier.classify("\n $url \t"))
        assertEquals(ImportSource.Subscription("http://example.com/sub"), ImportSourceClassifier.classify("http://example.com/sub"))
    }

    @Test fun subscriptionDetectionHandlesUppercaseSchemeIpv6AndPorts() {
        assertTrue(ImportSourceClassifier.isSubscriptionUrl("HTTPS://example.com/sub"))
        assertTrue(ImportSourceClassifier.isSubscriptionUrl("http://[2001:db8::1]:8080/sub"))
        assertFalse(ImportSourceClassifier.isSubscriptionUrl("https://example.com:70000/sub"))
        assertFalse(ImportSourceClassifier.isSubscriptionUrl("https://example.com:0/sub"))
    }

    @Test fun allSupportedNodeProtocolsStillOpenTheNodeEditor() {
        val vmess = Base64.getEncoder().encodeToString(
            """{"add":"example.com","port":"443","id":"11111111-1111-4111-8111-111111111111","ps":"Example"}""".toByteArray()
        )
        val links = listOf(
            "vmess://$vmess",
            "vless://11111111-1111-4111-8111-111111111111@example.com:443?security=tls#Example",
            "trojan://secret@example.com:443#Example",
            "ss://aes-256-gcm:secret@example.com:8388#Example"
        )
        links.forEach { assertEquals(ImportSource.Node(it), ImportSourceClassifier.classify(" $it ")) }
    }

    @Test fun malformedUnsupportedAndMultipleLinksAreNotSubscriptionImports() {
        listOf(
            "", "text", "https://", "https:///missing-host", "https://example.com/path with spaces",
            "ftp://example.com/sub", "content://example.com/sub", "vless://broken", "vmess://broken",
            "https://example.com/a\nhttps://example.com/b",
            "vless://id@example.com:443\nvless://other@example.com:443"
        ).forEach { assertEquals(it, ImportSource.Unsupported, ImportSourceClassifier.classify(it)) }
    }

    @Test fun defaultNameUsesHostAndNeverCopiesSubscriptionCredentials() {
        assertEquals("example.com", ImportSourceClassifier.suggestedSubscriptionName("https://example.com/sub?token=private"))
        assertEquals("2001:db8::1", ImportSourceClassifier.suggestedSubscriptionName("http://[2001:db8::1]/sub"))
        assertEquals("", ImportSourceClassifier.suggestedSubscriptionName("invalid"))
    }

    @Test fun pastedBomIsRemovedBeforeRouting() {
        assertEquals(ImportSource.Subscription("https://example.com/sub"), ImportSourceClassifier.classify("\uFEFFhttps://example.com/sub"))
    }
}
