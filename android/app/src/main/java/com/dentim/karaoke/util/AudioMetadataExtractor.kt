package com.dentim.karaoke.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.dentim.karaoke.domain.model.AudioMetadata
import java.io.File

/**
 * Utility for extracting metadata from audio files
 */
object AudioMetadataExtractor {
    
    private const val TAG = "AudioMetadataExtractor"
    
    /**
     * Extract metadata from URI
     */
    fun extractMetadata(context: Context, uri: Uri): AudioMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                extractFromRetriever(retriever)
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
                }
            }
        } catch (throwable: Exception) {
            Log.e(TAG, "Failed to extract metadata from URI: $uri", throwable)
            createEmptyMetadata()
        }
    }

    /**
     * Extract metadata from file
     */
    fun extractMetadata(file: File): AudioMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                extractFromRetriever(retriever)
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
                }
            }
        } catch (throwable: Exception) {
            Log.e(TAG, "Failed to extract metadata from file: ${file.absolutePath}", throwable)
            createEmptyMetadata()
        }
    }

    /**
     * Extract basic duration information quickly
     */
    fun getDuration(context: Context, uri: Uri): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                duration?.toLongOrNull() ?: 0L
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
                }
            }
        } catch (throwable: Exception) {
            Log.e(TAG, "Failed to get duration from URI: $uri", throwable)
            0L
        }
    }

    /**
     * Extract basic duration information quickly
     */
    fun getDuration(file: File): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                duration?.toLongOrNull() ?: 0L
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
                }
            }
        } catch (throwable: Exception) {
            Log.e(TAG, "Failed to get duration from file: ${file.path}", throwable)
            0L
        }
    }

    private fun extractFromRetriever(retriever: MediaMetadataRetriever): AudioMetadata {
        return try {
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                ?.toIntOrNull()
            
            AudioMetadata(
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                bitrate = bitrate,
                sampleRate = null, // Not available in MediaMetadataRetriever
                channels = null // Not available in MediaMetadataRetriever
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting metadata from retriever", e)
            createEmptyMetadata()
        }
    }

    private fun createEmptyMetadata(): AudioMetadata {
        return AudioMetadata(
            title = null,
            artist = null,
            album = null,
            duration = 0L,
            bitrate = null,
            sampleRate = null,
            channels = null
        )
    }
    
    /**
     * Format file size in human-readable format
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
    
    /**
     * Format duration in human-readable format
     */
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return when {
            hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%d:%02d", minutes, seconds)
        }
    }
}