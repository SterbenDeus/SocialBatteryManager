package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val auditRepository: AuditRepository
) {
    suspend fun insertActivity(activity: ActivityEntity, userId: String? = null) {
        val inserted = activity.copy(
            lastModified = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        activityDao.insertActivity(inserted)
        auditRepository.logAuditEntry(
            entityType = "activity",
            entityId = inserted.id.toString(),
            action = "create",
            newValues = inserted,
            userId = userId
        )
    }

    suspend fun updateActivity(activity: ActivityEntity, userId: String? = null) {
        val oldActivity = activityDao.getActivityById(activity.id)
        val updated = activity.copy(updatedAt = System.currentTimeMillis())
        activityDao.updateActivity(updated)
        auditRepository.logAuditEntry(
            entityType = "activity",
            entityId = activity.id.toString(),
            action = "update",
            oldValues = oldActivity,
            newValues = updated,
            userId = userId
        )
    }

    suspend fun deleteActivity(activityId: Int, userId: String? = null) {
        val activity = activityDao.getActivityById(activityId)
        if (activity != null) {
            activityDao.softDeleteActivity(activityId)
            auditRepository.logAuditEntry(
                entityType = "activity",
                entityId = activityId.toString(),
                action = "delete",
                oldValues = activity,
                userId = userId
            )
        }
    }

    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    suspend fun getActivityById(id: Int): ActivityEntity? = activityDao.getActivityById(id)

    suspend fun markAsUsed(activityId: Int) {
        activityDao.incrementUsageCount(activityId)
    }

    suspend fun insertActivityRaw(activity: ActivityEntity) {
        activityDao.insertActivity(activity)
    }

    suspend fun hardDeleteOldActivities(cutoff: Long) {
        activityDao.hardDeleteOldActivities(cutoff)
    }

    suspend fun clearAllActivities() {
        activityDao.deleteAllActivities()
    }
}

