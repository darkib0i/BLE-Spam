package com.blespam.app.di

import android.content.Context
import androidx.room.Room
import com.blespam.app.data.local.BleSpamDatabase
import com.blespam.app.data.local.HistoryDao
import com.blespam.app.data.local.SavedConfigDao
import com.blespam.app.ble.PayloadBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides application-wide singletons: the Room database + DAOs and the stateless payload builder.
 * Repositories, the advertiser, and controllers are constructor-injected via @Inject, so they need
 * no explicit @Provides here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BleSpamDatabase =
        Room.databaseBuilder(context, BleSpamDatabase::class.java, BleSpamDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSavedConfigDao(db: BleSpamDatabase): SavedConfigDao = db.savedConfigDao()

    @Provides
    fun provideHistoryDao(db: BleSpamDatabase): HistoryDao = db.historyDao()

    @Provides
    @Singleton
    fun providePayloadBuilder(): PayloadBuilder = PayloadBuilder()
}
