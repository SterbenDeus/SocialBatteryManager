package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String,
    val energy: Int,
    val people: String,
    val mood: String,
    val notes: String,
    val date: Long,
    val duration: Long = 0, // Duration in minutes
    val location: String? = null,
    val socialInteractionLevel: Int = 0, // 0-10 scale
    val stressLevel: Int = 0, // 0-10 scale
    val isManualEntry: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
    val lastModified: Long = System.currentTimeMillis(),
    val firebaseId: String? = null, // For Firebase sync
    val isDeleted: Int = 0, // Soft delete flag (0 = active, 1 = deleted)
    val updatedAt: Long = System.currentTimeMillis(), // Last update timestamp
    val rating: Float = 0.0f, // User rating for the activity (0.0 - 5.0)
    val usageCount: Int = 0 // How many times this activity has been selected
)

enum class SyncStatus {
    PENDING_SYNC,
    SYNCED,
    SYNC_ERROR
}
