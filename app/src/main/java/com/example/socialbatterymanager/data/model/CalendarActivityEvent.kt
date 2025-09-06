package com.example.socialbatterymanager.data.model

/**
 * Enhanced calendar event with energy tracking capabilities
 */
data class CalendarActivityEvent(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val location: String = "",
    val source: String = "", // "google", "teams", "manual"
    val externalId: String = "",
    val isImported: Boolean = false,
    
    // Energy tracking fields
    val energyBurn: Double = 0.0, // Energy burn in hours
    val peopleCount: Int = 0,
    val socialLevel: String = "", // "Close friend", "High stakes", etc.
    val peopleDetails: String = "", // Additional people information
    val activityType: String = "", // "meeting", "social", "presentation", etc.
    val stressLevel: Int = 0, // 0-10 scale
    val energyImpact: String = "" // "low", "medium", "high"
) {
    
    companion object {
        fun fromCalendarEvent(event: CalendarEvent): CalendarActivityEvent {
            return CalendarActivityEvent(
                id = event.id,
                title = event.title,
                description = event.description,
                startTime = event.startTime,
                endTime = event.endTime,
                location = event.location,
                source = event.source,
                externalId = event.externalId,
                isImported = event.isImported
            )
        }
    }
    
    /**
     * Calculate energy burn based on duration and activity type
     */
    fun calculateEnergyBurn(): Double {
        val durationHours = (endTime.toDouble() - startTime.toDouble()) / (1000 * 60 * 60).toDouble()
        val baseRate = when (activityType.lowercase()) {
            "meeting" -> 1.5
            "presentation" -> 2.0
            "social" -> when (socialLevel.lowercase()) {
                "close friend" -> 0.5
                "high stakes" -> 2.5
                else -> 1.0
            }
            else -> 1.0
        }
        
        val peopleMultiplier = when {
            peopleCount <= 1 -> 1.0
            peopleCount <= 5 -> 1.2
            peopleCount <= 10 -> 1.5
            else -> 2.0
        }
        
        return durationHours * baseRate * peopleMultiplier
    }
    
    /**
     * Get formatted people display text
     */
    fun getPeopleDisplayText(): String {
        return when {
            socialLevel.isNotEmpty() -> socialLevel
            peopleCount > 0 -> "$peopleCount people"
            peopleDetails.isNotEmpty() -> peopleDetails
            else -> "Solo activity"
        }
    }
    
    /**
     * Get color indicator based on energy impact
     */
    fun getColorIndicator(): String {
        val burnRate = calculateEnergyBurn()
        return when {
            burnRate < 0.5 -> "#4CAF50" // Green - low energy
            burnRate < 1.5 -> "#FF9800" // Orange - medium energy  
            else -> "#F44336" // Red - high energy
        }
    }
}
