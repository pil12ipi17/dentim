package com.dentim.karaoke.domain.model

import java.util.Date

/**
 * Processing job model for the UI layer
 */
data class ProcessingJob(
    val id: String,
    val trackName: String,
    val status: ProcessingStatus,
    val progressPercent: Int = 0,
    val currentStep: String? = null,
    val errorMessage: String? = null,
    val estimatedTimeRemaining: Long? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    val isActive: Boolean
        get() = status in listOf(ProcessingStatus.PENDING, ProcessingStatus.UPLOADING, ProcessingStatus.PROCESSING)
    
    val isCompleted: Boolean
        get() = status == ProcessingStatus.COMPLETED
    
    val isFailed: Boolean
        get() = status == ProcessingStatus.FAILED
    
    val displayStatus: String
        get() = when (status) {
            ProcessingStatus.PENDING -> "Waiting in queue"
            ProcessingStatus.UPLOADING -> "Uploading file"
            ProcessingStatus.PROCESSING -> currentStep ?: "Processing audio"
            ProcessingStatus.COMPLETED -> "Completed"
            ProcessingStatus.FAILED -> "Failed"
            ProcessingStatus.CANCELLED -> "Cancelled"
        }
}