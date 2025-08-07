package com.example.socialbatterymanager.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupData
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import io.mockk.mockk
import io.mockk.verify
import io.mockk.any
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class BackupManagerRestoreTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var activityRepo: ActivityRepository
    private lateinit var auditRepo: AuditRepository
    private lateinit var backupRepo: BackupRepository
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var backupManager: BackupManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val gson = Gson()
        auditRepo = AuditRepository(db.auditLogDao(), gson)
        activityRepo = ActivityRepository(db.activityDao(), auditRepo)
        backupRepo = BackupRepository(db.activityDao(), db.auditLogDao(), db.backupMetadataDao(), gson)
        val prefs = mockk<PreferencesManager>(relaxed = true)
        val firestore = mockk<FirebaseFirestore>(relaxed = true)
        crashlytics = mockk(relaxed = true)
        backupManager = BackupManager(
            context,
            activityRepo,
            auditRepo,
            backupRepo,
            prefs,
            db,
            firestore,
            gson,
            crashlytics
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun restoreFromLocalBackup_success() = runBlocking {
        activityRepo.insertActivityRaw(
            ActivityEntity(name = "old", type = "t", energy = 1, people = "p", mood = "m", notes = "n", date = 0)
        )
        auditRepo.insertAuditLogRaw(
            AuditLogEntity(entityType = "activity", entityId = "1", action = "create")
        )

        val newActivity = ActivityEntity(id = 2, name = "new", type = "t", energy = 2, people = "p", mood = "m", notes = "n", date = 1)
        val newAudit = AuditLogEntity(entityType = "activity", entityId = "2", action = "create")
        val backupData = BackupData(1, 0L, listOf(newActivity), listOf(newAudit), "chk")
        val backupId = "test"
        val file = File(context.getExternalFilesDir(null), "backup_${backupId}.json")
        file.writeText(Gson().toJson(backupData))

        val result = backupManager.restoreFromLocalBackup(backupId)
        assertTrue(result)
        val activities = activityRepo.getAllActivities().first()
        assertEquals(1, activities.size)
        assertEquals("new", activities[0].name)
        val logs = auditRepo.getAllAuditLogs().first()
        assertEquals(1, logs.size)
        assertEquals("2", logs[0].entityId)
    }

    @Test
    fun restoreFromLocalBackup_failure_logsException() = runBlocking {
        val backupId = "bad"
        val file = File(context.getExternalFilesDir(null), "backup_${backupId}.json")
        file.writeText("{ bad json")

        val result = backupManager.restoreFromLocalBackup(backupId)
        assertFalse(result)
        verify { crashlytics.recordException(any()) }
    }
}
