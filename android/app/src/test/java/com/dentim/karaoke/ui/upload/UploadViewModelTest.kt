package com.dentim.karaoke.ui.upload

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import com.dentim.karaoke.domain.usecase.UploadTrackUseCase
import com.dentim.karaoke.domain.usecase.UploadProgress
import com.dentim.karaoke.domain.model.AIModel
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.model.Processing
import com.dentim.karaoke.domain.model.ProcessingStatus
import com.dentim.karaoke.util.FileInfo
import android.net.Uri
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.util.Date

/**
 * Unit tests for UploadViewModel
 */
@ExperimentalCoroutinesApi
class UploadViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var uploadTrackUseCase: UploadTrackUseCase
    
    @Mock
    private lateinit var mockFile: File
    
    @Mock
    private lateinit var mockUri: Uri
    
    private lateinit var viewModel: UploadViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = UploadViewModel(uploadTrackUseCase)
        
        // Setup mock file
        whenever(mockFile.name).thenReturn("test_song.mp3")
        whenever(mockFile.length()).thenReturn(1024L * 1024L) // 1MB
    }
    
    @Test
    fun `initial state should be correct`() {
        assertNull("Initially no file should be selected", viewModel.selectedFile.value)
        assertEquals("Default AI model should be Demucs", AIModel.DEMUCS, viewModel.selectedAIModel.value)
        assertFalse("Initially should not be uploading", viewModel.uploadState.value.isUploading)
        assertFalse("Initially should not be able to upload", viewModel.uploadState.value.canUpload)
    }
    
    @Test
    fun `onFileSelected should update selected file state`() = runTest {
        val fileInfo = FileInfo(
            name = "test_song.mp3",
            size = 1024L * 1024L,
            mimeType = "audio/mpeg",
            uri = mockUri
        )
        
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val selectedFile = viewModel.selectedFile.value
        assertNotNull("File should be selected", selectedFile)
        assertEquals("Filename should match", "test_song.mp3", selectedFile?.filename)
        assertEquals("File size should match", 1024L * 1024L, selectedFile?.size)
    }
    
    @Test
    fun `selectAIModel should update selected model`() {
        viewModel.selectAIModel(AIModel.SPLEETER)
        
        assertEquals("AI model should be updated", AIModel.SPLEETER, viewModel.selectedAIModel.value)
    }
    
    @Test
    fun `clearSelection should reset state`() = runTest {
        val fileInfo = FileInfo("test.mp3", 1024L, "audio/mpeg", mockUri)
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.clearSelection()
        
        assertNull("File selection should be cleared", viewModel.selectedFile.value)
        assertFalse("Should not be uploading", viewModel.uploadState.value.isUploading)
        assertFalse("Should not be able to upload", viewModel.uploadState.value.canUpload)
    }
    
    @Test
    fun `startUpload should trigger upload process`() = runTest {
        // Setup
        val fileInfo = FileInfo("test.mp3", 1024L, "audio/mpeg", mockUri)
        val track = Track(
            id = "track1",
            filename = "test.mp3",
            originalPath = "/path/test.mp3",
            fileSize = 1024L,
            durationMs = 180000L,
            mimeType = "audio/mpeg",
            checksum = "abc123",
            createdAt = Date(),
            updatedAt = Date()
        )
        val processing = Processing(
            id = "proc1",
            trackId = "track1",
            status = ProcessingStatus.COMPLETED,
            progressPercent = 100,
            aiModel = AIModel.DEMUCS,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val uploadFlow = flowOf(
            UploadProgress.Started,
            UploadProgress.ExtractingMetadata,
            UploadProgress.UploadingToServer,
            UploadProgress.Completed(track, processing)
        )
        
        whenever(uploadTrackUseCase.execute(any(), any(), any())).thenReturn(uploadFlow)
        
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Test
        viewModel.startUpload()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify
        verify(uploadTrackUseCase).execute(eq(mockFile), eq(AIModel.DEMUCS), eq("test.mp3"))
        assertTrue("Upload should be completed", viewModel.uploadState.value.isCompleted)
        assertFalse("Should not be uploading after completion", viewModel.uploadState.value.isUploading)
    }
    
    @Test
    fun `startUpload with duplicate should handle duplicate state`() = runTest {
        val fileInfo = FileInfo("test.mp3", 1024L, "audio/mpeg", mockUri)
        val existingTrack = Track(
            id = "existing1",
            filename = "existing.mp3", 
            originalPath = "/path/existing.mp3",
            fileSize = 1024L,
            durationMs = 180000L,
            mimeType = "audio/mpeg",
            checksum = "abc123",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        val uploadFlow = flowOf(
            UploadProgress.Started,
            UploadProgress.CheckingDuplicates,
            UploadProgress.DuplicateFound(existingTrack)
        )
        
        whenever(uploadTrackUseCase.execute(any(), any(), any())).thenReturn(uploadFlow)
        
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startUpload()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue("Should have duplicate", viewModel.uploadState.value.hasDuplicate)
        assertFalse("Should not be uploading", viewModel.uploadState.value.isUploading)
    }
    
    @Test
    fun `startUpload with error should handle error state`() = runTest {
        val fileInfo = FileInfo("test.mp3", 1024L, "audio/mpeg", mockUri)
        val uploadFlow = flowOf(
            UploadProgress.Started,
            UploadProgress.UploadingToServer,
            UploadProgress.Failed("Network error")
        )
        
        whenever(uploadTrackUseCase.execute(any(), any(), any())).thenReturn(uploadFlow)
        
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startUpload()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("Should have error", "Network error", viewModel.uploadState.value.error)
        assertFalse("Should not be uploading", viewModel.uploadState.value.isUploading)
    }
    
    @Test
    fun `cancelUpload should stop upload process`() = runTest {
        val fileInfo = FileInfo("test.mp3", 1024L, "audio/mpeg", mockUri)
        viewModel.onFileSelected(mockFile, fileInfo)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startUpload()
        viewModel.cancelUpload()
        
        assertFalse("Should not be uploading", viewModel.uploadState.value.isUploading)
        assertFalse("Should not have duplicate", viewModel.uploadState.value.hasDuplicate)
        assertNull("Should not have error", viewModel.uploadState.value.error)
    }
}