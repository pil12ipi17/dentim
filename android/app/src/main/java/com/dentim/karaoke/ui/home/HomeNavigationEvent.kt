package com.dentim.karaoke.ui.home

/**
 * Navigation events for the Home screen
 */
sealed class HomeNavigationEvent {
    object NavigateToUpload : HomeNavigationEvent()
    data class NavigateToProcessing(val processingId: String) : HomeNavigationEvent()
    data class NavigateToPlayback(val trackId: String) : HomeNavigationEvent()
    data class ShowError(val message: String) : HomeNavigationEvent()
}