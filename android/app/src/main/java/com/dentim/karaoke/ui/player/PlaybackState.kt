package com.dentim.karaoke.ui.player

/**
 * Data class representing the current playback state
 */
data class PlaybackState(
    val trackTitle: String? = null,
    val artistName: String? = null,
    val isPlaying: Boolean = false,
    val currentTimeMs: Long = 0L,
    val totalTimeMs: Long = 0L,
    val progressPercent: Int = 0,
    val vocalsVolume: Int = 50,
    val instrumentalVolume: Int = 100
) {
    val currentTimeFormatted: String
        get() = formatTime(currentTimeMs)
    
    val totalTimeFormatted: String
        get() = formatTime(totalTimeMs)
    
    private fun formatTime(timeMs: Long): String {
        val seconds = (timeMs / 1000) % 60
        val minutes = (timeMs / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }
}