package com.blespam.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blespam.app.data.local.dao.HistoryDao
import com.blespam.app.data.local.dao.SavedConfigDao
import com.blespam.app.data.local.entity.HistoryEntity
import com.blespam.app.data.local.entity.SavedConfigEntity

@Database(
    entities = [SavedConfigEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BleSpamDatabase : RoomDatabase() {
    abstract fun savedConfigDao(): SavedConfigDao
    abstract fun historyDao(): HistoryDao

    companion object {
        const val NAME = "ble_spam.db"
    }
}
