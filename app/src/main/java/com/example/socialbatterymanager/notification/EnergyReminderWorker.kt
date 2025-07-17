package com.example.socialbatterymanager.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.AppDatabase
import kotlinx.coroutines.runBlocking

class EnergyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            runBlocking {
                val database = AppDatabase.getDatabase(context)
                val latestEnergyLog = database.energyLogDao().getLatestEnergyLog()
                
                val energyLevel = latestEnergyLog?.energyLevel ?: 65
                
                // Show notification if energy is low
                if (energyLevel < 30) {
                    showNotification(
                        "Low Social Battery",
                        "Your social battery is at $energyLevel%. Consider taking a break or doing something relaxing."
                    )
                }
                
                Result.success()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun showNotification(title: String, message: String) {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Social Battery Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders about your social battery level"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "social_battery_reminders"
        const val NOTIFICATION_ID = 1
    }
}