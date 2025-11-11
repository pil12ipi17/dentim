package com.dentim.karaoke.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.dentim.karaoke.data.local.dao.ProcessingDao
import com.dentim.karaoke.data.remote.api.KaraokeApiService
import com.dentim.karaoke.data.remote.websocket.KaraokeWebSocketClient
import com.dentim.karaoke.data.mapper.toDomain
import com.dentim.karaoke.data.mapper.toEntity
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.ProcessingStatus
import com.dentim.karaoke.domain.model.AIModel
import com.dentim.karaoke.domain.repository.ProcessingRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProcessingRepository
 * Manages processing jobs with local database and remote API
 */
@Singleton
class ProcessingRepositoryImpl @Inject constructor(
    private val processingDao: ProcessingDao,
    private val apiService: KaraokeApiService,
    private val webSocketClient: KaraokeWebSocketClient
) : ProcessingRepository {
    
    companion object {
        private const val TAG = "ProcessingRepository"
    }
    
    override fun getAllProcessingJobs(): Flow<List<Processing>> {
        return processingDao.getAllProcessingJobs().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getProcessingById(processingId: String): Processing? {
        return processingDao.getProcessingById(processingId)?.toDomain()
    }
    
    override fun getProcessingByIdFlow(processingId: String): Flow<Processing?> {
        return processingDao.getProcessingByIdFlow(processingId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override fun getProcessingByTrackId(trackId: String): Flow<List<Processing>> {
        return processingDao.getProcessingByTrackId(trackId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getProcessingByTrackIdAndStatus(trackId: String, status: ProcessingStatus): Processing? {
        return processingDao.getProcessingByTrackIdAndStatus(trackId, status.toEntity())?.toDomain()
    }
    
    override fun getActiveProcessingJobs(): Flow<List<Processing>> {
        val activeStatuses = listOf(ProcessingStatus.PENDING, ProcessingStatus.UPLOADING, ProcessingStatus.PROCESSING).map { it.toEntity() }
        return processingDao.getProcessingByStatuses(activeStatuses).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertProcessing(processing: Processing) {
        processingDao.insertProcessing(processing.toEntity())
    }
    
    override suspend fun updateProcessing(processing: Processing) {
        processingDao.updateProcessing(processing.toEntity())
    }
    
    override suspend fun updateProcessingProgress(processingId: String, progress: Int) {
        processingDao.updateProgress(processingId, progress, Date())
    }
    
    override suspend fun updateProcessingStatus(processingId: String, status: ProcessingStatus, errorMessage: String?) {
        if (errorMessage != null) {
            processingDao.updateStatusWithError(processingId, status.toEntity(), errorMessage, Date())
        } else {
            processingDao.updateStatus(processingId, status.toEntity(), Date())
        }
    }
    
    override suspend fun deleteProcessing(processingId: String) {
        processingDao.deleteProcessingById(processingId)
    }
    
    override suspend fun deleteProcessingByTrackId(trackId: String) {
        processingDao.deleteProcessingByTrackId(trackId)
    }
    
    // Remote operations
    override suspend fun uploadTrackForProcessing(file: File, aiModel: AIModel): Result<Processing> {
        return try {
            Log.d(TAG, "Uploading track for processing: ${file.name}, model: ${aiModel.apiValue}")
            
            // Create multipart request
            val requestFile = file.asRequestBody("audio/*".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            // Make API call
            val response = apiService.uploadAudioFile(body, aiModel.apiValue)
            
            if (response.isSuccessful) {
                val jobDto = response.body()!!
                val processing = jobDto.toDomain().copy(
                    id = jobDto.jobId,
                    trackId = UUID.randomUUID().toString() // Will be set by caller
                )
                
                // Save to local database
                insertProcessing(processing)
                
                // Subscribe to WebSocket updates
                subscribeToProcessingUpdates(processing.id)
                
                Log.d(TAG, "Processing job created: ${processing.id}")
                Result.success(processing)
            } else {
                val error = "Upload failed: ${response.code()} ${response.message()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getRemoteProcessingStatus(processingId: String): Result<Processing> {
        return try {
            val response = apiService.getProcessingStatus(processingId)
            
            if (response.isSuccessful) {
                val statusDto = response.body()!!
                val existingProcessing = getProcessingById(processingId)
                
                val updatedProcessing = statusDto.toDomain().copy(
                    trackId = existingProcessing?.trackId ?: "",
                    createdAt = existingProcessing?.createdAt ?: Date(),
                    aiModel = existingProcessing?.aiModel ?: AIModel.DEMUCS
                )
                
                // Update local database
                updateProcessing(updatedProcessing)
                
                Result.success(updatedProcessing)
            } else {
                val error = "Status check failed: ${response.code()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Status check error", e)
            Result.failure(e)
        }
    }
    
    override suspend fun cancelProcessing(processingId: String): Result<Boolean> {
        return try {
            val response = apiService.cancelProcessingJob(processingId)
            
            if (response.isSuccessful) {
                updateProcessingStatus(processingId, ProcessingStatus.CANCELLED)
                unsubscribeFromProcessingUpdates(processingId)
                Log.d(TAG, "Processing cancelled: $processingId")
                Result.success(true)
            } else {
                val error = "Cancel failed: ${response.code()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Cancel error", e)
            Result.failure(e)
        }
    }
    
    override suspend fun downloadProcessedFile(processingId: String, fileType: ProcessingRepository.FileType): Result<String> {
        return try {
            val response = when (fileType) {
                ProcessingRepository.FileType.VOCALS -> apiService.downloadVocals(processingId)
                ProcessingRepository.FileType.INSTRUMENTAL -> apiService.downloadInstrumental(processingId)
            }
            
            if (response.isSuccessful) {
                val responseBody = response.body()!!
                val fileName = "${processingId}_${fileType.name.lowercase()}.wav"
                val outputFile = File("/data/data/com.dentim.karaoke/files/processed", fileName)
                
                outputFile.parentFile?.mkdirs()
                
                FileOutputStream(outputFile).use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                
                Log.d(TAG, "File downloaded: ${outputFile.absolutePath}")
                Result.success(outputFile.absolutePath)
            } else {
                val error = "Download failed: ${response.code()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            Result.failure(e)
        }
    }
    
    override suspend fun syncProcessingJobs(): Result<List<Processing>> {
        return try {
            Log.d(TAG, "Syncing processing jobs from backend")
            val response = apiService.getAllProcessingJobs()
            
            if (response.isSuccessful) {
                val backendJobs = response.body() ?: emptyList()
                Log.d(TAG, "Fetched ${backendJobs.size} jobs from backend")
                
                // Convert and save to local database
                val domainJobs = backendJobs.map { it.toDomain() }
                domainJobs.forEach { job ->
                    processingDao.insertProcessing(job.toEntity())
                }
                
                Result.success(domainJobs)
            } else {
                val error = "Sync failed: ${response.code()}"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            Result.failure(e)
        }
    }
    
    override fun subscribeToProcessingUpdates(processingId: String) {
        Log.d(TAG, "Subscribing to updates: $processingId")
        webSocketClient.subscribeToProcessingUpdates(processingId)
    }
    
    override fun unsubscribeFromProcessingUpdates(processingId: String) {
        Log.d(TAG, "Unsubscribing from updates: $processingId")
        webSocketClient.unsubscribeFromProcessingUpdates(processingId)
    }
}