package com.dentim.karaoke.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dentim.karaoke.databinding.ItemRecentTrackBinding
import com.dentim.karaoke.ui.home.model.TrackWithProcessing
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying recent tracks with processing info
 */
class RecentTracksAdapter(
    private val onTrackClick: (TrackWithProcessing) -> Unit,
    private val onPlayClick: (TrackWithProcessing) -> Unit
) : ListAdapter<TrackWithProcessing, RecentTracksAdapter.TrackViewHolder>(TrackWithProcessingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemRecentTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrackViewHolder(
        private val binding: ItemRecentTrackBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trackWithProcessing: TrackWithProcessing) {
            val track = trackWithProcessing.track
            val processing = trackWithProcessing.processing
            
            binding.trackTitle.text = track.title ?: track.filename
            binding.artistText.text = track.artist ?: "Unknown Artist"
            binding.durationText.text = formatDuration(track.durationMs)
            binding.dateText.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(track.createdAt)
            
            // Handle main track click
            binding.root.setOnClickListener {
                onTrackClick(trackWithProcessing)
            }
            
            // Debug logging
            android.util.Log.d("RecentTracksAdapter", "Track: ${track.filename}")
            android.util.Log.d("RecentTracksAdapter", "Processing: $processing")
            android.util.Log.d("RecentTracksAdapter", "Status: ${processing?.status}")
            android.util.Log.d("RecentTracksAdapter", "IsActive: ${processing?.status?.isActive}")
            android.util.Log.d("RecentTracksAdapter", "Progress: ${processing?.progressPercent}")
            
            // Show processing status and progress
            when {
                processing == null -> {
                    android.util.Log.d("RecentTracksAdapter", "No processing info")
                    binding.processingProgressBar.visibility = android.view.View.GONE
                    binding.processingProgressText.visibility = android.view.View.GONE
                    binding.playButton.visibility = android.view.View.GONE
                }
                processing.status.isActive -> {
                    android.util.Log.d("RecentTracksAdapter", "Processing is active - showing progress")
                    binding.processingProgressBar.visibility = android.view.View.VISIBLE
                    binding.processingProgressText.visibility = android.view.View.VISIBLE
                    binding.playButton.visibility = android.view.View.GONE
                    
                    // Show estimated progress and remaining time
                    val estimatedProgress = trackWithProcessing.estimatedProgress
                    val remainingTime = trackWithProcessing.estimatedRemainingTime
                    
                    binding.processingProgressBar.progress = estimatedProgress
                    binding.processingProgressText.text = "${estimatedProgress}% - ${remainingTime ?: "Processing..."}"
                }
                processing.status.isCompleted -> {
                    android.util.Log.d("RecentTracksAdapter", "Processing completed - showing play button")
                    binding.processingProgressBar.visibility = android.view.View.GONE
                    binding.processingProgressText.visibility = android.view.View.GONE
                    binding.playButton.visibility = android.view.View.VISIBLE
                    
                    // Set up play button click
                    binding.playButton.setOnClickListener {
                        onPlayClick(trackWithProcessing)
                    }
                }
                processing.status.isFailed -> {
                    android.util.Log.d("RecentTracksAdapter", "Processing failed")
                    binding.processingProgressBar.visibility = android.view.View.GONE
                    binding.processingProgressText.visibility = android.view.View.VISIBLE
                    binding.processingProgressText.text = "Ошибка обработки"
                    binding.playButton.visibility = android.view.View.GONE
                }
                else -> {
                    android.util.Log.d("RecentTracksAdapter", "Processing in unknown state: ${processing.status}")
                    binding.processingProgressBar.visibility = android.view.View.GONE
                    binding.processingProgressText.visibility = android.view.View.VISIBLE
                    binding.processingProgressText.text = "Ожидание обработки"
                    binding.playButton.visibility = android.view.View.GONE
                }
            }
        }
        
        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    private class TrackWithProcessingDiffCallback : DiffUtil.ItemCallback<TrackWithProcessing>() {
        override fun areItemsTheSame(oldItem: TrackWithProcessing, newItem: TrackWithProcessing): Boolean {
            return oldItem.track.id == newItem.track.id
        }

        override fun areContentsTheSame(oldItem: TrackWithProcessing, newItem: TrackWithProcessing): Boolean {
            return oldItem == newItem
        }
    }
}