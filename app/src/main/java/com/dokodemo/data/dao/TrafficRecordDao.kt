package com.dokodemo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.dokodemo.data.model.TrafficRecord

@Dao
interface TrafficRecordDao {
    @Query("SELECT * FROM traffic_records ORDER BY connectTime DESC")
    fun getAllHistory(): Flow<List<TrafficRecord>>

    @Insert
    suspend fun insert(record: TrafficRecord)

    @Query("DELETE FROM traffic_records")
    suspend fun clearHistory()
}
