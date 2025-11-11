package com.dentim.karaoke.util

import android.util.Log
import com.dentim.karaoke.domain.model.AppError
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Centralized error handling and logging utilities
 * Provides consistent error handling across the application
 */
object ErrorHandler {
    
    private const val TAG = "KaraokeApp"
    
    /**
     * Log an error with context information
     */
    fun logError(tag: String = TAG, message: String, throwable: Throwable? = null) {
        val errorMessage = buildString {
            append(message)
            if (throwable != null) {
                append("\n")
                append(getStackTraceString(throwable))
            }
        }
        Log.e(tag, errorMessage)
    }
    
    /**
     * Convert any throwable to AppError
     */
    fun handleError(throwable: Throwable, tag: String = TAG, message: String = "Error occurred"): AppError {
        logError(tag, message, throwable)
        return when (throwable) {
            is AppError -> throwable
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> AppError.NetworkError(
                message = throwable.message ?: "Network connection error",
                cause = throwable
            )
            is java.io.IOException -> AppError.FileError(
                message = throwable.message ?: "File operation error",
                cause = throwable
            )
            else -> AppError.UnknownError(
                message = throwable.message ?: "Unknown error occurred",
                cause = throwable
            )
        }
    }
    
    /**
     * Get stack trace as string
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        printWriter.close()
        return stringWriter.toString()
    }
    
    /**
     * Handle network errors specifically
     */
    fun handleNetworkError(throwable: Throwable): AppError.NetworkError {
        logError(message = "Network error occurred", throwable = throwable)
        return AppError.NetworkError(
            message = throwable.message ?: "Network connection failed",
            cause = throwable
        )
    }
    
    /**
     * Handle file operation errors
     */
    fun handleFileError(throwable: Throwable): AppError.FileError {
        logError(message = "File operation error", throwable = throwable)
        return AppError.FileError(
            message = throwable.message ?: "File operation failed",
            cause = throwable
        )
    }
    
    /**
     * Handle validation errors
     */
    fun handleValidationError(message: String): AppError.ValidationError {
        logError(message = "Validation error: $message")
        return AppError.ValidationError(message = message)
    }
}

/**
 * Network exception for connection issues
 */
class NetworkException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)