package com.blespam.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedConfigDao {
    @Query("SELECT * FROM saved_configs ORDER BY isFavorite DESC, createdAt DESC")
    fun observeAll(): Flow<List<SavedConfigEntity>>

    @Query("SELECT * FROM saved_configs WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<SavedConfigEntity>>

    @Query("SELECT * FROM saved_configs WHERE id = :id")
    suspend fun getById(id: Long): SavedConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: SavedConfigEntity): Long

    @Update
    suspend fun update(config: SavedConfigEntity)

    @Query("UPDATE saved_configs SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Delete
    suspend fun delete(config: SavedConfigEntity)

    @Query("SELECT * FROM saved_configs ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<SavedConfigEntity>
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM advertising_history ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM advertising_history ORDER BY startedAt DESC")
    suspend fun getAllOnce(): List<HistoryEntity>

    @Insert
    suspend fun insert(entry: HistoryEntity): Long

    @Query("DELETE FROM advertising_history")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM advertising_history")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(estimatedPackets), 0) FROM advertising_history")
    fun observeTotalPackets(): Flow<Long>
}
