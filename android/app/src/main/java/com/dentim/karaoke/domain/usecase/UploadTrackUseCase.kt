package com.dentim.karaoke.domain.usecase

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.AIModel
import com.dentim.karaoke.domain.model.ProcessingStatus
import com.dentim.karaoke.domain.repository.TrackRepository
import com.dentim.karaoke.domain.repository.ProcessingRepository
import com.dentim.karaoke.util.AudioMetadataExtractor
import com.dentim.karaoke.util.ChecksumCalculator
import com.dentim.karaoke.util.ErrorHandler
import java.io.File
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for uploading and processing audio tracks
 * Handles the complete flow from file selection to processing initiation
 */
@Singleton
class UploadTrackUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val processingRepository: ProcessingRepository
) {
    
    companion object {
        private const val TAG = "UploadTrackUseCase"
    }
    
    /**
     * Upload track and initiate processing
     * Returns Flow with upload progress and final result
     */
    fun execute(
        file: File,
        aiModel: AIModel,
        filename: String? = null,
        skipDuplicateCheck: Boolean = false
    ): Flow<UploadProgress> = flow {
        
        emit(UploadProgress.Started)
        
        try {
            // Step 1: Extract metadata
            emit(UploadProgress.ExtractingMetadata)
            val metadata = AudioMetadataExtractor.extractMetadata(file)
            Log.d(TAG, "Extracted metadata: $metadata")
            
            // Step 2: Calculate checksum
            emit(UploadProgress.CalculatingChecksum)
            val checksum = ChecksumCalculator.calculateMD5(file)
            Log.d(TAG, "Calculated checksum: $checksum")
            
            // Step 3: Check for duplicates (skip if requested)
            if (!skipDuplicateCheck) {
                emit(UploadProgress.CheckingDuplicates)
                val existingTrack = trackRepository.getTrackByChecksum(checksum)
                if (existingTrack != null) {
                    Log.d(TAG, "Duplicate track found: ${existingTrack.id}")
                    emit(UploadProgress.DuplicateFound(existingTrack))
                    return@flow
                }
            } else {
                Log.d(TAG, "Skipping duplicate check as requested")
            }
            
            // Step 4: Create track entity
            val trackId = UUID.randomUUID().toString()
            val displayFilename = filename ?: file.name
            
            val track = Track(
                id = trackId,
                filename = displayFilename,
                originalPath = file.absolutePath,
                fileSize = file.length(),
                durationMs = metadata.duration,
                mimeType = getMimeTypeFromExtension(displayFilename),
                checksum = checksum,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            // Step 5: Save track to database
            emit(UploadProgress.SavingTrack)
            trackRepository.insertTrack(track)
            Log.d(TAG, "Track saved to database: $trackId")
            
            // Step 6: Upload to server
            emit(UploadProgress.UploadingToServer)
            val uploadResult = processingRepository.uploadTrackForProcessing(file, aiModel)
            
            if (uploadResult.isSuccess) {
                val processing = uploadResult.getOrThrow().copy(trackId = trackId)
                
                // Save processing with correct track ID
                processingRepository.insertProcessing(processing)
                
                Log.d(TAG, "Upload successful: ${processing.id}, processing started")
                
                // Show processing status with estimated time
                emit(UploadProgress.ProcessingOnServer(180)) // 3 minutes estimate
                
                // For now, we complete immediately after upload
                // In a real app, you'd poll server for completion
                emit(UploadProgress.Completed(track, processing))
            } else {
                val error = uploadResult.exceptionOrNull()
                Log.e(TAG, "Upload failed", error)
                emit(UploadProgress.Failed(error?.message ?: "Upload failed"))
            }
            
        } catch (e: Exception) {
            val appError = ErrorHandler.handleError(e, TAG, "Upload track")
            Log.e(TAG, "Upload error", e)
            emit(UploadProgress.Failed(appError.message))
        }
        
    }.catch { throwable ->
        val appError = ErrorHandler.handleError(throwable, TAG, "Upload flow error")
        emit(UploadProgress.Failed(appError.message))
    }
    
    private fun getMimeTypeFromExtension(filename: String): String {
        val extension = filename.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "flac" -> "audio/flac"
            "ogg" -> "audio/ogg"
            "3gp" -> "audio/3gpp"
            "amr" -> "audio/amr"
            else -> "audio/unknown"
        }
    }
}

/**
 * Represents the progress of track upload operation
 */
sealed class UploadProgress {
    object Started : UploadProgress()
    object ExtractingMetadata : UploadProgress()
    object CalculatingChecksum : UploadProgress()
    object CheckingDuplicates : UploadProgress()
    object SavingTrack : UploadProgress()
    object UploadingToServer : UploadProgress()
    data class ProcessingOnServer(val estimatedTime: Int? = null) : UploadProgress()
    
    data class DuplicateFound(val existingTrack: Track) : UploadProgress()
    data class Completed(val track: Track, val processing: Processing) : UploadProgress()
    data class Failed(val error: String) : UploadProgress()
    
    val isInProgress: Boolean
        get() = this !is Completed && this !is Failed && this !is DuplicateFound
}