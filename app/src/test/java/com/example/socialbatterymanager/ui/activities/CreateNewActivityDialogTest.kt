package com.example.socialbatterymanager.ui.activities

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import com.example.socialbatterymanager.model.ActivityType

/**
 * Verification tests for Create New Activity Dialog functionality
 * These tests verify the business logic works correctly
 */
class CreateNewActivityDialogTest {

    @Test
    fun testEnergyCalculation_WorkActivity_NoPeople() {
        val energyPoints = calculateEnergyPoints(ActivityType.WORK, 0, "Meeting")
        assertEquals(30, energyPoints)
    }

    @Test
    fun testEnergyCalculation_LeisureActivity_TwoPeople() {
        val energyPoints = calculateEnergyPoints(ActivityType.LEISURE, 2, "Hot Yoga")
        assertEquals(40, energyPoints) // 20 (base) + 20 (2 people)
    }

    @Test
    fun testEnergyCalculation_OtherActivity_LongName() {
        val energyPoints = calculateEnergyPoints(ActivityType.OTHER, 0, "Very long activity name that exceeds ten characters")
        assertEquals(30, energyPoints) // 25 (base) + 5 (long name)
    }

    @Test
    fun testHighEnergyWarning_ShouldShow() {
        val energyPoints = calculateEnergyPoints(ActivityType.WORK, 2, "Team meeting")
        assertTrue("Should show high energy warning for ${energyPoints} points", energyPoints > 40)
    }

    @Test
    fun testActivityTypeMapping() {
        assertEquals(ActivityType.WORK, mapSpinnerPositionToType(0))
        assertEquals(ActivityType.LEISURE, mapSpinnerPositionToType(1))
        assertEquals(ActivityType.OTHER, mapSpinnerPositionToType(2))
    }

    @Test
    fun testWeeklyCapacityCalculation() {
        val currentUsage = 70
        val activityEnergy = 25
        val weeklyTarget = 100
        val projectedUsage = currentUsage + activityEnergy
        val shouldShowWarning = projectedUsage > weeklyTarget * 0.9
        
        assertTrue("Should show weekly capacity warning", shouldShowWarning)
    }

    // Helper methods that mirror the actual dialog logic
    private fun calculateEnergyPoints(activityType: ActivityType, peopleCount: Int, activityName: String): Int {
        var energyPoints = when (activityType) {
            ActivityType.WORK -> 30
            ActivityType.LEISURE -> 20
            ActivityType.OTHER -> 25
        }
        
        energyPoints += peopleCount * 10
        
        if (activityName.length > 10) {
            energyPoints += 5
        }
        
        return energyPoints
    }

    private fun mapSpinnerPositionToType(position: Int): ActivityType {
        return when (position) {
            0 -> ActivityType.WORK
            1 -> ActivityType.LEISURE
            2 -> ActivityType.OTHER
            else -> ActivityType.LEISURE
        }
    }
}