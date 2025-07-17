package com.example.socialbatterymanager.calendar

import com.example.socialbatterymanager.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class CalendarEventTest {
    
    @Test
    fun testCalendarEventCreation() {
        val calendar = Calendar.getInstance()
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR, 1)
        val endTime = calendar.timeInMillis
        
        val event = CalendarEvent(
            id = 1,
            title = "Test Event",
            description = "Test Description",
            startTime = startTime,
            endTime = endTime,
            location = "Test Location",
            source = "google",
            externalId = "external123",
            isImported = true
        )
        
        assertEquals(1, event.id)
        assertEquals("Test Event", event.title)
        assertEquals("Test Description", event.description)
        assertEquals(startTime, event.startTime)
        assertEquals(endTime, event.endTime)
        assertEquals("Test Location", event.location)
        assertEquals("google", event.source)
        assertEquals("external123", event.externalId)
        assertEquals(true, event.isImported)
    }
    
    @Test
    fun testCalendarEventDefaults() {
        val calendar = Calendar.getInstance()
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR, 1)
        val endTime = calendar.timeInMillis
        
        val event = CalendarEvent(
            title = "Test Event",
            startTime = startTime,
            endTime = endTime
        )
        
        assertEquals(0, event.id)
        assertEquals("Test Event", event.title)
        assertEquals("", event.description)
        assertEquals(startTime, event.startTime)
        assertEquals(endTime, event.endTime)
        assertEquals("", event.location)
        assertEquals("", event.source)
        assertEquals("", event.externalId)
        assertEquals(false, event.isImported)
    }
}