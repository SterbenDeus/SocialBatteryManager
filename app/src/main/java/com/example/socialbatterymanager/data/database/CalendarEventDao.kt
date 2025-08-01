package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.socialbatterymanager.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEvent)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEvent>)
    
    @Update
    suspend fun update(event: CalendarEvent)
    
    @Delete
    suspend fun delete(event: CalendarEvent)
    
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEvent>>
    
    @Query("SELECT * FROM calendar_events WHERE startTime >= :startTime AND startTime < :endTime ORDER BY startTime ASC")
    suspend fun getEventsForDay(startTime: Long, endTime: Long): List<CalendarEvent>
    
    @Query("DELETE FROM calendar_events WHERE isImported = 1")
    suspend fun deleteImportedEvents()
}