package com.dentim.karaoke.data.local.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.dentim.karaoke.data.local.entity.SessionEntity

/**
 * Data Access Object for SessionEntity
 * Provides database operations for karaoke sessions
 */
@Dao
interface SessionDao {
    
    @Query("SELECT * FROM karaoke_sessions ORDER BY last_played_at DESC, created_at DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM karaoke_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): SessionEntity?
    
    @Query("SELECT * FROM karaoke_sessions WHERE id = :sessionId")
    fun getSessionByIdFlow(sessionId: String): Flow<SessionEntity?>
    
    @Query("SELECT * FROM karaoke_sessions WHERE track_id = :trackId ORDER BY created_at DESC")
    fun getSessionsByTrackId(trackId: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM karaoke_sessions WHERE processing_id = :processingId ORDER BY created_at DESC")
    fun getSessionsByProcessingId(processingId: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM karaoke_sessions ORDER BY last_played_at DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM karaoke_sessions WHERE play_count > 0 ORDER BY play_count DESC LIMIT :limit")
    fun getMostPlayedSessions(limit: Int = 10): Flow<List<SessionEntity>>
    
    @Query("SELECT COUNT(*) FROM karaoke_sessions")
    suspend fun getSessionCount(): Int
    
    @Query("SELECT SUM(play_count) FROM karaoke_sessions")
    suspend fun getTotalPlayCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Delete
    suspend fun deleteSession(session: SessionEntity)
    
    @Query("DELETE FROM karaoke_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)
    
    @Query("DELETE FROM karaoke_sessions WHERE track_id = :trackId")
    suspend fun deleteSessionsByTrackId(trackId: String)
    
    @Query("DELETE FROM karaoke_sessions WHERE processing_id = :processingId")
    suspend fun deleteSessionsByProcessingId(processingId: String)
    
    @Query("DELETE FROM karaoke_sessions")
    suspend fun deleteAllSessions()
    
    @Query("UPDATE karaoke_sessions SET playback_position_ms = :position, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updatePlaybackPosition(sessionId: String, position: Long, updatedAt: java.util.Date)
    
    @Query("UPDATE karaoke_sessions SET vocals_volume = :vocalsVolume, instrumental_volume = :instrumentalVolume, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun updateVolumeLevels(sessionId: String, vocalsVolume: Float, instrumentalVolume: Float, updatedAt: java.util.Date)
    
    @Query("UPDATE karaoke_sessions SET play_count = play_count + 1, last_played_at = :playedAt, updated_at = :updatedAt WHERE id = :sessionId")
    suspend fun incrementPlayCount(sessionId: String, playedAt: java.util.Date, updatedAt: java.util.Date)
}