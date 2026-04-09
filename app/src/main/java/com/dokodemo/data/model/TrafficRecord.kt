package com.dokodemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traffic_records")
data class TrafficRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serverName: String,
    val connectTime: Long,
    val disconnectTime: Long,
    val uploadBytes: Long,
    val downloadBytes: Long
)
