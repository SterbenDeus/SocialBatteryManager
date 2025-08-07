package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ActivityRepositoryTest {

    private val dao = mockk<ActivityDao>(relaxed = true)
    private val auditRepo = mockk<AuditRepository>(relaxed = true)
    private val repository = ActivityRepository(dao, auditRepo)

    @Test
    fun insertActivity_insertsAndLogs() = runBlocking {
        val entity = ActivityEntity(
            id = 1,
            name = "Test",
            type = "Type",
            energy = 5,
            people = "Solo",
            mood = "Good",
            notes = "",
            date = 0L,
        )

        repository.insertActivity(entity)

        coVerify { dao.insertActivity(any()) }
        coVerify {
            auditRepo.logAuditEntry(
                entityType = "activity",
                entityId = entity.id.toString(),
                action = "create",
                newValues = any(),
                userId = null,
            )
        }
    }

    @Test
    fun deleteActivity_softDeletesAndLogs() = runBlocking {
        val entity = ActivityEntity(
            id = 2,
            name = "ToDelete",
            type = "Type",
            energy = 3,
            people = "Solo",
            mood = "Ok",
            notes = "",
            date = 0L,
        )

        coEvery { dao.getActivityById(entity.id) } returns entity

        repository.deleteActivity(entity.id)

        coVerify { dao.softDeleteActivity(entity.id) }
        coVerify {
            auditRepo.logAuditEntry(
                entityType = "activity",
                entityId = entity.id.toString(),
                action = "delete",
                oldValues = entity,
                userId = null,
            )
        }
    }
}

