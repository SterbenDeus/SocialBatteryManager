package com.example.socialbatterymanager.features.home.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.databinding.FragmentHomeBinding
import com.example.socialbatterymanager.features.notifications.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimpleHomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!

    private lateinit var notificationService: NotificationService

    private val viewModel: SimpleHomeViewModel by viewModels()

    // Current energy level
    private var currentEnergyLevel = 65

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize notification service
        notificationService = NotificationService(requireContext())

        setupClickListeners()
        if (BuildConfig.DEBUG) {
            generateSampleNotifications()
        }

        viewModel.weeklyActivityCount.observe(viewLifecycleOwner) { count ->
            binding.tvWeeklyStats.text = getString(R.string.weekly_stats_message, count)
        }
        viewModel.loadWeeklyStats()

        // Initialize energy display
        binding.tvEnergyPercentage.text = "$currentEnergyLevel%"

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupClickListeners() {
        binding.btnAddEnergy.setOnClickListener {
            updateEnergyLevel(5)
        }

        binding.btnRemoveEnergy.setOnClickListener {
            updateEnergyLevel(-5)
        }

        binding.btnTestBattery.setOnClickListener {
            val testLevels = listOf(95, 75, 55, 35, 15)
            val randomLevel = testLevels.random()
            updateEnergyLevel(randomLevel - currentEnergyLevel)
        }
    }

    private fun updateEnergyLevel(change: Int) {
        val newLevel = (currentEnergyLevel + change).coerceIn(0, 100)
        currentEnergyLevel = newLevel

        // Update UI
        binding.tvEnergyPercentage.text = "$newLevel%"

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

