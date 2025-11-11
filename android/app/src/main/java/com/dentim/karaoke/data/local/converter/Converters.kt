package com.dentim.karaoke.data.local.converter

import androidx.room.TypeConverter
import com.dentim.karaoke.data.local.entity.ProcessingStatus
import java.util.Date

/**
 * Type converters for Room database
 * Handles conversion between complex types and primitive types for database storage
 */
class Converters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun fromProcessingStatus(status: ProcessingStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toProcessingStatus(status: String): ProcessingStatus {
        return ProcessingStatus.valueOf(status)
    }
}