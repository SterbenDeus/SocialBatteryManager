package com.example.socialbatterymanager.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityRepositoryInstrumentedTest {

    private val dao = mockk<ActivityDao>(relaxed = true)
    private val auditRepo = mockk<AuditRepository>(relaxed = true)
    private val repository = ActivityRepository(dao, auditRepo)

    @Test
    fun markAsUsed_incrementsUsageCount() = runBlocking {
        val entity = ActivityEntity(
            id = 3,
            name = "Test",
            type = "Type",
            energy = 1,
            people = "Solo",
            mood = "Ok",
            notes = "",
            date = 0L,
        )
        coEvery { dao.getActivityById(entity.id) } returns entity

        repository.markAsUsed(entity.id)

        coVerify { dao.incrementUsageCount(entity.id) }
    }
}

