package com.dokodemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Protocol types supported by DokoDemo
 */
enum class Protocol {
    VLESS,
    VMESS,
    TROJAN,
    SHADOWSOCKS,
    WIREGUARD
}

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromProtocol(protocol: Protocol): String = protocol.name
    
    @TypeConverter
    fun toProtocol(value: String): Protocol = Protocol.valueOf(value)
}

/**
 * ServerProfile entity for Room database
 * 
 * Stores V2Ray/Xray server configuration
 */
@Entity(tableName = "server_profiles")
@TypeConverters(Converters::class)
data class ServerProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本信息
    val name: String,
    val address: String,
    val port: Int,
    
    // 鉴权信息
    val uuid: String = "",
    val password: String = "",
    
    // 协议配置
    val protocol: Protocol = Protocol.VLESS,
    val encryption: String = "auto",    // VMess: auto/aes-128-gcm 等；SS: 加密方式
    val flow: String = "",              // VLESS Reality 用
    
    // TLS 设置
    val useTls: Boolean = true,
    val allowInsecure: Boolean = false,
    val serverName: String = "",        // SNI
    
    // 网络/传输设置
    val network: String = "tcp",        // tcp / ws / grpc / kcp
    val wsPath: String = "",            // WebSocket 路径
    val wsHost: String = "",            // WebSocket Host 头
    
    // ─── KCP 专用字段 ─────────────────────────────────────────────────────
    // KCP 伪装类型：none / dtls / utp / srtp / wechat-video / wireguard
    // 用户的 SanVPN 节点使用 dtls
    val kcpHeader: String = "none",
    // KCP 混淆密钥（可选）
    val kcpSeed: String = "",
    
    // ─── Shadowsocks 专用字段 ─────────────────────────────────────────────
    // SS 加密方法：aes-256-gcm / chacha20-ietf-poly1305 等
    val ssMethod: String = "aes-256-gcm",
    
    // 位置信息（用于节点列表显示国旗/地区）
    val countryCode: String = "",
    val countryName: String = "",
    
    // 状态信息
    val latency: Int? = null,
    val isSelected: Boolean = false,
    val lastConnected: Long? = null,
    
    // 所属订阅和分组
    val subscriptionId: Long? = null,
    val groupId: Long? = null,          // 新增：所属分组 ID
    
    // 时间戳
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

