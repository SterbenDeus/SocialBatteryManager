package com.example.socialbatterymanager.data.model

/**
 * Represents a user that has been blocked by another user.
 */
data class BlockedUser(
    val userId: String,
    val blockedUserId: String,
    val blockedUserName: String,
    val blockedUserEmail: String? = null
)

