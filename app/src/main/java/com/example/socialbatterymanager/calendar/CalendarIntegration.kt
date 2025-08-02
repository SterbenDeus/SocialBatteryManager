package com.example.socialbatterymanager.calendar

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.data.database.CalendarEventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles interactions with the user's calendars.
 *
 * Only events available on the device's local calendar can be imported. Direct
 * API integrations for services such as Google Calendar or Microsoft Teams are
 * not supported.
 */
class CalendarIntegration(
    private val context: Context,
    private val calendarEventDao: CalendarEventDao
) {
    
    suspend fun importDeviceCalendarEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val events = mutableListOf<CalendarEvent>()
        
        try {
            // Query device calendar for events
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.EVENT_LOCATION
            )
            
            val cursor = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                null,
                null,
                "${CalendarContract.Events.DTSTART} ASC"
            )
            
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val externalId = c.getString(0) ?: ""
                    val title = c.getString(1) ?: context.getString(R.string.untitled_event)
                    val description = c.getString(2) ?: ""
                    val startTime = c.getLong(3)
                    val endTime = c.getLong(4)
                    val location = c.getString(5) ?: ""
                    
                    val event = CalendarEvent(
                        title = title,
                        description = description,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        source = "device",
                        externalId = externalId,
                        isImported = true
                    )

                    events.add(event)
                }
            }
            
            // Store imported events in local database
            calendarEventDao.insertAll(events)
            
        } catch (e: SecurityException) {
            Log.w("CalendarIntegration", "Permission denied for calendar access", e)
        } catch (e: Exception) {
            Log.e("CalendarIntegration", "Error importing calendar events", e)
        }
        
        events
    }
    
    suspend fun createManualEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        location: String = ""
    ): CalendarEvent {
        val event = CalendarEvent(
            title = title,
            description = description,
            startTime = startTime,
            endTime = endTime,
            location = location,
            source = "manual",
            isImported = false
        )
        
        calendarEventDao.insert(event)
        return event
    }
    
    suspend fun deleteEvent(event: CalendarEvent) {
        calendarEventDao.delete(event)
    }
    
    suspend fun clearImportedEvents() {
        calendarEventDao.deleteImportedEvents()
    }
}
