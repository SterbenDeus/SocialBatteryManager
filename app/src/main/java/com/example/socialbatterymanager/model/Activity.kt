package com.example.socialbatterymanager.model

import com.example.socialbatterymanager.data.model.ActivityEntity

enum class ActivityType {
    WORK,
    LEISURE,
    OTHER
}

data class Activity(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val type: ActivityType,
    val energy: Int,
    val people: String,
    val mood: String,
    val notes: String,
    val date: Long = System.currentTimeMillis(),
    val startTime: String = "",
    val endTime: String = "",
    val usageCount: Int = 0,
    val rating: Float = 0.0f
)

// Extension functions to convert between Activity and ActivityEntity
fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        name = name,
        description = description,
        type = type.name,
        energy = energy,
        people = people,
        mood = mood,
        notes = notes,
        date = date,
        startTime = startTime,
        endTime = endTime,
        usageCount = usageCount,
        rating = rating
    )
}

fun ActivityEntity.toActivity(): Activity {
    return Activity(
        id = id,
        name = name,
        description = description ?: "",
        type = ActivityType.valueOf(type),
        energy = energy,
        people = people,
        mood = mood,
        notes = notes,
        date = date,
        startTime = startTime ?: "",
        endTime = endTime ?: "",
        usageCount = usageCount,
        rating = rating
    )
}
