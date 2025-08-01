package com.example.socialbatterymanager.features.energy

import com.example.socialbatterymanager.data.model.CalendarEvent
import org.junit.Test
import org.junit.Assert.*
import java.util.Calendar

class EnergyManagerTest {
    
    private val energyManager = EnergyManager()
    
    @Test
    fun `calculateCurrentEnergyLevel with no events returns full energy`() {
        val energyState = energyManager.calculateCurrentEnergyLevel(emptyList())
        
        assertEquals(100, energyState.currentEnergyLevel)
        assertEquals(8.0, energyState.remainingHours, 0.1)
        assertEquals(0, energyState.activitiesCount)
        assertEquals(0.0, energyState.plannedHours, 0.1)
    }
    
    @Test
    fun `calculateCurrentEnergyLevel with completed activities reduces energy`() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStart = today.timeInMillis
        
        // Create a completed meeting (1.5 hours energy burn)
        val completedMeeting = CalendarEvent(
            id = 1,
            title = "Team Meeting",
            description = "Weekly standup",
            startTime = todayStart + (9 * 60 * 60 * 1000), // 9:00 AM
            endTime = todayStart + (10 * 60 * 60 * 1000), // 10:00 AM
            source = "google"
        )
        
        // Test with current time after the meeting
        val currentTime = todayStart + (11 * 60 * 60 * 1000) // 11:00 AM
        val energyState = energyManager.calculateCurrentEnergyLevel(listOf(completedMeeting), currentTime)
        
        assertTrue("Energy level should be less than 100%", energyState.currentEnergyLevel < 100)
        assertEquals(1, energyState.activitiesCount)
        assertTrue("Remaining hours should be less than 8", energyState.remainingHours < 8.0)
    }
    
    @Test
    fun `calculateCurrentEnergyLevel with future activities shows planned energy`() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStart = today.timeInMillis
        
        // Create a future meeting
        val futureMeeting = CalendarEvent(
            id = 1,
            title = "Client Presentation",
            description = "Important presentation",
            startTime = todayStart + (15 * 60 * 60 * 1000), // 3:00 PM
            endTime = todayStart + (16 * 60 * 60 * 1000), // 4:00 PM
            source = "outlook"
        )
        
        // Test with current time before the meeting
        val currentTime = todayStart + (10 * 60 * 60 * 1000) // 10:00 AM
        val energyState = energyManager.calculateCurrentEnergyLevel(listOf(futureMeeting), currentTime)
        
        assertEquals(100, energyState.currentEnergyLevel) // No energy burned yet
        assertEquals(0, energyState.activitiesCount) // No completed activities
        assertTrue("Should have planned energy", energyState.plannedHours > 0)
    }
    
    @Test
    fun `createSampleTodayData returns three sample events`() {
        val sampleEvents = energyManager.createSampleTodayData()
        
        assertEquals(3, sampleEvents.size)
        
        val eventTitles = sampleEvents.map { it.title }
        assertTrue("Should contain Team Meeting", eventTitles.contains("Team Meeting"))
        assertTrue("Should contain Lunch with Sarah", eventTitles.contains("Lunch with Sarah"))
        assertTrue("Should contain Client Presentation", eventTitles.contains("Client Presentation"))
    }
    
    @Test
    fun `getEnergyRecommendations provides appropriate warnings`() {
        // Test with low energy state
        val lowEnergyState = EnergyState(
            currentEnergyLevel = 20,
            remainingHours = 1.5,
            activitiesCount = 5,
            plannedHours = 0.0,
            totalActivitiesToday = 5,
            dailyMaxEnergy = 8.0,
            energyBurnedToday = 6.5
        )
        
        val recommendations = energyManager.getEnergyRecommendations(lowEnergyState, emptyList())
        
        assertEquals(WarningLevel.HIGH, recommendations.warningLevel)
        assertTrue("Should suggest longer break for low energy", recommendations.suggestedBreakDuration >= 30)
        assertTrue("Should contain energy warning", 
            recommendations.recommendations.any { it.contains("low energy", ignoreCase = true) })
    }
}