package com.example.socialbatterymanager.profile

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.model.User
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ProfileTest {

    @Test
    fun testUserModelCreation() {
        val user = User(
            id = "test_id",
            name = "Test User",
            email = "test@example.com",
            batteryCapacity = 80,
            warningLevel = 25,
            currentMood = "happy"
        )

        assertEquals("test_id", user.id)
        assertEquals("Test User", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals(80, user.batteryCapacity)
        assertEquals(25, user.warningLevel)
        assertEquals("happy", user.currentMood)
        assertTrue(user.notificationsEnabled) // Default value
    }

    @Test
    fun testUserDefaultValues() {
        val user = User(
            id = "test_id",
            name = "Test User",
            email = "test@example.com"
        )

        assertEquals(100, user.batteryCapacity)
        assertEquals(30, user.warningLevel)
        assertEquals(10, user.criticalLevel)
        assertEquals("neutral", user.currentMood)
        assertTrue(user.notificationsEnabled)
        assertEquals(60, user.reminderFrequency)
    }
}