package com.dentim.karaoke.ui.profile

/**
 * Data class representing user profile statistics
 */
data class ProfileStatistics(
    val totalTracks: Int = 0,
    val totalSessions: Int = 0,
    val completedProcessing: Int = 0,
    val activeProcessing: Int = 0,
    val totalPlayTimeMinutes: Long = 0L,
    val accountCreatedDays: Int = 0
)