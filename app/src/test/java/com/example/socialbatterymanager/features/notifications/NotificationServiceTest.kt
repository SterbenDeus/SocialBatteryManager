package com.example.socialbatterymanager.features.notifications

import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.database.NotificationDao
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.room.Room

@RunWith(RobolectricTestRunner::class)
class NotificationServiceTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: NotificationDao
    private lateinit var preferences: PreferencesManager
    private lateinit var scope: TestScope
    private lateinit var service: NotificationService

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.notificationDao()
        preferences = mockk(relaxed = true)
        scope = TestScope(UnconfinedTestDispatcher())
        service = NotificationService(
            context,
            database,
            preferences,
            scope
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertsNotificationWhenEnabled() = runTest {
        every { preferences.notificationsEnabledFlow } returns flowOf(true)
        service.checkAndGenerateEnergyLowNotification(10)
        scope.advanceUntilIdle()
        val notifications = dao.getAllNotifications().first()
        assertEquals(1, notifications.size)
    }

    @Test
    fun doesNotInsertNotificationWhenDisabled() = runTest {
        every { preferences.notificationsEnabledFlow } returns flowOf(false)
        service.checkAndGenerateEnergyLowNotification(10)
        scope.advanceUntilIdle()
        val notifications = dao.getAllNotifications().first()
        assertEquals(0, notifications.size)
    }
}

