package com.example.socialbatterymanager.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R

class PeakUsageAdapter(private var peakUsages: List<PeakUsageTime>) :
    RecyclerView.Adapter<PeakUsageAdapter.PeakUsageViewHolder>() {

    class PeakUsageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeRangeText: TextView = view.findViewById(R.id.timeRangeText)
        val usageLevelText: TextView = view.findViewById(R.id.usageLevelText)
        val percentageText: TextView = view.findViewById(R.id.percentageText)
        val levelIndicator: View = view.findViewById(R.id.levelIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeakUsageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_peak_usage, parent, false)
        return PeakUsageViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeakUsageViewHolder, position: Int) {
        val peakUsage = peakUsages[position]
        holder.timeRangeText.text = peakUsage.timeRange
        holder.usageLevelText.text = peakUsage.level.displayName
        holder.percentageText.text = "${String.format("%.1f", peakUsage.percentage)}%"
        holder.levelIndicator.setBackgroundColor(peakUsage.level.color)
    }

    override fun getItemCount() = peakUsages.size

    fun updatePeakUsages(newPeakUsages: List<PeakUsageTime>) {
        peakUsages = newPeakUsages
        notifyDataSetChanged()
    }
}
