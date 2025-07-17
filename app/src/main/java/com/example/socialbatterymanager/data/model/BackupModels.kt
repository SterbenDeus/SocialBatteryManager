package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val version: Int,
    val dataCount: Int,
    val checksum: String,
    val cloudBackupId: String? = null,
    val isRestored: Boolean = false
)

data class BackupData(
    val version: Int,
    val timestamp: Long,
    val activities: List<ActivityEntity>,
    val auditLogs: List<AuditLogEntity>,
    val checksum: String
)