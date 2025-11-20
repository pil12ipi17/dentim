package com.dentim.karaoke.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.dentim.karaoke.domain.repository.TrackRepository
import com.dentim.karaoke.domain.repository.ProcessingRepository
import com.dentim.karaoke.domain.repository.SessionRepository
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.ui.home.model.TrackWithProcessing
import javax.inject.Inject

/**
 * ViewModel for managing karaoke playback with vocal/instrumental mixing
 */
@HiltViewModel 
class PlayerViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val processingRepository: ProcessingRepository,
    private val sessionRepository: SessionRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _availableTracks = MutableStateFlow<List<TrackWithProcessing>>(emptyList())
    val availableTracks: StateFlow<List<TrackWithProcessing>> = _availableTracks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()
    
    private var currentTrack: Track? = null
    private var currentProcessing: Processing? = null
    
    init {
        loadAvailableTracks()
        startPositionUpdater()
        initializeAudioPlayerObservers()
    }
    
    /**
     * Initialize audio player observers
     */
    private fun initializeAudioPlayerObservers() {
        // Observe player state for mixed playback
        viewModelScope.launch {
            audioPlayer.playbackState.collect { state ->
                val currentState = _playbackState.value
                _playbackState.value = currentState.copy(
                    isPlaying = state == AudioPlayer.PlaybackState.PLAYING
                )
            }
        }
        
        // Observe duration changes
        viewModelScope.launch {
            audioPlayer.duration.collect { durationMs ->
                val currentState = _playbackState.value
                _playbackState.value = currentState.copy(
                    totalTimeMs = durationMs.toLong()
                )
                Log.d("PlayerViewModel", "Updated duration: ${durationMs}ms")
            }
        }
        
        // Observe position changes
        viewModelScope.launch {
            audioPlayer.position.collect { positionMs ->
                val currentState = _playbackState.value
                val progressPercent = if (currentState.totalTimeMs > 0) {
                    ((positionMs.toFloat() / currentState.totalTimeMs) * 100).toInt()
                } else 0
                
                _playbackState.value = currentState.copy(
                    currentTimeMs = positionMs.toLong(),
                    progressPercent = progressPercent
                )
            }
        }
    }
    
    /**
     * Start periodic position updates
     */
    private fun startPositionUpdater() {
        viewModelScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(1000) // Update every second
                    
                    // Update position if playing
                    if (_playbackState.value.isPlaying) {
                        val currentPos = audioPlayer.getCurrentPosition()
                        val duration = audioPlayer.getDuration()
                        
                        if (duration > 0) {
                            val progressPercent = ((currentPos.toFloat() / duration) * 100).toInt()
                            
                            val currentState = _playbackState.value
                            _playbackState.value = currentState.copy(
                                currentTimeMs = currentPos.toLong(),
                                progressPercent = progressPercent,
                                totalTimeMs = duration.toLong()
                            )
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    Log.d("PlayerViewModel", "Position updater cancelled")
                    break // Exit the loop when cancelled
                } catch (e: Exception) {
                    // Continue the loop even if there's an error
                    Log.w("PlayerViewModel", "Error in position updater", e)
                }
            }
        }
    }
    
    /**
     * Load tracks that have completed processing
     */
    private fun loadAvailableTracks() {
        viewModelScope.launch {
            try {
                combine(
                    trackRepository.getAllTracks(),
                    processingRepository.getAllProcessingJobs()
                ) { tracks, processingJobs ->
                    tracks.mapNotNull { track ->
                        val completedProcessing = processingJobs
                            .filter { 
                                // Try to match by trackId first, then by filename
                                (it.trackId == track.id && it.status.isCompleted) ||
                                (it.filename == track.filename && it.status.isCompleted)
                            }
                            .maxByOrNull { it.createdAt }
                        
                        if (completedProcessing != null) {
                            TrackWithProcessing(track, completedProcessing)
                        } else null
                    }
                }.collect { tracksWithProcessing ->
                    _availableTracks.value = tracksWithProcessing
                    Log.d("PlayerViewModel", "Loaded ${tracksWithProcessing.size} available tracks")
                }
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error loading tracks", e)
                _errorMessage.emit("Failed to load tracks: ${e.message}")
            }
        }
    }
    
    /**
     * Select and load a track for karaoke playback
     */
    fun selectTrack(trackWithProcessing: TrackWithProcessing) {
        viewModelScope.launch {
            try {
                currentTrack = trackWithProcessing.track
                currentProcessing = trackWithProcessing.processing
                
                _playbackState.value = _playbackState.value.copy(
                    trackTitle = trackWithProcessing.track.title ?: trackWithProcessing.track.filename,
                    artistName = trackWithProcessing.track.artist ?: "Unknown Artist",
                    totalTimeMs = trackWithProcessing.track.durationMs,
                    isPlaying = false,
                    currentTimeMs = 0L,
                    progressPercent = 0
                )
                
                Log.d("PlayerViewModel", "Selected track: ${trackWithProcessing.track.filename}")
            } catch (e: Exception) {
                Log.e("PlayerViewModel", "Error selecting track", e)
                _errorMessage.emit("Failed to select track: ${e.message}")
            }
        }
    }
    
    /**
     * Toggle play/pause for mixed karaoke playback
     */
    fun togglePlayPause() {
        if (currentTrack == null || currentProcessing == null) {
            viewModelScope.launch {
                _errorMessage.emit("No track selected")
            }
            return
        }
        
        val currentState = _playbackState.value
        if (currentState.isPlaying) {
            pausePlayback()
        } else {
            // Check if we have existing players to resume, or need to start fresh
            if (audioPlayer.hasActivePlayers()) {
                resumePlayback()
            } else {
                startMixedPlayback()
            }
        }
    }
    
    /**
     * Start mixed playback of vocals and instrumental
     */
    private fun startMixedPlayback() {
        currentProcessing?.let { processing ->
            viewModelScope.launch {
                try {
                    // Use new mixed playback method
                    val vocalsUrl = "http://10.0.2.2:8000/api/v1/download/${processing.id}/vocals"
                    val instrumentalUrl = "http://10.0.2.2:8000/api/v1/download/${processing.id}/instrumental"
                    val trackName = currentTrack?.title ?: currentTrack?.filename ?: "Unknown Track"
                    
                    audioPlayer.playMixed(vocalsUrl, instrumentalUrl, trackName)
                    
                    _playbackState.value = _playbackState.value.copy(isPlaying = true)
                    Log.d("PlayerViewModel", "Started mixed playback for processing: ${processing.id}")
                    
                } catch (e: Exception) {
                    Log.e("PlayerViewModel", "Error starting playback", e)
                    _errorMessage.emit("Failed to start playback: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Pause mixed playback
     */
    private fun pausePlayback() {
        audioPlayer.pause()
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
        Log.d("PlayerViewModel", "Paused playback")
    }
    
    /**
     * Resume mixed playback
     */
    private fun resumePlayback() {
        audioPlayer.resume()
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
        Log.d("PlayerViewModel", "Resumed playback")
    }
    
    /**
     * Update progress position (from seek bar)
     */
    fun updateProgress(progressPercent: Int) {
        val currentState = _playbackState.value
        val newTimeMs = (currentState.totalTimeMs * progressPercent / 100).coerceIn(0L, currentState.totalTimeMs)
        
        // Seek in audio player
        audioPlayer.seekTo(newTimeMs.toInt())
        
        _playbackState.value = currentState.copy(
            progressPercent = progressPercent,
            currentTimeMs = newTimeMs
        )
        
        Log.d("PlayerViewModel", "Updated progress: $progressPercent% ($newTimeMs ms)")
    }
    
    /**
     * Update vocals volume
     */
    fun updateVocalsVolume(volume: Int) {
        val clampedVolume = volume.coerceIn(0, 100)
        val volumeFloat = clampedVolume / 100.0f
        
        // Apply volume to audio player
        audioPlayer.setVocalsVolume(volumeFloat)
        
        _playbackState.value = _playbackState.value.copy(vocalsVolume = clampedVolume)
        
        Log.d("PlayerViewModel", "Updated vocals volume: $clampedVolume")
    }
    
    /**
     * Update instrumental volume
     */
    fun updateInstrumentalVolume(volume: Int) {
        val clampedVolume = volume.coerceIn(0, 100)
        val volumeFloat = clampedVolume / 100.0f
        
        // Apply volume to audio player
        audioPlayer.setInstrumentalVolume(volumeFloat)
        
        _playbackState.value = _playbackState.value.copy(instrumentalVolume = clampedVolume)
        
        Log.d("PlayerViewModel", "Updated instrumental volume: $clampedVolume")
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        audioPlayer.stop()
        _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            currentTimeMs = 0L,
            progressPercent = 0
        )
        Log.d("PlayerViewModel", "Stopped playback")
    }
    
    /**
     * Set current track from a TrackWithProcessing object and start playing
     */
    fun setCurrentTrack(trackWithProcessing: TrackWithProcessing) {
        viewModelScope.launch {
            if (trackWithProcessing.canPlay) {
                Log.d("PlayerViewModel", "Setting current track: ${trackWithProcessing.track.filename}")
                selectTrack(trackWithProcessing)
                // Auto-start playback when track is selected from home
                startMixedPlayback()
            } else {
                Log.w("PlayerViewModel", "Cannot play track: ${trackWithProcessing.track.filename}")
                _errorMessage.emit("Track is not ready for playback")
            }
        }
    }
    
    /**
     * Get the currently selected track
     */
    fun getCurrentTrack(): Track? = currentTrack
    
    /**
     * Get the first available track (for auto-selection)
     */
    fun selectFirstAvailableTrack() {
        viewModelScope.launch {
            val tracks = _availableTracks.value
            Log.d("PlayerViewModel", "selectFirstAvailableTrack: ${tracks.size} tracks available")
            if (tracks.isNotEmpty()) {
                Log.d("PlayerViewModel", "Auto-selecting first track: ${tracks.first().track.filename}")
                selectTrack(tracks.first())
            } else {
                Log.d("PlayerViewModel", "No tracks available for auto-selection")
            }
        }
    }
    
    /**
     * Play next track in the list
     */
    fun playNextTrack() {
        viewModelScope.launch {
            val tracks = _availableTracks.value
            if (tracks.isEmpty() || currentTrack == null) return@launch
            
            val currentIndex = tracks.indexOfFirst { it.track.id == currentTrack?.id }
            if (currentIndex >= 0 && currentIndex < tracks.size - 1) {
                selectTrack(tracks[currentIndex + 1])
                if (_playbackState.value.isPlaying) {
                    // Auto-play next track if currently playing
                    startMixedPlayback()
                }
            }
        }
    }
    
    /**
     * Play previous track in the list
     */
    fun playPreviousTrack() {
        viewModelScope.launch {
            val tracks = _availableTracks.value
            if (tracks.isEmpty() || currentTrack == null) return@launch
            
            val currentIndex = tracks.indexOfFirst { it.track.id == currentTrack?.id }
            if (currentIndex > 0) {
                selectTrack(tracks[currentIndex - 1])
                if (_playbackState.value.isPlaying) {
                    // Auto-play previous track if currently playing
                    startMixedPlayback()
                }
            }
        }
    }
    
    // Legacy methods for backward compatibility
    fun playVocals(processingId: String, trackName: String) {
        val url = "http://10.0.2.2:8000/api/v1/download/$processingId/vocals"
        Log.d("PlayerViewModel", "Playing vocals: $url")
        audioPlayer.playFromUrl(url, "Vocals: $trackName")
    }

    fun playInstrumental(processingId: String, trackName: String) {
        val url = "http://10.0.2.2:8000/api/v1/download/$processingId/instrumental"
        Log.d("PlayerViewModel", "Playing instrumental: $url")
        audioPlayer.playFromUrl(url, "Instrumental: $trackName")
    }
    
    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}