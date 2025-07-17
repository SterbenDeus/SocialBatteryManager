package com.example.socialbatterymanager.model

import com.example.socialbatterymanager.data.ActivityEntity
import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test for Activity model.
 */
class ActivityTest {
    @Test
    fun testActivityToEntityConversion() {
        val activity = Activity(
            id = 1,
            name = "Test Activity",
            type = ActivityType.WORK,
            energy = 5,
            people = "Team",
            mood = "Happy",
            notes = "Test notes",
            date = 1234567890L,
            usageCount = 2,
            rating = 4.5f
        )
        
        val entity = activity.toEntity()
        
        assertEquals(activity.id, entity.id)
        assertEquals(activity.name, entity.name)
        assertEquals(activity.type.name, entity.type)
        assertEquals(activity.energy, entity.energy)
        assertEquals(activity.people, entity.people)
        assertEquals(activity.mood, entity.mood)
        assertEquals(activity.notes, entity.notes)
        assertEquals(activity.date, entity.date)
        assertEquals(activity.usageCount, entity.usageCount)
        assertEquals(activity.rating, entity.rating)
    }
    
    @Test
    fun testEntityToActivityConversion() {
        val entity = ActivityEntity(
            id = 1,
            name = "Test Activity",
            type = "SOCIAL",
            energy = 3,
            people = "Friends",
            mood = "Excited",
            notes = "Fun activity",
            date = 1234567890L,
            usageCount = 1,
            rating = 5.0f
        )
        
        val activity = entity.toActivity()
        
        assertEquals(entity.id, activity.id)
        assertEquals(entity.name, activity.name)
        assertEquals(ActivityType.SOCIAL, activity.type)
        assertEquals(entity.energy, activity.energy)
        assertEquals(entity.people, activity.people)
        assertEquals(entity.mood, activity.mood)
        assertEquals(entity.notes, activity.notes)
        assertEquals(entity.date, activity.date)
        assertEquals(entity.usageCount, activity.usageCount)
        assertEquals(entity.rating, activity.rating)
    }
}