package com.example.socialbatterymanager.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.repository.DataRepository
import com.example.socialbatterymanager.data.model.ActivityEntity
import kotlinx.coroutines.launch

class ActivitiesViewModel(private val repository: DataRepository) : ViewModel() {

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
                repository.database.activityDao().incrementUsageCount(activityId)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}

