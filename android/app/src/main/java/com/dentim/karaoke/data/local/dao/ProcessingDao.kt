package com.dentim.karaoke.data.local.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import com.dentim.karaoke.data.local.entity.ProcessingEntity
import com.dentim.karaoke.data.local.entity.ProcessingStatus

/**
 * Data Access Object for ProcessingEntity
 * Provides database operations for processing jobs
 */
@Dao
interface ProcessingDao {
    
    @Query("SELECT * FROM processing_jobs ORDER BY created_at DESC")
    fun getAllProcessingJobs(): Flow<List<ProcessingEntity>>
    
    @Query("SELECT * FROM processing_jobs WHERE id = :processingId")
    suspend fun getProcessingById(processingId: String): ProcessingEntity?
    
    @Query("SELECT * FROM processing_jobs WHERE id = :processingId")
    fun getProcessingByIdFlow(processingId: String): Flow<ProcessingEntity?>
    
    @Query("SELECT * FROM processing_jobs WHERE track_id = :trackId ORDER BY created_at DESC")
    fun getProcessingByTrackId(trackId: String): Flow<List<ProcessingEntity>>
    
    @Query("SELECT * FROM processing_jobs WHERE track_id = :trackId AND status = :status LIMIT 1")
    suspend fun getProcessingByTrackIdAndStatus(trackId: String, status: ProcessingStatus): ProcessingEntity?
    
    @Query("SELECT * FROM processing_jobs WHERE status = :status ORDER BY created_at ASC")
    fun getProcessingByStatus(status: ProcessingStatus): Flow<List<ProcessingEntity>>
    
    @Query("SELECT * FROM processing_jobs WHERE status IN (:statuses) ORDER BY created_at DESC")
    fun getProcessingByStatuses(statuses: List<ProcessingStatus>): Flow<List<ProcessingEntity>>
    
    @Query("SELECT COUNT(*) FROM processing_jobs WHERE status = :status")
    suspend fun getProcessingCountByStatus(status: ProcessingStatus): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessing(processing: ProcessingEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessings(processings: List<ProcessingEntity>)
    
    @Update
    suspend fun updateProcessing(processing: ProcessingEntity)
    
    @Delete
    suspend fun deleteProcessing(processing: ProcessingEntity)
    
    @Query("DELETE FROM processing_jobs WHERE id = :processingId")
    suspend fun deleteProcessingById(processingId: String)
    
    @Query("DELETE FROM processing_jobs WHERE track_id = :trackId")
    suspend fun deleteProcessingByTrackId(trackId: String)
    
    @Query("DELETE FROM processing_jobs WHERE status = :status")
    suspend fun deleteProcessingByStatus(status: ProcessingStatus)
    
    @Query("DELETE FROM processing_jobs")
    suspend fun deleteAllProcessing()
    
    @Query("UPDATE processing_jobs SET progress_percent = :progress, updated_at = :updatedAt WHERE id = :processingId")
    suspend fun updateProgress(processingId: String, progress: Int, updatedAt: java.util.Date)
    
    @Query("UPDATE processing_jobs SET status = :status, updated_at = :updatedAt WHERE id = :processingId")
    suspend fun updateStatus(processingId: String, status: ProcessingStatus, updatedAt: java.util.Date)
    
    @Query("UPDATE processing_jobs SET status = :status, error_message = :errorMessage, updated_at = :updatedAt WHERE id = :processingId")
    suspend fun updateStatusWithError(processingId: String, status: ProcessingStatus, errorMessage: String, updatedAt: java.util.Date)
}