package com.example.socialbatterymanager.features.notifications

import android.content.Context
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.NotificationEntity
import com.example.socialbatterymanager.data.model.NotificationType
import com.example.socialbatterymanager.di.ApplicationScope
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val preferencesManager: PreferencesManager,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private suspend fun notificationsEnabled(): Boolean =
        preferencesManager.notificationsEnabledFlow.first()
    
    fun generateSampleNotifications() {
        if (BuildConfig.DEBUG) {
            scope.launch {
                if (!notificationsEnabled()) return@launch

                // Retrieve current notifications to avoid duplicate inserts
                val existingNotifications =
                    database.notificationDao().getAllNotifications().first()

                if (existingNotifications.isEmpty()) {
                    // Generate sample notifications only when none exist
                    val notifications = listOf(
                        NotificationEntity(
                            type = NotificationType.ENERGY_LOW.name,
                            title = context.getString(R.string.notification_energy_low_title),
                            message =
                                context.getString(
                                    R.string.notification_energy_low_sample_message
                                ),
                            timestamp = System.currentTimeMillis() - (2 * 60 * 1000), // 2 minutes ago
                        ),
                        NotificationEntity(
                            type = NotificationType.BUSY_WEEK.name,
                            title = context.getString(R.string.notification_busy_week_title),
                            message =
                                context.getString(
                                    R.string.notification_busy_week_sample_message
                                ),
                            timestamp = System.currentTimeMillis() - (60 * 60 * 1000), // 1 hour ago
                        ),
                        NotificationEntity(
                            type = NotificationType.RATE_ACTIVITY.name,
                            title = context.getString(R.string.notification_rate_activity_title),
                            message =
                                context.getString(
                                    R.string.notification_rate_activity_sample_message
                                ),
                            timestamp = System.currentTimeMillis() - (3 * 60 * 60 * 1000), // 3 hours ago
                            activityId = 1, // Assuming there's an activity with ID 1
                        ),
                    )

                    notifications.forEach { notification ->
                        database.notificationDao().insertNotification(notification)
                    }
                }
            }
        }
    }

    fun checkAndGenerateEnergyLowNotification(currentEnergyLevel: Int) {
        if (currentEnergyLevel <= 30) {
            scope.launch {
                if (!notificationsEnabled()) return@launch

                val notification = NotificationEntity(
                    type = NotificationType.ENERGY_LOW.name,
                    title = context.getString(R.string.notification_energy_low_title),
                    message =
                        context.getString(
                            R.string.notification_energy_low_message,
                            currentEnergyLevel,
                        ),
                    timestamp = System.currentTimeMillis(),
                )
                database.notificationDao().insertNotification(notification)
            }
        }
    }

    fun generateActivityRatingNotification(activityId: Int, activityName: String) {
        scope.launch {
            if (!notificationsEnabled()) return@launch

            val notification = NotificationEntity(
                type = NotificationType.RATE_ACTIVITY.name,
                title = context.getString(R.string.notification_rate_activity_title),
                message =
                    context.getString(
                        R.string.notification_rate_activity_message,
                        activityName,
                    ),
                timestamp = System.currentTimeMillis(),
                activityId = activityId,
            )
            database.notificationDao().insertNotification(notification)
        }
    }

    fun generateBusyWeekNotification(activitiesCount: Int) {
        scope.launch {
            if (!notificationsEnabled()) return@launch

            val notification = NotificationEntity(
                type = NotificationType.BUSY_WEEK.name,
                title = context.getString(R.string.notification_busy_week_title),
                message =
                    context.getString(
                        R.string.notification_busy_week_message,
                        activitiesCount,
                    ),
                timestamp = System.currentTimeMillis(),
            )
            database.notificationDao().insertNotification(notification)
        }
    }
}
