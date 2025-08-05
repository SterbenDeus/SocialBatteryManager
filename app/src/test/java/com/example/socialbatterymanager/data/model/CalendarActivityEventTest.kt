package com.example.socialbatterymanager.data.model

import org.junit.Test
import org.junit.Assert.*

class CalendarActivityEventTest {
    
    @Test
    fun `fromCalendarEvent creates enhanced event from basic calendar event`() {
        val calendarEvent = CalendarEvent(
            id = 1,
            title = "Team Meeting",
            description = "Weekly standup",
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + (60 * 60 * 1000L), // 1 hour later
            location = "Conference Room",
            source = "google"
        )
        
        val activityEvent = CalendarActivityEvent.fromCalendarEvent(calendarEvent)
        
        assertEquals(calendarEvent.id, activityEvent.id)
        assertEquals(calendarEvent.title, activityEvent.title)
        assertEquals(calendarEvent.description, activityEvent.description)
        assertEquals(calendarEvent.startTime, activityEvent.startTime)
        assertEquals(calendarEvent.endTime, activityEvent.endTime)
        assertEquals(calendarEvent.location, activityEvent.location)
        assertEquals(calendarEvent.source, activityEvent.source)
    }
    
    @Test
    fun `calculateEnergyBurn returns correct values for different activity types`() {
        val baseTime = System.currentTimeMillis()
        val oneHourLater = baseTime + (60 * 60 * 1000L)
        
        // Meeting activity
        val meetingEvent = CalendarActivityEvent(
            title = "Meeting",
            startTime = baseTime,
            endTime = oneHourLater,
            activityType = "meeting",
            peopleCount = 5
        )
        
        val meetingBurn = meetingEvent.calculateEnergyBurn()
        assertTrue("Meeting should have higher energy burn", meetingBurn > 1.0)
        
        // Social activity with close friend
        val socialEvent = CalendarActivityEvent(
            title = "Coffee",
            startTime = baseTime,
            endTime = oneHourLater,
            activityType = "social",
            socialLevel = "close friend"
        )
        
        val socialBurn = socialEvent.calculateEnergyBurn()
        assertTrue("Close friend activity should have lower energy burn", socialBurn < meetingBurn)
        
        // High stakes presentation
        val presentationEvent = CalendarActivityEvent(
            title = "Presentation",
            startTime = baseTime,
            endTime = oneHourLater,
            activityType = "presentation",
            peopleCount = 20
        )
        
        val presentationBurn = presentationEvent.calculateEnergyBurn()
        assertTrue("Presentation should have highest energy burn", presentationBurn > meetingBurn)
    }
    
    @Test
    fun `getPeopleDisplayText returns appropriate text for different scenarios`() {
        // With social level
        val socialEvent = CalendarActivityEvent(
            title = "Lunch",
            startTime = 0,
            endTime = 0,
            socialLevel = "Close friend"
        )
        assertEquals("Close friend", socialEvent.getPeopleDisplayText())
        
        // With people count
        val meetingEvent = CalendarActivityEvent(
            title = "Meeting",
            startTime = 0,
            endTime = 0,
            peopleCount = 5
        )
        assertEquals("5 people", meetingEvent.getPeopleDisplayText())
        
        // With people details
        val detailEvent = CalendarActivityEvent(
            title = "Call",
            startTime = 0,
            endTime = 0,
            peopleDetails = "Development team"
        )
        assertEquals("Development team", detailEvent.getPeopleDisplayText())
        
        // Solo activity (default)
        val soloEvent = CalendarActivityEvent(
            title = "Solo work",
            startTime = 0,
            endTime = 0
        )
        assertEquals("Solo activity", soloEvent.getPeopleDisplayText())
    }
    
    @Test
    fun `getColorIndicator returns correct colors for energy levels`() {
        val baseTime = System.currentTimeMillis()
        val thirtyMinutes = baseTime + (30 * 60 * 1000L)
        val oneHour = baseTime + (60 * 60 * 1000L)
        val twoHours = baseTime + (2 * 60 * 60 * 1000L)
        
        // Low energy activity (should be green)
        val lowEnergyEvent = CalendarActivityEvent(
            title = "Quick chat",
            startTime = baseTime,
            endTime = thirtyMinutes,
            activityType = "social",
            socialLevel = "close friend"
        )
        assertEquals("#4CAF50", lowEnergyEvent.getColorIndicator()) // Green
        
        // High energy activity (should be red)  
        val highEnergyEvent = CalendarActivityEvent(
            title = "Big presentation",
            startTime = baseTime,
            endTime = twoHours,
            activityType = "presentation",
            peopleCount = 50
        )
        assertEquals("#F44336", highEnergyEvent.getColorIndicator()) // Red
    }
}