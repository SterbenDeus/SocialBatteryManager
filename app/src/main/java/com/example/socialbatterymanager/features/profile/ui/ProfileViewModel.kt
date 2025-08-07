package com.example.socialbatterymanager.features.profile.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profile_preferences")

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val dataStore = context.profileDataStore

    private val NAME_KEY = stringPreferencesKey("user_name")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val CAPACITY_KEY = intPreferencesKey("battery_capacity")
    private val WARNING_KEY = intPreferencesKey("warning_level")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    private val MOOD_KEY = stringPreferencesKey("current_mood")
    private val PHOTO_KEY = stringPreferencesKey("profile_photo")

    private val _user = MutableStateFlow(
        User(
            id = "1",
            name = context.getString(R.string.default_user_name),
            email = context.getString(R.string.default_user_email),
            batteryCapacity = 100,
            warningLevel = 30,
            currentMood = context.getString(R.string.default_mood),
            notificationsEnabled = true
        )
    )
    val user: StateFlow<User> = _user.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                User(
                    id = "1",
                    name = preferences[NAME_KEY] ?: context.getString(R.string.default_user_name),
                    email = preferences[EMAIL_KEY] ?: context.getString(R.string.default_user_email),
                    photoUri = preferences[PHOTO_KEY],
                    batteryCapacity = preferences[CAPACITY_KEY] ?: 100,
                    warningLevel = preferences[WARNING_KEY] ?: 30,
                    currentMood = preferences[MOOD_KEY] ?: context.getString(R.string.default_mood),
                    notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true
                )
            }.collect { user ->
                _user.value = user
            }
        }
    }

    fun saveProfile(
        name: String,
        email: String,
        capacity: Int,
        warning: Int,
        notificationsEnabled: Boolean,
        mood: String
    ) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[NAME_KEY] = name
                preferences[EMAIL_KEY] = email
                preferences[CAPACITY_KEY] = capacity
                preferences[WARNING_KEY] = warning
                preferences[NOTIFICATIONS_KEY] = notificationsEnabled
                preferences[MOOD_KEY] = mood
            }
        }
    }

    fun saveImageUri(uri: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PHOTO_KEY] = uri
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            dataStore.edit { it.clear() }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            dataStore.edit { it.clear() }
        }
    }

    fun recalibrate() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CAPACITY_KEY] = 100
                preferences[WARNING_KEY] = 30
            }
        }
    }
}
