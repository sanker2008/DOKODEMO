package com.dokodemo.core

import com.dokodemo.data.model.ServerProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionFetcher @Inject constructor(
    private val httpClient: OkHttpClient,
    private val shareLinkParser: ShareLinkParser
) {
    data class SubscriptionInfo(val upload: Long, val download: Long, val total: Long, val expire: Long)
    class InvalidContentException : Exception("Subscription has no supported nodes")

    suspend fun fetchAndParse(url: String, defaultGroupId: Long?): Result<Pair<List<ServerProfile>, SubscriptionInfo?>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36").build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "HTTP ${response.code}" }
                val source = requireNotNull(response.body).source()
                val limit = 8L * 1024 * 1024
                source.request(limit + 1)
                require(source.buffer.size <= limit) { "Subscription exceeds 8 MiB" }
                val content = source.readUtf8().trim().removePrefix("\uFEFF")
                ensureActive()
                val decoded = if (content.filterNot(Char::isWhitespace).matches(Regex("[A-Za-z0-9+/_=-]+"))) {
                    runCatching { decodeShareBase64(content) }.getOrDefault(content)
                } else content
                val nodes = decoded.lineSequence().mapNotNull { shareLinkParser.parse(it.trim()) }
                    .map { it.copy(groupId = defaultGroupId) }.toList()
                if (nodes.isEmpty()) throw InvalidContentException()
                val info = response.header("Subscription-Userinfo")?.let { header ->
                    val values = header.split(';').mapNotNull {
                        val pair = it.trim().split('=', limit = 2)
                        if (pair.size == 2) pair[0].lowercase() to (pair[1].toLongOrNull() ?: 0L).coerceAtLeast(0L) else null
                    }.toMap()
                    SubscriptionInfo(values["upload"] ?: 0L, values["download"] ?: 0L, values["total"] ?: 0L, values["expire"] ?: 0L)
                }
                Result.success(nodes to info)
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }
}
