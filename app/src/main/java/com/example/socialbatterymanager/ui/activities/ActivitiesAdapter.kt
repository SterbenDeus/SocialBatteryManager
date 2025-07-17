package com.example.socialbatterymanager.ui.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.ActivityEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitiesAdapter : ListAdapter<ActivityEntity, ActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = getItem(position)
        holder.bind(activity)
    }

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvType: TextView = itemView.findViewById(R.id.tvType)
        private val tvEnergy: TextView = itemView.findViewById(R.id.tvEnergy)
        private val tvPeople: TextView = itemView.findViewById(R.id.tvPeople)
        private val tvMood: TextView = itemView.findViewById(R.id.tvMood)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)

        fun bind(activity: ActivityEntity) {
            tvName.text = activity.name
            tvType.text = activity.type
            tvEnergy.text = activity.energy.toString()
            tvPeople.text = activity.people
            tvMood.text = activity.mood
            tvNotes.text = activity.notes
            
            // You can add click listeners here for edit/delete functionality
        }
    }

    class ActivityDiffCallback : DiffUtil.ItemCallback<ActivityEntity>() {
        override fun areItemsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ActivityEntity, newItem: ActivityEntity): Boolean {
            return oldItem == newItem
        }
    }
}