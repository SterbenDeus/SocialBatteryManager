package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val location: String = "",
    val source: String = "", // "google", "teams", "manual"
    val externalId: String = "", // ID from external calendar
    val isImported: Boolean = false
)