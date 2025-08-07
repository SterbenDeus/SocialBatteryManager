package com.example.socialbatterymanager.features.profile.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.example.socialbatterymanager.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
class ProfileViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        context = ApplicationProvider.getApplicationContext()
        runBlocking {
            context.profileDataStore.edit { it.clear() }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveProfile_updatesUserState() = runTest {
        val viewModel = ProfileViewModel(context)

        viewModel.saveProfile("John Doe", "john@example.com", 80, 20, true, "happy")
        advanceUntilIdle()

        val user: User = viewModel.user.value
        assertEquals("John Doe", user.name)
        assertEquals("john@example.com", user.email)
        assertEquals(80, user.batteryCapacity)
        assertEquals(20, user.warningLevel)
        assertEquals(true, user.notificationsEnabled)
        assertEquals("happy", user.currentMood)
    }

    @Test
    fun recalibrate_resetsCapacityAndWarning() = runTest {
        val viewModel = ProfileViewModel(context)
        viewModel.saveProfile("Jane", "jane@example.com", 60, 25, true, "tired")
        advanceUntilIdle()

        viewModel.recalibrate()
        advanceUntilIdle()

        val user = viewModel.user.value
        assertEquals(100, user.batteryCapacity)
        assertEquals(30, user.warningLevel)
    }
}
