package com.dentim.karaoke.data.mapper

import com.dentim.karaoke.data.local.entity.TrackEntity
import com.dentim.karaoke.data.local.entity.ProcessingEntity
import com.dentim.karaoke.data.local.entity.SessionEntity
import com.dentim.karaoke.data.remote.dto.ProcessingJobDto
import com.dentim.karaoke.data.remote.dto.ProcessingStatusDto
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.Session
import com.dentim.karaoke.domain.model.AIModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ProcessingStatus mappers
fun com.dentim.karaoke.domain.model.ProcessingStatus.toEntity(): com.dentim.karaoke.data.local.entity.ProcessingStatus {
    return when (this) {
        com.dentim.karaoke.domain.model.ProcessingStatus.PENDING -> com.dentim.karaoke.data.local.entity.ProcessingStatus.PENDING
        com.dentim.karaoke.domain.model.ProcessingStatus.UPLOADING -> com.dentim.karaoke.data.local.entity.ProcessingStatus.UPLOADING
        com.dentim.karaoke.domain.model.ProcessingStatus.PROCESSING -> com.dentim.karaoke.data.local.entity.ProcessingStatus.PROCESSING
        com.dentim.karaoke.domain.model.ProcessingStatus.COMPLETED -> com.dentim.karaoke.data.local.entity.ProcessingStatus.COMPLETED
        com.dentim.karaoke.domain.model.ProcessingStatus.FAILED -> com.dentim.karaoke.data.local.entity.ProcessingStatus.FAILED
        com.dentim.karaoke.domain.model.ProcessingStatus.CANCELLED -> com.dentim.karaoke.data.local.entity.ProcessingStatus.CANCELLED
    }
}

fun com.dentim.karaoke.data.local.entity.ProcessingStatus.toDomain(): com.dentim.karaoke.domain.model.ProcessingStatus {
    return when (this) {
        com.dentim.karaoke.data.local.entity.ProcessingStatus.PENDING -> com.dentim.karaoke.domain.model.ProcessingStatus.PENDING
        com.dentim.karaoke.data.local.entity.ProcessingStatus.UPLOADING -> com.dentim.karaoke.domain.model.ProcessingStatus.UPLOADING
        com.dentim.karaoke.data.local.entity.ProcessingStatus.PROCESSING -> com.dentim.karaoke.domain.model.ProcessingStatus.PROCESSING
        com.dentim.karaoke.data.local.entity.ProcessingStatus.COMPLETED -> com.dentim.karaoke.domain.model.ProcessingStatus.COMPLETED
        com.dentim.karaoke.data.local.entity.ProcessingStatus.FAILED -> com.dentim.karaoke.domain.model.ProcessingStatus.FAILED
        com.dentim.karaoke.data.local.entity.ProcessingStatus.CANCELLED -> com.dentim.karaoke.domain.model.ProcessingStatus.CANCELLED
    }
}

// Track mappers
fun TrackEntity.toDomain(): Track = Track(
    id = id,
    filename = filename,
    originalPath = originalPath,
    fileSize = fileSize,
    durationMs = durationMs,
    mimeType = mimeType,
    checksum = checksum,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Track.toEntity(): TrackEntity = TrackEntity(
    id = id,
    filename = filename,
    originalPath = originalPath,
    fileSize = fileSize,
    durationMs = durationMs,
    mimeType = mimeType,
    checksum = checksum ?: "",
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Processing mappers
fun ProcessingEntity.toDomain(): Processing = Processing(
    id = id,
    trackId = trackId,
    status = status.toDomain(),
    progressPercent = progressPercent,
    aiModel = AIModel.fromApiValue(aiModel),
    errorMessage = errorMessage,
    vocalsPath = vocalsPath,
    instrumentalPath = instrumentalPath,
    processingTimeMs = processingTimeMs,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)

fun Processing.toEntity(): ProcessingEntity = ProcessingEntity(
    id = id,
    trackId = trackId,
    status = status.toEntity(),
    progressPercent = progressPercent,
    aiModel = aiModel.apiValue,
    errorMessage = errorMessage,
    vocalsPath = vocalsPath,
    instrumentalPath = instrumentalPath,
    processingTimeMs = processingTimeMs,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)

// Session mappers
fun SessionEntity.toDomain(): Session = Session(
    id = id,
    trackId = trackId,
    processingId = processingId,
    sessionName = sessionName,
    vocalsVolume = vocalsVolume,
    instrumentalVolume = instrumentalVolume,
    playbackPositionMs = playbackPositionMs,
    totalDurationMs = totalDurationMs,
    playCount = playCount,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    trackId = trackId,
    processingId = processingId,
    sessionName = sessionName,
    vocalsVolume = vocalsVolume,
    instrumentalVolume = instrumentalVolume,
    playbackPositionMs = playbackPositionMs,
    totalDurationMs = totalDurationMs,
    playCount = playCount,
    lastPlayedAt = lastPlayedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// DTO mappers
private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US)

fun ProcessingJobDto.toDomain(): Processing = Processing(
    id = jobId,
    trackId = "", // Will be set from context
    status = com.dentim.karaoke.domain.model.ProcessingStatus.valueOf(status.uppercase()),
    progressPercent = progress,
    aiModel = AIModel.fromApiValue(aiModel),
    errorMessage = errorMessage,
    createdAt = parseDate(createdAt),
    updatedAt = parseDate(updatedAt),
    estimatedCompletion = estimatedCompletion?.let { parseDate(it) }
)

fun ProcessingStatusDto.toDomain(): Processing = Processing(
    id = jobId,
    trackId = "", // Will be set from context
    status = com.dentim.karaoke.domain.model.ProcessingStatus.valueOf(status.uppercase()),
    progressPercent = progress,
    aiModel = AIModel.DEMUCS, // Default, will be updated
    errorMessage = errorMessage,
    currentStep = currentStep,
    createdAt = Date(), // Will be updated from database
    updatedAt = Date()
)

private fun parseDate(dateString: String): Date {
    return try {
        dateFormat.parse(dateString) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}