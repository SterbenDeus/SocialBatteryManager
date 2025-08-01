package com.example.socialbatterymanager.features.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.features.notifications.NotificationService
import kotlinx.coroutines.launch

class SimpleHomeFragment : Fragment() {

    private lateinit var btnNotifications: ImageButton
    private lateinit var btnAddEnergy: Button
    private lateinit var btnRemoveEnergy: Button
    private lateinit var btnTestBattery: Button
    private lateinit var tvWeeklyStats: TextView
    private lateinit var tvEnergyLevel: TextView
    private lateinit var database: AppDatabase
    private lateinit var notificationService: NotificationService

    // Current energy level
    private var currentEnergyLevel = 65

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize database and notification service
        database = AppDatabase.getDatabase(requireContext())
        notificationService = NotificationService(requireContext())

        // Initialize views
        btnNotifications = view.findViewById(R.id.btnNotifications)
        btnAddEnergy = view.findViewById(R.id.btnAddEnergy)
        btnRemoveEnergy = view.findViewById(R.id.btnRemoveEnergy)
        btnTestBattery = view.findViewById(R.id.btnTestBattery)
        tvWeeklyStats = view.findViewById(R.id.tvWeeklyStats)
        tvEnergyLevel = view.findViewById(R.id.tvEnergyLevel)

        setupClickListeners()
        if (BuildConfig.DEBUG) {
            generateSampleNotifications()
        }
        updateWeeklyStats()

        return view
    }

    private fun setupClickListeners() {
        btnNotifications.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment)
        }

        btnAddEnergy.setOnClickListener {
            updateEnergyLevel(5)
        }

        btnRemoveEnergy.setOnClickListener {
            updateEnergyLevel(-5)
        }

        btnTestBattery.setOnClickListener {
            val testLevels = listOf(95, 75, 55, 35, 15)
            val randomLevel = testLevels.random()
            updateEnergyLevel(randomLevel - currentEnergyLevel)
        }
    }

    private fun updateEnergyLevel(change: Int) {
        val newLevel = (currentEnergyLevel + change).coerceIn(0, 100)
        currentEnergyLevel = newLevel
        
        // Update UI
        tvEnergyLevel.text = "$newLevel%"
        
        // Check if we should generate a low energy notification
        notificationService.checkAndGenerateEnergyLowNotification(newLevel)
    }

    private fun generateSampleNotifications() {
        if (BuildConfig.DEBUG) {
            lifecycleScope.launch {
                notificationService.generateSampleNotifications()
            }
        }
    }

    private fun updateWeeklyStats() {
        lifecycleScope.launch {
            val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            try {
                val activityCount = database.activityDao().getActiveActivityCount()
                tvWeeklyStats.text = getString(R.string.weekly_stats_message, activityCount)
            } catch (e: Exception) {
                tvWeeklyStats.text = getString(R.string.weekly_stats_loading)
            }
        }
    }
}