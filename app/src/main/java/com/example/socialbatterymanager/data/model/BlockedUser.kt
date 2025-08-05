package com.example.socialbatterymanager.data.model

/**
 * Represents a user that has been blocked by another user.
 *
 * @param userId The ID of the user who performed the block.
 * @param blockedUserId The unique ID of the blocked user.
 * @param blockedUserName The display name of the blocked user.
 * @param blockedUserEmail Optional email of the blocked user.
 * @param blockedAt Timestamp of when the user was blocked.
 */
data class BlockedUser(
    val userId: String, // ID of the user who blocked someone
    val blockedUserId: String, // ID of the user that was blocked
    val blockedUserName: String, // Display name of the blocked user
    val blockedUserEmail: String? = null, // Email of the blocked user (optional)
    val blockedAt: Long = System.currentTimeMillis() // When the block occurred
)
