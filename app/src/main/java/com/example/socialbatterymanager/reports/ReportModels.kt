package com.example.socialbatterymanager.reports

/**
 * Enum for different report time periods
 */
enum class ReportPeriod(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

/**
 * Enum for energy usage levels
 */
enum class UsageLevel(val displayName: String, val color: Int) {
    HIGH_DRAIN("High Drain", android.graphics.Color.parseColor("#FF5252")),
    MEDIUM_DRAIN("Medium Drain", android.graphics.Color.parseColor("#FFC107")),
    LOW_DRAIN("Low Drain", android.graphics.Color.parseColor("#4CAF50")),
    RECHARGE("Recharge", android.graphics.Color.parseColor("#2196F3"))
}

/**
 * Data class for peak usage time entries
 */
data class PeakUsageTime(
    val timeRange: String,
    val level: UsageLevel,
    val percentage: Float
)

/**
 * Data class for capacity growth data
 */
data class CapacityGrowth(
    val period: String,
    val growthPercentage: Float,
    val isPositive: Boolean
)

/**
 * Data class for AI insights
 */
data class AIInsight(
    val type: InsightType,
    val title: String,
    val description: String,
    val iconResId: Int = 0
)

/**
 * Enum for different types of AI insights
 */
enum class InsightType(val displayName: String) {
    DO_MORE("Do More"),
    REDUCE("Reduce"),
    PATTERN_DETECTED("Pattern Detected"),
    OPTIMIZE_CONTACTS("Optimize Contacts")
}

/**
 * Data class for aggregated energy data by time period
 */
data class EnergyTrendData(
    val label: String,
    val value: Float,
    val timestamp: Long
)

/**
 * Data class for report summary statistics
 */
data class ReportSummary(
    val averageEnergy: Float,
    val totalActivities: Int,
    val mostCommonMood: String,
    val energyChange: Float,
    val period: ReportPeriod
)
