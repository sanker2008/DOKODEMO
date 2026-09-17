package com.dokodemo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Query
import androidx.room.Update
import com.dokodemo.data.model.ServerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    
    @Query("SELECT * FROM server_profiles ORDER BY name ASC")
    fun getAllServers(): Flow<List<ServerProfile>>
    
    @Query("SELECT * FROM server_profiles WHERE id = :id")
    suspend fun getServerById(id: Long): ServerProfile?
    
    @Query("SELECT * FROM server_profiles WHERE id = :id")
    fun getServerByIdFlow(id: Long): Flow<ServerProfile?>
    
    @Query("SELECT * FROM server_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedServer(): ServerProfile?
    
    @Query("SELECT * FROM server_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedServerFlow(): Flow<ServerProfile?>
    
    @Query("SELECT * FROM server_profiles WHERE subscriptionId = :subscriptionId")
    fun getServersBySubscription(subscriptionId: Long): Flow<List<ServerProfile>>
    
    @Query("SELECT * FROM server_profiles WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    fun searchServers(query: String): Flow<List<ServerProfile>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: ServerProfile): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(servers: List<ServerProfile>)
    
    @Update
    suspend fun update(server: ServerProfile)
    
    @Delete
    suspend fun delete(server: ServerProfile)
    
    @Query("DELETE FROM server_profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM server_profiles WHERE subscriptionId = :subscriptionId")
    suspend fun deleteBySubscription(subscriptionId: Long)
    
    @Query("SELECT * FROM server_profiles WHERE subscriptionId = :subscriptionId")
    suspend fun subscriptionSnapshot(subscriptionId: Long): List<ServerProfile>

    @Transaction
    suspend fun replaceSubscription(subscriptionId: Long, servers: List<ServerProfile>) {
        require(servers.isNotEmpty()) { "Subscription contains no supported servers" }
        val previous = subscriptionSnapshot(subscriptionId).toMutableList()
        val selected = getSelectedServer()
        var replacement = servers.map { incoming ->
            val old = previous.firstOrNull {
                it.protocol == incoming.protocol && it.address == incoming.address &&
                    it.port == incoming.port && it.uuid == incoming.uuid && it.password == incoming.password
            }
            if (old != null) previous.remove(old)
            incoming.copy(id = old?.id ?: 0, subscriptionId = subscriptionId,
                isSelected = old?.isSelected ?: false, createdAt = old?.createdAt ?: incoming.createdAt,
                lastConnected = old?.lastConnected, latency = old?.latency)
        }
        if ((selected == null || selected.subscriptionId == subscriptionId) && replacement.none { it.isSelected }) {
            replacement = replacement.mapIndexed { index, server -> server.copy(isSelected = index == 0) }
        }
        deleteBySubscription(subscriptionId)
        insertAll(replacement)
    }

    @Transaction
    suspend fun insertAndSelect(server: ServerProfile): Long {
        val id = insert(server.copy(isSelected = false))
        selectServer(id)
        return id
    }

    @Transaction
    suspend fun updateEditableServer(server: ServerProfile) {
        val current = requireNotNull(getServerById(server.id)) { "Node was deleted" }
        update(server.copy(subscriptionId = current.subscriptionId, isSelected = current.isSelected,
            countryCode = current.countryCode, countryName = current.countryName,
            latency = current.latency, lastConnected = current.lastConnected, createdAt = current.createdAt))
    }

    @Query("UPDATE server_profiles SET isSelected = 0")
    suspend fun clearSelection()
    
    @Query("UPDATE server_profiles SET isSelected = 1 WHERE id = :id")
    suspend fun markSelected(id: Long)
    
    @Transaction
    suspend fun selectServer(id: Long) {
        if (getServerById(id) == null) return
        clearSelection()
        markSelected(id)
    }
    
    @Query("UPDATE server_profiles SET latency = :latency, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Int?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM server_profiles")
    suspend fun getServerCount(): Int
}
