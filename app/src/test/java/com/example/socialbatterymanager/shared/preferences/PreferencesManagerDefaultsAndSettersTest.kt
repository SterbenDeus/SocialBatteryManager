package com.example.socialbatterymanager.shared.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PreferencesManagerDefaultsAndSettersTest {

    @Test
    fun defaultValues_areEmittedWhenNotSet() = runTest {
        val file = File.createTempFile("test", "prefs")
        val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
        val manager = PreferencesManager(dataStore)

        assertEquals(PreferencesManager.THEME_SYSTEM, manager.themeFlow.first())
        assertFalse(manager.onboardingCompletedFlow.first())
        assertFalse(manager.biometricEnabledFlow.first())
        assertTrue(manager.notificationsEnabledFlow.first())
        assertTrue(manager.syncEnabledFlow.first())
        assertEquals(25, manager.batteryNotificationThresholdFlow.first())

        file.delete()
    }

    @Test
    fun setters_updateStoredValues() = runTest {
        val file = File.createTempFile("test", "prefs")
        val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
        val manager = PreferencesManager(dataStore)

        manager.setTheme(PreferencesManager.THEME_DARK)
        manager.setOnboardingCompleted(true)
        manager.setBiometricEnabled(true)
        manager.setNotificationsEnabled(false)
        manager.setSyncEnabled(false)
        manager.setBatteryNotificationThreshold(40)

        assertEquals(PreferencesManager.THEME_DARK, manager.themeFlow.first())
        assertTrue(manager.onboardingCompletedFlow.first())
        assertTrue(manager.biometricEnabledFlow.first())
        assertFalse(manager.notificationsEnabledFlow.first())
        assertFalse(manager.syncEnabledFlow.first())
        assertEquals(40, manager.batteryNotificationThresholdFlow.first())

        file.delete()
    }
}

