package com.dentim.karaoke.domain.model

import java.util.Date

/**
 * Domain models for the application business logic
 * These models represent the core entities independent of data sources
 */

data class Processing(
    val id: String,
    val trackId: String?,
    val status: ProcessingStatus,
    val progressPercent: Int,
    val aiModel: AIModel,
    val errorMessage: String? = null,
    val filename: String? = null,
    val vocalsPath: String? = null,
    val instrumentalPath: String? = null,
    val processingTimeMs: Long? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val completedAt: Date? = null,
    val estimatedCompletion: Date? = null,
    val currentStep: String? = null
)

data class Session(
    val id: String,
    val trackId: String,
    val processingId: String,
    val sessionName: String?,
    val vocalsVolume: Float,
    val instrumentalVolume: Float,
    val playbackPositionMs: Long,
    val totalDurationMs: Long,
    val playCount: Int,
    val lastPlayedAt: Date?,
    val createdAt: Date,
    val updatedAt: Date
)

/**
 * Processing status enumeration
 */
enum class ProcessingStatus {
    PENDING,
    QUEUED,
    UPLOADING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;
    
    val isActive: Boolean
        get() = this in listOf(PENDING, QUEUED, UPLOADING, PROCESSING)
    
    val isCompleted: Boolean
        get() = this == COMPLETED
    
    val isFailed: Boolean
        get() = this in listOf(FAILED, CANCELLED)
}

/**
 * AI model enumeration
 */
enum class AIModel(val displayName: String, val apiValue: String) {
    DEMUCS("Demucs (Recommended)", "demucs"),
    SPLEETER("Spleeter (Fast)", "spleeter");
    
    companion object {
        fun fromApiValue(value: String): AIModel {
            // Clean the value from any surrounding quotes
            val cleanValue = value.trim().removeSurrounding("\"")
            return values().find { it.apiValue == cleanValue } ?: DEMUCS
        }
    }
}

/**
 * Audio track metadata
 */
data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val bitrate: Int?,
    val sampleRate: Int?,
    val channels: Int?
)