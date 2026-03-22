package com.dokodemo.core

import android.util.Base64
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton
import java.nio.charset.StandardCharsets

/**
 * 订阅抓取与解析引擎
 */
@Singleton
class SubscriptionFetcher @Inject constructor(
    private val httpClient: OkHttpClient,
    private val shareLinkParser: ShareLinkParser
) {

    /**
     * 抓取并解析订阅链接
     * @param url 订阅链接
     * @param defaultGroupId 给解析出的节点分配的默认分组id
     * @return 解析得出的节点列表，带有错误处理
     */
    suspend fun fetchAndParse(url: String, defaultGroupId: Long?): Result<List<ServerProfile>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                // 伪装 User-Agent 以防止被一些机场的 WAF 拦截
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP 错误: ${response.code}"))
            }

            val bodyText = response.body?.string() ?: return@withContext Result.failure(Exception("订阅内容为空"))

            // 解析内容并设置 groupId
            val parsedProfiles = parseContent(bodyText).map { it.copy(groupId = defaultGroupId) }
            
            Result.success(parsedProfiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseContent(content: String): List<ServerProfile> {
        val servers = mutableListOf<ServerProfile>()

        // 尝试按 Base64 解码
        val decodedText = tryDecodeBase64(content) ?: content

        // 按行分割并解析
        decodedText.lines().map { it.trim() }.filter { it.isNotEmpty() }.forEach { line ->
            // 这里我们复用 ShareLinkParser 的逻辑，但 ShareLinkParser 返回的是 VpnProfile 或内部模型
            // 为了安全，这使用 shareLinkParser 解析出内部对象（需补齐 ShareLinkParser）
            try {
                if (line.startsWith("vmess://") || line.startsWith("vless://") || line.startsWith("trojan://") || line.startsWith("ss://")) {
                    // shareLinkParser 目前需要适配返回 ServerProfile，或者在其中处理
                    // 暂时这里手动转接一次或者调用 parser
                    val profile = shareLinkParser.parse(line)
                    if (profile != null) {
                        servers.add(profile)
                    }
                }
            } catch (e: Exception) {
                // 忽略解析失败的单行
                e.printStackTrace()
            }
        }
        return servers
    }

    private fun tryDecodeBase64(content: String): String? {
        // Base64 文本通常不包含空格/换行，但是如果较长可能会有
        val cleanContent = content.replace(Regex("\\s+"), "")
        if (cleanContent.matches(Regex("^[A-Za-z0-9+/]+={0,2}$"))) {
            try {
                val bytes = Base64.decode(cleanContent, Base64.DEFAULT)
                return String(bytes, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                // Not valid base64
            }
        }
        return null
    }
}
