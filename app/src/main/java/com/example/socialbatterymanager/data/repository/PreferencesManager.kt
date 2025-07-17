package com.example.socialbatterymanager.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.socialbatterymanager.data.model.PreferencesKeys
import com.example.socialbatterymanager.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesManager private constructor(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    
    private val autoBackupEnabledKey = booleanPreferencesKey(PreferencesKeys.AUTO_BACKUP_ENABLED)
    private val backupIntervalKey = longPreferencesKey(PreferencesKeys.BACKUP_INTERVAL)
    private val encryptionEnabledKey = booleanPreferencesKey(PreferencesKeys.ENCRYPTION_ENABLED)
    private val lastBackupTimeKey = longPreferencesKey(PreferencesKeys.LAST_BACKUP_TIME)
    private val cloudBackupEnabledKey = booleanPreferencesKey(PreferencesKeys.CLOUD_BACKUP_ENABLED)
    private val exportFormatKey = stringPreferencesKey(PreferencesKeys.EXPORT_FORMAT)
    private val darkModeKey = booleanPreferencesKey(PreferencesKeys.DARK_MODE)
    private val notificationsEnabledKey = booleanPreferencesKey(PreferencesKeys.NOTIFICATIONS_ENABLED)
    private val auditLogRetentionDaysKey = intPreferencesKey(PreferencesKeys.AUDIT_LOG_RETENTION_DAYS)
    
    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            autoBackupEnabled = preferences[autoBackupEnabledKey] ?: true,
            backupInterval = preferences[backupIntervalKey] ?: 24 * 60 * 60 * 1000,
            encryptionEnabled = preferences[encryptionEnabledKey] ?: true,
            lastBackupTime = preferences[lastBackupTimeKey] ?: 0,
            cloudBackupEnabled = preferences[cloudBackupEnabledKey] ?: false,
            exportFormat = preferences[exportFormatKey] ?: "CSV",
            darkMode = preferences[darkModeKey] ?: false,
            notificationsEnabled = preferences[notificationsEnabledKey] ?: true,
            auditLogRetentionDays = preferences[auditLogRetentionDaysKey] ?: 30
        )
    }
    
    suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[autoBackupEnabledKey] = enabled
        }
    }
    
    suspend fun updateBackupInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[backupIntervalKey] = interval
        }
    }
    
    suspend fun updateEncryptionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[encryptionEnabledKey] = enabled
        }
    }
    
    suspend fun updateLastBackupTime(time: Long) {
        context.dataStore.edit { preferences ->
            preferences[lastBackupTimeKey] = time
        }
    }
    
    suspend fun updateCloudBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[cloudBackupEnabledKey] = enabled
        }
    }
    
    suspend fun updateExportFormat(format: String) {
        context.dataStore.edit { preferences ->
            preferences[exportFormatKey] = format
        }
    }
    
    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[darkModeKey] = enabled
        }
    }
    
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }
    
    suspend fun updateAuditLogRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[auditLogRetentionDaysKey] = days
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}