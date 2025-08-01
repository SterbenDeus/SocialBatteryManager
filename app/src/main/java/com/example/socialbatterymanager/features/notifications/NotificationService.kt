package com.example.socialbatterymanager.features.notifications

import android.content.Context
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.NotificationEntity
import com.example.socialbatterymanager.data.model.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService(private val context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun generateSampleNotifications() {
        scope.launch {
            // Check if we already have notifications
            val existingNotifications = database.notificationDao().getAllNotifications()
            
            // Generate sample notifications
            val notifications = listOf(
                NotificationEntity(
                    type = NotificationType.ENERGY_LOW.name,
                    title = "Energy Running Low",
                    message = "Your social energy is at 25%. Time to recharge!",
                    timestamp = System.currentTimeMillis() - (2 * 60 * 1000) // 2 minutes ago
                ),
                NotificationEntity(
                    type = NotificationType.BUSY_WEEK.name,
                    title = "Busy Week Ahead",
                    message = "You have 6 social activities scheduled. Plan recovery time!",
                    timestamp = System.currentTimeMillis() - (60 * 60 * 1000) // 1 hour ago
                ),
                NotificationEntity(
                    type = NotificationType.RATE_ACTIVITY.name,
                    title = "Rate Your Activity",
                    message = "How was your team meeting? Help us track your energy better.",
                    timestamp = System.currentTimeMillis() - (3 * 60 * 60 * 1000), // 3 hours ago
                    activityId = 1 // Assuming there's an activity with ID 1
                )
            )
            
            notifications.forEach { notification ->
                database.notificationDao().insertNotification(notification)
            }
        }
    }
    
    fun checkAndGenerateEnergyLowNotification(currentEnergyLevel: Int) {
        if (currentEnergyLevel <= 30) {
            scope.launch {
                val notification = NotificationEntity(
                    type = NotificationType.ENERGY_LOW.name,
                    title = "Energy Running Low",
                    message = "Your social energy is at $currentEnergyLevel%. Consider taking a break!",
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
                title = "Rate Your Activity",
                message = "How was $activityName? Help us track your energy better.",
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
                title = "Busy Week Ahead",
                message = "You have $activitiesCount social activities scheduled. Plan recovery time!",
                timestamp = System.currentTimeMillis()
            )
            database.notificationDao().insertNotification(notification)
        }
    }
}