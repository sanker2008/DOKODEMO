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
    /**
     * Seed database with mock data for testing
     * Legacy: Method kept but empty to prevent mock data injection.
     */
    suspend fun seedMockData() {
        // Mock data removed for production functionality
    }
}
