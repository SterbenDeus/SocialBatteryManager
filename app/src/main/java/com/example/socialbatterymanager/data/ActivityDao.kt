package com.example.socialbatterymanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityEntity)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?

    @Query("SELECT * FROM activities WHERE type = :type ORDER BY date DESC")
    fun getActivitiesByType(type: String): Flow<List<ActivityEntity>>

    @Query("UPDATE activities SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Int)

    @Query("UPDATE activities SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Int, rating: Float)
}
