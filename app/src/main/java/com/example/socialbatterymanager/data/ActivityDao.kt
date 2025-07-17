package com.example.socialbatterymanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>
    
    @Query("SELECT * FROM activities WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getActivitiesByDateRange(startDate: Long, endDate: Long): Flow<List<ActivityEntity>>
    
    @Query("SELECT AVG(energy) FROM activities WHERE date >= :startDate AND date <= :endDate")
    suspend fun getAverageEnergyByDateRange(startDate: Long, endDate: Long): Double?
    
    @Query("SELECT * FROM activities WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getActivitiesByDateRangeSync(startDate: Long, endDate: Long): List<ActivityEntity>
}
