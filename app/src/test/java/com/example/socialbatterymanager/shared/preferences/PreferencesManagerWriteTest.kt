package com.example.socialbatterymanager.shared.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class PreferencesManagerWriteTest {

    @Test
    fun setSyncEnabled_updatesFlow() = runTest {
        val file = File.createTempFile("test", "prefs")
        val dataStore = PreferenceDataStoreFactory.create(scope = this) { file }
        val manager = PreferencesManager(dataStore)

        manager.setSyncEnabled(false)

        assertFalse(manager.syncEnabledFlow.first())
        file.delete()
    }
}

