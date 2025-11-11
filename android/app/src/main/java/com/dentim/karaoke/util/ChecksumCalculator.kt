package com.dentim.karaoke.util

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for calculating file checksums to detect duplicates
 * Supports MD5 and SHA-256 algorithms with coroutine-based async processing
 */
object ChecksumCalculator {
    
    private const val TAG = "ChecksumCalculator"
    private const val BUFFER_SIZE = 8192 // 8KB buffer for file reading
    
    /**
     * Calculate MD5 checksum of a file (async)
     */
    suspend fun calculateMD5(file: File): String = withContext(Dispatchers.IO) {
        calculateChecksum(file, "MD5")
    }
    
    /**
     * Calculate SHA-256 checksum of a file (async)
     */
    suspend fun calculateSHA256(file: File): String = withContext(Dispatchers.IO) {
        calculateChecksum(file, "SHA-256")
    }
    
    /**
     * Calculate MD5 checksum synchronously (for blocking operations)
     */
    fun calculateMD5Sync(file: File): String {
        return calculateChecksum(file, "MD5")
    }
    
    /**
     * Calculate SHA-256 checksum synchronously (for blocking operations)
     */
    fun calculateSHA256Sync(file: File): String {
        return calculateChecksum(file, "SHA-256")
    }
    
    private fun calculateChecksum(file: File, algorithm: String): String {
        return try {
            val digest = MessageDigest.getInstance(algorithm)
            val buffer = ByteArray(BUFFER_SIZE)
            
            FileInputStream(file).use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            
            // Convert digest bytes to hex string
            val checksum = digest.digest().joinToString("") { "%02x".format(it) }
            
            Log.d(TAG, "Calculated $algorithm checksum for ${file.name}: $checksum")
            checksum
            
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating $algorithm checksum for ${file.name}", e)
            throw e
        }
    }
    
    /**
     * Calculate quick checksum using file size and first/last bytes
     * Faster but less reliable than full checksum - use for initial duplicate detection
     */
    suspend fun calculateQuickChecksum(file: File): String = withContext(Dispatchers.IO) {
        try {
            val size = file.length()
            val firstBytes = ByteArray(1024) // First 1KB
            val lastBytes = ByteArray(1024)  // Last 1KB
            
            FileInputStream(file).use { inputStream ->
                // Read first bytes
                val firstRead = inputStream.read(firstBytes)
                
                // Skip to end for last bytes (if file is large enough)
                if (size > 2048) {
                    inputStream.skip(size - 1024)
                    val lastRead = inputStream.read(lastBytes, 0, minOf(1024, (size - firstRead).toInt()))
                    
                    // Combine size, first bytes, and last bytes for quick hash
                    val combined = "$size${firstBytes.contentHashCode()}${lastBytes.contentHashCode()}"
                    combined.hashCode().toString(16)
                } else {
                    // For small files, just use size and content hash
                    "$size${firstBytes.contentHashCode()}".hashCode().toString(16)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating quick checksum for ${file.name}", e)
            throw e
        }
    }
    
    /**
     * Validate checksum format (hexadecimal string)
     */
    fun isValidChecksum(checksum: String, algorithm: String = "MD5"): Boolean {
        val expectedLength = when (algorithm.uppercase()) {
            "MD5" -> 32
            "SHA-256" -> 64
            else -> return false
        }
        
        return checksum.length == expectedLength && checksum.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }
    }
    
    /**
     * Compare two files by their checksums
     */
    suspend fun areFilesIdentical(file1: File, file2: File): Boolean {
        return try {
            // Quick check first - if sizes differ, files are different
            if (file1.length() != file2.length()) {
                return false
            }
            
            // Calculate checksums for comparison
            val checksum1 = calculateMD5(file1)
            val checksum2 = calculateMD5(file2)
            
            checksum1 == checksum2
        } catch (e: Exception) {
            Log.e(TAG, "Error comparing files", e)
            false
        }
    }
}