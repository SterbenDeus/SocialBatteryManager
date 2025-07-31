package com.example.socialbatterymanager.home

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.data.ActivityEntity
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.data.model.EnergyLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HomeFunctionalityTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadActivity() = runBlocking {
        val activityDao = database.activityDao()
        val activity = ActivityEntity(
            name = "Test Activity",
            type = "Work",
            energy = 5,
            people = "Team",
            mood = "😊 Happy",
            notes = "Test notes",
            date = System.currentTimeMillis()
        )
        
        activityDao.insertActivity(activity)
        val activities = activityDao.getAllActivities().first()
        
        assertEquals(1, activities.size)
        assertEquals("Test Activity", activities[0].name)
        assertEquals(5, activities[0].energy)
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadEnergyLog() = runBlocking {
        val energyLogDao = database.energyLogDao()
        val energyLog = EnergyLog(
            energyLevel = 75,
            timestamp = System.currentTimeMillis(),
            changeAmount = 5,
            reason = "Test energy change"
        )
        
        energyLogDao.insertEnergyLog(energyLog)
        val latest = energyLogDao.getLatestEnergyLog()
        
        assertNotNull(latest)
        assertEquals(75, latest!!.energyLevel)
        assertEquals(5, latest.changeAmount)
    }

    @Test
    @Throws(Exception::class)
    fun updateAndDeleteActivity() = runBlocking {
        val activityDao = database.activityDao()
        val activity = ActivityEntity(
            name = "Original",
            type = "Work",
            energy = 3,
            people = "Solo",
            mood = "😐 Neutral",
            notes = "Original notes",
            date = System.currentTimeMillis()
        )
        
        activityDao.insertActivity(activity)
        val inserted = activityDao.getAllActivities().first()[0]
        
        val updated = inserted.copy(name = "Updated", energy = 4)
        activityDao.updateActivity(updated)
        
        val afterUpdate = activityDao.getAllActivities().first()
        assertEquals(1, afterUpdate.size)
        assertEquals("Updated", afterUpdate[0].name)
        assertEquals(4, afterUpdate[0].energy)
        
        activityDao.deleteActivity(afterUpdate[0])
        val afterDelete = activityDao.getAllActivities().first()
        assertEquals(0, afterDelete.size)
    }
}