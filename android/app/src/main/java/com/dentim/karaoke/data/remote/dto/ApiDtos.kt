package com.dentim.karaoke.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * API Response DTOs for communication with backend
 */

@JsonClass(generateAdapter = true)
data class ApiResponseDto(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "timestamp") val timestamp: String
)

@JsonClass(generateAdapter = true)
data class ProcessingJobDto(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "progress") val progress: Int,
    @Json(name = "ai_model") val aiModel: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "estimated_completion") val estimatedCompletion: String? = null,
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class ProcessingStatusDto(
    @Json(name = "job_id") val jobId: String,
    @Json(name = "status") val status: String,
    @Json(name = "progress") val progress: Int,
    @Json(name = "current_step") val currentStep: String? = null,
    @Json(name = "estimated_remaining_ms") val estimatedRemainingMs: Long? = null,
    @Json(name = "vocals_ready") val vocalsReady: Boolean = false,
    @Json(name = "instrumental_ready") val instrumentalReady: Boolean = false,
    @Json(name = "error_message") val errorMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class WebSocketMessageDto(
    @Json(name = "type") val type: String,
    @Json(name = "job_id") val jobId: String? = null,
    @Json(name = "data") val data: Map<String, Any>? = null,
    @Json(name = "timestamp") val timestamp: String? = null
)