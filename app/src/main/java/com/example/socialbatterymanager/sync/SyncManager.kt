package com.example.socialbatterymanager.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.socialbatterymanager.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Manager class for handling sync operations
 */
class SyncManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val SYNC_WORK_NAME = "social_battery_sync"
        private const val SYNC_INTERVAL_HOURS = 4L
    }
    
    /**
     * Schedule periodic sync if sync is enabled
     */
    suspend fun schedulePeriodicSync() {
        val isSyncEnabled = preferencesManager.syncEnabledFlow.first()
        
        if (isSyncEnabled) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                SYNC_INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } else {
            cancelPeriodicSync()
        }
    }
    
    /**
     * Cancel periodic sync
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Force immediate sync
     */
    fun forceSyncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueue(syncRequest)
    }
    
    /**
     * Check if sync is currently running
     */
    suspend fun isSyncRunning(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        return workInfos.any { it.state == androidx.work.WorkInfo.State.RUNNING }
    }
}