package com.example.socialbatterymanager.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.SyncStatus
import com.example.socialbatterymanager.shared.utils.NetworkStatusProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class ConnectedNetworkManager : NetworkStatusProvider {
    override fun isCurrentlyConnected(): Boolean = true
    override fun unregister() {}
}

class SyncWorkerFirebaseTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun buildWorker(vararg documents: DocumentSnapshot): SyncWorker {
        val firestore = mockk<FirebaseFirestore>()
        val collection = mockk<CollectionReference>()
        val query = mockk<Query>()
        val snapshot = mockk<QuerySnapshot>()

        every { firestore.collection("activities") } returns collection
        every { collection.orderBy(any<String>(), any()) } returns query
        every { query.limit(any()) } returns query
        every { query.get() } returns Tasks.forResult(snapshot)
        every { snapshot.documents } returns documents.toList()

        val network = ConnectedNetworkManager()

        val factory = object : androidx.work.WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return SyncWorker(appContext, workerParameters, db, firestore, network)
            }
        }

        return TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(factory)
            .build()
    }

    private fun createDocument(id: String, data: Map<String, Any?>?): DocumentSnapshot {
        val doc = mockk<DocumentSnapshot>()
        every { doc.id } returns id
        every { doc.data } returns data
        return doc
    }

    @Test
    fun insertsDocumentUsingDocumentIdWhenDataIdMissing() = runTest {
        val doc = createDocument(
            id = "firebase123",
            data = mapOf(
                "name" to "Test",
                "type" to "Type",
                "energy" to 5,
                "people" to "",
                "mood" to "",
                "notes" to "",
                "date" to 1L,
                "duration" to 10L,
                "socialInteractionLevel" to 1,
                "stressLevel" to 2,
                "isManualEntry" to true,
                "lastModified" to 1L
            )
        )

        val worker = buildWorker(doc)
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)

        val activities = db.activityDao().getAllActivitiesForBackup()
        assertEquals(1, activities.size)
        assertEquals("firebase123", activities[0].firebaseId)
    }

    @Test
    fun skipsDocumentWhenIdMissing() = runTest {
        val doc = createDocument(
            id = "",
            data = mapOf(
                "name" to "Test",
                "type" to "Type",
                "energy" to 5,
                "people" to "",
                "mood" to "",
                "notes" to "",
                "date" to 1L,
                "duration" to 10L,
                "socialInteractionLevel" to 1,
                "stressLevel" to 2,
                "isManualEntry" to true,
                "lastModified" to 1L
            )
        )

        val worker = buildWorker(doc)
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)

        val activities = db.activityDao().getAllActivitiesForBackup()
        assertTrue(activities.isEmpty())
    }

    @Test
    fun softDeletesLocalActivityMissingFromFirebase() = runTest {
        val activity = ActivityEntity(
            name = "Local",
            type = "Type",
            energy = 5,
            people = "",
            mood = "",
            notes = "",
            date = 1L,
            firebaseId = "missing",
            syncStatus = SyncStatus.SYNCED
        )
        db.activityDao().insertActivity(activity)

        val worker = buildWorker()
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)

        val cursor = db.query("SELECT isDeleted FROM activities WHERE firebaseId = ?", arrayOf<Any>("missing"))
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()
    }

    @Test
    fun softDeletesLocalActivityWhenRemoteMarkedDeleted() = runTest {
        val activity = ActivityEntity(
            name = "Local",
            type = "Type",
            energy = 5,
            people = "",
            mood = "",
            notes = "",
            date = 1L,
            firebaseId = "remote1",
            syncStatus = SyncStatus.SYNCED
        )
        db.activityDao().insertActivity(activity)

        val doc = createDocument(
            id = "remote1",
            data = mapOf(
                "name" to "Remote",
                "type" to "Type",
                "energy" to 5,
                "people" to "",
                "mood" to "",
                "notes" to "",
                "date" to 1L,
                "duration" to 10L,
                "socialInteractionLevel" to 1,
                "stressLevel" to 2,
                "isManualEntry" to true,
                "lastModified" to 1L,
                "isDeleted" to 1
            )
        )

        val worker = buildWorker(doc)
        val result = worker.doWork()
        assertTrue(result is ListenableWorker.Result.Success)

        val cursor = db.query("SELECT isDeleted FROM activities WHERE firebaseId = ?", arrayOf<Any>("remote1"))
        assertTrue(cursor.moveToFirst())
        assertEquals(1, cursor.getInt(0))
        cursor.close()
    }
}
