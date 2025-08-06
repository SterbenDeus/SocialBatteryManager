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
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.databinding.FragmentHomeBinding
import com.example.socialbatterymanager.features.notifications.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimpleHomeFragment : Fragment() {

    private lateinit var btnNotifications: ImageButton
    private lateinit var btnAddEnergy: Button
    private lateinit var btnRemoveEnergy: Button
    private lateinit var btnTestBattery: Button
    private lateinit var tvWeeklyStats: TextView
    private lateinit var tvEnergyPercentage: TextView
    private lateinit var notificationService: NotificationService

    private val viewModel: SimpleHomeViewModel by viewModels()

    // Current energy level
    private var currentEnergyLevel = 65

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize notification service
        notificationService = NotificationService(requireContext())

        // Initialize views
        btnNotifications = requireView().findViewById(R.id.btnNotifications)
        btnAddEnergy = requireView().findViewById(R.id.btnAddEnergy)
        btnRemoveEnergy = requireView().findViewById(R.id.btnRemoveEnergy)
        btnTestBattery = requireView().findViewById(R.id.btnTestBattery)
        tvWeeklyStats = requireView().findViewById(R.id.tvWeeklyStats)
        tvEnergyPercentage = requireView().findViewById(R.id.tvEnergyPercentage)
        setupClickListeners()
        if (BuildConfig.DEBUG) {
            generateSampleNotifications()
        }

        viewModel.weeklyActivityCount.observe(viewLifecycleOwner) { count ->
            binding.tvWeeklyStats.text = getString(R.string.weekly_stats_message, count)
        }
        viewModel.loadWeeklyStats()

        // Initialize energy display
        binding.tvEnergyPercentage.text = getString(R.string.energy_percentage, currentEnergyLevel)

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
        tvEnergyPercentage.text = getString(R.string.energy_percentage, newLevel)

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
