package com.example.socialbatterymanager.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyncManagerInstrumentedTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val prefs = mockk<PreferencesManager>(relaxed = true)

    @Test
    fun forceSyncNow_enqueuesWork() {
        val manager = SyncManager(workManager, prefs)

        manager.forceSyncNow()

        verify { workManager.enqueue(any()) }
    }
}

