package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityType: String, // "activity", "user", etc.
    val entityId: String, // ID of the affected entity
    val action: String, // "create", "update", "delete"
    val oldValues: String? = null, // JSON string of old values
    val newValues: String? = null, // JSON string of new values
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String? = null // User who performed the action
)