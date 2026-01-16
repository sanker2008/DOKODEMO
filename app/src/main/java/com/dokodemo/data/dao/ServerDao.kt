package com.dokodemo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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
    
    @Query("UPDATE server_profiles SET isSelected = 0")
    suspend fun clearSelection()
    
    @Query("UPDATE server_profiles SET isSelected = 1 WHERE id = :id")
    suspend fun selectServer(id: Long)
    
    @Query("UPDATE server_profiles SET latency = :latency, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLatency(id: Long, latency: Int?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM server_profiles")
    suspend fun getServerCount(): Int
}
