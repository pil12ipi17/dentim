package com.dentim.karaoke.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dentim.karaoke.databinding.ItemRecentTrackBinding
import com.dentim.karaoke.domain.model.Track
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying recent tracks
 */
class RecentTracksAdapter(
    private val onTrackClick: (Track) -> Unit
) : ListAdapter<Track, RecentTracksAdapter.TrackViewHolder>(TrackDiffCallback()) {

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

        fun bind(track: Track) {
            binding.trackTitle.text = track.title
            binding.artistText.text = track.artist
            binding.durationText.text = formatDuration(track.durationMs)
            binding.dateText.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(track.createdAt)
            
            binding.root.setOnClickListener {
                onTrackClick(track)
            }
        }
        
        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
    }

    private class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}