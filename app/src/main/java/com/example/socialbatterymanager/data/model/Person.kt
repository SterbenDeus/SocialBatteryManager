package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val avatarPath: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val label: PersonLabel = PersonLabel.FRIEND,
    val socialEnergyLevel: Int = 50, // 0-100 scale
    val mood: PersonMood = PersonMood.NEUTRAL
)

enum class PersonLabel(val displayName: String, val colorCode: String) {
    CLOSE_FRIEND("Close Friend", "#4CAF50"),
    FRIEND("Friend", "#2196F3"),
    COWORKER("Coworker", "#FF9800"),
    ACQUAINTANCE("Acquaintance", "#9C27B0"),
    FAMILY("Family", "#E91E63")
}

enum class PersonMood(val emoji: String) {
    VERY_HAPPY("😄"),
    HAPPY("😊"),
    NEUTRAL("😐"),
    SAD("😔"),
    VERY_SAD("😢"),
    ANGRY("😠"),
    EXCITED("🤩"),
    TIRED("😴")
}
