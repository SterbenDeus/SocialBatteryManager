package com.example.socialbatterymanager.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.room.withTransaction
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.SyncStatus
import com.example.socialbatterymanager.shared.utils.NetworkConnectivityManager
import com.example.socialbatterymanager.shared.utils.NetworkStatusProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Worker class for syncing local data with Firebase
 */
class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val database: AppDatabase = AppDatabase.getDatabase(context),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val networkManager: NetworkStatusProvider = NetworkConnectivityManager(context),
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            // Check if network is available
            if (!networkManager.isCurrentlyConnected()) {
                return Result.retry()
            }
            
            // Sync pending activities
            syncPendingActivities()
            
            // Download updates from Firebase
            downloadUpdatesFromFirebase()

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during synchronization work", e)
            FirebaseCrashlytics.getInstance().recordException(
                RuntimeException("Error during synchronization work", e)
            )
            Result.retry()
        } finally {
            networkManager.unregister()
        }
    }
    
    private suspend fun syncPendingActivities() {
        val pendingActivities = database.activityDao().getActivitiesBySyncStatus(SyncStatus.PENDING_SYNC)
        
        for (activity in pendingActivities) {
            try {
                val activityMap = mapOf(
                    "name" to activity.name,
                    "type" to activity.type,
                    "energy" to activity.energy,
                    "people" to activity.people,
                    "mood" to activity.mood,
                    "notes" to activity.notes,
                    "date" to activity.date,
                    "duration" to activity.duration,
                    "location" to activity.location,
                    "socialInteractionLevel" to activity.socialInteractionLevel,
                    "stressLevel" to activity.stressLevel,
                    "isManualEntry" to activity.isManualEntry,
                    "lastModified" to activity.lastModified
                )
                
                val documentRef = if (activity.firebaseId != null) {
                    // Update existing document
                    firestore.collection("activities").document(activity.firebaseId)
                } else {
                    // Create new document
                    firestore.collection("activities").document()
                }
                
                documentRef.set(activityMap).await()
                
                // Update local record
                database.activityDao().updateSyncStatus(activity.id, SyncStatus.SYNCED)
                if (activity.firebaseId == null) {
                    database.activityDao().updateFirebaseId(activity.id, documentRef.id)
                }
                
            } catch (e: Exception) {
                // Mark as sync error
                database.activityDao().updateSyncStatus(activity.id, SyncStatus.SYNC_ERROR)
                Log.e("SyncWorker", "Failed to sync activity ${activity.id}", e)
                FirebaseCrashlytics.getInstance().recordException(
                    RuntimeException("Failed to sync activity ${activity.id}", e)
                )
            }
        }
    }
    
    private suspend fun downloadUpdatesFromFirebase() {
        try {
            val querySnapshot = firestore.collection("activities")
                .orderBy("lastModified", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            database.withTransaction {
                for (document in querySnapshot.documents) {
                    val firebaseId = document.id
                    if (firebaseId.isBlank()) {
                        Log.e("SyncWorker", "Skipping document with missing ID")
                        continue
                    }

                    val data = document.data
                    if (data == null) {
                        Log.e("SyncWorker", "Document $firebaseId has no data")
                        continue
                    }

                    try {
                        val existingActivity = database.activityDao().getActivityByFirebaseId(firebaseId)

                        val firebaseActivity = ActivityEntity(
                            id = existingActivity?.id ?: 0,
                            name = data["name"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            energy = (data["energy"] as? Number)?.toInt() ?: 0,
                            people = data["people"] as? String ?: "",
                            mood = data["mood"] as? String ?: "",
                            notes = data["notes"] as? String ?: "",
                            date = (data["date"] as? Number)?.toLong() ?: 0L,
                            duration = (data["duration"] as? Number)?.toLong() ?: 0L,
                            location = data["location"] as? String,
                            socialInteractionLevel = (data["socialInteractionLevel"] as? Number)?.toInt() ?: 0,
                            stressLevel = (data["stressLevel"] as? Number)?.toInt() ?: 0,
                            isManualEntry = data["isManualEntry"] as? Boolean ?: true,
                            syncStatus = SyncStatus.SYNCED,
                            lastModified = (data["lastModified"] as? Number)?.toLong() ?: 0L,
                            firebaseId = firebaseId
                        )

                        if (existingActivity == null) {
                            // Insert new activity
                            database.activityDao().insertActivity(firebaseActivity)
                        } else {
                            // Update if Firebase version is newer
                            if (firebaseActivity.lastModified > existingActivity.lastModified) {
                                database.activityDao().updateActivity(firebaseActivity.copy(id = existingActivity.id))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SyncWorker", "Malformed document $firebaseId", e)
                        FirebaseCrashlytics.getInstance().recordException(
                            RuntimeException("Malformed document $firebaseId", e)
                        )
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e("SyncWorker", "Failed to download updates from Firebase", e)
            FirebaseCrashlytics.getInstance().recordException(
                RuntimeException("Failed to download updates from Firebase", e)
            )
        }
    }
}