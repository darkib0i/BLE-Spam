package com.blespam.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

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
