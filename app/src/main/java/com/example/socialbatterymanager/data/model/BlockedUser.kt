package com.example.socialbatterymanager.data.model

data class BlockedUser(
    val userId: String,
    val blockedUserId: String,
    val blockedUserName: String,
    val blockedUserEmail: String?
)
