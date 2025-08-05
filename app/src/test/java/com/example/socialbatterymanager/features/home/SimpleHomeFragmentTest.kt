package com.example.socialbatterymanager.features.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.features.home.ui.SimpleHomeFragment
import com.example.socialbatterymanager.features.notifications.NotificationService
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class SimpleHomeFragmentTest {

    @Test
    fun clickingAddEnergy_updatesLevelAndNotifies() {
        val controller = Robolectric.buildFragment(SimpleHomeFragment::class.java).create().start().resume()
        val fragment = controller.get()
        val mockService = mockk<NotificationService>(relaxed = true, constructorArgs = arrayOf(fragment.requireContext()))
        val field = SimpleHomeFragment::class.java.getDeclaredField("notificationService")
        field.isAccessible = true
        field.set(fragment, mockService)

        val btnAdd = fragment.binding.btnAddEnergy
        val tvEnergy = fragment.binding.tvEnergyPercentage

        btnAdd.performClick()

        assertEquals("70%", tvEnergy.text.toString())
        verify { mockService.checkAndGenerateEnergyLowNotification(70) }
    }

    @Test
    fun clickingRemoveEnergy_updatesLevelAndNotifies() {
        val controller = Robolectric.buildFragment(SimpleHomeFragment::class.java).create().start().resume()
        val fragment = controller.get()
        val mockService = mockk<NotificationService>(relaxed = true, constructorArgs = arrayOf(fragment.requireContext()))
        val field = SimpleHomeFragment::class.java.getDeclaredField("notificationService")
        field.isAccessible = true
        field.set(fragment, mockService)

        val btnRemove = fragment.binding.btnRemoveEnergy
        val tvEnergy = fragment.binding.tvEnergyPercentage

        btnRemove.performClick()

        assertEquals("60%", tvEnergy.text.toString())
        verify { mockService.checkAndGenerateEnergyLowNotification(60) }
    }
}
