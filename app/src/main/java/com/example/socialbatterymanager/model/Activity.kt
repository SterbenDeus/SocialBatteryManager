package com.example.socialbatterymanager.model

import com.example.socialbatterymanager.data.ActivityEntity

enum class ActivityType {
    WORK,
    SOCIAL
}

data class Activity(
    val id: Int = 0,
    val name: String,
    val type: ActivityType,
    val energy: Int,
    val people: String,
    val mood: String,
    val notes: String,
    val date: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val rating: Float = 0.0f
)

// Extension functions to convert between Activity and ActivityEntity
fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        name = name,
        type = type.name,
        energy = energy,
        people = people,
        mood = mood,
        notes = notes,
        date = date,
        usageCount = usageCount,
        rating = rating
    )
}

fun ActivityEntity.toActivity(): Activity {
    return Activity(
        id = id,
        name = name,
        type = ActivityType.valueOf(type),
        energy = energy,
        people = people,
        mood = mood,
        notes = notes,
        date = date,
        usageCount = usageCount,
        rating = rating
    )
}
