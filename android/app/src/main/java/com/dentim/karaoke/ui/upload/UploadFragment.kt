package com.dentim.karaoke.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dentim.karaoke.R
import com.dentim.karaoke.databinding.FragmentUploadBinding
import com.dentim.karaoke.domain.model.AIModel
import com.dentim.karaoke.util.FilePicker
import com.dentim.karaoke.util.FilePickerResult
import com.dentim.karaoke.util.UserFeedback
import com.dentim.karaoke.domain.model.AppError
import com.dentim.karaoke.ui.common.ProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Upload fragment for selecting and uploading audio files
 * Handles file selection, AI model choice, and upload progress
 */
@AndroidEntryPoint
class UploadFragment : Fragment() {
    
    private lateinit var binding: FragmentUploadBinding
    private val viewModel: UploadViewModel by viewModels()
    
    private lateinit var filePicker: FilePicker
    private var progressDialog: ProgressDialog? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_upload, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupFilePicker()
        setupClickListeners()
        setupAIModelSelection()
        observeViewModel()
    }
    
    private fun setupFilePicker() {
        filePicker = FilePicker(this)
    }
    
    private fun setupClickListeners() {
        binding.selectFileButton.setOnClickListener {
            selectAudioFile()
        }
        
        binding.uploadButton.setOnClickListener {
            viewModel.startUpload()
        }
        
        binding.cancelButton.setOnClickListener {
            viewModel.cancelUpload()
            findNavController().navigateUp()
        }
        
        binding.clearSelectionButton.setOnClickListener {
            viewModel.clearSelection()
        }
    }
    
    private fun setupAIModelSelection() {
        binding.aiModelRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedModel = when (checkedId) {
                R.id.radio_demucs -> AIModel.DEMUCS
                R.id.radio_spleeter -> AIModel.SPLEETER
                else -> AIModel.DEMUCS
            }
            viewModel.selectAIModel(selectedModel)
        }
        
        // Set default selection
        binding.radioDemucs.isChecked = true
        viewModel.selectAIModel(AIModel.DEMUCS)
    }
    
    private fun selectAudioFile() {
        filePicker.pickAudioFile(
            onFileSelected = { result ->
                when (result) {
                    is FilePickerResult.Success -> {
                        viewModel.onFileSelected(result.file, result.fileInfo)
                    }
                    is FilePickerResult.Error -> {
                        UserFeedback.showError(
                            context = requireContext(),
                            error = com.dentim.karaoke.domain.model.AppError.FileError(result.message),
                            view = binding.root
                        )
                    }
                    FilePickerResult.Cancelled -> {
                        // User cancelled file selection - no action needed
                    }
                }
            },
            onPermissionDenied = {
                UserFeedback.showError(
                    context = requireContext(),
                    error = com.dentim.karaoke.domain.model.AppError.PermissionError("Storage permission required"),
                    view = binding.root
                )
            }
        )
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe selected file state
            viewModel.selectedFile.collect { fileState ->
                binding.selectedFileState = fileState
                binding.hasSelectedFile = fileState != null
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe selected AI model
            viewModel.selectedAIModel.collect { model ->
                binding.selectedAIModel = model
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe upload progress
            viewModel.uploadProgress.collect { progress ->
                handleUploadProgress(progress)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe upload state
            viewModel.uploadState.collect { state ->
                binding.uploadState = state
                binding.isUploading = state.isUploading
                binding.canUpload = state.canUpload
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe navigation events
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is UploadNavigationEvent.NavigateToHome -> {
                        findNavController().navigate(
                            UploadFragmentDirections.actionUploadToHome()
                        )
                    }
                    is UploadNavigationEvent.NavigateToPlayback -> {
                        findNavController().navigate(
                            UploadFragmentDirections.actionUploadToProcessing(event.processingId)
                        )
                    }
                    is UploadNavigationEvent.NavigateToProcessing -> {
                        findNavController().navigate(
                            UploadFragmentDirections.actionUploadToProcessing(event.processingId)
                        )
                    }
                    is UploadNavigationEvent.ShowError -> {
                        UserFeedback.showInfo(requireContext(), event.message)
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe error events
            viewModel.errorEvent.collect { error ->
                UserFeedback.showError(
                    context = requireContext(),
                    error = error,
                    view = binding.root,
                    actionLabel = getString(R.string.retry)
                ) {
                    viewModel.retryUpload()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe success messages
            viewModel.messageEvent.collect { message ->
                UserFeedback.showSuccess(
                    context = requireContext(),
                    message = message,
                    view = binding.root
                )
            }
        }
    }
    
    private fun handleUploadProgress(progress: com.dentim.karaoke.domain.usecase.UploadProgress) {
        when (progress) {
            is com.dentim.karaoke.domain.usecase.UploadProgress.Started -> {
                showProgressDialog("Starting upload...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.ExtractingMetadata -> {
                updateProgressDialog("Extracting metadata...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.CalculatingChecksum -> {
                updateProgressDialog("Calculating checksum...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.CheckingDuplicates -> {
                updateProgressDialog("Checking for duplicates...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.SavingTrack -> {
                updateProgressDialog("Saving track...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.UploadingToServer -> {
                updateProgressDialog("Uploading to server...")
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.DuplicateFound -> {
                hideProgressDialog()
                showDuplicateDialog(progress.existingTrack)
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.Completed -> {
                hideProgressDialog()
                UserFeedback.showSuccess(
                    context = requireContext(),
                    message = "Upload completed successfully!",
                    view = binding.root
                )
            }
            is com.dentim.karaoke.domain.usecase.UploadProgress.Failed -> {
                hideProgressDialog()
                // Error will be handled by errorEvent flow
            }
        }
    }
    
    private fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(
                context = requireContext(),
                message = message,
                cancelable = false
            )
            progressDialog?.show()
        } else {
            progressDialog?.updateMessage(message)
            if (!progressDialog!!.isShowing) {
                progressDialog?.show()
            }
        }
    }
    
    private fun updateProgressDialog(message: String) {
        progressDialog?.updateMessage(message)
    }
    
    private fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
    
    private fun showDuplicateDialog(existingTrack: com.dentim.karaoke.domain.model.Track) {
        UserFeedback.showConfirmation(
            context = requireContext(),
            title = "Duplicate Track Found",
            message = "A track with the same content already exists: ${existingTrack.filename}. Do you want to upload anyway?",
            positiveText = "Upload Anyway",
            negativeText = "Cancel",
            onPositive = {
                viewModel.uploadAnyway()
            },
            onNegative = {
                viewModel.cancelUpload()
            }
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        hideProgressDialog()
    }
}