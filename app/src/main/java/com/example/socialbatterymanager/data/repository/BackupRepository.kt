package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.database.AuditLogDao
import com.example.socialbatterymanager.data.database.BackupMetadataDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

class BackupRepository(
    private val activityDao: ActivityDao,
    private val auditLogDao: AuditLogDao,
    private val backupMetadataDao: BackupMetadataDao,
    private val gson: Gson = Gson()
) {
    suspend fun createBackup(): BackupMetadataEntity {
        val activities = activityDao.getAllActivitiesForBackup()
        val auditLogs = auditLogDao.getAllAuditLogs().first()

        val checksum = generateChecksum(activities, auditLogs)
        val backupData = com.example.socialbatterymanager.data.model.BackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            activities = activities,
            auditLogs = auditLogs,
            checksum = checksum
        )

        val metadata = BackupMetadataEntity(
            id = UUID.randomUUID().toString(),
            timestamp = backupData.timestamp,
            version = backupData.version,
            dataCount = activities.size,
            checksum = backupData.checksum
        )

        backupMetadataDao.insertBackupMetadata(metadata)
        return metadata
    }

    suspend fun getLatestBackupMetadata(): BackupMetadataEntity? =
        backupMetadataDao.getLatestBackupMetadata()

    fun getAllBackupMetadata(): Flow<List<BackupMetadataEntity>> =
        backupMetadataDao.getAllBackupMetadata()

    private fun generateChecksum(
        activities: List<ActivityEntity>,
        auditLogs: List<AuditLogEntity>
    ): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val activitiesBytes = gson.toJson(activities).toByteArray(StandardCharsets.UTF_8)
        val auditLogsBytes = gson.toJson(auditLogs).toByteArray(StandardCharsets.UTF_8)
        digest.update(activitiesBytes)
        digest.update(auditLogsBytes)
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun insertBackupMetadata(metadata: BackupMetadataEntity) {
        backupMetadataDao.insertBackupMetadata(metadata)
    }

    suspend fun updateBackupMetadata(metadata: BackupMetadataEntity) {
        backupMetadataDao.updateBackupMetadata(metadata)
    }

    suspend fun deleteBackupMetadata(id: String) {
        backupMetadataDao.deleteBackupMetadata(id)
    }
}

