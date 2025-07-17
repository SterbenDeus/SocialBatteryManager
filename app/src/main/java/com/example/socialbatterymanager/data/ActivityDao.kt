package com.example.socialbatterymanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityEntity)
    
    @Update
    suspend fun updateActivity(activity: ActivityEntity)
    
    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
<<<<<<< HEAD
    
    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?
    
    @Query("SELECT * FROM activities WHERE syncStatus = :status")
    suspend fun getActivitiesBySyncStatus(status: SyncStatus): List<ActivityEntity>
    
    @Query("SELECT * FROM activities WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>>
    
    @Query("SELECT * FROM activities WHERE type = :type ORDER BY date DESC")
    fun getActivitiesByType(type: String): Flow<List<ActivityEntity>>
    
    @Query("SELECT * FROM activities WHERE socialInteractionLevel >= :minLevel ORDER BY date DESC")
    fun getActivitiesBySocialLevel(minLevel: Int): Flow<List<ActivityEntity>>
    
    @Query("UPDATE activities SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: SyncStatus)
    
    @Query("UPDATE activities SET firebaseId = :firebaseId WHERE id = :id")
    suspend fun updateFirebaseId(id: Int, firebaseId: String)
    
    @Query("DELETE FROM activities WHERE date < :cutoffDate")
    suspend fun deleteOldActivities(cutoffDate: Long)
    
    @Query("SELECT COUNT(*) FROM activities")
    suspend fun getActivityCount(): Int
    
    @Query("SELECT AVG(energy) FROM activities WHERE date >= :startDate")
    suspend fun getAverageEnergy(startDate: Long): Double?
    
    @Query("SELECT AVG(socialInteractionLevel) FROM activities WHERE date >= :startDate")
    suspend fun getAverageSocialLevel(startDate: Long): Double?
=======

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityEntity?
>>>>>>> copilot/fix-5
}
