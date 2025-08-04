package com.example.socialbatterymanager.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.shared.utils.NetworkStatusProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private class FakeNetworkManager(var connected: Boolean) : NetworkStatusProvider {
    var unregistered = false
    override fun isCurrentlyConnected(): Boolean = connected
    override fun unregister() { unregistered = true }
}

@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(
            context,
            FirebaseOptions.Builder()
                .setProjectId("test")
                .setApplicationId("1:1:test")
                .setApiKey("test")
                .build()
        )
    }

    @After
    fun tearDown() {
        FirebaseApp.clearInstancesForTest()
        AppDatabase.clearInstance()
    }

    @Test
    fun workerRetriesWhenOffline() {
        val network = FakeNetworkManager(false)
        val db = AppDatabase.getDatabase(context)
        val firestore = FirebaseFirestore.getInstance()

        val factory = object : WorkerFactory() {
            override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): androidx.work.ListenableWorker? {
                return if (workerClassName == SyncWorker::class.java.name) {
                    SyncWorker(appContext, workerParameters, db, firestore, network)
                } else {
                    null
                }
            }
        }

        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(factory)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get(10, TimeUnit.SECONDS)
        val info = workManager.getWorkInfoById(request.id).get()

        assertEquals(WorkInfo.State.RETRY, info.state)
        assertTrue(network.unregistered)
    }
}
