package com.example.socialbatterymanager.shared.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PreferencesManagerInstrumentedTest {

    @Test
    fun setTheme_persistsValue() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val file = File(context.filesDir, "prefs_test.preferences_pb").apply { delete() }
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO),
            produceFile = { file },
        )
        val manager = PreferencesManager(dataStore)

        manager.setTheme(PreferencesManager.THEME_DARK)

        assertEquals(PreferencesManager.THEME_DARK, manager.themeFlow.first())
        file.delete()
    }
}

