package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_settings")
data class PrivacySettings(
    @PrimaryKey val userId: String,
    val moodVisibilityLevel: VisibilityLevel = VisibilityLevel.FRIENDS_ONLY,
    val energyVisibilityLevel: VisibilityLevel = VisibilityLevel.FRIENDS_ONLY,
    val moodStatusEnabled: Boolean = true,
    val energyLevelEnabled: Boolean = true,
    val activityPatternsEnabled: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class VisibilityLevel(val displayName: String) {
    EVERYONE("Everyone"),
    FRIENDS_ONLY("Friends Only"),
    CLOSE_FRIENDS("Close Friends"),
    ONLY_ME("Only Me")
}

@Entity(tableName = "blocked_users")
data class BlockedUser(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String, // Current user's ID
    val blockedUserId: String, // ID of the blocked user
    val blockedUserName: String, // Name for display
    val blockedUserEmail: String? = null, // Email for identification
    val blockedAt: Long = System.currentTimeMillis()
)