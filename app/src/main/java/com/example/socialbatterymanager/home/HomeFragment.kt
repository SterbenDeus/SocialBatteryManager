package com.example.socialbatterymanager.home

import android.animation.ValueAnimator
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.utils.ErrorHandler
import com.example.socialbatterymanager.utils.NetworkConnectivityManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var lottieBattery: LottieAnimationView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var btnTestBattery: Button
    private lateinit var networkManager: NetworkConnectivityManager

    // Battery percent (0-100)
    private var batteryLevel = 65

    // Test values to cycle through for demo
    private val testLevels = listOf(95, 75, 55, 35, 15)
    private var testIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        lottieBattery = view.findViewById(R.id.lottieBattery)
        tvBatteryPercent = view.findViewById(R.id.tvBatteryPercent)
        btnTestBattery = view.findViewById(R.id.btnTestBattery)
        
        // Initialize network manager
        networkManager = NetworkConnectivityManager(requireContext())

        setupAccessibility()
        updateUI()
        observeNetworkState()

        btnTestBattery.setOnClickListener {
            try {
                val nextLevel = testLevels[testIndex]
                animateBatteryLevelChange(nextLevel)
                testIndex = (testIndex + 1) % testLevels.size
            } catch (e: Exception) {
                ErrorHandler.handleException(view, e) {
                    // Retry action
                    val nextLevel = testLevels[testIndex]
                    animateBatteryLevelChange(nextLevel)
                    testIndex = (testIndex + 1) % testLevels.size
                }
            }
        }
        
        btnLogout.setOnClickListener {
            userViewModel.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }

        return view
    }
    
    private fun setupAccessibility() {
        // Set content descriptions for accessibility
        lottieBattery.contentDescription = getString(R.string.battery_level_description, batteryLevel)
        tvBatteryPercent.contentDescription = getString(R.string.battery_percent_description, batteryLevel)
        btnTestBattery.contentDescription = getString(R.string.test_battery_description)
        
        // Enable focus for accessibility
        lottieBattery.isFocusable = true
        tvBatteryPercent.isFocusable = true
        btnTestBattery.isFocusable = true
        
        // Set minimum touch target size for accessibility
        val minTouchSize = resources.getDimensionPixelSize(R.dimen.min_touch_target_size)
        btnTestBattery.minimumHeight = minTouchSize
        btnTestBattery.minimumWidth = minTouchSize
    }
    
    private fun observeNetworkState() {
        lifecycleScope.launch {
            networkManager.isConnected.collect { isConnected ->
                if (!isConnected) {
                    view?.let { ErrorHandler.showOfflineSnackbar(it) }
                }
            }
        }
    }

    private fun animateBatteryLevelChange(targetLevel: Int) {
        val startLevel = batteryLevel
        val animator = ValueAnimator.ofInt(startLevel, targetLevel)
        animator.duration = 600
        animator.addUpdateListener { valueAnimator ->
            val level = valueAnimator.animatedValue as Int
            batteryLevel = level
            updateUI()
        }
        animator.start()
    }

    private fun updateUI() {
        lottieBattery.progress = batteryLevel / 100f
        setLottieBatteryColor(batteryLevel)
        tvBatteryPercent.text = "$batteryLevel%"
        
        // Update accessibility descriptions
        lottieBattery.contentDescription = getString(R.string.battery_level_description, batteryLevel)
        tvBatteryPercent.contentDescription = getString(R.string.battery_percent_description, batteryLevel)
        
        // Announce battery level changes for accessibility
        tvBatteryPercent.announceForAccessibility(getString(R.string.battery_level_changed, batteryLevel))
    }

    private fun setLottieBatteryColor(level: Int) {
        val color = when {
            level >= 80 -> "#4CAF50".toColorInt()    // Green
            level >= 60 -> "#FFEB3B".toColorInt()    // Yellow
            level >= 40 -> "#FF9800".toColorInt()    // Orange
            else -> "#F44336".toColorInt()           // Red
        }
        lottieBattery.addValueCallback(
            KeyPath("BatteryFill"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP))
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        networkManager.unregister()
    }
}
