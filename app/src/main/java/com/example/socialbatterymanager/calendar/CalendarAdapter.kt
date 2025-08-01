package com.example.socialbatterymanager.calendar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.data.model.CalendarActivityEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarAdapter(
    private val onEventClick: (CalendarEvent) -> Unit
) : ListAdapter<CalendarEvent, CalendarAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event, onEventClick)
    }

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val timeTextView: TextView = itemView.findViewById(R.id.tvEventTime)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.tvEventDescription)
        private val peopleTextView: TextView = itemView.findViewById(R.id.tvEventPeople)
        private val energyBurnTextView: TextView = itemView.findViewById(R.id.tvEnergyBurn)
        private val colorIndicator: View = itemView.findViewById(R.id.colorIndicator)

        private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        fun bind(event: CalendarEvent, onEventClick: (CalendarEvent) -> Unit) {
            // Convert to enhanced event for display
            val activityEvent = CalendarActivityEvent.fromCalendarEvent(event)
            
            titleTextView.text = event.title
            timeTextView.text = "${timeFormat.format(Date(event.startTime))} - ${timeFormat.format(Date(event.endTime))}"
            
            // Enhanced activity information
            val energyBurn = activityEvent.calculateEnergyBurn()
            energyBurnTextView.text = String.format("%.1fh", energyBurn)
            
            // People information with smart defaults
            val peopleText = when {
                event.title.contains("meeting", ignoreCase = true) -> {
                    val peopleCount = extractPeopleCount(event.title, event.description)
                    if (peopleCount > 0) "👥 $peopleCount people" else "👥 Team meeting"
                }
                event.title.contains("lunch", ignoreCase = true) || 
                event.title.contains("coffee", ignoreCase = true) -> "👤 Close friend"
                event.title.contains("presentation", ignoreCase = true) ||
                event.title.contains("client", ignoreCase = true) -> "⚡ High stakes"
                event.title.contains("call", ignoreCase = true) -> "📞 Phone call"
                else -> "👤 Solo activity"
            }
            peopleTextView.text = peopleText
            
            // Set color indicator based on energy impact
            val colorHex = when {
                energyBurn < 0.5 -> "#4CAF50" // Green - low energy
                energyBurn < 1.5 -> "#FF9800" // Orange - medium energy
                else -> "#F44336" // Red - high energy
            }
            colorIndicator.setBackgroundColor(Color.parseColor(colorHex))
            
            // Description handling
            if (event.description.isNotEmpty()) {
                descriptionTextView.text = event.description
                descriptionTextView.visibility = View.VISIBLE
            } else {
                descriptionTextView.visibility = View.GONE
            }
            
            itemView.setOnClickListener {
                onEventClick(event)
            }
        }
        
        private fun extractPeopleCount(title: String, description: String): Int {
            // Simple heuristic to extract people count from title/description
            val text = "$title $description".lowercase()
            return when {
                text.contains("team") -> 5
                text.contains("group") -> 4
                text.contains("meeting") -> 3
                text.contains("with") -> 2
                else -> 1
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<CalendarEvent>() {
        override fun areItemsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CalendarEvent, newItem: CalendarEvent): Boolean {
            return oldItem == newItem
        }
    }
}