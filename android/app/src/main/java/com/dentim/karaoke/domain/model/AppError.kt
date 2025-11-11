package com.dentim.karaoke.domain.model

/**
 * Application-specific error types
 */
sealed class AppError(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    data class NetworkError(
        override val message: String = "Network connection error",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class ApiError(
        val code: Int,
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class FileError(
        override val message: String = "File operation error",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class PermissionError(
        override val message: String = "Permission denied",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class ProcessingError(
        override val message: String = "Processing operation failed",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
    
    data class UnknownError(
        override val message: String = "Unknown error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * Utility function to convert exceptions to AppError
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is java.net.UnknownHostException,
        is java.net.SocketTimeoutException,
        is java.net.ConnectException -> AppError.NetworkError(
            message = this.message ?: "Network connection error",
            cause = this
        )
        is java.io.IOException -> AppError.FileError(
            message = this.message ?: "File operation error",
            cause = this
        )
        else -> AppError.UnknownError(
            message = this.message ?: "Unknown error occurred",
            cause = this
        )
    }
}