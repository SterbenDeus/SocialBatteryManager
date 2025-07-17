package com.example.socialbatterymanager.data

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
    val usageCount: Int = 0,
    val rating: Float = 0.0f
)
