package com.example.socialbatterymanager.shared.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

private class FailingDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw IOException("Test failure") }
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        throw UnsupportedOperationException()
    }
}

class PreferencesManagerTest {
    private val manager = PreferencesManager(FailingDataStore())

    @Test
    fun themeFlow_emitsDefaultOnError() = runBlocking {
        assertEquals(PreferencesManager.THEME_SYSTEM, manager.themeFlow.first())
    }

    @Test
    fun onboardingCompletedFlow_emitsDefaultOnError() = runBlocking {
        assertFalse(manager.onboardingCompletedFlow.first())
    }

    @Test
    fun biometricEnabledFlow_emitsDefaultOnError() = runBlocking {
        assertFalse(manager.biometricEnabledFlow.first())
    }

    @Test
    fun notificationsEnabledFlow_emitsDefaultOnError() = runBlocking {
        assertTrue(manager.notificationsEnabledFlow.first())
    }

    @Test
    fun syncEnabledFlow_emitsDefaultOnError() = runBlocking {
        assertTrue(manager.syncEnabledFlow.first())
    }

    @Test
    fun batteryNotificationThresholdFlow_emitsDefaultOnError() = runBlocking {
        assertEquals(25, manager.batteryNotificationThresholdFlow.first())
    }
}

