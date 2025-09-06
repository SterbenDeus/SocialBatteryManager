package com.example.socialbatterymanager.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.BackupData
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRepository: ActivityRepository,
    private val auditRepository: AuditRepository,
    private val backupRepository: BackupRepository,
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val gson: Gson = Gson(),
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance(),
) {
    
    suspend fun createLocalBackup(): BackupMetadataEntity {
        val metadata = backupRepository.createBackup()
        
        // Create backup file
        val backupFile = File(context.getExternalFilesDir(null), "backup_${metadata.id}.json")
        val activities = activityRepository.getAllActivities().first()
        val auditLogs = auditRepository.getAllAuditLogs().first()
        
        val backupData = BackupData(
            version = metadata.version,
            timestamp = metadata.timestamp,
            activities = activities,
            auditLogs = auditLogs,
            checksum = metadata.checksum
        )
        
        backupFile.writeText(gson.toJson(backupData))
        
        // Update last backup time
        preferencesManager.updateLastBackupTime(System.currentTimeMillis())
        
        return metadata
    }
    
    suspend fun createCloudBackup(): BackupMetadataEntity? {
        val preferences: PreferencesManager.UserPreferences = preferencesManager.userPreferences.first()
        if (!preferences.cloudBackupEnabled) {
            return null
        }
        
        val metadata = createLocalBackup()
        
        try {
            val activities = activityRepository.getAllActivities().first()
            val auditLogs = auditRepository.getAllAuditLogs().first()
            
            val backupData = BackupData(
                version = metadata.version,
                timestamp = metadata.timestamp,
                activities = activities,
                auditLogs = auditLogs,
                checksum = metadata.checksum
            )
            
            val cloudBackupId = UUID.randomUUID().toString()
            firestore.collection("backups")
                .document(cloudBackupId)
                .set(backupData)
                .await()
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }

        return metadata
    }
    
    suspend fun restoreFromCloudBackup(cloudBackupId: String): Boolean {
        return try {
            val document = firestore.collection("backups")
                .document(cloudBackupId)
                .get()
                .await()
            
            val backupData = document.toObject(BackupData::class.java)
            if (backupData != null) {
                restoreFromBackupData(backupData)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            false
        }
    }
    
    suspend fun restoreFromLocalBackup(backupId: String): Boolean {
        return try {
            val backupFile = File(context.getExternalFilesDir(null), "backup_${backupId}.json")
            if (backupFile.exists()) {
                val backupContent = backupFile.readText()
                val backupData = gson.fromJson(backupContent, BackupData::class.java)
                restoreFromBackupData(backupData)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            crashlytics.recordException(e)
            false
        }
    }
    
    private suspend fun restoreFromBackupData(backupData: BackupData) {
        database.withTransaction {
            activityRepository.clearAllActivities()
            auditRepository.clearAuditLogs()

            for (activity in backupData.activities) {
                activityRepository.insertActivityRaw(activity)
            }

            for (auditLog in backupData.auditLogs) {
                auditRepository.insertAuditLogRaw(auditLog)
            }

            val restoreMetadata = BackupMetadataEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                version = backupData.version,
                dataCount = backupData.activities.size,
                checksum = backupData.checksum,
                isRestored = true
            )

            backupRepository.insertBackupMetadata(restoreMetadata)
        }
    }
    
    suspend fun getAvailableBackups(): List<BackupMetadataEntity> {
        return backupRepository.getAllBackupMetadata().first()
    }
    
    suspend fun deleteBackup(backupId: String) {
        // Delete local backup file
        val backupFile = File(context.getExternalFilesDir(null), "backup_${backupId}.json")
        if (backupFile.exists()) {
            backupFile.delete()
        }
        
        // Delete metadata
        backupRepository.deleteBackupMetadata(backupId)
    }
    
    suspend fun shouldCreateAutoBackup(): Boolean {
        val preferences: PreferencesManager.UserPreferences = preferencesManager.userPreferences.first()
        if (!preferences.autoBackupEnabled) {
            return false
        }

        val currentTime = System.currentTimeMillis()
        val lastBackupTime = preferences.lastBackupTime
        val interval = preferences.backupInterval

        return (currentTime - lastBackupTime) >= interval
    }
    
}
