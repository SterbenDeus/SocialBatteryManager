package com.example.socialbatterymanager.data.model

/**
 * Represents a user's privacy configuration within the app.
 */
data class UserPrivacySettings(
    val userId: String,
    val moodVisibilityLevel: VisibilityLevel,
    val moodStatusEnabled: Boolean,
    val energyLevelEnabled: Boolean,
    val activityPatternsEnabled: Boolean
)

