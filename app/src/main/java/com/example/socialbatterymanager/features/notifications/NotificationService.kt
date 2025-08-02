package com.example.socialbatterymanager.features.notifications

import android.content.Context
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.NotificationEntity
import com.example.socialbatterymanager.data.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class NotificationService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun generateSampleNotifications() {
        if (!BuildConfig.DEBUG) return
        scope.launch {
            // Retrieve current notifications to avoid duplicate inserts
            val existingNotifications =
                database.notificationDao().getAllNotifications().first()

            if (existingNotifications.isEmpty()) {
                // Generate sample notifications only when none exist
                val notifications = listOf(
                    NotificationEntity(
                        type = NotificationType.ENERGY_LOW.name,
                        title = context.getString(R.string.notification_energy_low_title),
                        message = context.getString(R.string.notification_energy_low_sample_message),
                        timestamp = System.currentTimeMillis() - (2 * 60 * 1000) // 2 minutes ago
                    ),
                    NotificationEntity(
                        type = NotificationType.BUSY_WEEK.name,
                        title = context.getString(R.string.notification_busy_week_title),
                        message = context.getString(R.string.notification_busy_week_sample_message),
                        timestamp = System.currentTimeMillis() - (60 * 60 * 1000) // 1 hour ago
                    ),
                    NotificationEntity(
                        type = NotificationType.RATE_ACTIVITY.name,
                        title = context.getString(R.string.notification_rate_activity_title),
                        message = context.getString(R.string.notification_rate_activity_sample_message),
                        timestamp = System.currentTimeMillis() - (3 * 60 * 60 * 1000), // 3 hours ago
                        activityId = 1 // Assuming there's an activity with ID 1
                    )
                )

                notifications.forEach { notification ->
                    database.notificationDao().insertNotification(notification)
                }
            }
        }
    }
    
    fun checkAndGenerateEnergyLowNotification(currentEnergyLevel: Int) {
        if (currentEnergyLevel <= 30) {
            scope.launch {
                val notification = NotificationEntity(
                    type = NotificationType.ENERGY_LOW.name,
                    title = context.getString(R.string.notification_energy_low_title),
                    message = context.getString(R.string.notification_energy_low_message, currentEnergyLevel),
                    timestamp = System.currentTimeMillis()
                )
                database.notificationDao().insertNotification(notification)
            }
        }
    }
    
    fun generateActivityRatingNotification(activityId: Int, activityName: String) {
        scope.launch {
            val notification = NotificationEntity(
                type = NotificationType.RATE_ACTIVITY.name,
                title = context.getString(R.string.notification_rate_activity_title),
                message = context.getString(R.string.notification_rate_activity_message, activityName),
                timestamp = System.currentTimeMillis(),
                activityId = activityId
            )
            database.notificationDao().insertNotification(notification)
        }
    }
    
    fun generateBusyWeekNotification(activitiesCount: Int) {
        scope.launch {
            val notification = NotificationEntity(
                type = NotificationType.BUSY_WEEK.name,
                title = context.getString(R.string.notification_busy_week_title),
                message = context.getString(R.string.notification_busy_week_message, activitiesCount),
                timestamp = System.currentTimeMillis()
            )
            database.notificationDao().insertNotification(notification)
        }
    }
}