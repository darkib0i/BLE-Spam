package com.blespam.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blespam.app.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY startedAt DESC LIMIT 200")
    fun observeRecent(): Flow<List<HistoryEntity>>

    @Insert
    suspend fun insert(entity: HistoryEntity): Long

    @Query("SELECT COUNT(*) FROM history")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(packetsSent), 0) FROM history")
    fun observeTotalPackets(): Flow<Long>

    @Query("SELECT COALESCE(SUM(durationMillis), 0) FROM history")
    fun observeTotalDuration(): Flow<Long>

    @Query("SELECT * FROM history ORDER BY startedAt DESC")
    suspend fun getAllOnce(): List<HistoryEntity>

    @Query("DELETE FROM history")
    suspend fun clear()
}
