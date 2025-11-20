package com.dentim.karaoke.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dentim.karaoke.R
import com.dentim.karaoke.databinding.FragmentHomeBinding
import com.dentim.karaoke.ui.home.adapter.RecentTracksAdapter
import com.dentim.karaoke.ui.home.adapter.ActiveProcessingAdapter
import com.dentim.karaoke.ui.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Home screen fragment showing recent tracks, active processing, and quick actions
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()
    
    private lateinit var recentTracksAdapter: RecentTracksAdapter
    private lateinit var activeProcessingAdapter: ActiveProcessingAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAudioPlayer()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
        
        // Refresh data when fragment loads
        viewModel.refreshData()
    }
    
    override fun onResume() {
        super.onResume()
        // Force refresh data when user returns to home screen
        viewModel.forceRefreshData()
    }
    
    private fun setupAudioPlayer() {
        // AudioPlayer is now automatically injected via DI
        // No need to create or initialize it manually
    }
    
    private fun setupRecyclerViews() {
        // Recent tracks adapter
        recentTracksAdapter = RecentTracksAdapter(
            onTrackClick = { trackWithProcessing ->
                // Handle track click
                viewModel.onTrackWithProcessingSelected(trackWithProcessing)
            },
            onPlayClick = { trackWithProcessing ->
                // Navigate to player and start playing this track
                val processingId = trackWithProcessing.processingId
                if (processingId != null && trackWithProcessing.canPlay) {
                    // Set the track in player view model
                    playerViewModel.setCurrentTrack(trackWithProcessing)
                    // Navigate to player tab via navigation
                    findNavController().navigate(HomeFragmentDirections.actionHomeToPlayer())
                }
            }
        )
        
        binding.recentTracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentTracksAdapter
        }
        
        // Active processing adapter
        activeProcessingAdapter = ActiveProcessingAdapter { processing ->
            // Handle processing click
        }
        
        binding.activeProcessingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeProcessingAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.uploadFab.setOnClickListener {
            // Navigate to upload screen
            findNavController().navigate(R.id.nav_upload)
        }
        
        // Setup swipe to refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe recent tracks with processing info
            viewModel.recentTracksWithProcessing.collect { tracksWithProcessing ->
                android.util.Log.d("HomeFragment", "Received ${tracksWithProcessing.size} tracks with processing")
                tracksWithProcessing.forEach { trackWithProcessing ->
                    android.util.Log.d("HomeFragment", 
                        "Track: ${trackWithProcessing.track.filename}, " +
                        "Processing: ${trackWithProcessing.processing?.id}, " +
                        "Status: ${trackWithProcessing.processing?.status}, " +
                        "Can play: ${trackWithProcessing.canPlay}")
                }
                recentTracksAdapter.submitList(tracksWithProcessing)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe active processing jobs
            viewModel.activeProcessing.collect { processingJobs ->
                activeProcessingAdapter.submitList(processingJobs)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe loading state
            viewModel.isLoading.collect { isLoading ->
                binding.swipeRefreshLayout.isRefreshing = isLoading
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // AudioPlayer is now managed as singleton via DI, don't release it here
        _binding = null
    }
}