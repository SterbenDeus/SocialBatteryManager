package com.example.socialbatterymanager.features.home

import com.example.socialbatterymanager.features.home.data.HomeRepository
import com.example.socialbatterymanager.data.database.*
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeActivityDao(private val count: Int) : ActivityDao {
    var receivedFromDate: Long? = null
    override suspend fun getActivitiesCountFromDate(fromDate: Long): Int {
        receivedFromDate = fromDate
        return count
    }
    override suspend fun insertActivity(activity: ActivityEntity): Long = throw NotImplementedError()
    override suspend fun updateActivity(activity: ActivityEntity) = throw NotImplementedError()
    override fun getAllActivities(): Flow<List<ActivityEntity>> = throw NotImplementedError()
    override suspend fun getActivityById(id: Int): ActivityEntity? = throw NotImplementedError()
    override suspend fun getActivityByFirebaseId(firebaseId: String): ActivityEntity? = throw NotImplementedError()
    override suspend fun softDeleteActivity(id: Int, timestamp: Long) = throw NotImplementedError()
    override suspend fun deleteActivity(activity: ActivityEntity) = throw NotImplementedError()
    override suspend fun getAllActivitiesForBackup(): List<ActivityEntity> = throw NotImplementedError()
    override suspend fun getActiveActivityCount(): Int = throw NotImplementedError()
    override suspend fun hardDeleteOldActivities(cutoff: Long) = throw NotImplementedError()
    override suspend fun getActivitiesBySyncStatus(status: SyncStatus): List<ActivityEntity> = throw NotImplementedError()
    override suspend fun updateSyncStatus(id: Int, status: SyncStatus) = throw NotImplementedError()
    override suspend fun updateFirebaseId(id: Int, firebaseId: String) = throw NotImplementedError()
    override suspend fun incrementUsageCount(id: Int) = throw NotImplementedError()
    override suspend fun getActivitiesByDateRangeSync(start: Long, end: Long): List<ActivityEntity> = throw NotImplementedError()
    override suspend fun getTotalEnergyUsedFromDate(fromDate: Long): Int = throw NotImplementedError()
    override suspend fun deleteAllActivities() = throw NotImplementedError()
}

private class FakeAppDatabase(private val activityDao: ActivityDao) : AppDatabase() {
    override fun activityDao(): ActivityDao = activityDao
    override fun auditLogDao(): AuditLogDao = throw NotImplementedError()
    override fun backupMetadataDao(): BackupMetadataDao = throw NotImplementedError()
    override fun energyLogDao(): EnergyLogDao = throw NotImplementedError()
    override fun userDao(): UserDao = throw NotImplementedError()
    override fun personDao(): PersonDao = throw NotImplementedError()
    override fun calendarEventDao(): CalendarEventDao = throw NotImplementedError()
    override fun notificationDao(): NotificationDao = throw NotImplementedError()
}

class HomeRepositoryTest {
    @Test
    fun getActivitiesCountFromDate_delegatesToDao() = runBlocking {
        val expectedCount = 5
        val fakeDao = FakeActivityDao(expectedCount)
        val repository = HomeRepository(FakeAppDatabase(fakeDao))
        val fromDate = 100L

        val result = repository.getActivitiesCountFromDate(fromDate)

        assertEquals(expectedCount, result)
        assertEquals(fromDate, fakeDao.receivedFromDate)
    }
}
