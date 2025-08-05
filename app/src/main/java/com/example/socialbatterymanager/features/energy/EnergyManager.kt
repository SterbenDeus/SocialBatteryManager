package com.example.socialbatterymanager.features.energy

import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.data.model.CalendarActivityEvent
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Energy management service for calculating and tracking social battery levels
 */
class EnergyManager {
    
    companion object {
        private const val DAILY_MAX_ENERGY = 8.0 // 8 hours max daily energy
        private const val BASE_ENERGY_RATE = 1.0 // Base energy recovery rate per hour
    }
    
    /**
     * Calculate current energy level based on today's activities
     */
    fun calculateCurrentEnergyLevel(events: List<CalendarEvent>, currentTime: Long = System.currentTimeMillis()): EnergyState {
        val todayStart = getTodayStart(currentTime)
        val todayEnd = getTodayEnd(currentTime)
        
        // Filter events for today only
        val todayEvents = events.filter { event ->
            event.startTime >= todayStart && event.startTime < todayEnd
        }
        
        // Calculate total energy burned
        var totalEnergyBurned = 0.0
        var completedActivities = 0
        var plannedEnergyBurn = 0.0
        
        todayEvents.forEach { event ->
            val activityEvent = CalendarActivityEvent.fromCalendarEvent(event)
            val energyBurn = activityEvent.calculateEnergyBurn()
            
            if (event.endTime <= currentTime) {
                // Activity completed
                totalEnergyBurned += energyBurn
                completedActivities++
            } else {
                // Activity planned but not completed
                plannedEnergyBurn += energyBurn
            }
        }
        
        // Calculate remaining energy
        val remainingEnergy = (DAILY_MAX_ENERGY - totalEnergyBurned).coerceAtLeast(0.0)
        
        // Calculate energy percentage
        val energyPercentage = ((remainingEnergy / DAILY_MAX_ENERGY) * 100).toInt().coerceIn(0, 100)
        
        return EnergyState(
            currentEnergyLevel = energyPercentage,
            remainingHours = remainingEnergy,
            activitiesCount = completedActivities,
            plannedHours = plannedEnergyBurn,
            totalActivitiesToday = todayEvents.size,
            dailyMaxEnergy = DAILY_MAX_ENERGY,
            energyBurnedToday = totalEnergyBurned
        )
    }
    
    /**
     * Get recommended energy allocation for remaining activities
     */
    fun getEnergyRecommendations(energyState: EnergyState, upcomingEvents: List<CalendarEvent>): EnergyRecommendations {
        val recommendations = mutableListOf<EnergyRecommendation>()

        when {
            energyState.currentEnergyLevel >= 75 -> {
                recommendations.add(EnergyRecommendation(R.string.energy_recommendation_great))
            }
            energyState.currentEnergyLevel >= 50 -> {
                recommendations.add(EnergyRecommendation(R.string.energy_recommendation_good))
            }
            energyState.currentEnergyLevel >= 25 -> {
                recommendations.add(EnergyRecommendation(R.string.energy_recommendation_low))
            }
            else -> {
                recommendations.add(EnergyRecommendation(R.string.energy_recommendation_very_low))
            }
        }

        // Check upcoming activities
        val upcomingBurn = upcomingEvents.sumOf {
            CalendarActivityEvent.fromCalendarEvent(it).calculateEnergyBurn()
        }

        if (upcomingBurn > energyState.remainingHours) {
            recommendations.add(
                EnergyRecommendation(
                    R.string.energy_warning_planned_exceed,
                    listOf<Any>(upcomingBurn, energyState.remainingHours)
                )
            )
        }

        return EnergyRecommendations(
            recommendations = recommendations,
            warningLevel = when {
                energyState.currentEnergyLevel < 25 -> WarningLevel.HIGH
                energyState.currentEnergyLevel < 50 -> WarningLevel.MEDIUM
                else -> WarningLevel.LOW
            },
            suggestedBreakDuration = calculateSuggestedBreak(energyState)
        )
    }
    
    /**
     * Create sample data for today to demonstrate the energy tracking
     */
    fun createSampleTodayData(): List<CalendarEvent> {
        if (BuildConfig.DEBUG) {
            val todayStart = getTodayStart()
            val sampleEvents = listOf(
                CalendarEvent(
                    id = 1,
                    title = "Team Meeting",
                    description = "Weekly standup with development team",
                    startTime = todayStart + TimeUnit.HOURS.toMillis(9), // 9:00 AM
                    endTime = todayStart + TimeUnit.HOURS.toMillis(10) + TimeUnit.MINUTES.toMillis(30), // 10:30 AM
                    source = "google",
                ),
                CalendarEvent(
                    id = 2,
                    title = "Lunch with Sarah",
                    description = "Catching up with close friend",
                    startTime = todayStart + TimeUnit.HOURS.toMillis(12), // 12:00 PM
                    endTime = todayStart + TimeUnit.HOURS.toMillis(13), // 1:00 PM
                    source = "manual",
                ),
                CalendarEvent(
                    id = 3,
                    title = "Client Presentation",
                    description = "Important project presentation",
                    startTime = todayStart + TimeUnit.HOURS.toMillis(15), // 3:00 PM
                    endTime = todayStart + TimeUnit.HOURS.toMillis(16) + TimeUnit.MINUTES.toMillis(30), // 4:30 PM
                    source = "outlook",
                )
            )
            return sampleEvents
        }
        return emptyList()
    }
    
    private fun getTodayStart(currentTime: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun getTodayEnd(currentTime: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    private fun calculateSuggestedBreak(energyState: EnergyState): Int {
        return when {
            energyState.currentEnergyLevel < 25 -> 30 // 30 minutes
            energyState.currentEnergyLevel < 50 -> 15 // 15 minutes
            else -> 5 // 5 minutes
        }
    }
}

/**
 * Current energy state
 */
data class EnergyState(
    val currentEnergyLevel: Int, // Percentage 0-100
    val remainingHours: Double, // Remaining energy in hours
    val activitiesCount: Int, // Number of completed activities today
    val plannedHours: Double, // Planned energy burn for remaining activities
    val totalActivitiesToday: Int, // Total activities (completed + planned)
    val dailyMaxEnergy: Double, // Maximum daily energy capacity
    val energyBurnedToday: Double // Total energy burned today
)

/**
 * Energy recommendations and warnings
 */
data class EnergyRecommendations(
    val recommendations: List<EnergyRecommendation>,
    val warningLevel: WarningLevel,
    val suggestedBreakDuration: Int // In minutes
)

enum class WarningLevel {
    LOW, MEDIUM, HIGH
}

/**
 * Recommendation message referencing a string resource and optional format arguments
 */
data class EnergyRecommendation(
    val messageRes: Int,
    val formatArgs: List<Any> = emptyList()
)
