package com.dokodemo.core

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class SubscriptionFetcherTest {
    @Test fun successfulHttpWithHtmlErrorPageIsAnUpdateFailure() = runTest {
        assertTrue(fetcher("<html><body>Upstream unavailable</body></html>").fetchAndParse("https://example.com/sub", null).isFailure)
    }

    @Test fun successfulHttpWithEmptyBodyIsAnUpdateFailure() = runTest {
        assertTrue(fetcher("").fetchAndParse("https://example.com/sub", null).isFailure)
    }

    @Test fun unsupportedClashYamlIsAnUpdateFailure() = runTest {
        assertTrue(fetcher("proxies:\n  - name: unsupported\n    type: ss\n").fetchAndParse("https://example.com/sub", null).isFailure)
    }

    @Test fun validSubscriptionAssignsGroupAndReadsQuota() = runTest {
        val body = "vless://11111111-1111-4111-8111-111111111111@example.com:443?security=tls#Example"
        val result = fetcher(body).fetchAndParse("https://example.com/sub", 42L).getOrThrow()
        assertEquals(42L, result.first.single().groupId)
        assertEquals("Example", result.first.single().name)
        assertEquals(1000L, result.second?.total)
    }

    private fun fetcher(body: String): SubscriptionFetcher {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK").body(body.toResponseBody())
                .header("Subscription-Userinfo", "upload=10; download=20; total=1000; expire=2000000000")
                .build()
        }.build()
        return SubscriptionFetcher(client, ShareLinkParser())
    }
}
