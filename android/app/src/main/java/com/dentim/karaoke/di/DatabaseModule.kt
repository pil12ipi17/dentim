package com.dentim.karaoke.di

import android.content.Context
import androidx.room.Room
import com.dentim.karaoke.data.local.KaraokeDatabase
import com.dentim.karaoke.data.local.dao.TrackDao
import com.dentim.karaoke.data.local.dao.ProcessingDao
import com.dentim.karaoke.data.local.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 * Provides database instance and DAOs for dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideKaraokeDatabase(
        @ApplicationContext context: Context
    ): KaraokeDatabase {
        return Room.databaseBuilder(
            context,
            KaraokeDatabase::class.java,
            KaraokeDatabase.DATABASE_NAME
        )
            .addMigrations(*KaraokeDatabase.ALL_MIGRATIONS)
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }
    
    @Provides
    fun provideTrackDao(database: KaraokeDatabase): TrackDao {
        return database.trackDao()
    }
    
    @Provides
    fun provideProcessingDao(database: KaraokeDatabase): ProcessingDao {
        return database.processingDao()
    }
    
    @Provides
    fun provideSessionDao(database: KaraokeDatabase): SessionDao {
        return database.sessionDao()
    }
}