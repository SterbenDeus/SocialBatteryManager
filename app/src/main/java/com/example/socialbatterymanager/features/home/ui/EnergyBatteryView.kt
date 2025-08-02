package com.example.socialbatterymanager.features.home.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.example.socialbatterymanager.R

class EnergyBatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val energyFill: View

    private var currentBatteryLevel = 65

    init {
        LayoutInflater.from(context).inflate(R.layout.view_energy_battery, this, true)
        
        energyFill = findViewById(R.id.energyFill)

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
        // Update energy fill height based on battery level
        val layoutParams = energyFill.layoutParams
        val maxHeight = 240 // Max height in dp (matches background battery height)
        val targetHeight = (maxHeight * currentBatteryLevel / 100).coerceAtLeast(0)
        
        layoutParams.height = dpToPx(targetHeight)
        energyFill.layoutParams = layoutParams
        
        // Update color based on energy level
        updateEnergyColor()
    }

    private fun updateEnergyColor() {
        val colorResId = when {
            currentBatteryLevel >= 80 -> R.color.energy_high
            currentBatteryLevel >= 60 -> R.color.energy_medium
            currentBatteryLevel >= 30 -> R.color.energy_low
            else -> R.color.energy_recharge
        }
        
        energyFill.setBackgroundResource(colorResId)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun getEnergyLevelLabel(): String {
        return when {
            currentBatteryLevel >= 80 -> context.getString(R.string.energy_level_high)
            currentBatteryLevel >= 60 -> context.getString(R.string.energy_level_medium)
            currentBatteryLevel >= 30 -> context.getString(R.string.energy_level_low)
            else -> context.getString(R.string.energy_level_recharge)
        }
    }
}