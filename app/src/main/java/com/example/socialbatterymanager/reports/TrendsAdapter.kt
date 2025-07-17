package com.example.socialbatterymanager.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import java.text.SimpleDateFormat
import java.util.*

data class TrendData(
    val date: String,
    val avgEnergy: Double,
    val avgMood: String,
    val activityCount: Int
)

class TrendsAdapter(private var trends: List<TrendData>) :
    RecyclerView.Adapter<TrendsAdapter.TrendViewHolder>() {

    class TrendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val energyText: TextView = view.findViewById(R.id.energyText)
        val moodText: TextView = view.findViewById(R.id.moodText)
        val activityCountText: TextView = view.findViewById(R.id.activityCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return TrendViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendViewHolder, position: Int) {
        val trend = trends[position]
        holder.dateText.text = trend.date
        holder.energyText.text = "Energy: ${String.format("%.1f", trend.avgEnergy)}"
        holder.moodText.text = "Mood: ${trend.avgMood}"
        holder.activityCountText.text = "Activities: ${trend.activityCount}"
    }

    override fun getItemCount() = trends.size

    fun updateTrends(newTrends: List<TrendData>) {
        trends = newTrends
        notifyDataSetChanged()
    }
}