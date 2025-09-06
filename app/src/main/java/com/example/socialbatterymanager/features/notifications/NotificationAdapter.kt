package com.example.socialbatterymanager.features.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.NotificationEntity
import com.example.socialbatterymanager.data.model.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onNotificationClick: (NotificationEntity) -> Unit,
    private val onRateActivity: (NotificationEntity) -> Unit,
    private val onSkipRating: (NotificationEntity) -> Unit
) : ListAdapter<NotificationEntity, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivNotificationIcon = itemView.findViewById<ImageView>(R.id.ivNotificationIcon)
        private val tvNotificationTitle = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
        private val tvNotificationMessage = itemView.findViewById<TextView>(R.id.tvNotificationMessage)
        private val tvNotificationTime = itemView.findViewById<TextView>(R.id.tvNotificationTime)
        private val btnRate = itemView.findViewById<Button>(R.id.btnRate)
        private val btnSkip = itemView.findViewById<Button>(R.id.btnSkip)

        fun bind(notification: NotificationEntity) {
            tvNotificationTitle.text = notification.title
            tvNotificationMessage.text = notification.message
            
            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            tvNotificationTime.text = dateFormat.format(Date(notification.timestamp))

            // Set icon based on notification type
            when (NotificationType.valueOf(notification.type)) {
                NotificationType.ENERGY_LOW -> {
                    ivNotificationIcon.setImageResource(R.drawable.ic_battery_low)
                    ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.notification_energy_low))
                }
                NotificationType.BUSY_WEEK -> {
                    ivNotificationIcon.setImageResource(R.drawable.ic_calendar_warning)
                    ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.notification_busy_week))
                }
                NotificationType.RATE_ACTIVITY -> {
                    ivNotificationIcon.setImageResource(R.drawable.ic_star)
                    ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.notification_rate_activity))
                }
            }

            // Show action buttons only for RATE_ACTIVITY notifications
            if (NotificationType.valueOf(notification.type) == NotificationType.RATE_ACTIVITY && !notification.isRead) {
                btnRate.visibility = View.VISIBLE
                btnSkip.visibility = View.VISIBLE
                
                btnRate.setOnClickListener {
                    onRateActivity(notification)
                }
                
                btnSkip.setOnClickListener {
                    onSkipRating(notification)
                }
            } else {
                btnRate.visibility = View.GONE
                btnSkip.visibility = View.GONE
            }

            // Set background based on read status
            if (notification.isRead) {
                itemView.alpha = 0.6f
            } else {
                itemView.alpha = 1.0f
            }

            itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationEntity>() {
        override fun areItemsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationEntity, newItem: NotificationEntity): Boolean {
            return oldItem == newItem
        }
    }
}
