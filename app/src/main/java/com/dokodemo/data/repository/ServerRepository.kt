package com.dokodemo.data.repository

import com.dokodemo.data.dao.ServerDao
import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
    private val serverDao: ServerDao
) {
    fun getAllServers(): Flow<List<ServerProfile>> = serverDao.getAllServers()
    
    fun getSelectedServer(): Flow<ServerProfile?> = serverDao.getSelectedServerFlow()
    
    fun getServerById(id: Long): Flow<ServerProfile?> = serverDao.getServerByIdFlow(id)
    
    fun searchServers(query: String): Flow<List<ServerProfile>> = serverDao.searchServers(query)
    
    fun getServersBySubscription(subscriptionId: Long): Flow<List<ServerProfile>> = 
        serverDao.getServersBySubscription(subscriptionId)
    
    suspend fun addServer(server: ServerProfile): Long = serverDao.insert(server)
    
    suspend fun addServers(servers: List<ServerProfile>) = serverDao.insertAll(servers)
    
    suspend fun updateServer(server: ServerProfile) = serverDao.update(server)
    
    suspend fun deleteServer(server: ServerProfile) = serverDao.delete(server)
    
    suspend fun deleteServerById(id: Long) = serverDao.deleteById(id)
    
    suspend fun selectServer(id: Long) {
        serverDao.clearSelection()
        serverDao.selectServer(id)
    }
    
    suspend fun updateLatency(id: Long, latency: Int?) = serverDao.updateLatency(id, latency)
    
    suspend fun getServerCount(): Int = serverDao.getServerCount()
    
    /**
     * Seed database with mock data for testing
     */
    suspend fun seedMockData() {
        if (getServerCount() == 0) {
            val mockServers = listOf(
                ServerProfile(
                    name = "TOKYO_CORE_01",
                    address = "jp1.dokodemo.vpn",
                    port = 443,
                    uuid = "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                    protocol = Protocol.VLESS,
                    countryCode = "JP",
                    countryName = "Japan",
                    latency = 12,
                    isSelected = true
                ),
                ServerProfile(
                    name = "NEW_YORK_EAST",
                    address = "us1.dokodemo.vpn",
                    port = 443,
                    uuid = "b2c3d4e5-f678-9012-3456-7890abcdef12",
                    protocol = Protocol.VMESS,
                    countryCode = "US",
                    countryName = "United States",
                    latency = 45
                ),
                ServerProfile(
                    name = "FRANKFURT_HUB",
                    address = "de1.dokodemo.vpn",
                    port = 443,
                    uuid = "c3d4e5f6-7890-1234-5678-90abcdef1234",
                    protocol = Protocol.VLESS,
                    countryCode = "DE",
                    countryName = "Germany",
                    latency = 102
                ),
                ServerProfile(
                    name = "SINGAPORE_DIRECT",
                    address = "sg1.dokodemo.vpn",
                    port = 443,
                    uuid = "d4e5f678-9012-3456-7890-abcdef123456",
                    protocol = Protocol.VLESS,
                    countryCode = "SG",
                    countryName = "Singapore",
                    latency = 18
                ),
                ServerProfile(
                    name = "LONDON_BRIDGE",
                    address = "uk1.dokodemo.vpn",
                    port = 443,
                    uuid = "e5f67890-1234-5678-90ab-cdef12345678",
                    protocol = Protocol.TROJAN,
                    countryCode = "UK",
                    countryName = "United Kingdom",
                    latency = 88
                )
            )
            addServers(mockServers)
        }
    }
}
