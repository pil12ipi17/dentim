package com.dentim.karaoke.ui.home.model

import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.util.ProcessingProgressEstimator
import java.util.Date

/**
 * Data model that combines Track with its associated Processing
 * Used in home screen to display tracks that can be played
 */
data class TrackWithProcessing(
    val track: Track,
    val processing: Processing? = null
) {
    /**
     * Check if track can be played (has completed processing)
     */
    val canPlay: Boolean
        get() = processing?.status?.isCompleted == true
    
    /**
     * Get processing ID for playback
     */
    val processingId: String?
        get() = processing?.id
        
    /**
     * Check if processing is active
     */
    val isProcessing: Boolean
        get() = processing?.status?.isActive == true
    
    /**
     * Get estimated progress percentage for active processing
     * Returns server progress if available, otherwise calculates estimated progress
     */
    val estimatedProgress: Int
        get() {
            val proc = processing ?: return 0
            
            // For active processing, estimate based on time
            if (isProcessing) {
                // Use track duration if available, otherwise default to 3 minutes
                val durationMs = if (track.durationMs > 0) track.durationMs else (3 * 60 * 1000L)
                
                return ProcessingProgressEstimator.calculateEstimatedProgress(
                    startTime = proc.createdAt,
                    audioDurationMs = durationMs
                )
            }
            
            return 0
        }
    
    /**
     * Get estimated remaining time for active processing
     */
    val estimatedRemainingTime: String?
        get() {
            val proc = processing ?: return null
            
            if (!isProcessing) return null
            
            // Use track duration if available, otherwise default to 3 minutes
            val durationMs = if (track.durationMs > 0) track.durationMs else (3 * 60 * 1000L)
            
            val remainingSeconds = ProcessingProgressEstimator.getEstimatedRemainingSeconds(
                startTime = proc.createdAt,
                audioDurationMs = durationMs
            )
            
            return ProcessingProgressEstimator.formatRemainingTime(remainingSeconds)
        }
}