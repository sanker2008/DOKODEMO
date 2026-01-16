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
    
    // Basic Info
    val name: String,
    val address: String,
    val port: Int,
    
    // Authentication
    val uuid: String = "",
    val password: String = "",
    
    // Protocol Configuration
    val protocol: Protocol = Protocol.VLESS,
    val encryption: String = "none",
    val flow: String = "",
    
    // TLS Settings
    val useTls: Boolean = true,
    val allowInsecure: Boolean = false,
    val serverName: String = "",
    
    // Network Settings
    val network: String = "tcp", // tcp, ws, grpc, etc.
    val wsPath: String = "",
    val wsHost: String = "",
    
    // Location
    val countryCode: String = "",
    val countryName: String = "",
    
    // Status
    val latency: Int? = null,
    val isSelected: Boolean = false,
    val lastConnected: Long? = null,
    
    // Subscription
    val subscriptionId: Long? = null,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
