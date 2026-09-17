package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import com.google.gson.JsonParser
import java.net.URI
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Decode standard or URL-safe, optionally unpadded Base64 on API 24+. */
@OptIn(ExperimentalEncodingApi::class)
internal fun decodeShareBase64(value: String): String {
    val normalized = value.filterNot(Char::isWhitespace).replace('-', '+').replace('_', '/')
    return Base64.decode(normalized.padEnd((normalized.length + 3) / 4 * 4, '=')).decodeToString()
}

@Singleton
class ShareLinkParser @Inject constructor() {
    fun parse(link: String): ServerProfile? = runCatching {
        val value = link.trim()
        when {
            value.startsWith("vmess://") -> parseVmess(value.removePrefix("vmess://"))
            value.startsWith("ss://") -> parseShadowsocks(value.removePrefix("ss://"))
            value.startsWith("vless://") -> parseUri(value, Protocol.VLESS)
            value.startsWith("trojan://") -> parseUri(value, Protocol.TROJAN)
            else -> null
        }?.takeIf { it.address.isNotBlank() && it.port in 1..65535 }
    }.getOrNull()

    private fun decode(value: String): String = URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")

    private fun parameters(query: String?): Map<String, String> = query.orEmpty().split('&')
        .mapNotNull { item ->
            val pair = item.split('=', limit = 2)
            if (pair.size == 2) decode(pair[0]) to decode(pair[1]) else null
        }.toMap()

    private fun parseUri(value: String, protocol: Protocol): ServerProfile {
        val uri = URI(value)
        val address = requireNotNull(uri.host).removeSurrounding("[", "]")
        val credential = decode(requireNotNull(uri.rawUserInfo))
        require(credential.isNotEmpty())
        val p = parameters(uri.rawQuery)
        val network = p["type"] ?: "tcp"
        val reality = p["security"] == "reality"
        val port = if (uri.port == -1) 443 else uri.port
        require(port in 1..65535)
        return ServerProfile(
            name = uri.rawFragment?.let(::decode)?.ifBlank { null } ?: "$address:$port",
            address = address, port = port, protocol = protocol, uuid = credential,
            password = if (protocol == Protocol.TROJAN) credential else "",
            encryption = p["encryption"] ?: "none", flow = p["flow"].orEmpty(),
            useTls = (p["security"] ?: if (protocol == Protocol.TROJAN) "tls" else "none") == "tls" || reality,
            useReality = reality,
            realityPublicKey = p["pbk"] ?: p["publicKey"].orEmpty(),
            realityShortId = p["sid"] ?: p["shortId"].orEmpty(),
            realitySpiderX = p["spx"] ?: p["spiderX"].orEmpty(),
            fingerprint = p["fp"] ?: p["fingerprint"] ?: "chrome", network = network,
            wsPath = if (network == "grpc") p["serviceName"] ?: p["path"].orEmpty() else p["path"].orEmpty(),
            wsHost = p["host"].orEmpty(), serverName = p["sni"] ?: address,
            kcpHeader = p["headerType"] ?: "none", kcpSeed = p["seed"].orEmpty()
        )
    }

    private fun parseVmess(encoded: String): ServerProfile {
        val fields = JsonParser.parseString(decodeShareBase64(encoded)).asJsonObject
        fun field(name: String, default: String = "") = fields.get(name)?.takeUnless { it.isJsonNull }?.asString ?: default
        val address = field("add")
        val port = field("port", "443").toInt()
        require(address.isNotBlank() && field("id").isNotBlank() && port in 1..65535)
        return ServerProfile(
            name = field("ps").ifBlank { "$address:$port" }, address = address, port = port,
            uuid = field("id"), protocol = Protocol.VMESS, encryption = field("scy", "auto"),
            useTls = field("tls") == "tls", network = field("net", "tcp"),
            wsPath = field("path"), wsHost = field("host"), serverName = field("sni"),
            kcpHeader = field("type", "none"), kcpSeed = field("seed")
        )
    }

    private fun parseShadowsocks(value: String): ServerProfile {
        val name = value.substringAfter('#', "").let(::decode)
        val main = value.substringBefore('#')
        val query = parameters(main.substringAfter('?', ""))
        // SIP003 plugins are unsupported; silently dropping one breaks the connection.
        require(query["plugin"].isNullOrBlank())
        val authority = main.substringBefore('?').removeSuffix("/")
        val legacy = '@' !in authority
        val decoded = if (legacy) decodeShareBase64(authority) else authority
        val separator = decoded.lastIndexOf('@')
        require(separator > 0)
        val userInfo = decoded.substring(0, separator)
        val credentials = if (legacy) userInfo else if (':' in userInfo) decode(userInfo) else decodeShareBase64(decode(userInfo))
        val method = credentials.substringBefore(':')
        val password = credentials.substringAfter(':', "")
        require(method.isNotBlank() && password.isNotEmpty())
        val endpoint = URI("ss://${decoded.substring(separator + 1)}")
        val address = requireNotNull(endpoint.host).removeSurrounding("[", "]")
        require(endpoint.port in 1..65535)
        return ServerProfile(
            name = name.ifBlank { "$address:${endpoint.port}" }, address = address, port = endpoint.port,
            protocol = Protocol.SHADOWSOCKS, password = password, encryption = method, ssMethod = method,
            useTls = false
        )
    }
}
