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
    val date: Long,
    val usageCount: Int = 0,
    val rating: Float = 0.0f
)

// Extension functions to convert between Activity and ActivityEntity
fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = this.id,
        name = this.name,
        type = this.type.name,
        energy = this.energy,
        people = this.people,
        mood = this.mood,
        notes = this.notes,
        date = this.date,
        usageCount = this.usageCount,
        rating = this.rating
    )
}

fun ActivityEntity.toActivity(): Activity {
    return Activity(
        id = this.id,
        name = this.name,
        type = ActivityType.valueOf(this.type),
        energy = this.energy,
        people = this.people,
        mood = this.mood,
        notes = this.notes,
        date = this.date,
        usageCount = this.usageCount,
        rating = this.rating
    )
}