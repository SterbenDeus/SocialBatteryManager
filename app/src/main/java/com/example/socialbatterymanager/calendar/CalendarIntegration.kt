package com.example.socialbatterymanager.calendar

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.data.model.CalendarEventDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

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
                CalendarContract.Events.EVENT_LOCATION,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME
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
                    val title = c.getString(1) ?: "Untitled Event"
                    val description = c.getString(2) ?: ""
                    val startTime = c.getLong(3)
                    val endTime = c.getLong(4)
                    val location = c.getString(5) ?: ""
                    val calendarName = c.getString(6) ?: ""
                    
                    val source = when {
                        calendarName.contains("Google", ignoreCase = true) -> "google"
                        calendarName.contains("Teams", ignoreCase = true) -> "teams"
                        calendarName.contains("Outlook", ignoreCase = true) -> "outlook"
                        else -> "device"
                    }
                    
                    val event = CalendarEvent(
                        title = title,
                        description = description,
                        startTime = startTime,
                        endTime = endTime,
                        location = location,
                        source = source,
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
    
    suspend fun importGoogleCalendarEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        // TODO: Implement Google Calendar API integration
        // This would require Google Play Services and OAuth setup
        // For now, we'll return events from device calendar that are Google-sourced
        val allEvents = calendarEventDao.getEventsBySource("google")
        allEvents
    }
    
    suspend fun importTeamsCalendarEvents(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        // TODO: Implement Microsoft Teams Calendar API integration
        // This would require Microsoft Graph API setup
        // For now, we'll return events from device calendar that are Teams-sourced
        val allEvents = calendarEventDao.getEventsBySource("teams")
        allEvents
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
    
    suspend fun clearImportedEvents(source: String) {
        calendarEventDao.deleteEventsBySource(source)
    }
}