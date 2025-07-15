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
}
