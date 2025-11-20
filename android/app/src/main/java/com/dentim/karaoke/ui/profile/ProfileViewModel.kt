package com.dentim.karaoke.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.dentim.karaoke.domain.repository.TrackRepository
import com.dentim.karaoke.domain.repository.ProcessingRepository
import com.dentim.karaoke.domain.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for ProfileFragment
 * Manages user profile data and statistics
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val processingRepository: ProcessingRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    // Profile statistics
    val statistics: StateFlow<ProfileStatistics> = combine(
        trackRepository.getAllTracks(),
        sessionRepository.getAllSessions(),
        processingRepository.getAllProcessingJobs()
    ) { tracks, sessions, processingJobs ->
        ProfileStatistics(
            totalTracks = tracks.size,
            totalSessions = sessions.size,
            completedProcessing = processingJobs.count { it.status.isCompleted },
            activeProcessing = processingJobs.count { it.status.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileStatistics()
    )
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        Log.d("ProfileViewModel", "ProfileViewModel initialized")
    }
    
    /**
     * Refresh profile data
     */
    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Refresh data from repositories (this will automatically update the flows)
                Log.d("ProfileViewModel", "Profile data refreshed")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error refreshing profile data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}