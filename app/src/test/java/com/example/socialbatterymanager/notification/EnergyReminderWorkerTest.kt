package com.example.socialbatterymanager.notification

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.WorkerFactory
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.database.EnergyLogDao
import com.example.socialbatterymanager.data.model.EnergyLog
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EnergyReminderWorkerTest {

    @Test
    fun doesNotShowNotificationWhenDisabled() = runTest {
        val context = spyk(ApplicationProvider.getApplicationContext<Context>())
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        val notificationManager = mockk<NotificationManager>(relaxed = true)
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager

        val energyLogDao = mockk<EnergyLogDao>()
        coEvery { energyLogDao.getLatestEnergyLog() } returns EnergyLog(energyLevel = 20, timestamp = 0L)
        val database = mockk<AppDatabase>()
        every { database.energyLogDao() } returns energyLogDao

        val preferencesManager = mockk<PreferencesManager>()
        every { preferencesManager.notificationsEnabledFlow } returns flowOf(false)
        every { preferencesManager.batteryNotificationThresholdFlow } returns flowOf(30)

        val worker = TestListenableWorkerBuilder<EnergyReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    params: WorkerParameters
                ): ListenableWorker =
                    EnergyReminderWorker(appContext, params, database, preferencesManager)
            })
            .build()

        worker.doWork()

        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }
}

