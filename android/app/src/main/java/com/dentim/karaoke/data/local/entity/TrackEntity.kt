package com.dentim.karaoke.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * Database entity representing an audio track that can be processed for karaoke
 * Contains metadata about the original file and processing status
 */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "filename")
    val filename: String,
    
    @ColumnInfo(name = "original_path")
    val originalPath: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    
    @ColumnInfo(name = "mime_type")
    val mimeType: String,
    
    @ColumnInfo(name = "checksum")
    val checksum: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date
)