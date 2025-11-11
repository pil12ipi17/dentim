package com.dentim.karaoke.ui.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.dentim.karaoke.R
import com.dentim.karaoke.databinding.DialogProgressBinding

/**
 * Common progress dialog for showing loading states
 * Used during file upload, processing, and other long-running operations
 */
class ProgressDialog(
    context: Context,
    private var message: String = context.getString(R.string.loading),
    private val cancelable: Boolean = true
) : Dialog(context) {
    
    private var binding: DialogProgressBinding? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.dialog_progress,
            null,
            false
        )
        
        setContentView(binding!!.root)
        setCancelable(cancelable)
        
        binding!!.progressMessage.text = message
    }
    
    fun updateMessage(newMessage: String) {
        message = newMessage
        binding?.progressMessage?.text = newMessage
    }
    
    fun updateProgress(progress: Int) {
        binding?.progressBar?.progress = progress
        binding?.progressPercent?.text = context.getString(R.string.progress_percent, progress)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        binding = null
    }
}