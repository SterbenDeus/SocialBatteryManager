package com.example.socialbatterymanager.features.people.data

import android.content.Context
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person

data class PersonStats(
    val person: Person,
    val totalActivities: Int,
    val averageEnergyImpact: Double,
    val mostCommonMood: String,
    val lastInteraction: Long?,
    val energyTrend: String // "positive", "negative", "neutral"
)

class PersonStatsCalculator(private val context: Context) {
    
    suspend fun calculateStats(person: Person): PersonStats {
        val db = AppDatabase.getDatabase(context)
        val personActivities = db.activityDao().getActivitiesByPersonName(person.name)
        
        val totalActivities = personActivities.size
        val averageEnergyImpact: Double = if (personActivities.isNotEmpty()) {
            personActivities.map { it.energy }.average()
        } else {
            0.0
        }
        
        val mostCommonMood = personActivities
            .groupingBy { it.mood }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "Unknown"
        
        val lastInteraction = personActivities.maxOfOrNull { it.date }
        
        val energyTrend = when {
            averageEnergyImpact > 0.5 -> "positive"
            averageEnergyImpact < -0.5 -> "negative"
            else -> "neutral"
        }
        
        return PersonStats(
            person = person,
            totalActivities = totalActivities,
            averageEnergyImpact = averageEnergyImpact,
            mostCommonMood = mostCommonMood,
            lastInteraction = lastInteraction,
            energyTrend = energyTrend
        )
    }
}
