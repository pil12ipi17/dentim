package com.dentim.karaoke.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.Session
import com.dentim.karaoke.domain.repository.TrackRepository
import com.dentim.karaoke.domain.repository.ProcessingRepository
import com.dentim.karaoke.domain.repository.SessionRepository
import com.dentim.karaoke.util.ErrorHandler
import com.dentim.karaoke.domain.model.AppError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for HomeFragment
 * Manages recent tracks, active processing jobs, and user interactions
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val processingRepository: ProcessingRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _navigationEvent = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvent: SharedFlow<HomeNavigationEvent> = _navigationEvent.asSharedFlow()
    
    private val _errorEvent = MutableSharedFlow<AppError>()
    val errorEvent: SharedFlow<AppError> = _errorEvent.asSharedFlow()
    
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()
    
    // Recent tracks (limit to 5)
    val recentTracks: StateFlow<List<Track>> = trackRepository.getAllTracks()
        .map { tracks -> tracks.take(5) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Active processing jobs
    val activeProcessing: StateFlow<List<Processing>> = processingRepository.getActiveProcessingJobs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Statistics
    val statistics: StateFlow<HomeStatistics> = combine(
        trackRepository.getAllTracks(),
        sessionRepository.getAllSessions(),
        processingRepository.getAllProcessingJobs()
    ) { tracks, sessions, processingJobs ->
        HomeStatistics(
            totalTracks = tracks.size,
            totalSessions = sessions.size,
            completedProcessing = processingJobs.count { it.status.isCompleted },
            activeProcessing = processingJobs.count { it.status.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeStatistics()
    )
    
    init {
        refreshData()
        startDataSync()
    }
    
    /**
     * Start periodic data sync with backend
     */
    private fun startDataSync() {
        viewModelScope.launch {
            try {
                // Sync processing jobs from backend
                val syncResult = processingRepository.syncProcessingJobs()
                if (syncResult.isSuccess) {
                    val jobs = syncResult.getOrNull() ?: emptyList()
                    Log.d("HomeViewModel", "Synced ${jobs.size} processing jobs")
                    
                    // Create corresponding tracks for completed jobs if they don't exist
                    createTracksFromProcessingJobs(jobs)
                } else {
                    Log.w("HomeViewModel", "Sync failed: ${syncResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Failed to sync data on startup", e)
            }
        }
    }
    
    /**
     * Create tracks from processing jobs if they don't exist
     */
    private suspend fun createTracksFromProcessingJobs(jobs: List<Processing>) {
        jobs.forEach { processing ->
            try {
                // Check if track already exists
                val existingTrack = trackRepository.getTrackById(processing.trackId)
                if (existingTrack == null && processing.status.isCompleted) {
                    // Create a new track based on processing job
                    val track = Track(
                        id = processing.trackId,
                        filename = "track_${processing.trackId.take(8)}.mp3",
                        title = "Track ${processing.trackId.take(8)}",
                        artist = "Unknown Artist",
                        originalPath = "", // Not available from backend
                        fileSize = 5000000L, // 5MB default
                        durationMs = 180000L, // 3 minutes default
                        mimeType = "audio/mpeg",
                        checksum = null,
                        createdAt = java.util.Date(),
                        updatedAt = java.util.Date()
                    )
                    trackRepository.insertTrack(track)
                    Log.d("HomeViewModel", "Created track: ${track.title}")
                }
            } catch (e: Exception) {
                Log.w("HomeViewModel", "Failed to create track for processing ${processing.id}", e)
            }
        }
    }
    
    /**
     * Refresh all data
     */
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Sync processing jobs from backend
                val syncResult = processingRepository.syncProcessingJobs()
                if (syncResult.isSuccess) {
                    val jobs = syncResult.getOrNull() ?: emptyList()
                    Log.d("HomeViewModel", "Refreshed ${jobs.size} processing jobs")
                    createTracksFromProcessingJobs(jobs)
                }
                kotlinx.coroutines.delay(500) // Small delay for UI feedback
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, "HomeViewModel", "Refresh data")
                _errorEvent.emit(appError)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Handle track selection
     */
    fun onTrackSelected(track: Track) {
        viewModelScope.launch {
            try {
                // Find completed processing for this track
                val processingJobs = processingRepository.getProcessingByTrackId(track.id).first()
                val completedProcessing = processingJobs.find { it.status.isCompleted }
                
                if (completedProcessing != null) {
                    // Check if session exists
                    val sessions = sessionRepository.getSessionsByProcessingId(completedProcessing.id).first()
                    val existingSession = sessions.firstOrNull()
                    
                    if (existingSession != null) {
                        // Navigate to existing session
                        _navigationEvent.emit(HomeNavigationEvent.NavigateToPlayback(existingSession.id))
                    } else {
                        // Create new session and navigate
                        val newSession = createNewSession(track, completedProcessing)
                        sessionRepository.insertSession(newSession)
                        _navigationEvent.emit(HomeNavigationEvent.NavigateToPlayback(newSession.id))
                    }
                } else {
                    _messageEvent.emit("Track is still being processed or failed to process")
                }
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, "HomeViewModel", "Handle track selection")
                _errorEvent.emit(appError)
            }
        }
    }
    
    /**
     * Handle processing job selection
     */
    fun onProcessingSelected(processing: Processing) {
        viewModelScope.launch {
            try {
                if (processing.status.isCompleted) {
                    // Processing is complete, navigate to player
                    val track = trackRepository.getTrackById(processing.trackId)
                    if (track != null) {
                        onTrackSelected(track)
                    }
                } else {
                    // Show processing status
                    _messageEvent.emit("Processing is ${processing.status.name.lowercase()}")
                }
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, "HomeViewModel", "Handle processing selection")
                _errorEvent.emit(appError)
            }
        }
    }
    
    /**
     * Cancel processing job
     */
    fun cancelProcessing(processingId: String) {
        viewModelScope.launch {
            try {
                val result = processingRepository.cancelProcessing(processingId)
                if (result.isSuccess) {
                    _messageEvent.emit("Processing cancelled successfully")
                } else {
                    val error = result.exceptionOrNull()
                    val appError = ErrorHandler.handleError(
                        error ?: Exception("Cancel failed"), 
                        "HomeViewModel", 
                        "Cancel processing"
                    )
                    _errorEvent.emit(appError)
                }
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, "HomeViewModel", "Cancel processing")
                _errorEvent.emit(appError)
            }
        }
    }
    
    /**
     * Show track context menu
     */
    fun showTrackContextMenu(track: Track) {
        // This could open a bottom sheet or dialog with track options
        // For now, just navigate to track details or player
        onTrackSelected(track)
    }
    
    private fun createNewSession(track: Track, processing: Processing): Session {
        return Session(
            id = java.util.UUID.randomUUID().toString(),
            trackId = track.id,
            processingId = processing.id,
            sessionName = null,
            vocalsVolume = 1.0f,
            instrumentalVolume = 1.0f,
            playbackPositionMs = 0L,
            totalDurationMs = track.durationMs,
            playCount = 0,
            lastPlayedAt = null,
            createdAt = java.util.Date(),
            updatedAt = java.util.Date()
        )
    }
}

/**
 * Home screen statistics
 */
data class HomeStatistics(
    val totalTracks: Int = 0,
    val totalSessions: Int = 0,
    val completedProcessing: Int = 0,
    val activeProcessing: Int = 0
)