package com.dentim.karaoke.data.local.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.dentim.karaoke.data.local.entity.TrackEntity

/**
 * Data Access Object for TrackEntity
 * Provides database operations for audio tracks
 */
@Dao
interface TrackDao {
    
    @Query("SELECT * FROM tracks ORDER BY created_at DESC")
    fun getAllTracks(): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?
    
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    fun getTrackByIdFlow(trackId: String): Flow<TrackEntity?>
    
    @Query("SELECT * FROM tracks WHERE checksum = :checksum LIMIT 1")
    suspend fun getTrackByChecksum(checksum: String): TrackEntity?
    
    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int
    
    @Query("SELECT SUM(file_size) FROM tracks")
    suspend fun getTotalFileSize(): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)
    
    @Update
    suspend fun updateTrack(track: TrackEntity)
    
    @Delete
    suspend fun deleteTrack(track: TrackEntity)
    
    @Query("DELETE FROM tracks WHERE id = :trackId")
    suspend fun deleteTrackById(trackId: String)
    
    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
    
    @Query("SELECT * FROM tracks WHERE filename LIKE :query OR original_path LIKE :query ORDER BY created_at DESC")
    fun searchTracks(query: String): Flow<List<TrackEntity>>
}