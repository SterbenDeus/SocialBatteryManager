package com.example.socialbatterymanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.repository.ActivityRepository
import com.example.socialbatterymanager.data.model.ActivityEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    val activities: LiveData<List<ActivityEntity>> = repository.getAllActivities().asLiveData()

    fun insertActivity(activity: ActivityEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.insertActivity(activity)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateActivity(activity: ActivityEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateActivity(activity)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun deleteActivity(activityId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteActivity(activityId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun markAsUsed(activityId: Int, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.markAsUsed(activityId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}

