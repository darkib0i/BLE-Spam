package com.blespam.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blespam.app.data.local.entity.SavedConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedConfigDao {
    @Query("SELECT * FROM saved_configs ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedConfigEntity>>

    @Query("SELECT * FROM saved_configs WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<SavedConfigEntity>>

    @Query("SELECT * FROM saved_configs WHERE id = :id")
    suspend fun getById(id: Long): SavedConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SavedConfigEntity): Long

    @Update
    suspend fun update(entity: SavedConfigEntity)

    @Query("UPDATE saved_configs SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Delete
    suspend fun delete(entity: SavedConfigEntity)

    @Query("SELECT * FROM saved_configs ORDER BY updatedAt DESC")
    suspend fun getAllOnce(): List<SavedConfigEntity>

    @Query("DELETE FROM saved_configs")
    suspend fun clear()
}
