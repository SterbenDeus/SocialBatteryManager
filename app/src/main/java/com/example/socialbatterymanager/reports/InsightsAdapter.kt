package com.example.socialbatterymanager.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R

class InsightsAdapter(private var insights: List<AIInsight>) :
    RecyclerView.Adapter<InsightsAdapter.InsightViewHolder>() {

    class InsightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.insightTitleText)
        val descriptionText: TextView = view.findViewById(R.id.insightDescriptionText)
        val iconView: View = view.findViewById(R.id.insightIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_insight, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        val insight = insights[position]
        holder.titleText.text = insight.title
        holder.descriptionText.text = insight.description
        
        // Set icon color based on insight type
        val iconColor = when (insight.type) {
            InsightType.DO_MORE -> android.graphics.Color.parseColor("#4CAF50")
            InsightType.REDUCE -> android.graphics.Color.parseColor("#FF9800")
            InsightType.PATTERN_DETECTED -> android.graphics.Color.parseColor("#2196F3")
            InsightType.OPTIMIZE_CONTACTS -> android.graphics.Color.parseColor("#9C27B0")
        }
        holder.iconView.setBackgroundColor(iconColor)
    }

    override fun getItemCount() = insights.size

    fun updateInsights(newInsights: List<AIInsight>) {
        insights = newInsights
        notifyDataSetChanged()
    }
}
