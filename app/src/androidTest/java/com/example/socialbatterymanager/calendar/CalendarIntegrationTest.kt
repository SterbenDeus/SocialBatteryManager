package com.example.socialbatterymanager.calendar

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.CalendarEvent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class CalendarIntegrationTest {
    
    private lateinit var database: AppDatabase
    private lateinit var calendarIntegration: CalendarIntegration
    
    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = AppDatabase.getDatabase(context)
        calendarIntegration = CalendarIntegration(context, database.calendarEventDao())
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun testCreateManualEvent() = runBlocking {
        val calendar = Calendar.getInstance()
        val startTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR, 1)
        val endTime = calendar.timeInMillis
        
        val event = calendarIntegration.createManualEvent(
            title = "Test Event",
            description = "Test Description",
            startTime = startTime,
            endTime = endTime,
            location = "Test Location"
        )
        
        assertEquals("Test Event", event.title)
        assertEquals("Test Description", event.description)
        assertEquals(startTime, event.startTime)
        assertEquals(endTime, event.endTime)
        assertEquals("Test Location", event.location)
        assertEquals("manual", event.source)
        assertEquals(false, event.isImported)
    }
    
    @Test
    fun testEventStorageAndRetrieval() = runBlocking {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        val eventStartTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        val eventEndTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis
        
        // Create a test event
        val event = CalendarEvent(
            title = "Test Event",
            description = "Test Description",
            startTime = eventStartTime,
            endTime = eventEndTime,
            location = "Test Location",
            source = "manual"
        )
        
        database.calendarEventDao().insert(event)
        
        // Retrieve events for the day
        val eventsForDay = database.calendarEventDao().getEventsForDay(startOfDay, endOfDay)
        
        assertTrue(eventsForDay.isNotEmpty())
        assertEquals("Test Event", eventsForDay[0].title)
    }
}