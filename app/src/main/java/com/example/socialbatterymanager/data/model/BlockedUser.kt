package com.example.socialbatterymanager.data.model

/**
 * Represents a user that has been blocked by another user.
 *
 * @property userId The ID of the user who blocked someone.
 * @property blockedUserId The ID of the blocked user.
 * @property blockedUserName The name of the blocked user.
 * @property blockedUserEmail The email of the blocked user, if known.
 */
data class BlockedUser(
    val userId: String,
    val blockedUserId: String,
    val blockedUserName: String,
    val blockedUserEmail: String? = null
)
