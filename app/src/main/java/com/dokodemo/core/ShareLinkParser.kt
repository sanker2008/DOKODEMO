package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import java.net.URLDecoder
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for V2Ray share links (vless://, vmess://, trojan://)
 */
@Singleton
class ShareLinkParser @Inject constructor() {
    
    /**
     * Parse a share link and convert to ServerProfile
     */
    fun parse(link: String): ServerProfile? {
        return try {
            when {
                link.startsWith("vless://") -> parseVless(link)
                link.startsWith("vmess://") -> parseVmess(link)
                link.startsWith("trojan://") -> parseTrojan(link)
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse VLESS share link
     * Format: vless://uuid@address:port?params#name
     */
    private fun parseVless(link: String): ServerProfile {
        val url = link.removePrefix("vless://")
        val parts = url.split("#", limit = 2)
        val name = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
        
        val mainPart = parts[0]
        val queryIndex = mainPart.indexOf("?")
        val params = if (queryIndex > 0) {
            parseQueryParams(mainPart.substring(queryIndex + 1))
        } else {
            emptyMap()
        }
        
        val hostPart = if (queryIndex > 0) mainPart.substring(0, queryIndex) else mainPart
        val uuidAndHost = hostPart.split("@")
        val uuid = uuidAndHost[0]
        val addressPort = uuidAndHost[1].split(":")
        val address = addressPort[0]
        val port = addressPort[1].toIntOrNull() ?: 443
        
        return ServerProfile(
            name = name.ifEmpty { "$address:$port" },
            address = address,
            port = port,
            uuid = uuid,
            protocol = Protocol.VLESS,
            encryption = params["encryption"] ?: "none",
            flow = params["flow"] ?: "",
            useTls = params["security"] == "tls" || params["security"] == "reality",
            network = params["type"] ?: "tcp",
            wsPath = params["path"] ?: "",
            wsHost = params["host"] ?: "",
            serverName = params["sni"] ?: ""
        )
    }
    
    /**
     * Parse VMess share link (Base64 encoded JSON)
     * Format: vmess://base64_json
     */
    private fun parseVmess(link: String): ServerProfile {
        val base64 = link.removePrefix("vmess://")
        val json = String(Base64.getDecoder().decode(base64))
        
        // Parse JSON manually (simple implementation)
        val fields = parseSimpleJson(json)
        
        return ServerProfile(
            name = fields["ps"] ?: "${fields["add"]}:${fields["port"]}",
            address = fields["add"] ?: "",
            port = fields["port"]?.toIntOrNull() ?: 443,
            uuid = fields["id"] ?: "",
            protocol = Protocol.VMESS,
            encryption = fields["scy"] ?: "auto",
            useTls = fields["tls"] == "tls",
            network = fields["net"] ?: "tcp",
            wsPath = fields["path"] ?: "",
            wsHost = fields["host"] ?: "",
            serverName = fields["sni"] ?: ""
        )
    }
    
    /**
     * Parse Trojan share link
     * Format: trojan://password@address:port?params#name
     */
    private fun parseTrojan(link: String): ServerProfile {
        val url = link.removePrefix("trojan://")
        val parts = url.split("#", limit = 2)
        val name = if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else ""
        
        val mainPart = parts[0]
        val queryIndex = mainPart.indexOf("?")
        val params = if (queryIndex > 0) {
            parseQueryParams(mainPart.substring(queryIndex + 1))
        } else {
            emptyMap()
        }
        
        val hostPart = if (queryIndex > 0) mainPart.substring(0, queryIndex) else mainPart
        val passwordAndHost = hostPart.split("@")
        val password = passwordAndHost[0]
        val addressPort = passwordAndHost[1].split(":")
        val address = addressPort[0]
        val port = addressPort[1].toIntOrNull() ?: 443
        
        return ServerProfile(
            name = name.ifEmpty { "$address:$port" },
            address = address,
            port = port,
            password = password,
            uuid = password, // Trojan uses password as UUID field
            protocol = Protocol.TROJAN,
            useTls = true, // Trojan always uses TLS
            network = params["type"] ?: "tcp",
            serverName = params["sni"] ?: address
        )
    }
    
    private fun parseQueryParams(query: String): Map<String, String> {
        return query.split("&")
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to URLDecoder.decode(parts[1], "UTF-8")
                } else null
            }
            .toMap()
    }
    
    private fun parseSimpleJson(json: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val cleaned = json.trim().removeSurrounding("{", "}")
        
        // Very simple JSON parser for flat objects
        val pattern = """"(\w+)"\s*:\s*"?([^",}]*)"?""".toRegex()
        pattern.findAll(cleaned).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].trim().removeSurrounding("\"")
            result[key] = value
        }
        
        return result
    }
}
