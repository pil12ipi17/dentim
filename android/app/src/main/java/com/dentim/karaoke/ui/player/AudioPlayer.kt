package com.dentim.karaoke.ui.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/**
 * Enhanced audio player with mixing capabilities for karaoke
 */
class AudioPlayer(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioPlayer"
    }
    
    private var vocalsPlayer: MediaPlayer? = null
    private var instrumentalPlayer: MediaPlayer? = null
    private var currentPlayer: MediaPlayer? = null // For single track playback
    
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentTrack = MutableStateFlow<String?>(null)
    val currentTrack: StateFlow<String?> = _currentTrack.asStateFlow()
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()
    
    private val _position = MutableStateFlow(0)
    val position: StateFlow<Int> = _position.asStateFlow()
    
    private var vocalsVolume = 0.8f
    private var instrumentalVolume = 0.7f
    private var vocalsReady = false
    private var instrumentalReady = false
    
    /**
     * Play mixed karaoke with separate vocal and instrumental tracks
     */
    fun playMixed(vocalsUrl: String, instrumentalUrl: String, trackName: String) {
        Log.d(TAG, "Starting mixed playback: $trackName")
        try {
            stop() // Stop any existing playback
            vocalsReady = false
            instrumentalReady = false
            
            // Create vocal player
            vocalsPlayer = createMediaPlayer(vocalsUrl, "vocals") { player ->
                player.setVolume(vocalsVolume, vocalsVolume)
            }
            
            // Create instrumental player  
            instrumentalPlayer = createMediaPlayer(instrumentalUrl, "instrumental") { player ->
                player.setVolume(instrumentalVolume, instrumentalVolume)
            }
            
            _currentTrack.value = trackName
            _playbackState.value = PlaybackState.PREPARING
            
            // Prepare both players
            vocalsPlayer?.prepareAsync()
            instrumentalPlayer?.prepareAsync()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting mixed playback", e)
            _playbackState.value = PlaybackState.ERROR
        }
    }
    
    /**
     * Play single track (vocals OR instrumental)
     */
    fun playFromUrl(url: String, trackName: String = "Audio") {
        Log.d(TAG, "Starting single track playback: $trackName from URL: $url")
        try {
            stop() // Stop any existing playback
            
            currentPlayer = createMediaPlayer(url, trackName) { player ->
                // Get duration when prepared
                val durationMs = player.duration
                _duration.value = durationMs
                Log.d(TAG, "Track duration: ${durationMs}ms (${durationMs/1000}s)")
            }
            
            _playbackState.value = PlaybackState.PREPARING
            currentPlayer?.prepareAsync()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing single track", e)
            _playbackState.value = PlaybackState.ERROR
        }
    }
    
    private fun createMediaPlayer(url: String, trackType: String, onPrepared: ((MediaPlayer) -> Unit)? = null): MediaPlayer {
        return MediaPlayer().apply {
            reset()
            
            // Set audio attributes for music playback
            setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            
            setDataSource(url)
            
            setOnPreparedListener { player ->
                Log.d(TAG, "Audio prepared: $trackType")
                onPrepared?.invoke(player)
                
                // Mark track as ready
                when (trackType) {
                    "vocals" -> vocalsReady = true
                    "instrumental" -> instrumentalReady = true
                }
                
                // Start playback for single player mode
                if (currentPlayer != null) {
                    player.start()
                    _playbackState.value = PlaybackState.PLAYING
                    _currentTrack.value = trackType
                    _duration.value = player.duration
                    startPositionUpdater()
                }
                
                // For mixed playback, start both tracks when ready
                if (vocalsReady && instrumentalReady && vocalsPlayer != null && instrumentalPlayer != null) {
                    // Add small delay for better synchronization
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        try {
                            // Start both players as close together as possible
                            instrumentalPlayer?.start()
                            vocalsPlayer?.start()
                            _playbackState.value = PlaybackState.PLAYING
                            _duration.value = player.duration
                            startPositionUpdater()
                            Log.d(TAG, "Started synchronized mixed playback with delay")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error starting synchronized playback", e)
                        }
                    }, 50) // 50ms delay for synchronization
                }
            }
            
            setOnCompletionListener {
                Log.d(TAG, "Audio completed: $trackType")
                if (currentPlayer != null || trackType == "instrumental") {
                    _playbackState.value = PlaybackState.STOPPED
                    _currentTrack.value = null
                }
            }
            
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error for $trackType: what=$what, extra=$extra, URL: $url")
                _playbackState.value = PlaybackState.ERROR
                _currentTrack.value = null
                true
            }
        }
    }
    
    private fun startPositionUpdater() {
        // TODO: Implement position updates using coroutines
        // For now, just log current position
        val player = currentPlayer ?: instrumentalPlayer
        player?.let {
            try {
                val pos = it.currentPosition
                _position.value = pos
                Log.d(TAG, "Current position: ${pos}ms")
            } catch (e: Exception) {
                Log.w(TAG, "Error getting position", e)
            }
        }
    }
    
    /**
     * Set vocals volume (0.0 to 1.0)
     */
    fun setVocalsVolume(volume: Float) {
        vocalsVolume = volume.coerceIn(0f, 1f)
        vocalsPlayer?.setVolume(vocalsVolume, vocalsVolume)
        Log.d(TAG, "Set vocals volume: $vocalsVolume")
    }
    
    /**
     * Set instrumental volume (0.0 to 1.0)
     */
    fun setInstrumentalVolume(volume: Float) {
        instrumentalVolume = volume.coerceIn(0f, 1f)
        instrumentalPlayer?.setVolume(instrumentalVolume, instrumentalVolume)
        currentPlayer?.setVolume(instrumentalVolume, instrumentalVolume) // For single track
        Log.d(TAG, "Set instrumental volume: $instrumentalVolume")
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(positionMs: Int) {
        try {
            currentPlayer?.seekTo(positionMs)
            vocalsPlayer?.seekTo(positionMs)
            instrumentalPlayer?.seekTo(positionMs)
            _position.value = positionMs
            Log.d(TAG, "Seeked to: ${positionMs}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }
    
    /**
     * Get current position in milliseconds
     */
    fun getCurrentPosition(): Int {
        return try {
            currentPlayer?.currentPosition ?: instrumentalPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Error getting current position", e)
            0
        }
    }
    
    /**
     * Get duration in milliseconds
     */
    fun getDuration(): Int {
        return try {
            currentPlayer?.duration ?: instrumentalPlayer?.duration ?: 0
        } catch (e: Exception) {
            Log.w(TAG, "Error getting duration", e)
            0
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        try {
            currentPlayer?.pause()
            vocalsPlayer?.pause()
            instrumentalPlayer?.pause()
            _playbackState.value = PlaybackState.PAUSED
            Log.d(TAG, "Playback paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing playback", e)
        }
    }
    
    /**
     * Resume playback
     */
    fun resume() {
        try {
            // Check if there are existing players to resume
            val hasCurrentPlayer = currentPlayer != null
            val hasMixedPlayers = vocalsPlayer != null && instrumentalPlayer != null
            
            if (hasCurrentPlayer || hasMixedPlayers) {
                currentPlayer?.start()
                vocalsPlayer?.start()
                instrumentalPlayer?.start()
                _playbackState.value = PlaybackState.PLAYING
                Log.d(TAG, "Playback resumed")
            } else {
                Log.w(TAG, "No players available to resume")
                _playbackState.value = PlaybackState.STOPPED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming playback", e)
            _playbackState.value = PlaybackState.ERROR
        }
    }
    
    /**
     * Check if there are active players (paused or playing)
     */
    fun hasActivePlayers(): Boolean {
        return currentPlayer != null || (vocalsPlayer != null && instrumentalPlayer != null)
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        try {
            currentPlayer?.apply {
                stop()
                release()
            }
            vocalsPlayer?.apply {
                stop()
                release()
            }
            instrumentalPlayer?.apply {
                stop()
                release()
            }
            
            currentPlayer = null
            vocalsPlayer = null
            instrumentalPlayer = null
            vocalsReady = false
            instrumentalReady = false
            
            _playbackState.value = PlaybackState.STOPPED
            _currentTrack.value = null
            _position.value = 0
            _duration.value = 0
            
            Log.d(TAG, "Playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }
    
    /**
     * Release resources
     */
    fun release() {
        stop()
    }
    
    enum class PlaybackState {
        STOPPED, PREPARING, PLAYING, PAUSED, ERROR
    }
}