package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.UserPreferences
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the data models and basic repository functionality.
 */
class DataModelsTest {

    @Test
    fun activityEntity_creation_isCorrect() {
        val activity = ActivityEntity(
            id = 1,
            name = "Test Activity",
            type = "Social",
            energy = 5,
            people = "Friends",
            mood = "Happy",
            notes = "Test notes",
            date = System.currentTimeMillis()
        )

        assertEquals("Test Activity", activity.name)
        assertEquals("Social", activity.type)
        assertEquals(5, activity.energy)
        assertEquals("Friends", activity.people)
        assertEquals("Happy", activity.mood)
        assertEquals("Test notes", activity.notes)
        assertEquals(false, activity.isDeleted)
    }

    @Test
    fun userPreferences_defaults_areCorrect() {
        val preferences = UserPreferences()

        assertEquals(true, preferences.autoBackupEnabled)
        assertEquals(24 * 60 * 60 * 1000, preferences.backupInterval)
        assertEquals(true, preferences.encryptionEnabled)
        assertEquals(0, preferences.lastBackupTime)
        assertEquals(false, preferences.cloudBackupEnabled)
        assertEquals("CSV", preferences.exportFormat)
        assertEquals(false, preferences.darkMode)
        assertEquals(true, preferences.notificationsEnabled)
        assertEquals(30, preferences.auditLogRetentionDays)
    }

    @Test
    fun auditLog_creation_isCorrect() {
        val auditLog = com.example.socialbatterymanager.data.model.AuditLogEntity(
            id = 1,
            entityType = "activity",
            entityId = "123",
            action = "create",
            newValues = "{\"name\":\"Test\"}",
            userId = "user123"
        )

        assertEquals("activity", auditLog.entityType)
        assertEquals("123", auditLog.entityId)
        assertEquals("create", auditLog.action)
        assertEquals("{\"name\":\"Test\"}", auditLog.newValues)
        assertEquals("user123", auditLog.userId)
    }

    @Test
    fun backupData_creation_isCorrect() {
        val activities = listOf(
            ActivityEntity(
                name = "Activity 1",
                type = "Social",
                energy = 3,
                people = "Family",
                mood = "Neutral",
                notes = "Notes 1",
                date = System.currentTimeMillis()
            )
        )

        val auditLogs = listOf(
            com.example.socialbatterymanager.data.model.AuditLogEntity(
                entityType = "activity",
                entityId = "1",
                action = "create",
                newValues = "{\"name\":\"Activity 1\"}"
            )
        )

        val backupData = com.example.socialbatterymanager.data.model.BackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            activities = activities,
            auditLogs = auditLogs,
            checksum = "test_checksum"
        )

        assertEquals(1, backupData.version)
        assertEquals(1, backupData.activities.size)
        assertEquals(1, backupData.auditLogs.size)
        assertEquals("test_checksum", backupData.checksum)
    }
}