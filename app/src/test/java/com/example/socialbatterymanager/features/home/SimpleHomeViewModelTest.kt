package com.example.socialbatterymanager.features.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.features.home.data.HomeRepository
import com.example.socialbatterymanager.features.home.ui.SimpleHomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimpleHomeViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadWeeklyStats_postsActivityCount() = runTest {
        val expected = 4
        val activityDao = mockk<ActivityDao>()
        coEvery { activityDao.getActivitiesCountFromDate(any()) } returns expected
        val database = mockk<AppDatabase>()
        every { database.activityDao() } returns activityDao

        val repository = HomeRepository(database)
        val viewModel = SimpleHomeViewModel(repository)

        viewModel.loadWeeklyStats()
        advanceUntilIdle()

        assertEquals(expected, viewModel.weeklyActivityCount.value)
    }
}
