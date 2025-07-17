package com.example.socialbatterymanager.model

import org.junit.Test
import org.junit.Assert.*

class ModelTest {

    @Test
    fun `EnergyLog creation with default values`() {
        val energyLog = EnergyLog(
            energyLevel = 50,
            timestamp = 1234567890L
        )
        
        assertEquals(50, energyLog.energyLevel)
        assertEquals(1234567890L, energyLog.timestamp)
        assertEquals(0, energyLog.changeAmount)
        assertNull(energyLog.reason)
    }

    @Test
    fun `EnergyLog creation with all values`() {
        val energyLog = EnergyLog(
            energyLevel = 75,
            timestamp = 1234567890L,
            changeAmount = 10,
            reason = "Energy boost"
        )
        
        assertEquals(75, energyLog.energyLevel)
        assertEquals(1234567890L, energyLog.timestamp)
        assertEquals(10, energyLog.changeAmount)
        assertEquals("Energy boost", energyLog.reason)
    }

    @Test
    fun `Activity creation with default values`() {
        val activity = Activity(
            name = "Test Activity",
            type = "Work",
            energyImpact = 5,
            people = "Team",
            mood = "😊 Happy",
            notes = "Test notes"
        )
        
        assertEquals("Test Activity", activity.name)
        assertEquals("Work", activity.type)
        assertEquals(5, activity.energyImpact)
        assertEquals("Team", activity.people)
        assertEquals("😊 Happy", activity.mood)
        assertEquals("Test notes", activity.notes)
        assertTrue(activity.date > 0)
    }

    @Test
    fun `Activity energy impact validation`() {
        val positiveActivity = Activity(
            name = "Relaxing",
            type = "Rest",
            energyImpact = 3,
            people = "Solo",
            mood = "😊 Happy",
            notes = "Recharging"
        )
        
        val negativeActivity = Activity(
            name = "Stressful Meeting",
            type = "Work",
            energyImpact = -4,
            people = "Team",
            mood = "😤 Stressed",
            notes = "Draining"
        )
        
        assertTrue(positiveActivity.energyImpact > 0)
        assertTrue(negativeActivity.energyImpact < 0)
    }
}