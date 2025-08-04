package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.*
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.example.socialbatterymanager.data.model.SyncStatus
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

private class FakeActivityDao : ActivityDao {
    val activities = mutableListOf<ActivityEntity>()
    override suspend fun insertActivity(activity: ActivityEntity): Long {
        activities += activity
        return activity.id.toLong()
    }
    override suspend fun updateActivity(activity: ActivityEntity) {
        val index = activities.indexOfFirst { it.id == activity.id }
        if (index >= 0) activities[index] = activity
    }
    override fun getAllActivities(): Flow<List<ActivityEntity>> = MutableStateFlow(activities.toList())
    override suspend fun getActivityById(id: Int): ActivityEntity? = activities.find { it.id == id }
    override suspend fun softDeleteActivity(id: Int, timestamp: Long) {
        val index = activities.indexOfFirst { it.id == id }
        if (index >= 0) {
            val old = activities[index]
            activities[index] = old.copy(isDeleted = 1, updatedAt = timestamp)
        }
    }
    override suspend fun deleteActivity(activity: ActivityEntity) { activities.removeIf { it.id == activity.id } }
    override suspend fun getAllActivitiesForBackup(): List<ActivityEntity> = activities.filter { it.isDeleted == 0 }
    override suspend fun getActiveActivityCount(): Int = activities.count { it.isDeleted == 0 }
    override suspend fun hardDeleteOldActivities(cutoff: Long) {}
    override suspend fun getActivitiesBySyncStatus(status: SyncStatus): List<ActivityEntity> = activities.filter { it.syncStatus == status }
    override suspend fun updateSyncStatus(id: Int, status: SyncStatus) {}
    override suspend fun updateFirebaseId(id: Int, firebaseId: String) {}
    override suspend fun incrementUsageCount(id: Int) {}
    override suspend fun getActivitiesByDateRangeSync(start: Long, end: Long): List<ActivityEntity> = emptyList()
    override suspend fun getActivitiesCountFromDate(fromDate: Long): Int = 0
    override suspend fun getTotalEnergyUsedFromDate(fromDate: Long): Int = 0
}

private class FakeAuditLogDao : AuditLogDao {
    val logs = MutableStateFlow<List<AuditLogEntity>>(emptyList())
    override suspend fun insertAuditLog(auditLog: AuditLogEntity) {
        logs.value = logs.value + auditLog
    }
    override fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = logs
    override fun getAuditLogsForEntity(entityType: String, entityId: String): Flow<List<AuditLogEntity>> =
        MutableStateFlow(logs.value.filter { it.entityType == entityType && it.entityId == entityId })
    override fun getAuditLogsSince(since: Long): Flow<List<AuditLogEntity>> =
        MutableStateFlow(logs.value.filter { it.timestamp > since })
    override suspend fun deleteAuditLogsOlderThan(cutoff: Long) {
        logs.value = logs.value.filter { it.timestamp >= cutoff }
    }
    override suspend fun getAuditLogCount(): Int = logs.value.size
}

private class FakeBackupMetadataDao : BackupMetadataDao {
    val metadata = mutableListOf<BackupMetadataEntity>()
    override suspend fun insertBackupMetadata(metadata: BackupMetadataEntity) { this.metadata += metadata }
    override suspend fun updateBackupMetadata(metadata: BackupMetadataEntity) {}
    override fun getAllBackupMetadata(): Flow<List<BackupMetadataEntity>> = MutableStateFlow(metadata.toList())
    override suspend fun getBackupMetadataById(id: String): BackupMetadataEntity? = metadata.find { it.id == id }
    override suspend fun getLatestBackupMetadata(): BackupMetadataEntity? = metadata.maxByOrNull { it.timestamp }
    override suspend fun deleteBackupMetadata(id: String) { metadata.removeIf { it.id == id } }
}

private class FakeAppDatabase : AppDatabase() {
    val activityDao = FakeActivityDao()
    val auditLogDao = FakeAuditLogDao()
    val backupDao = FakeBackupMetadataDao()
    override fun activityDao(): ActivityDao = activityDao
    override fun auditLogDao(): AuditLogDao = auditLogDao
    override fun backupMetadataDao(): BackupMetadataDao = backupDao
    override fun energyLogDao(): EnergyLogDao = throw NotImplementedError()
    override fun userDao(): UserDao = throw NotImplementedError()
    override fun personDao(): PersonDao = throw NotImplementedError()
    override fun calendarEventDao(): CalendarEventDao = throw NotImplementedError()
    override fun notificationDao(): NotificationDao = throw NotImplementedError()
}

class DataRepositoryAuditBackupTest {
    private fun createRepository(db: FakeAppDatabase): DataRepository {
        val constructor = DataRepository::class.java.getDeclaredConstructor(AppDatabase::class.java, Gson::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(db, Gson())
    }

    @Test
    fun insertAndDeleteActivity_logsAuditEntries() = runBlocking {
        val db = FakeAppDatabase()
        val repo = createRepository(db)
        val activity = ActivityEntity(id = 1, name = "A", type = "T", energy = 1, people = "P", mood = "M", notes = "N", date = 0)

        repo.insertActivity(activity, userId = "u1")
        repo.deleteActivity(activity.id, userId = "u2")

        val logs = db.auditLogDao.logs.value
        assertEquals(2, logs.size)
        assertEquals("create", logs[0].action)
        assertEquals("delete", logs[1].action)
    }

    @Test
    fun createBackup_storesMetadata() = runBlocking {
        val db = FakeAppDatabase()
        val repo = createRepository(db)
        val activity = ActivityEntity(id = 1, name = "A", type = "T", energy = 1, people = "P", mood = "M", notes = "N", date = 0)
        repo.insertActivity(activity)

        val metadata = repo.createBackup()
        assertNotNull(metadata)
        assertEquals(1, metadata.dataCount)
        assertEquals(1, db.backupDao.metadata.size)
        assertEquals(metadata.id, db.backupDao.metadata.first().id)
    }
}
