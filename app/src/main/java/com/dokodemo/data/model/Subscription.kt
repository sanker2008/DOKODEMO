package com.dokodemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Subscription entity for storing subscription URLs
 */
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val name: String,
    val url: String,
    
    val isActive: Boolean = true,
    val lastUpdated: Long? = null,
    val serverCount: Int = 0,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
