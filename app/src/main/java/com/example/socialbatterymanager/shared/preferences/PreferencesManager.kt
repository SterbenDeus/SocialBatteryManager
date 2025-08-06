package com.example.socialbatterymanager.shared.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * Handles persistence of simple user preferences.
 *
 * Each exposed [Flow] emits a sensible default when the underlying DataStore
 * encounters an error, ensuring callers always receive a value.
 */
class PreferencesManager(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.dataStore)
    
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_preference")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val SYNC_ENABLED_KEY = booleanPreferencesKey("sync_enabled")
        private val BATTERY_NOTIFICATION_THRESHOLD_KEY = intPreferencesKey("battery_notification_threshold")
        private val LAST_BACKUP_TIME_KEY = longPreferencesKey("last_backup_time")
        private val CLOUD_BACKUP_ENABLED_KEY = booleanPreferencesKey("cloud_backup_enabled")
        private val AUTO_BACKUP_ENABLED_KEY = booleanPreferencesKey("auto_backup_enabled")
        private val BACKUP_INTERVAL_KEY = longPreferencesKey("backup_interval")
        
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
    
    /**
     * Emits the stored theme or [THEME_SYSTEM] if unavailable or on read error.
     */
    val themeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: THEME_SYSTEM
        }
        .catch { e ->
            if (e is IOException) {
                emit(THEME_SYSTEM)
            } else {
                throw e
            }
        }

    /**
     * Emits whether onboarding is completed. Defaults to `false` on errors.
     */
    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] ?: false
        }
        .catch { e ->
            if (e is IOException) {
                emit(false)
            } else {
                throw e
            }
        }
    
    /**
     * Emits the biometric preference, defaulting to `false` when reading fails.
     */
    val biometricEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] ?: false
        }
        .catch { e ->
            if (e is IOException) {
                emit(false)
            } else {
                throw e
            }
        }
    
    /**
     * Emits notification setting, defaulting to `true` on errors.
     */
    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
        }
        .catch { e ->
            if (e is IOException) {
                emit(true)
            } else {
                throw e
            }
        }
    
    /**
     * Emits sync setting, defaulting to `true` on errors.
     */
    val syncEnabledFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SYNC_ENABLED_KEY] ?: true
        }
        .catch { e ->
            if (e is IOException) {
                emit(true)
            } else {
                throw e
            }
        }
    
    /**
     * Emits notification threshold, defaulting to `25` on errors.
     */
    val batteryNotificationThresholdFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[BATTERY_NOTIFICATION_THRESHOLD_KEY] ?: 25
        }
        .catch { e ->
            if (e is IOException) {
                emit(25)
            } else {
                throw e
            }
        }
    
    val userPreferences: Flow<UserPreferences> = dataStore.data
        .map { preferences ->
            UserPreferences(
                cloudBackupEnabled = preferences[CLOUD_BACKUP_ENABLED_KEY] ?: false,
                autoBackupEnabled = preferences[AUTO_BACKUP_ENABLED_KEY] ?: false,
                lastBackupTime = preferences[LAST_BACKUP_TIME_KEY] ?: 0L,
                backupInterval = preferences[BACKUP_INTERVAL_KEY] ?: 0L
            )
        }
        .catch { e ->
            if (e is IOException) {
                emit(UserPreferences())
            } else {
                throw e
            }
        }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = completed
        }
    }
    
    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SYNC_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun setBatteryNotificationThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[BATTERY_NOTIFICATION_THRESHOLD_KEY] = threshold
        }
    }

    suspend fun updateLastBackupTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_BACKUP_TIME_KEY] = time
        }
    }

    data class UserPreferences(
        val cloudBackupEnabled: Boolean = false,
        val autoBackupEnabled: Boolean = false,
        val lastBackupTime: Long = 0L,
        val backupInterval: Long = 0L
    )
}
