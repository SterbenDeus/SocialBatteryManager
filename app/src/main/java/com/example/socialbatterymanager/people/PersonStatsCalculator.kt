package com.example.socialbatterymanager.people

import android.content.Context
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.model.Person
import kotlinx.coroutines.flow.first

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
        val activities = db.activityDao().getAllActivities().first()
        
        // Filter activities that mention this person
        val personActivities = activities.filter { activity ->
            activity.people.contains(person.name, ignoreCase = true)
        }
        
        val totalActivities = personActivities.size
        val averageEnergyImpact = if (personActivities.isNotEmpty()) {
            personActivities.map { it.energy }.average()
        } else {
            0.0
        }
        
        val mostCommonMood = personActivities
            .groupingBy { it.mood }
            .eachCount()
            .maxByOrNull { (_, count) -> count }
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