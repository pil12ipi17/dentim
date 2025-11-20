package com.dentim.karaoke.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.dentim.karaoke.data.remote.dto.ProcessingJobDto
import com.dentim.karaoke.data.remote.dto.UploadResponseDto
import com.dentim.karaoke.data.remote.dto.ProcessingStatusDto
import com.dentim.karaoke.data.remote.dto.ApiResponseDto

/**
 * Retrofit API service for karaoke voice separation backend
 * Defines endpoints for file upload, processing management, and status tracking
 */
interface KaraokeApiService {
    
    /**
     * Upload audio file for processing
     */
    @Multipart
    @POST("api/v1/upload")
    suspend fun uploadAudioFile(
        @Part file: MultipartBody.Part,
        @Part("ai_model") aiModel: String = "demucs"
    ): Response<UploadResponseDto>
    
    /**
     * Get processing job status
     */
    @GET("api/v1/processing/{job_id}/status")
    suspend fun getProcessingStatus(
        @Path("job_id") jobId: String
    ): Response<ProcessingStatusDto>
    
    /**
     * Get all processing jobs
     */
    @GET("api/v1/processing")
    suspend fun getAllProcessingJobs(): Response<List<ProcessingJobDto>>
    
    /**
     * Cancel processing job
     */
    @POST("api/v1/processing/{job_id}/cancel")
    suspend fun cancelProcessingJob(
        @Path("job_id") jobId: String
    ): Response<ApiResponseDto>
    
    /**
     * Download processed vocals file
     */
    @GET("api/v1/processing/{job_id}/vocals")
    @Streaming
    suspend fun downloadVocals(
        @Path("job_id") jobId: String
    ): Response<okhttp3.ResponseBody>
    
    /**
     * Download processed instrumental file
     */
    @GET("api/v1/processing/{job_id}/instrumental")
    @Streaming
    suspend fun downloadInstrumental(
        @Path("job_id") jobId: String
    ): Response<okhttp3.ResponseBody>
    
    /**
     * Health check endpoint
     */
    @GET("api/v1/health")
    suspend fun healthCheck(): Response<ApiResponseDto>
}