package com.dentim.karaoke.ui.upload

/**
 * Navigation events for the Upload screen
 */
sealed class UploadNavigationEvent {
    object NavigateToHome : UploadNavigationEvent()
    data class NavigateToProcessing(val processingId: String) : UploadNavigationEvent()
    data class NavigateToPlayback(val processingId: String) : UploadNavigationEvent()
    data class ShowError(val message: String) : UploadNavigationEvent()
}