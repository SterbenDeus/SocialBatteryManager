package com.example.socialbatterymanager.features.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.database.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SimpleHomeViewModel @Inject constructor(private val database: AppDatabase) : ViewModel() {

    private val _weeklyActivityCount = MutableLiveData<Int>()
    val weeklyActivityCount: LiveData<Int> = _weeklyActivityCount

    fun loadWeeklyStats() {
        viewModelScope.launch {
            val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val count = database.activityDao().getActivitiesCountFromDate(weekStart)
            _weeklyActivityCount.value = count
        }
    }
}

