package com.example.socialbatterymanager.features.home.data

import com.example.socialbatterymanager.data.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(private val database: AppDatabase) {
    suspend fun getActivitiesCountFromDate(fromDate: Long): Int {
        return database.activityDao().getActivitiesCountFromDate(fromDate)
    }
}
