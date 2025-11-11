package com.dentim.karaoke.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dentim.karaoke.data.local.converter.Converters
import com.dentim.karaoke.data.local.dao.TrackDao
import com.dentim.karaoke.data.local.dao.ProcessingDao
import com.dentim.karaoke.data.local.dao.SessionDao
import com.dentim.karaoke.data.local.entity.TrackEntity
import com.dentim.karaoke.data.local.entity.ProcessingEntity
import com.dentim.karaoke.data.local.entity.SessionEntity

/**
 * Room database configuration for the Karaoke app
 * Manages local data storage for tracks, processing jobs, and sessions
 */
@Database(
    entities = [
        TrackEntity::class,
        ProcessingEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KaraokeDatabase : RoomDatabase() {
    
    abstract fun trackDao(): TrackDao
    abstract fun processingDao(): ProcessingDao
    abstract fun sessionDao(): SessionDao
    
    companion object {
        const val DATABASE_NAME = "karaoke_database"
        
        // Migration from version 1 to 2 (placeholder for future use)
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration logic will go here
            }
        }
        
        // Add additional migrations as needed
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            // MIGRATION_1_2 - uncomment when needed
        )
    }
}