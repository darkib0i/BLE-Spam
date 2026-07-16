package com.blespam.app.data.repository

import com.blespam.app.data.local.SavedConfigDao
import com.blespam.app.data.local.SavedConfigEntity
import com.blespam.app.data.model.BleConfig
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for saved configurations and favorites. Wraps the [SavedConfigDao] and
 * keeps the UI in domain types.
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val dao: SavedConfigDao,
) {
    fun observeSavedConfigs(): Flow<List<SavedConfigEntity>> = dao.observeAll()
    fun observeFavorites(): Flow<List<SavedConfigEntity>> = dao.observeFavorites()

    suspend fun save(config: BleConfig, name: String): Long =
        dao.upsert(config.toEntity(name = name))

    suspend fun update(entity: SavedConfigEntity) = dao.update(entity)

    suspend fun setFavorite(id: Long, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun delete(entity: SavedConfigEntity) = dao.delete(entity)

    suspend fun getConfig(id: Long): BleConfig? = dao.getById(id)?.toConfig()

    suspend fun exportAll(): List<SavedConfigEntity> = dao.getAllOnce()

    suspend fun importAll(entities: List<SavedConfigEntity>) {
        // Re-key on insert so imports never collide with existing rows.
        entities.forEach { dao.upsert(it.copy(id = 0)) }
    }
}
