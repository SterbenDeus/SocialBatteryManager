package com.example.socialbatterymanager.features.people.ui

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.features.people.data.PersonStatsCalculator
import kotlinx.coroutines.launch
import java.util.Locale
import android.util.Log

class PersonStatsDialog(
    private val fragment: Fragment,
    private val person: Person
) {
    
    private val context: Context get() = fragment.requireContext()
    
    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_person_stats, null)
        
        val tvPersonName = view.findViewById<TextView>(R.id.tvPersonName)
        val tvLastInteraction = view.findViewById<TextView>(R.id.tvLastInteraction)
        val tvTotalActivities = view.findViewById<TextView>(R.id.tvTotalActivities)
        val tvAverageEnergy = view.findViewById<TextView>(R.id.tvAverageEnergy)
        val tvMostCommonMood = view.findViewById<TextView>(R.id.tvMostCommonMood)
        val tvEnergyTrend = view.findViewById<TextView>(R.id.tvEnergyTrend)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .create()
        
        // Set person name
        tvPersonName.text = person.name
        
        // Calculate and display stats
        fragment.lifecycleScope.launch {
            try {
                val statsCalculator = PersonStatsCalculator(context)
                val stats = statsCalculator.calculateStats(person)
                fragment.activity?.runOnUiThread {
                    tvTotalActivities.text = stats.totalActivities.toString()
                    tvAverageEnergy.text = String.format(Locale.ROOT, "%.1f", stats.averageEnergyImpact)
                    tvMostCommonMood.text = stats.mostCommonMood
                    tvEnergyTrend.text = stats.energyTrend.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }

                    val energyTrendColor = when (stats.energyTrend) {
                        "positive" -> context.getColor(android.R.color.holo_green_dark)
                        "negative" -> context.getColor(android.R.color.holo_red_dark)
                        else -> context.getColor(android.R.color.darker_gray)
                    }
                    tvEnergyTrend.setTextColor(energyTrendColor)

                    val averageEnergyColor = when {
                        stats.averageEnergyImpact > 0.5 -> context.getColor(android.R.color.holo_green_dark)
                        stats.averageEnergyImpact < -0.5 -> context.getColor(android.R.color.holo_red_dark)
                        else -> context.getColor(android.R.color.darker_gray)
                    }
                    tvAverageEnergy.setTextColor(averageEnergyColor)
                }
            } catch (e: Exception) {
                Log.e("PersonStatsDialog", "Error calculating stats", e)
                fragment.activity?.runOnUiThread {
                    tvTotalActivities.text = context.getString(R.string.error)
                    tvAverageEnergy.text = context.getString(R.string.error)
                    tvMostCommonMood.text = context.getString(R.string.error)
                    tvEnergyTrend.text = context.getString(R.string.error)
                    tvLastInteraction.text = context.getString(R.string.error_loading_stats)
                }
            }
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
}