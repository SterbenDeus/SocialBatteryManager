package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityEntity): Long
    
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    @Query("SELECT * FROM activities WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    @Query("SELECT * FROM activities WHERE id = :id AND isDeleted = 0")
    suspend fun getActivityById(id: Int): ActivityEntity?

    @Query("SELECT * FROM activities WHERE firebaseId = :firebaseId AND isDeleted = 0")
    suspend fun getActivityByFirebaseId(firebaseId: String): ActivityEntity?

    
    @Query("UPDATE activities SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteActivity(id: Int, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)
    
    @Query("SELECT * FROM activities WHERE isDeleted = 0")
    suspend fun getAllActivitiesForBackup(): List<ActivityEntity>
    
    @Query("SELECT COUNT(*) FROM activities WHERE isDeleted = 0")
    suspend fun getActiveActivityCount(): Int
    
    @Query("DELETE FROM activities WHERE isDeleted = 1 AND updatedAt < :cutoff")
    suspend fun hardDeleteOldActivities(cutoff: Long)

    @Query("SELECT * FROM activities WHERE syncStatus = :status AND isDeleted = 0")
    suspend fun getActivitiesBySyncStatus(status: SyncStatus): List<ActivityEntity>

    @Query("UPDATE activities SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: SyncStatus)

    @Query("UPDATE activities SET firebaseId = :firebaseId WHERE id = :id")
    suspend fun updateFirebaseId(id: Int, firebaseId: String)

    @Query("UPDATE activities SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Int)

    @Query("SELECT * FROM activities WHERE date BETWEEN :start AND :end AND isDeleted = 0 ORDER BY date DESC")
    suspend fun getActivitiesByDateRangeSync(start: Long, end: Long): List<ActivityEntity>

    @Query("SELECT COUNT(*) FROM activities WHERE date >= :fromDate AND isDeleted = 0")
    suspend fun getActivitiesCountFromDate(fromDate: Long): Int

    @Query("SELECT COALESCE(SUM(ABS(energy)), 0) FROM activities WHERE date >= :fromDate AND isDeleted = 0")
    suspend fun getTotalEnergyUsedFromDate(fromDate: Long): Int

    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
}
