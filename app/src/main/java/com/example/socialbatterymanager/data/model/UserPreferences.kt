package com.example.socialbatterymanager.data.model

data class UserPreferences(
    val autoBackupEnabled: Boolean = true,
    val backupInterval: Long = 24 * 60 * 60 * 1000, // 24 hours in milliseconds
    val encryptionEnabled: Boolean = true,
    val lastBackupTime: Long = 0,
    val cloudBackupEnabled: Boolean = false,
    val exportFormat: String = "CSV", // "CSV" or "PDF"
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val auditLogRetentionDays: Int = 30
)

object PreferencesKeys {
    const val AUTO_BACKUP_ENABLED = "auto_backup_enabled"
    const val BACKUP_INTERVAL = "backup_interval"
    const val ENCRYPTION_ENABLED = "encryption_enabled"
    const val LAST_BACKUP_TIME = "last_backup_time"
    const val CLOUD_BACKUP_ENABLED = "cloud_backup_enabled"
    const val EXPORT_FORMAT = "export_format"
    const val DARK_MODE = "dark_mode"
    const val NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val AUDIT_LOG_RETENTION_DAYS = "audit_log_retention_days"
}