package com.dentim.karaoke.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

/**
 * Database entity representing the processing job for voice separation
 * Tracks the status and progress of AI processing operations
 */
@Entity(
    tableName = "processing_jobs",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["track_id"]),
        Index(value = ["status"])
    ]
)
data class ProcessingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "track_id")
    val trackId: String,
    
    @ColumnInfo(name = "status")
    val status: ProcessingStatus,
    
    @ColumnInfo(name = "progress_percent")
    val progressPercent: Int = 0,
    
    @ColumnInfo(name = "ai_model")
    val aiModel: String, // "demucs" or "spleeter"
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,
    
    @ColumnInfo(name = "vocals_path")
    val vocalsPath: String? = null,
    
    @ColumnInfo(name = "instrumental_path")
    val instrumentalPath: String? = null,
    
    @ColumnInfo(name = "processing_time_ms")
    val processingTimeMs: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date,
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null
)

/**
 * Processing status enumeration
 */
enum class ProcessingStatus {
    PENDING,
    UPLOADING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}