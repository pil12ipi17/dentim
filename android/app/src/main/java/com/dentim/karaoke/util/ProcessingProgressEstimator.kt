package com.dentim.karaoke.util

import java.util.Date
import kotlin.math.min
import kotlin.math.max

/**
 * Utility for calculating estimated progress for audio processing
 * Based on empirical observation: ~90 seconds processing time per 1 minute of audio
 */
object ProcessingProgressEstimator {
    
    // Processing rate: 90 seconds of processing per 1 minute of audio (more realistic estimate)
    private const val PROCESSING_SECONDS_PER_AUDIO_MINUTE = 90f
    
    // Minimum estimated time in seconds
    private const val MIN_ESTIMATED_TIME_SECONDS = 30f
    
    // Maximum estimated time in seconds (10 minutes for safety)
    private const val MAX_ESTIMATED_TIME_SECONDS = 600f
    
    /**
     * Calculate estimated total processing time in seconds based on audio duration
     */
    fun estimateProcessingTimeSeconds(audioDurationMs: Long): Float {
        val audioDurationMinutes = (audioDurationMs / 1000f) / 60f
        val estimatedSeconds = audioDurationMinutes * PROCESSING_SECONDS_PER_AUDIO_MINUTE
        return max(MIN_ESTIMATED_TIME_SECONDS, min(estimatedSeconds, MAX_ESTIMATED_TIME_SECONDS))
    }
    
    /**
     * Calculate current estimated progress percentage based on elapsed time
     */
    fun calculateEstimatedProgress(
        startTime: Date,
        audioDurationMs: Long,
        currentTime: Date = Date()
    ): Int {
        val elapsedSeconds = (currentTime.time - startTime.time) / 1000f
        val estimatedTotalSeconds = estimateProcessingTimeSeconds(audioDurationMs)
        
        val progressPercent = (elapsedSeconds / estimatedTotalSeconds * 100f).toInt()
        
        // Cap at 95% to avoid showing 100% before actual completion
        return min(progressPercent, 95)
    }
    
    /**
     * Get estimated remaining time in seconds
     */
    fun getEstimatedRemainingSeconds(
        startTime: Date,
        audioDurationMs: Long,
        currentTime: Date = Date()
    ): Long {
        val elapsedSeconds = (currentTime.time - startTime.time) / 1000f
        val estimatedTotalSeconds = estimateProcessingTimeSeconds(audioDurationMs)
        
        val remainingSeconds = max(0f, estimatedTotalSeconds - elapsedSeconds)
        return remainingSeconds.toLong()
    }
    
    /**
     * Format remaining time as human readable string
     */
    fun formatRemainingTime(remainingSeconds: Long): String {
        return when {
            remainingSeconds <= 0 -> "Almost done..."
            remainingSeconds < 60 -> "${remainingSeconds}s remaining"
            else -> {
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                "${minutes}m ${seconds}s remaining"
            }
        }
    }
}