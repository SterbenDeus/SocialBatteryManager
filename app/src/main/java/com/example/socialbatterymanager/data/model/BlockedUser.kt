package com.example.socialbatterymanager.data.model

data class BlockedUser(
    val userId: String, // ID of the user who blocked someone
    val blockedUserId: String, // ID of the user that was blocked
    val blockedUserName: String, // Display name of the blocked user
    val blockedUserEmail: String? = null, // Email of the blocked user (optional)
    val blockedAt: Long = System.currentTimeMillis() // When the block occurred
)
