package com.example.socialbatterymanager.features.home


import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.features.home.ui.SimpleHomeFragment
import com.example.socialbatterymanager.features.notifications.NotificationService
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SimpleHomeFragmentTest {

    @Test
    fun clickingAddEnergy_updatesLevelAndNotifies() {
        val controller = Robolectric.buildFragment(SimpleHomeFragment::class.java)
        val fragment = controller.get()
        val mockService = mockk<NotificationService>(
            relaxed = true,
            constructorArgs = arrayOf(
                ApplicationProvider.getApplicationContext(),
                mockk<AppDatabase>(relaxed = true),
                mockk<PreferencesManager>(relaxed = true),
                CoroutineScope(Dispatchers.Unconfined)
            )
        )
        val field = SimpleHomeFragment::class.java.getDeclaredField("notificationService")
        field.isAccessible = true
        field.set(fragment, mockService)
        controller.create().start().resume()
        val btnAdd = fragment.requireView().findViewById<Button>(R.id.btnAddEnergy)
        val tvEnergy = fragment.requireView().findViewById<TextView>(R.id.tvEnergyPercentage)
        btnAdd.performClick()

        assertEquals("70%", tvEnergy.text.toString())
        verify { mockService.checkAndGenerateEnergyLowNotification(70) }
    }

    @Test
    fun clickingRemoveEnergy_updatesLevelAndNotifies() {
        val controller = Robolectric.buildFragment(SimpleHomeFragment::class.java)
        val fragment = controller.get()
        val mockService = mockk<NotificationService>(
            relaxed = true,
            constructorArgs = arrayOf(
                ApplicationProvider.getApplicationContext(),
                mockk<AppDatabase>(relaxed = true),
                mockk<PreferencesManager>(relaxed = true),
                CoroutineScope(Dispatchers.Unconfined)
            )
        )
        val field = SimpleHomeFragment::class.java.getDeclaredField("notificationService")
        field.isAccessible = true
        field.set(fragment, mockService)
        controller.create().start().resume()
        val btnRemove = fragment.requireView().findViewById<Button>(R.id.btnRemoveEnergy)
        val tvEnergy = fragment.requireView().findViewById<TextView>(R.id.tvEnergyPercentage)
        btnRemove.performClick()

        assertEquals("60%", tvEnergy.text.toString())
        verify { mockService.checkAndGenerateEnergyLowNotification(60) }
    }

    @Test
    fun clickingNotifications_navigatesToNotifications() {
        val controller = Robolectric.buildFragment(SimpleHomeFragment::class.java)
        val fragment = controller.get()
        val mockService = mockk<NotificationService>(
            relaxed = true,
            constructorArgs = arrayOf(
                ApplicationProvider.getApplicationContext(),
                mockk<AppDatabase>(relaxed = true),
                mockk<PreferencesManager>(relaxed = true),
                CoroutineScope(Dispatchers.Unconfined)
            )
        )
        val field = SimpleHomeFragment::class.java.getDeclaredField("notificationService")
        field.isAccessible = true
        field.set(fragment, mockService)
        controller.create().start().resume()

        val navController = mockk<NavController>(relaxed = true)
        Navigation.setViewNavController(fragment.requireView(), navController)

        val btnNotifications = fragment.requireView().findViewById<ImageButton>(R.id.btnNotifications)
        btnNotifications.performClick()

        verify { navController.navigate(R.id.action_homeFragment_to_notificationsFragment) }
    }
}
