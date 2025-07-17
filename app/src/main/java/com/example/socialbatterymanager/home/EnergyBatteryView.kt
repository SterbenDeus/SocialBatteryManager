package com.example.socialbatterymanager.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.socialbatterymanager.R

class EnergyBatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val lottieBattery: LottieAnimationView
    private val tvBatteryPercent: TextView
    private val tvCapacity: TextView
    private val tvRemaining: TextView
    private val tvEstTime: TextView

    private var currentBatteryLevel = 65

    init {
        LayoutInflater.from(context).inflate(R.layout.view_energy_battery, this, true)
        
        lottieBattery = findViewById(R.id.lottieBattery)
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        tvCapacity = findViewById(R.id.tvCapacity)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvEstTime = findViewById(R.id.tvEstTime)

        updateUI()
    }

    fun setBatteryLevel(level: Int, animate: Boolean = true) {
        if (animate) {
            animateBatteryLevelChange(level)
        } else {
            currentBatteryLevel = level
            updateUI()
        }
    }

    fun getBatteryLevel(): Int = currentBatteryLevel

    private fun animateBatteryLevelChange(targetLevel: Int) {
        val startLevel = currentBatteryLevel
        val animator = ValueAnimator.ofInt(startLevel, targetLevel)
        animator.duration = 600
        animator.addUpdateListener { valueAnimator ->
            val level = valueAnimator.animatedValue as Int
            currentBatteryLevel = level
            updateUI()
        }
        animator.start()
    }

    private fun updateUI() {
        lottieBattery.progress = currentBatteryLevel / 100f
        setLottieBatteryColor(currentBatteryLevel)
        tvBatteryPercent.text = "$currentBatteryLevel%"
        tvCapacity.text = "100"
        tvRemaining.text = currentBatteryLevel.toString()
        
        // Estimate time to reach a target level (e.g., 80%)
        val targetLevel = 80
        val estimatedDays = if (currentBatteryLevel < targetLevel) {
            ((targetLevel - currentBatteryLevel) / 5).coerceAtLeast(1)
        } else {
            0
        }
        
        tvEstTime.text = if (estimatedDays > 0) "${estimatedDays}d" else "Full"
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