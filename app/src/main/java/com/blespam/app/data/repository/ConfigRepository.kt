package com.blespam.app.data.repository

import com.blespam.app.data.local.dao.HistoryDao
import com.blespam.app.data.local.dao.SavedConfigDao
import com.blespam.app.data.mapper.toDomain
import com.blespam.app.data.mapper.toEntity
import com.blespam.app.domain.model.HistoryRecord
import com.blespam.app.domain.model.SavedConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for persisted configurations and history. The UI layer
 * only sees domain types; entity <-> domain translation is confined here.
 */
@Singleton
class ConfigRepository @Inject constructor(
    private val savedConfigDao: SavedConfigDao,
    private val historyDao: HistoryDao,
) {
    // --- Saved configurations ---
    val savedConfigs: Flow<List<SavedConfig>> =
        savedConfigDao.observeAll().map { list -> list.map { it.toDomain() } }

    val favorites: Flow<List<SavedConfig>> =
        savedConfigDao.observeFavorites().map { list -> list.map { it.toDomain() } }

    suspend fun save(config: SavedConfig): Long = savedConfigDao.insert(config.toEntity())

    suspend fun update(config: SavedConfig) = savedConfigDao.update(config.toEntity())

    suspend fun setFavorite(id: Long, favorite: Boolean) = savedConfigDao.setFavorite(id, favorite)

    suspend fun delete(config: SavedConfig) = savedConfigDao.delete(config.toEntity())

    suspend fun exportConfigs(): List<SavedConfig> =
        savedConfigDao.getAllOnce().map { it.toDomain() }

    suspend fun importConfigs(configs: List<SavedConfig>) {
        configs.forEach { savedConfigDao.insert(it.copy(id = 0).toEntity()) }
    }

    // --- History ---
    val history: Flow<List<HistoryRecord>> =
        historyDao.observeRecent().map { list -> list.map { it.toDomain() } }

    val historyCount: Flow<Int> = historyDao.observeCount()
    val totalPackets: Flow<Long> = historyDao.observeTotalPackets()
    val totalDuration: Flow<Long> = historyDao.observeTotalDuration()

    suspend fun recordHistory(record: HistoryRecord) = historyDao.insert(record.toEntity())

    suspend fun exportHistory(): List<HistoryRecord> =
        historyDao.getAllOnce().map { it.toDomain() }

    suspend fun clearHistory() = historyDao.clear()
}
