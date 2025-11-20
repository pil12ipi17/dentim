package com.dentim.karaoke.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dentim.karaoke.R
import com.dentim.karaoke.databinding.FragmentPlayerBinding
import com.dentim.karaoke.ui.common.TrackSelectionDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Karaoke player fragment with volume mixing controls
 */
@AndroidEntryPoint
class PlayerFragment : Fragment() {
    
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        android.util.Log.d("PlayerFragment", "onViewCreated: Starting Player fragment setup")
        
        // AudioPlayer is now automatically injected into PlayerViewModel via DI
        setupObservers()
        setupClickListeners()
        setupSeekBars()
        
        // Auto-select first available track if none is selected
        viewModel.selectFirstAvailableTrack()
        
        android.util.Log.d("PlayerFragment", "onViewCreated: Player fragment setup completed")
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe playback state
            viewModel.playbackState.collect { state ->
                binding.playbackState = state
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe available tracks to enable/disable navigation buttons
            viewModel.availableTracks.collect { tracks ->
                val currentTrack = viewModel.getCurrentTrack()
                val currentIndex = tracks.indexOfFirst { it.track.id == currentTrack?.id }
                
                binding.previousButton.isEnabled = currentIndex > 0
                binding.nextButton.isEnabled = currentIndex >= 0 && currentIndex < tracks.size - 1
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe error messages
            viewModel.errorMessage.collect { message ->
                // TODO: Show error message in a snackbar or toast
                android.util.Log.e("PlayerFragment", "Error: $message")
            }
        }
    }
    
    private fun setupClickListeners() {
        // Play/Pause button
        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }
        
        // Previous/Next buttons
        binding.previousButton.setOnClickListener {
            viewModel.playPreviousTrack()
        }
        
        binding.nextButton.setOnClickListener {
            viewModel.playNextTrack()
        }
        
        // Select track button
        binding.selectTrackButton.setOnClickListener {
            showTrackSelectionDialog()
        }
    }
    
    private fun setupSeekBars() {
        // Progress seek bar
        binding.progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.updateProgress(progress)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Vocals volume seek bar
        binding.vocalsVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.updateVocalsVolume(progress)
                    binding.vocalsVolumeText.text = progress.toString()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Instrumental volume seek bar
        binding.instrumentalVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.updateInstrumentalVolume(progress)
                    binding.instrumentalVolumeText.text = progress.toString()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun showTrackSelectionDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            val availableTracks = viewModel.availableTracks.value
            
            if (availableTracks.isEmpty()) {
                // No tracks available, navigate to upload
                findNavController().navigate(R.id.nav_upload)
                return@launch
            }
            
            // Create simple track selection dialog
            val trackNames = availableTracks.map { "${it.track.title ?: it.track.filename} - ${it.track.artist ?: "Unknown Artist"}" }.toTypedArray()
            
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose Track")
                .setItems(trackNames) { _, which ->
                    if (which >= 0 && which < availableTracks.size) {
                        viewModel.selectTrack(availableTracks[which])
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}