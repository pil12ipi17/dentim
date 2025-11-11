package com.dentim.karaoke.ui.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.dentim.karaoke.domain.model.AIModel
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.usecase.UploadTrackUseCase
import com.dentim.karaoke.domain.usecase.UploadProgress
import com.dentim.karaoke.util.FileInfo
import com.dentim.karaoke.util.AudioMetadataExtractor
import com.dentim.karaoke.util.ErrorHandler
import com.dentim.karaoke.domain.model.AppError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for UploadFragment
 * Manages file selection, AI model choice, and upload process
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadTrackUseCase: UploadTrackUseCase
) : ViewModel() {
    
    private val _selectedFile = MutableStateFlow<SelectedFileState?>(null)
    val selectedFile: StateFlow<SelectedFileState?> = _selectedFile.asStateFlow()
    
    private val _selectedAIModel = MutableStateFlow(AIModel.DEMUCS)
    val selectedAIModel: StateFlow<AIModel> = _selectedAIModel.asStateFlow()
    
    private val _uploadProgress = MutableSharedFlow<UploadProgress>()
    val uploadProgress: SharedFlow<UploadProgress> = _uploadProgress.asSharedFlow()
    
    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()
    
    private val _navigationEvent = MutableSharedFlow<UploadNavigationEvent>()
    val navigationEvent: SharedFlow<UploadNavigationEvent> = _navigationEvent.asSharedFlow()
    
    private val _errorEvent = MutableSharedFlow<AppError>()
    val errorEvent: SharedFlow<AppError> = _errorEvent.asSharedFlow()
    
    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()
    
    private var currentUploadJob: kotlinx.coroutines.Job? = null
    private var duplicateTrack: Track? = null
    
    /**
     * Handle file selection from FilePicker
     */
    fun onFileSelected(file: File, fileInfo: FileInfo) {
        viewModelScope.launch {
            try {
                // Extract metadata for display
                val metadata = AudioMetadataExtractor.extractMetadata(file)
                
                val fileState = SelectedFileState(
                    file = file,
                    filename = fileInfo.name,
                    size = fileInfo.size,
                    duration = metadata.duration,
                    formattedSize = AudioMetadataExtractor.formatFileSize(fileInfo.size),
                    formattedDuration = AudioMetadataExtractor.formatDuration(metadata.duration),
                    mimeType = fileInfo.mimeType,
                    title = metadata.title,
                    artist = metadata.artist
                )
                
                _selectedFile.value = fileState
                updateUploadState()
                
            } catch (e: Exception) {
                val appError = ErrorHandler.handleError(e, "UploadViewModel", "Process selected file")
                _errorEvent.emit(appError)
            }
        }
    }
    
    /**
     * Clear file selection
     */
    fun clearSelection() {
        _selectedFile.value = null
        duplicateTrack = null
        cancelUpload()
        updateUploadState()
    }
    
    /**
     * Select AI model
     */
    fun selectAIModel(model: AIModel) {
        _selectedAIModel.value = model
        updateUploadState()
    }
    
    /**
     * Start upload process
     */
    fun startUpload() {
        val fileState = _selectedFile.value ?: return
        val aiModel = _selectedAIModel.value
        
        currentUploadJob?.cancel()
        currentUploadJob = viewModelScope.launch {
            try {
                _uploadState.value = _uploadState.value.copy(isUploading = true)
                
                uploadTrackUseCase.execute(fileState.file, aiModel, fileState.filename)
                    .collect { progress ->
                        _uploadProgress.emit(progress)
                        
                        when (progress) {
                            is UploadProgress.DuplicateFound -> {
                                duplicateTrack = progress.existingTrack
                                _uploadState.value = _uploadState.value.copy(
                                    isUploading = false,
                                    hasDuplicate = true
                                )
                            }
                            is UploadProgress.Completed -> {
                                _uploadState.value = _uploadState.value.copy(
                                    isUploading = false,
                                    isCompleted = true
                                )
                                _messageEvent.emit("Upload completed successfully!")
                                
                                // Auto-navigate back to home after success
                                kotlinx.coroutines.delay(1500)
                                _navigationEvent.emit(UploadNavigationEvent.NavigateToHome)
                            }
                            is UploadProgress.Failed -> {
                                _uploadState.value = _uploadState.value.copy(
                                    isUploading = false,
                                    error = progress.error
                                )
                                val appError = AppError.ProcessingError(progress.error)
                                _errorEvent.emit(appError)
                            }
                            else -> {
                                // Progress updates are handled in the Fragment
                            }
                        }
                    }
                    
            } catch (e: Exception) {
                _uploadState.value = _uploadState.value.copy(
                    isUploading = false,
                    error = e.message
                )
                val appError = ErrorHandler.handleError(e, "UploadViewModel", "Start upload")
                _errorEvent.emit(appError)
            }
        }
    }
    
    /**
     * Upload anyway (ignore duplicate)
     */
    fun uploadAnyway() {
        duplicateTrack = null
        _uploadState.value = _uploadState.value.copy(hasDuplicate = false)
        startUpload()
    }
    
    /**
     * Cancel upload process
     */
    fun cancelUpload() {
        currentUploadJob?.cancel()
        currentUploadJob = null
        _uploadState.value = _uploadState.value.copy(
            isUploading = false,
            hasDuplicate = false,
            error = null
        )
    }
    
    /**
     * Retry upload after error
     */
    fun retryUpload() {
        _uploadState.value = _uploadState.value.copy(error = null)
        startUpload()
    }
    
    private fun updateUploadState() {
        val hasFile = _selectedFile.value != null
        val isUploading = _uploadState.value.isUploading
        val hasError = _uploadState.value.error != null
        
        _uploadState.value = _uploadState.value.copy(
            canUpload = hasFile && !isUploading && !hasError
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        cancelUpload()
    }
}

/**
 * State of selected file
 */
data class SelectedFileState(
    val file: File,
    val filename: String,
    val size: Long,
    val duration: Long,
    val formattedSize: String,
    val formattedDuration: String,
    val mimeType: String,
    val title: String?,
    val artist: String?
)

/**
 * Upload state
 */
data class UploadState(
    val isUploading: Boolean = false,
    val canUpload: Boolean = false,
    val hasDuplicate: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)