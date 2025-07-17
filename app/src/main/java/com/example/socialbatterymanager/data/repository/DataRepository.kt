package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import java.util.UUID

class DataRepository private constructor(
    private val database: AppDatabase,
    private val gson: Gson = Gson()
) {
    
    // Activity operations with audit trail
    suspend fun insertActivity(activity: ActivityEntity, userId: String? = null) {
        val insertedActivity = activity.copy(
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        database.activityDao().insertActivity(insertedActivity)
        
        // Log the action
        logAuditEntry(
            entityType = "activity",
            entityId = insertedActivity.id.toString(),
            action = "create",
            newValues = gson.toJson(insertedActivity),
            userId = userId
        )
    }
    
    suspend fun updateActivity(activity: ActivityEntity, userId: String? = null) {
        val oldActivity = database.activityDao().getActivityById(activity.id)
        val updatedActivity = activity.copy(updatedAt = System.currentTimeMillis())
        
        database.activityDao().updateActivity(updatedActivity)
        
        // Log the action
        logAuditEntry(
            entityType = "activity",
            entityId = activity.id.toString(),
            action = "update",
            oldValues = gson.toJson(oldActivity),
            newValues = gson.toJson(updatedActivity),
            userId = userId
        )
    }
    
    suspend fun deleteActivity(activityId: Int, userId: String? = null) {
        val activity = database.activityDao().getActivityById(activityId)
        if (activity != null) {
            database.activityDao().softDeleteActivity(activityId)
            
            // Log the action
            logAuditEntry(
                entityType = "activity",
                entityId = activityId.toString(),
                action = "delete",
                oldValues = gson.toJson(activity),
                userId = userId
            )
        }
    }
    
    fun getAllActivities(): Flow<List<ActivityEntity>> {
        return database.activityDao().getAllActivities()
    }
    
    suspend fun getActivityById(id: Int): ActivityEntity? {
        return database.activityDao().getActivityById(id)
    }
    
    // Audit log operations
    private suspend fun logAuditEntry(
        entityType: String,
        entityId: String,
        action: String,
        oldValues: String? = null,
        newValues: String? = null,
        userId: String? = null
    ) {
        val auditLog = AuditLogEntity(
            entityType = entityType,
            entityId = entityId,
            action = action,
            oldValues = oldValues,
            newValues = newValues,
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        database.auditLogDao().insertAuditLog(auditLog)
    }
    
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> {
        return database.auditLogDao().getAllAuditLogs()
    }
    
    fun getAuditLogsForEntity(entityType: String, entityId: String): Flow<List<AuditLogEntity>> {
        return database.auditLogDao().getAuditLogsForEntity(entityType, entityId)
    }
    
    suspend fun cleanupOldAuditLogs(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000)
        database.auditLogDao().deleteAuditLogsOlderThan(cutoff)
    }
    
    // Backup operations
    suspend fun createBackup(): BackupMetadataEntity {
        val activities = database.activityDao().getAllActivitiesForBackup()
        val auditLogs = database.auditLogDao().getAllAuditLogs().first()
        
        val backupData = com.example.socialbatterymanager.data.model.BackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            activities = activities,
            auditLogs = auditLogs,
            checksum = generateChecksum(activities, auditLogs)
        )
        
        val metadata = BackupMetadataEntity(
            id = UUID.randomUUID().toString(),
            timestamp = backupData.timestamp,
            version = backupData.version,
            dataCount = activities.size,
            checksum = backupData.checksum
        )
        
        database.backupMetadataDao().insertBackupMetadata(metadata)
        return metadata
    }
    
    suspend fun getLatestBackupMetadata(): BackupMetadataEntity? {
        return database.backupMetadataDao().getLatestBackupMetadata()
    }
    
    fun getAllBackupMetadata(): Flow<List<BackupMetadataEntity>> {
        return database.backupMetadataDao().getAllBackupMetadata()
    }
    
    private fun generateChecksum(activities: List<ActivityEntity>, auditLogs: List<AuditLogEntity>): String {
        val data = gson.toJson(activities) + gson.toJson(auditLogs)
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
    
    // Cleanup operations
    suspend fun performMaintenance(auditLogRetentionDays: Int) {
        cleanupOldAuditLogs(auditLogRetentionDays)
        
        // Clean up hard-deleted activities older than 90 days
        val cutoff = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000)
        database.activityDao().hardDeleteOldActivities(cutoff)
    }
    
    companion object {
        @Volatile
        private var INSTANCE: DataRepository? = null
        
        fun getInstance(context: Context, passphrase: String? = null): DataRepository {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getDatabase(context, passphrase)
                val instance = DataRepository(database)
                INSTANCE = instance
                instance
            }
        }
        
        fun clearInstance() {
            INSTANCE = null
        }
    }
}