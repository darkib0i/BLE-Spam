package com.blespam.app.di

import android.content.Context
import androidx.room.Room
import com.blespam.app.data.local.BleSpamDatabase
import com.blespam.app.data.local.dao.HistoryDao
import com.blespam.app.data.local.dao.SavedConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and its DAOs. Everything else in the app is
 * constructor-injected, so this is the only module that needs @Provides.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BleSpamDatabase =
        Room.databaseBuilder(context, BleSpamDatabase::class.java, BleSpamDatabase.NAME)
            // Schema is versioned; destructive migration is acceptable for a
            // tool whose data is user-recreatable, and keeps startup simple.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSavedConfigDao(db: BleSpamDatabase): SavedConfigDao = db.savedConfigDao()

    @Provides
    fun provideHistoryDao(db: BleSpamDatabase): HistoryDao = db.historyDao()
}
