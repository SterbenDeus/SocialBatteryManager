package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.socialbatterymanager.data.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityEntity)
    
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    @Query("SELECT * FROM activities WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    @Query("SELECT * FROM activities WHERE id = :id AND isDeleted = 0")
    suspend fun getActivityById(id: Int): ActivityEntity?
    
    @Query("UPDATE activities SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteActivity(id: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM activities WHERE isDeleted = 0")
    suspend fun getAllActivitiesForBackup(): List<ActivityEntity>
    
    @Query("SELECT COUNT(*) FROM activities WHERE isDeleted = 0")
    suspend fun getActiveActivityCount(): Int
    
    @Query("DELETE FROM activities WHERE isDeleted = 1 AND updatedAt < :cutoff")
    suspend fun hardDeleteOldActivities(cutoff: Long)
    
    @Query("SELECT * FROM activities WHERE date >= :startDate AND date <= :endDate AND isDeleted = 0 ORDER BY date ASC")
    suspend fun getActivitiesByDateRangeSync(startDate: Long, endDate: Long): List<ActivityEntity>
    
    @Query("UPDATE activities SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Int)

    @Query("UPDATE activities SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Int, rating: Float)
}
