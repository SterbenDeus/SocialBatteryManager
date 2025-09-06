package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // "ENERGY_LOW", "BUSY_WEEK", "RATE_ACTIVITY"
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val activityId: Int? = null, // For RATE_ACTIVITY notifications
    val actionData: String? = null // JSON data for additional action info
)

enum class NotificationType {
    ENERGY_LOW,
    BUSY_WEEK,
    RATE_ACTIVITY
}
