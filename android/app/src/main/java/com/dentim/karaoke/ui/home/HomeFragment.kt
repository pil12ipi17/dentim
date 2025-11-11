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
import com.dentim.karaoke.databinding.FragmentHomeBinding
import com.dentim.karaoke.ui.home.adapter.RecentTracksAdapter
import com.dentim.karaoke.ui.home.adapter.ActiveProcessingAdapter
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
        
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupRecyclerViews() {
        // Recent tracks adapter
        recentTracksAdapter = RecentTracksAdapter { track ->
            // Handle track click
        }
        
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
            findNavController().navigate(HomeFragmentDirections.actionHomeToUpload())
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe recent tracks
            viewModel.recentTracks.collect { tracks ->
                recentTracksAdapter.submitList(tracks)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe active processing jobs
            viewModel.activeProcessing.collect { processingJobs ->
                activeProcessingAdapter.submitList(processingJobs)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}