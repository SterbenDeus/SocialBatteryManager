# Data Storage, Backup & Privacy Features Documentation

This document outlines the implementation of data storage, backup, and privacy features for the Social Battery Manager application.

## Overview

The implementation includes:
- **Auto-save functionality** with audit trail
- **Cloud backup support** using Firebase Firestore
- **Import/export capabilities** (CSV/PDF)
- **Change logs** (audit trail) for all data operations
- **Encryption** using SQLCipher for Room database
- **DataStore** for preferences management
- **Security features** with Android Keystore

## Architecture

### Core Components

1. **Data Models** (`data/model/`)
   - `ActivityEntity`: Enhanced with audit trail support (soft delete, timestamps)
   - `AuditLogEntity`: Tracks all data changes
   - `BackupMetadataEntity`: Manages backup information
   - `UserPreferences`: Application settings

2. **Database Layer** (`data/database/`)
   - `AppDatabase`: Enhanced with encryption support using SQLCipher
   - `ActivityDao`: Activity operations with audit trail
   - `AuditLogDao`: Audit log operations
   - `BackupMetadataDao`: Backup metadata operations

3. **Repository Layer** (`data/repository/`)
   - `DataRepository`: Centralized data access with audit logging
   - `PreferencesManager`: Settings management using DataStore
   - `BackupManager`: Local and cloud backup operations
   - `ImportExportManager`: Data import/export functionality
   - `SecurityManager`: Encryption key management

## Features

### 1. Auto-Save with Audit Trail

All data operations are automatically logged with:
- Entity type and ID
- Action performed (create, update, delete)
- Old and new values (JSON format)
- Timestamp and user ID

**Usage:**
```kotlin
val dataRepository = DataRepository.getInstance(context, passphrase)
dataRepository.insertActivity(activity, userId = "user123")
```

### 2. Cloud Backup (Firebase Firestore)

Automatic and manual backup to Firebase Firestore with:
- Configurable backup intervals
- Backup metadata tracking
- Checksum validation
- Restore functionality

**Usage:**
```kotlin
val backupManager = BackupManager.getInstance(context, dataRepository, preferencesManager)
val metadata = backupManager.createCloudBackup()
```

### 3. Import/Export (CSV/PDF)

Export data to CSV or PDF formats:
- CSV: Comma-separated values for spreadsheet applications
- PDF: Formatted report with tables

**Usage:**
```kotlin
val importExportManager = ImportExportManager.getInstance(context, dataRepository)
val csvFile = importExportManager.exportToCsv()
val pdfFile = importExportManager.exportToPdf()
```

### 4. Encryption (SQLCipher)

Database encryption using SQLCipher:
- Automatic key generation using Android Keystore
- Secure key storage in EncryptedSharedPreferences
- Configurable encryption on/off

**Usage:**
```kotlin
val securityManager = SecurityManager.getInstance(context)
val passphrase = securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
val dataRepository = DataRepository.getInstance(context, passphrase)
```

### 5. DataStore for Preferences

Modern preferences management using DataStore:
- Type-safe preferences
- Asynchronous operations
- Observation support

**Usage:**
```kotlin
val preferencesManager = PreferencesManager.getInstance(context)
preferencesManager.updateAutoBackupEnabled(true)
preferencesManager.userPreferences.collect { preferences ->
    // Use preferences
}
```

## Security Features

### Database Encryption
- Uses SQLCipher for Room database encryption
- Keys generated using Android Keystore
- Fallback key generation for devices without Keystore support

### Secure Preferences
- Uses EncryptedSharedPreferences for sensitive data
- AES-256 encryption for key storage
- Automatic key rotation support

### Audit Trail
- Complete change tracking for all data operations
- Configurable retention policy
- Tamper-evident logging

## Configuration

### Dependencies Added
```kotlin
// Room database with encryption
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
implementation("net.zetetic:android-database-sqlcipher:4.5.4")

// DataStore for preferences
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Firebase for cloud backup
implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
implementation("com.google.firebase:firebase-auth-ktx:22.3.0")

// Export functionality
implementation("com.opencsv:opencsv:5.8")
implementation("org.apache.pdfbox:pdfbox:2.0.30")

// Security
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

### Permissions Added
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Usage Examples

### Basic Setup
```kotlin
// Initialize with encryption
val securityManager = SecurityManager.getInstance(context)
val passphrase = if (securityManager.isEncryptionEnabled()) {
    securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
} else null

val dataRepository = DataRepository.getInstance(context, passphrase)
val preferencesManager = PreferencesManager.getInstance(context)
// Enable auto-backup
```

### Data Operations
```kotlin
// Create activity with audit trail
val activity = ActivityEntity(
    name = "Meeting",
    type = "Work",
    energy = 3,
    people = "Team",
    mood = "Focused",
    notes = "Weekly standup",
    date = System.currentTimeMillis()
)

dataRepository.insertActivity(activity, userId = "user123")

// Update activity (automatically logged)
val updatedActivity = activity.copy(energy = 4)
dataRepository.updateActivity(updatedActivity, userId = "user123")

// Soft delete (maintains audit trail)
dataRepository.deleteActivity(activity.id, userId = "user123")
```

### Backup and Restore
```kotlin
val backupManager = BackupManager.getInstance(context, dataRepository, preferencesManager)

// Create backup
val metadata = backupManager.createLocalBackup()

// List available backups
val backups = backupManager.getAvailableBackups()

// Restore from backup
val success = backupManager.restoreFromLocalBackup(metadata.id)
```

### Export Data
```kotlin
val importExportManager = ImportExportManager.getInstance(context, dataRepository)

// Export to CSV
val csvFile = importExportManager.exportToCsv()

// Export to PDF
val pdfFile = importExportManager.exportToPdf()

// Import from CSV
val result = importExportManager.importFromCsv(csvFile)
```

## Migration

The implementation includes database migration from version 1 to 2:
- Adds audit trail columns to activities table
- Creates audit_logs table
- Creates backup_metadata table
- Preserves existing data

## Testing

Unit tests are provided for:
- Data model validation
- Repository functionality
- Backup and restore operations
- Import/export functionality

Run tests with:
```bash
./gradlew test
```

## Security Considerations

1. **Database Encryption**: Uses industry-standard SQLCipher
2. **Key Management**: Leverages Android Keystore for secure key generation
3. **Audit Trail**: Immutable log of all data changes
4. **Secure Preferences**: Encrypted storage for sensitive settings
5. **Data Validation**: Input validation and sanitization
6. **Backup Security**: Encrypted backups with checksum validation

## Performance Considerations

1. **Database Indexing**: Proper indexing for audit log queries
2. **Lazy Loading**: Paginated loading for large datasets
3. **Background Operations**: Backup and export operations run in background
4. **Memory Management**: Efficient handling of large export files
5. **Network Optimization**: Compressed cloud backups

## Future Enhancements

1. **Data Compression**: Compress backup files for storage efficiency
2. **Incremental Backups**: Only backup changed data
3. **Multiple Cloud Providers**: Support for Google Drive, Dropbox, etc.
4. **Advanced Encryption**: Additional encryption algorithms
5. **Real-time Sync**: Real-time data synchronization
6. **Analytics**: Usage analytics and insights