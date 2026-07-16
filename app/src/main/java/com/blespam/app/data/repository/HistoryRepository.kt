package com.blespam.app.data.repository

import com.blespam.app.data.local.HistoryDao
import com.blespam.app.data.local.HistoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Records and exposes the advertising session history used by the Advanced tab. */
@Singleton
class HistoryRepository @Inject constructor(
    private val dao: HistoryDao,
) {
    fun observeRecent(limit: Int = 200): Flow<List<HistoryEntity>> = dao.observeRecent(limit)
    fun observeCount(): Flow<Int> = dao.observeCount()
    fun observeTotalPackets(): Flow<Long> = dao.observeTotalPackets()

    suspend fun record(entry: HistoryEntity): Long = dao.insert(entry)
    suspend fun clear() = dao.clear()
    suspend fun exportAll(): List<HistoryEntity> = dao.getAllOnce()
}
