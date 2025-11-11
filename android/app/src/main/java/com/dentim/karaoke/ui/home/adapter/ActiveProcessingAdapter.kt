package com.dentim.karaoke.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dentim.karaoke.databinding.ItemProcessingJobBinding
import com.dentim.karaoke.domain.model.Processing
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for displaying active processing jobs
 */
class ActiveProcessingAdapter(
    private val onJobClick: (Processing) -> Unit
) : ListAdapter<Processing, ActiveProcessingAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = ItemProcessingJobBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JobViewHolder(
        private val binding: ItemProcessingJobBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(job: Processing) {
            binding.jobTitle.text = "Processing: Track ${job.trackId}"
            binding.statusText.text = "${job.status.name.lowercase().replaceFirstChar { it.uppercase() }} • ${job.progressPercent}% complete"
            binding.progressBar.progress = job.progressPercent
            
            val timeText = when {
                job.estimatedCompletion != null -> {
                    val remaining = job.estimatedCompletion!!.time - System.currentTimeMillis()
                    if (remaining > 0) {
                        formatEstimatedTime(remaining / 1000)
                    } else {
                        "Almost done"
                    }
                }
                else -> "Unknown"
            }
            binding.estimatedTimeText.text = "Estimated time: $timeText"
            
            // Show/hide progress indicator based on status
            binding.progressIndicator.visibility = if (job.status.isActive) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
            
            binding.root.setOnClickListener {
                onJobClick(job)
            }
        }
        
        private fun formatEstimatedTime(seconds: Long): String {
            return when {
                seconds <= 0 -> "Almost done"
                seconds < 60 -> "$seconds seconds remaining"
                seconds < 3600 -> "${seconds / 60} minutes remaining"
                else -> "${seconds / 3600} hours remaining"
            }
        }
    }

    private class JobDiffCallback : DiffUtil.ItemCallback<Processing>() {
        override fun areItemsTheSame(oldItem: Processing, newItem: Processing): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Processing, newItem: Processing): Boolean {
            return oldItem == newItem
        }
    }
}