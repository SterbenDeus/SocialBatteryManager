package com.example.socialbatterymanager.features.people.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.PersonLabel
import com.example.socialbatterymanager.features.people.data.PersonWithStats

class PersonAdapter(
    private val onItemClick: (PersonWithStats) -> Unit,
    private val onMoreClick: (PersonWithStats) -> Unit
) : ListAdapter<PersonWithStats, PersonAdapter.PersonViewHolder>(PersonWithStatsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLabel: TextView = itemView.findViewById(R.id.tvLabel)
        private val tvInteractions: TextView = itemView.findViewById(R.id.tvInteractions)
        private val tvEnergyDrain: TextView = itemView.findViewById(R.id.tvEnergyDrain)
        private val ivTrend: ImageView = itemView.findViewById(R.id.ivTrend)
        private val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
        private val progressSocialEnergy: ProgressBar = itemView.findViewById(R.id.progressSocialEnergy)
        private val tvSocialEnergyLevel: TextView = itemView.findViewById(R.id.tvSocialEnergyLevel)
        private val tvMood: TextView = itemView.findViewById(R.id.tvMood)

        fun bind(personWithStats: PersonWithStats) {
            val person = personWithStats.person
            
            // Basic info
            tvName.text = person.name
            
            // Label with background color
            tvLabel.text = person.label.displayName
            try {
                tvLabel.background.setTint(Color.parseColor(person.label.colorCode))
            } catch (e: Exception) {
                // Fallback color if parsing fails
                tvLabel.setBackgroundResource(R.drawable.label_background)
            }
            
            // Interactions count
            tvInteractions.text = "${personWithStats.totalInteractions} interactions"
            
            // Energy drain calculation (convert to hours and minutes)
            val totalEnergyMinutes = personWithStats.totalEnergyUsed
            val hours = totalEnergyMinutes / 60
            val minutes = totalEnergyMinutes % 60
            tvEnergyDrain.text = if (hours > 0) {
                "${hours}.${(minutes * 10) / 60}h"
            } else {
                "${minutes}m"
            }
            
            // Trend indicator (simplified - show up if interactions this week > average)
            val avgInteractionsPerWeek = if (personWithStats.totalInteractions > 0) {
                personWithStats.totalInteractions / 4 // rough estimate of weeks
            } else 0
            
            if (personWithStats.interactionsThisWeek > avgInteractionsPerWeek) {
                ivTrend.setImageResource(R.drawable.ic_arrow_up)
                ivTrend.visibility = View.VISIBLE
            } else if (personWithStats.interactionsThisWeek < avgInteractionsPerWeek) {
                ivTrend.setImageResource(R.drawable.ic_arrow_down)
                ivTrend.visibility = View.VISIBLE
            } else {
                ivTrend.visibility = View.GONE
            }
            
            // Social energy level
            progressSocialEnergy.progress = person.socialEnergyLevel
            tvSocialEnergyLevel.text = "${person.socialEnergyLevel}%"
            
            // Update progress bar color based on energy level
            val energyColor = when {
                person.socialEnergyLevel >= 70 -> Color.parseColor("#4CAF50") // Green
                person.socialEnergyLevel >= 40 -> Color.parseColor("#FF9800") // Orange
                else -> Color.parseColor("#F44336") // Red
            }
            progressSocialEnergy.progressDrawable.setTint(energyColor)
            
            // Mood emoji
            tvMood.text = person.mood.emoji

            // Load avatar image if available
            if (!person.avatarPath.isNullOrEmpty()) {
                try {
                    val uri = android.net.Uri.parse(person.avatarPath)
                    ivAvatar.setImageURI(uri)
                } catch (e: Exception) {
                    // If avatar can't be loaded, use default icon
                    ivAvatar.setImageResource(R.drawable.ic_person)
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person)
            }

            itemView.setOnClickListener { onItemClick(personWithStats) }
            ivMore.setOnClickListener { onMoreClick(personWithStats) }
        }
    }

    class PersonWithStatsDiffCallback : DiffUtil.ItemCallback<PersonWithStats>() {
        override fun areItemsTheSame(oldItem: PersonWithStats, newItem: PersonWithStats): Boolean {
            return oldItem.person.id == newItem.person.id
        }

        override fun areContentsTheSame(oldItem: PersonWithStats, newItem: PersonWithStats): Boolean {
            return oldItem == newItem
        }
    }
}