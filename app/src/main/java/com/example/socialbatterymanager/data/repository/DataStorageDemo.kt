package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.ActivityEntity
import kotlinx.coroutines.runBlocking

/**
 * Demo class showing how to use the new data storage, backup & privacy features.
 * This is for demonstration purposes and should not be used in production.
 */
class DataStorageDemo {
    
    fun demonstrateUsage(context: Context) = runBlocking {
        // 1. Initialize security manager and check encryption
        val securityManager = SecurityManager.getInstance(context)
        val encryptionEnabled = securityManager.isEncryptionEnabled()
        
        // 2. Get or generate database passphrase if encryption is enabled
        val passphrase = if (encryptionEnabled) {
            securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
        } else {
            null
        }
        
        // 3. Initialize data repository with encryption support
        val dataRepository = DataRepository.getInstance(context, passphrase)
        
        // 4. Initialize preferences manager
        val preferencesManager = PreferencesManager.getInstance(context)
        
        // 5. Initialize backup manager
        val backupManager = BackupManager.getInstance(context, dataRepository, preferencesManager)
        
        // 6. Initialize import/export manager
        val importExportManager = ImportExportManager.getInstance(context, dataRepository)
        
        // 7. Create a sample activity with audit trail
        val sampleActivity = ActivityEntity(
            name = "Team Meeting",
            type = "Work",
            energy = 3,
            people = "Colleagues",
            mood = "Focused",
            notes = "Quarterly planning session",
            date = System.currentTimeMillis()
        )
        
        // Insert activity (this will automatically create an audit log entry)
        dataRepository.insertActivity(sampleActivity, userId = "demo_user")
        
        // 8. Demonstrate backup functionality
        if (backupManager.shouldCreateAutoBackup()) {
            val backupMetadata = backupManager.createLocalBackup()
            println("Backup created: ${backupMetadata.id}")
        }
        
        // 9. Demonstrate preferences management
        preferencesManager.updateAutoBackupEnabled(true)
        preferencesManager.updateExportFormat("PDF")
        
        // 10. Demonstrate export functionality
        val csvFile = importExportManager.exportToCsv()
        val pdfFile = importExportManager.exportToPdf()
        
        println("Data storage demo completed!")
        println("- Encryption enabled: $encryptionEnabled")
        println("- CSV export file: ${csvFile?.name}")
        println("- PDF export file: ${pdfFile?.name}")
    }
    
    fun demonstrateAuditTrail(context: Context) = runBlocking {
        val dataRepository = DataRepository.getInstance(context)
        
        // Get audit logs for a specific entity
        val auditLogs = dataRepository.getAuditLogsForEntity("activity", "1")
        
        auditLogs.collect { logs ->
            println("Audit trail for activity 1:")
            logs.forEach { log ->
                println("${log.timestamp}: ${log.action} - ${log.newValues}")
            }
        }
    }
    
    fun demonstrateBackupRestore(context: Context) = runBlocking {
        val dataRepository = DataRepository.getInstance(context)
        val preferencesManager = PreferencesManager.getInstance(context)
        val backupManager = BackupManager.getInstance(context, dataRepository, preferencesManager)
        
        // Create a backup
        val backupMetadata = backupManager.createLocalBackup()
        println("Created backup: ${backupMetadata.id}")
        
        // List available backups
        val availableBackups = backupManager.getAvailableBackups()
        println("Available backups: ${availableBackups.size}")
        
        // Restore from backup (in a real app, you'd want to confirm this action)
        val restoreSuccess = backupManager.restoreFromLocalBackup(backupMetadata.id)
        println("Restore successful: $restoreSuccess")
    }
    
    fun demonstrateMaintenanceOperations(context: Context) = runBlocking {
        val dataRepository = DataRepository.getInstance(context)
        val preferencesManager = PreferencesManager.getInstance(context)
        
        // Get user preferences
        preferencesManager.userPreferences.collect { preferences ->
            // Perform maintenance (cleanup old audit logs, etc.)
            dataRepository.performMaintenance(preferences.auditLogRetentionDays)
            println("Maintenance completed. Audit log retention: ${preferences.auditLogRetentionDays} days")
        }
    }
}