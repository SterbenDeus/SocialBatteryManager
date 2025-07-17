package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.BackupData
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BackupManager private constructor(
    private val context: Context,
    private val dataRepository: DataRepository,
    private val preferencesManager: PreferencesManager,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val gson: Gson = Gson()
) {
    
    suspend fun createLocalBackup(): BackupMetadataEntity {
        val metadata = dataRepository.createBackup()
        
        // Create backup file
        val backupFile = File(context.getExternalFilesDir(null), "backup_${metadata.id}.json")
        val activities = dataRepository.getAllActivities().first()
        val auditLogs = dataRepository.getAllAuditLogs().first()
        
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
        val preferences = preferencesManager.userPreferences.first()
        if (!preferences.cloudBackupEnabled) {
            return null
        }
        
        val metadata = createLocalBackup()
        
        try {
            val activities = dataRepository.getAllActivities().first()
            val auditLogs = dataRepository.getAllAuditLogs().first()
            
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
            
            // Update metadata with cloud backup ID
            val updatedMetadata = metadata.copy(cloudBackupId = cloudBackupId)
            dataRepository.database.backupMetadataDao().updateBackupMetadata(updatedMetadata)
            
            return updatedMetadata
        } catch (e: Exception) {
            // Log error but return local backup metadata
            return metadata
        }
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
            false
        }
    }
    
    private suspend fun restoreFromBackupData(backupData: BackupData) {
        // Clear existing data (this is a full restore)
        // Note: In a real app, you might want to create a backup before restoring
        
        // Restore activities
        for (activity in backupData.activities) {
            dataRepository.database.activityDao().insertActivity(activity)
        }
        
        // Restore audit logs
        for (auditLog in backupData.auditLogs) {
            dataRepository.database.auditLogDao().insertAuditLog(auditLog)
        }
        
        // Create restore metadata
        val restoreMetadata = BackupMetadataEntity(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            version = backupData.version,
            dataCount = backupData.activities.size,
            checksum = backupData.checksum,
            isRestored = true
        )
        
        dataRepository.database.backupMetadataDao().insertBackupMetadata(restoreMetadata)
    }
    
    suspend fun getAvailableBackups(): List<BackupMetadataEntity> {
        return dataRepository.getAllBackupMetadata().first()
    }
    
    suspend fun deleteBackup(backupId: String) {
        // Delete local backup file
        val backupFile = File(context.getExternalFilesDir(null), "backup_${backupId}.json")
        if (backupFile.exists()) {
            backupFile.delete()
        }
        
        // Delete metadata
        dataRepository.database.backupMetadataDao().deleteBackupMetadata(backupId)
    }
    
    suspend fun shouldCreateAutoBackup(): Boolean {
        val preferences = preferencesManager.userPreferences.first()
        if (!preferences.autoBackupEnabled) {
            return false
        }
        
        val lastBackupTime = preferences.lastBackupTime
        val interval = preferences.backupInterval
        val currentTime = System.currentTimeMillis()
        
        return (currentTime - lastBackupTime) >= interval
    }
    
    companion object {
        @Volatile
        private var INSTANCE: BackupManager? = null
        
        fun getInstance(
            context: Context,
            dataRepository: DataRepository,
            preferencesManager: PreferencesManager
        ): BackupManager {
            return INSTANCE ?: synchronized(this) {
                val instance = BackupManager(context, dataRepository, preferencesManager)
                INSTANCE = instance
                instance
            }
        }
    }
}