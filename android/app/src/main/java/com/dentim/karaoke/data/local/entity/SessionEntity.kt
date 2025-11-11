package com.dentim.karaoke.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

/**
 * Database entity representing a karaoke playback session
 * Tracks user interactions and playback history
 */
@Entity(
    tableName = "karaoke_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProcessingEntity::class,
            parentColumns = ["id"],
            childColumns = ["processing_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["track_id"]),
        Index(value = ["processing_id"]),
        Index(value = ["created_at"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "track_id")
    val trackId: String,
    
    @ColumnInfo(name = "processing_id")
    val processingId: String,
    
    @ColumnInfo(name = "session_name")
    val sessionName: String?,
    
    @ColumnInfo(name = "vocals_volume")
    val vocalsVolume: Float = 1.0f, // 0.0 to 1.0
    
    @ColumnInfo(name = "instrumental_volume")
    val instrumentalVolume: Float = 1.0f, // 0.0 to 1.0
    
    @ColumnInfo(name = "playback_position_ms")
    val playbackPositionMs: Long = 0,
    
    @ColumnInfo(name = "total_duration_ms")
    val totalDurationMs: Long,
    
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    
    @ColumnInfo(name = "last_played_at")
    val lastPlayedAt: Date? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date
)