package com.dentim.karaoke.util

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import android.view.View
import com.dentim.karaoke.R
import com.dentim.karaoke.domain.model.AppError

/**
 * User feedback utilities for showing errors, messages, and confirmations
 * Provides consistent UI feedback across the application
 */
object UserFeedback {
    
    /**
     * Show error message to user based on error type
     */
    fun showError(
        context: Context,
        error: AppError,
        view: View? = null,
        actionLabel: String? = null,
        action: (() -> Unit)? = null
    ) {
        val message = getUserFriendlyMessage(context, error)
        
        if (view != null && actionLabel != null && action != null) {
            // Show Snackbar with action
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionLabel) { action() }
                .show()
        } else if (view != null) {
            // Show Snackbar without action
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        } else {
            // Show Toast as fallback
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Show success message
     */
    fun showSuccess(
        context: Context,
        message: String,
        view: View? = null
    ) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Show info message
     */
    fun showInfo(
        context: Context,
        message: String,
        view: View? = null
    ) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Show confirmation dialog
     */
    fun showConfirmation(
        context: Context,
        title: String,
        message: String,
        positiveText: String = context.getString(R.string.ok),
        negativeText: String = context.getString(R.string.cancel),
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ -> onPositive() }
            .setNegativeButton(negativeText) { _, _ -> onNegative?.invoke() }
            .show()
    }
    
    /**
     * Show error dialog with retry option
     */
    fun showErrorDialog(
        context: Context,
        error: AppError,
        onRetry: (() -> Unit)? = null
    ) {
        val message = getUserFriendlyMessage(context, error)
        val builder = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.error_title))
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.ok), null)
        
        if (onRetry != null) {
            builder.setNeutralButton(context.getString(R.string.retry)) { _, _ -> onRetry() }
        }
        
        builder.show()
    }
    
    /**
     * Convert AppError to user-friendly message
     */
    private fun getUserFriendlyMessage(context: Context, error: AppError): String {
        return when (error) {
            is AppError.NetworkError -> context.getString(R.string.error_network)
            is AppError.ApiError -> context.getString(R.string.error_network)
            is AppError.FileError -> context.getString(R.string.error_file_not_found)
            is AppError.PermissionError -> context.getString(R.string.error_permission_denied)
            is AppError.ValidationError -> context.getString(R.string.error_invalid_file)
            is AppError.ProcessingError -> context.getString(R.string.error_processing_failed)
            is AppError.UnknownError -> error.message.takeIf { it.isNotBlank() } 
                ?: context.getString(R.string.error_unknown)
        }
    }
}