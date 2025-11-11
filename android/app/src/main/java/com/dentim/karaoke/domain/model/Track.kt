package com.dentim.karaoke.domain.model

import java.util.Date

/**
 * Track domain model
 */
data class Track(
    val id: String = "",
    val filename: String,
    val title: String = filename.substringBeforeLast('.'),
    val artist: String = "Unknown Artist",
    val originalPath: String,
    val fileSize: Long,
    val durationMs: Long = 0,
    val duration: Long = durationMs,
    val mimeType: String,
    val checksum: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Get the display name for the track
     */
    val displayName: String
        get() = if (title != filename.substringBeforeLast('.')) title else filename
    
    /**
     * Get the file extension
     */
    val extension: String
        get() = filename.substringAfterLast('.', "")
    
    /**
     * Check if the track has a valid duration
     */
    val hasValidDuration: Boolean
        get() = durationMs > 0
    
    /**
     * Get human-readable file size
     */
    val humanReadableSize: String
        get() = when {
            fileSize >= 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024 * 1024)} GB"
            fileSize >= 1024 * 1024 -> "${fileSize / (1024 * 1024)} MB"
            fileSize >= 1024 -> "${fileSize / 1024} KB"
            else -> "$fileSize B"
        }
}