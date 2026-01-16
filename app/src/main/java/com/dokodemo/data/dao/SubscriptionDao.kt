package com.dokodemo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dokodemo.data.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscriptions ORDER BY name ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): Subscription?
    
    @Query("SELECT * FROM subscriptions WHERE isActive = 1")
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: Subscription): Long
    
    @Update
    suspend fun update(subscription: Subscription)
    
    @Delete
    suspend fun delete(subscription: Subscription)
    
    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE subscriptions SET lastUpdated = :timestamp, serverCount = :count WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, timestamp: Long, count: Int)
}
