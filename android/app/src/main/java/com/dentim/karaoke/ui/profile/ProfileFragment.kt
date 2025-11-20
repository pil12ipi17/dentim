package com.dentim.karaoke.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dentim.karaoke.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Profile screen fragment showing user statistics and account information
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        
        // Refresh data on view creation
        viewModel.refreshData()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe statistics
            viewModel.statistics.collect { statistics ->
                binding.statistics = statistics
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.upgradeButton.setOnClickListener {
            // TODO: Implement upgrade functionality in the future
            // For now, this is disabled
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}