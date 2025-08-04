package com.example.socialbatterymanager.features.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.features.home.data.HomeRepository
import kotlinx.coroutines.launch

class SimpleHomeViewModel(private val homeRepository: HomeRepository) : ViewModel() {

    private val _weeklyActivityCount = MutableLiveData<Int>()
    val weeklyActivityCount: LiveData<Int> = _weeklyActivityCount

    fun loadWeeklyStats() {
        viewModelScope.launch {
            val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            val count = homeRepository.getActivitiesCountFromDate(weekStart)
            _weeklyActivityCount.value = count
        }
    }
}

