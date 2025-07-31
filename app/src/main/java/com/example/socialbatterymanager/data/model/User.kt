package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val photoUri: String? = null,
    val batteryCapacity: Int = 100,
    val warningLevel: Int = 30,
    val criticalLevel: Int = 10,
    val currentMood: String = "neutral",
    val notificationsEnabled: Boolean = true,
    val reminderFrequency: Int = 60, // minutes
    val lastUpdated: Long = System.currentTimeMillis()
)