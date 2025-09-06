package com.example.socialbatterymanager.sync

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SyncManagerTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val preferences = mockk<PreferencesManager>()

    @Test
    fun schedulePeriodicSync_enqueuesWhenEnabled() = runBlocking {
        every { preferences.syncEnabledFlow } returns flowOf(true)
        val manager = SyncManager(workManager, preferences)

        manager.schedulePeriodicSync()

        verify { workManager.enqueueUniquePeriodicWork(any(), ExistingPeriodicWorkPolicy.KEEP, any()) }
    }

    @Test
    fun schedulePeriodicSync_cancelsWhenDisabled() = runBlocking {
        every { preferences.syncEnabledFlow } returns flowOf(false)
        val manager = SyncManager(workManager, preferences)

        manager.schedulePeriodicSync()

        verify { workManager.cancelUniqueWork(any()) }
    }

    @Test
    fun forceSyncNow_enqueuesWork() {
        val manager = SyncManager(workManager, preferences)

        manager.forceSyncNow()

        verify { workManager.enqueue(any()) }
    }

    @Test
    fun schedulePeriodicSync_enqueuesWithCorrectNameAndPolicy() = runBlocking {
        val workManager = mockk<WorkManager>(relaxed = true)
        val preferences = mockk<PreferencesManager>()
        every { preferences.syncEnabledFlow } returns flowOf(true)
        val manager = SyncManager(context, workManager, preferences)

        manager.schedulePeriodicSync()

        verify {
            workManager.enqueueUniquePeriodicWork(
                "social_battery_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                any()
            )
        }
    }

    @Test
    fun schedulePeriodicSync_cancelsAndDoesNotEnqueueWhenDisabled() = runBlocking {
        val workManager = mockk<WorkManager>(relaxed = true)
        val preferences = mockk<PreferencesManager>()
        every { preferences.syncEnabledFlow } returns flowOf(false)
        val manager = SyncManager(context, workManager, preferences)

        manager.schedulePeriodicSync()

        verify { workManager.cancelUniqueWork("social_battery_sync") }
        verify(exactly = 0) { workManager.enqueueUniquePeriodicWork(any(), any(), any()) }
    }
}

