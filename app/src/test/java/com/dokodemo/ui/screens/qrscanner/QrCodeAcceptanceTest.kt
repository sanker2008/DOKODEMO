package com.dokodemo.ui.screens.qrscanner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Base64

class QrCodeAcceptanceTest {
    @Test fun subscriptionsAreAcceptedWithTokensPreservedAndWhitespaceRemoved() {
        val url = "https://example.com/sub?token=a%2Bb%3D#My%20subscription"
        assertEquals(url, acceptedQrCode(" \n\uFEFF$url \t"))
        assertEquals("http://example.com/sub", acceptedQrCode("http://example.com/sub"))
    }

    @Test fun supportedNodeCodesRemainAcceptedAndNormalized() {
        val vmess = Base64.getEncoder().encodeToString(
            """{"add":"example.com","port":"443","id":"11111111-1111-4111-8111-111111111111"}""".toByteArray()
        )
        listOf(
            "vmess://$vmess",
            "vless://11111111-1111-4111-8111-111111111111@example.com:443?security=tls#Example",
            "trojan://secret@example.com:443#Example",
            "ss://aes-256-gcm:secret@example.com:8388#Example"
        ).forEach { assertEquals(it, acceptedQrCode("\uFEFF$it\n")) }
    }

    @Test fun missingMalformedUnsupportedAndMultipleCodesAreRejected() {
        listOf(
            null, "", "plain text", "https://", "https://example.com/path with spaces",
            "ftp://example.com/sub", "vless://broken", "vmess://broken",
            "https://example.com/a\nhttps://example.com/b"
        ).forEach { assertNull(it, acceptedQrCode(it)) }
    }
}
