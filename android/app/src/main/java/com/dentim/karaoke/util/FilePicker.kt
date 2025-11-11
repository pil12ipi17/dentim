package com.dentim.karaoke.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility class for handling file picking operations
 * Supports audio file selection with proper permission handling and validation
 */
class FilePicker(private val fragment: Fragment) {
    
    companion object {
        private val SUPPORTED_AUDIO_FORMATS = listOf(
            "audio/mpeg",     // MP3
            "audio/wav",      // WAV
            "audio/mp4",      // M4A/AAC
            "audio/flac",     // FLAC
            "audio/ogg",      // OGG
            "audio/3gpp",     // 3GP
            "audio/amr"       // AMR
        )
        
        private const val MAX_FILE_SIZE_MB = 100L // 100 MB limit
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024L * 1024L
    }
    
    private var onFileSelectedCallback: ((FilePickerResult) -> Unit)? = null
    private var onPermissionDeniedCallback: (() -> Unit)? = null
    
    // Activity result launchers
    private val filePickerLauncher: ActivityResultLauncher<Intent> = 
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleFilePickerResult(result.data)
        }
    
    private val permissionLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                launchFilePicker()
            } else {
                onPermissionDeniedCallback?.invoke()
            }
        }
    
    /**
     * Pick audio file with automatic permission handling
     */
    fun pickAudioFile(
        onFileSelected: (FilePickerResult) -> Unit,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        onFileSelectedCallback = onFileSelected
        onPermissionDeniedCallback = onPermissionDenied
        
        if (hasRequiredPermissions()) {
            launchFilePicker()
        } else {
            requestPermissions()
        }
    }
    
    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ doesn't need READ_EXTERNAL_STORAGE for file picker
            true
        } else {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses granular media permissions
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        permissionLauncher.launch(permissions)
    }
    
    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, SUPPORTED_AUDIO_FORMATS.toTypedArray())
        }
        
        val chooser = Intent.createChooser(intent, "Select Audio File")
        filePickerLauncher.launch(chooser)
    }
    
    private fun handleFilePickerResult(data: Intent?) {
        val uri = data?.data
        if (uri == null) {
            onFileSelectedCallback?.invoke(FilePickerResult.Cancelled)
            return
        }
        
        try {
            val context = fragment.requireContext()
            val fileInfo = extractFileInfo(context, uri)
            
            // Validate file
            val validationResult = validateAudioFile(context, uri, fileInfo)
            if (validationResult !is FileValidationResult.Valid) {
                val error = when (validationResult) {
                    is FileValidationResult.InvalidFormat -> "Unsupported audio format"
                    is FileValidationResult.FileTooLarge -> "File too large (max ${MAX_FILE_SIZE_MB}MB)"
                    is FileValidationResult.InvalidDuration -> "Invalid audio duration"
                    is FileValidationResult.Error -> validationResult.message
                    else -> "Invalid file"
                }
                onFileSelectedCallback?.invoke(FilePickerResult.Error(error))
                return
            }
            
            // Copy file to app's internal storage
            val copiedFile = copyFileToInternalStorage(context, uri, fileInfo.name)
            
            onFileSelectedCallback?.invoke(
                FilePickerResult.Success(
                    file = copiedFile,
                    originalUri = uri,
                    fileInfo = fileInfo
                )
            )
            
        } catch (e: Exception) {
            ErrorHandler.handleError(e, "FilePicker", "Failed to process selected file")
            onFileSelectedCallback?.invoke(FilePickerResult.Error("Failed to process file: ${e.message}"))
        }
    }
    
    private fun extractFileInfo(context: Context, uri: Uri): FileInfo {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            
            it.moveToFirst()
            
            val name = if (nameIndex >= 0) it.getString(nameIndex) else "unknown_audio.mp3"
            val size = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
            
            val mimeType = context.contentResolver.getType(uri) ?: "audio/unknown"
            
            FileInfo(
                name = name,
                size = size,
                mimeType = mimeType,
                uri = uri
            )
        } ?: FileInfo("unknown_audio.mp3", 0L, "audio/unknown", uri)
    }
    
    private fun validateAudioFile(context: Context, uri: Uri, fileInfo: FileInfo): FileValidationResult {
        // Check file size
        if (fileInfo.size > MAX_FILE_SIZE_BYTES) {
            return FileValidationResult.FileTooLarge(fileInfo.size, MAX_FILE_SIZE_BYTES)
        }
        
        // Check MIME type
        if (!SUPPORTED_AUDIO_FORMATS.contains(fileInfo.mimeType)) {
            return FileValidationResult.InvalidFormat(fileInfo.mimeType)
        }
        
        // Additional validation can be added here (duration, etc.)
        return FileValidationResult.Valid
    }
    
    private fun copyFileToInternalStorage(context: Context, uri: Uri, filename: String): File {
        val internalDir = File(context.filesDir, "uploads")
        internalDir.mkdirs()
        
        // Generate unique filename to avoid conflicts
        val timestamp = System.currentTimeMillis()
        val fileExtension = filename.substringAfterLast(".", "mp3")
        val uniqueFilename = "${timestamp}_$filename"
        
        val destinationFile = File(internalDir, uniqueFilename)
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalStateException("Unable to open input stream for URI: $uri")
        
        return destinationFile
    }
}

/**
 * Result of file picking operation
 */
sealed class FilePickerResult {
    data class Success(
        val file: File,
        val originalUri: Uri,
        val fileInfo: FileInfo
    ) : FilePickerResult()
    
    object Cancelled : FilePickerResult()
    
    data class Error(val message: String) : FilePickerResult()
}

/**
 * Information about selected file
 */
data class FileInfo(
    val name: String,
    val size: Long,
    val mimeType: String,
    val uri: Uri
)

/**
 * File validation results
 */
sealed class FileValidationResult {
    object Valid : FileValidationResult()
    data class InvalidFormat(val mimeType: String) : FileValidationResult()
    data class FileTooLarge(val actualSize: Long, val maxSize: Long) : FileValidationResult()
    data class InvalidDuration(val duration: Long) : FileValidationResult()
    data class Error(val message: String) : FileValidationResult()
}