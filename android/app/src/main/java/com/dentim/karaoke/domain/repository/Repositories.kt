package com.dentim.karaoke.domain.repository

import kotlinx.coroutines.flow.Flow
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.Session
import com.dentim.karaoke.domain.model.ProcessingStatus
import com.dentim.karaoke.domain.model.AIModel
import java.io.File

/**
 * Repository interfaces defining data layer contracts
 * Clean Architecture: Domain layer defines contracts, Data layer implements them
 */

interface TrackRepository {
    
    fun getAllTracks(): Flow<List<Track>>
    
    suspend fun getTrackById(trackId: String): Track?
    
    fun getTrackByIdFlow(trackId: String): Flow<Track?>
    
    suspend fun getTrackByChecksum(checksum: String): Track?
    
    suspend fun insertTrack(track: Track)
    
    suspend fun updateTrack(track: Track)
    
    suspend fun deleteTrack(trackId: String)
    
    suspend fun deleteAllTracks()
    
    fun searchTracks(query: String): Flow<List<Track>>
    
    suspend fun getTrackCount(): Int
    
    suspend fun getTotalFileSize(): Long
}

interface ProcessingRepository {
    
    fun getAllProcessingJobs(): Flow<List<Processing>>
    
    suspend fun getProcessingById(processingId: String): Processing?
    
    fun getProcessingByIdFlow(processingId: String): Flow<Processing?>
    
    fun getProcessingByTrackId(trackId: String): Flow<List<Processing>>
    
    suspend fun getProcessingByTrackIdAndStatus(trackId: String, status: ProcessingStatus): Processing?
    
    fun getActiveProcessingJobs(): Flow<List<Processing>>
    
    suspend fun insertProcessing(processing: Processing)
    
    suspend fun updateProcessing(processing: Processing)
    
    suspend fun updateProcessingProgress(processingId: String, progress: Int)
    
    suspend fun updateProcessingStatus(processingId: String, status: ProcessingStatus, errorMessage: String? = null)
    
    suspend fun deleteProcessing(processingId: String)
    
    suspend fun deleteProcessingByTrackId(trackId: String)
    
    // Remote operations
    suspend fun uploadTrackForProcessing(file: File, aiModel: AIModel): Result<Processing>
    
    suspend fun getRemoteProcessingStatus(processingId: String): Result<Processing>
    
    suspend fun cancelProcessing(processingId: String): Result<Boolean>
    
    suspend fun downloadProcessedFile(processingId: String, fileType: FileType): Result<String>
    
    suspend fun syncProcessingJobs(): Result<List<Processing>>
    
    fun subscribeToProcessingUpdates(processingId: String)
    
    fun unsubscribeFromProcessingUpdates(processingId: String)
    
    enum class FileType {
        VOCALS, INSTRUMENTAL
    }
}

interface SessionRepository {
    
    fun getAllSessions(): Flow<List<Session>>
    
    suspend fun getSessionById(sessionId: String): Session?
    
    fun getSessionByIdFlow(sessionId: String): Flow<Session?>
    
    fun getSessionsByTrackId(trackId: String): Flow<List<Session>>
    
    fun getSessionsByProcessingId(processingId: String): Flow<List<Session>>
    
    fun getRecentSessions(limit: Int = 10): Flow<List<Session>>
    
    fun getMostPlayedSessions(limit: Int = 10): Flow<List<Session>>
    
    suspend fun insertSession(session: Session)
    
    suspend fun updateSession(session: Session)
    
    suspend fun updatePlaybackPosition(sessionId: String, position: Long)
    
    suspend fun updateVolumeLevels(sessionId: String, vocalsVolume: Float, instrumentalVolume: Float)
    
    suspend fun incrementPlayCount(sessionId: String)
    
    suspend fun deleteSession(sessionId: String)
    
    suspend fun deleteSessionsByTrackId(trackId: String)
    
    suspend fun deleteSessionsByProcessingId(processingId: String)
    
    suspend fun getSessionCount(): Int
    
    suspend fun getTotalPlayCount(): Int
}