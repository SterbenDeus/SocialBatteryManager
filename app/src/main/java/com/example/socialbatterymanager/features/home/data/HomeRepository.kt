package com.example.socialbatterymanager.features.home.data

import com.example.socialbatterymanager.data.database.AppDatabase

class HomeRepository(private val database: AppDatabase) {
    suspend fun getActivitiesCountFromDate(fromDate: Long): Int {
        return database.activityDao().getActivitiesCountFromDate(fromDate)
    }
}
