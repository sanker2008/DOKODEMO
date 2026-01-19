package com.dokodemo.ui.screens.serverlist

import com.dokodemo.data.dao.ServerDao
import com.dokodemo.data.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ServerRepository @Inject constructor(
    private val serverDao: ServerDao
) {
    fun getAllServers(): Flow<List<ServerProfile>> = serverDao.getAllServers()

    suspend fun getServerById(id: Long): ServerProfile? = serverDao.getServerById(id)

    suspend fun insert(serverProfile: ServerProfile) {
        serverDao.insert(serverProfile)
    }

    suspend fun selectServer(serverId: Long) {
        serverDao.selectServer(serverId)
    }

    suspend fun deleteServerById(id: Long) {
        serverDao.deleteById(id)
    }

    suspend fun updateLatency(id: Long, latency: Int?) {
        serverDao.updateLatency(id, latency)
    }
}
