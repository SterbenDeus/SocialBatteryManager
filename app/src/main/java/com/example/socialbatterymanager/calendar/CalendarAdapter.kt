package com.example.socialbatterymanager.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.CalendarEvent
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
        private val sourceTextView: TextView = itemView.findViewById(R.id.tvEventSource)

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(event: CalendarEvent, onEventClick: (CalendarEvent) -> Unit) {
            titleTextView.text = event.title
            timeTextView.text = "${timeFormat.format(Date(event.startTime))} - ${timeFormat.format(Date(event.endTime))}"
            
            if (event.description.isNotEmpty()) {
                descriptionTextView.text = event.description
                descriptionTextView.visibility = View.VISIBLE
            } else {
                descriptionTextView.visibility = View.GONE
            }
            
            sourceTextView.text = event.source.takeIf { it.isNotEmpty() } ?: "Manual"
            
            itemView.setOnClickListener {
                onEventClick(event)
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