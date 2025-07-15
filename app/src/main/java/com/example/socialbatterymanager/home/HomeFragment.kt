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
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.socialbatterymanager.R

class HomeFragment : Fragment() {

    private lateinit var lottieBattery: LottieAnimationView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var btnTestBattery: Button

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

        updateUI()

        btnTestBattery.setOnClickListener {
            val nextLevel = testLevels[testIndex]
            animateBatteryLevelChange(nextLevel)
            testIndex = (testIndex + 1) % testLevels.size
        }

        return view
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
}
