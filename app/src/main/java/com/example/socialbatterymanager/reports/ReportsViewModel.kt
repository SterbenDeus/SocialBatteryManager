package com.example.socialbatterymanager.reports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.EnergyLog
import kotlinx.coroutines.launch

class ReportsViewModel(private val database: AppDatabase) : ViewModel() {

    private val _reportData = MutableLiveData<Pair<List<ActivityEntity>, List<EnergyLog>>>()
    val reportData: LiveData<Pair<List<ActivityEntity>, List<EnergyLog>>> = _reportData

    fun loadReportData(start: Long, end: Long) {
        viewModelScope.launch {
            val activities = database.activityDao().getActivitiesByDateRangeSync(start, end)
            val energyLogs = database.energyLogDao().getEnergyLogsByDateRangeSync(start, end)
            _reportData.value = activities to energyLogs
        }
    }

    fun getActivitiesForPeriod(start: Long, end: Long, callback: (List<ActivityEntity>) -> Unit) {
        viewModelScope.launch {
            val activities = database.activityDao().getActivitiesByDateRangeSync(start, end)
            callback(activities)
        }
    }
}

