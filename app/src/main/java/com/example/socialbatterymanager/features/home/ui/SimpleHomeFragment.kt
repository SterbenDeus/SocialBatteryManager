package com.example.socialbatterymanager.features.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var notificationService: NotificationService
    private val viewModel: SimpleHomeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(requireContext())
                return SimpleHomeViewModel(db) as T
            }
        }
    }

    // Current energy level
    private var currentEnergyLevel = 65

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize notification service
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

        viewModel.weeklyActivityCount.observe(viewLifecycleOwner) { count ->
            tvWeeklyStats.text = getString(R.string.weekly_stats_message, count)
        }
        viewModel.loadWeeklyStats()

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
            viewLifecycleOwner.lifecycleScope.launch {
                notificationService.generateSampleNotifications()
            }
        }
    }
}