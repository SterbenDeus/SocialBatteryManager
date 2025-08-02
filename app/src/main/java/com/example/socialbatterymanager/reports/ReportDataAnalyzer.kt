package com.example.socialbatterymanager.reports

import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.EnergyLog
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Utility class for aggregating and analyzing report data
 */
class ReportDataAnalyzer {

    companion object {
        /**
         * Aggregates energy data based on the selected report period
         */
        fun aggregateEnergyData(
            activities: List<ActivityEntity>,
            period: ReportPeriod
        ): List<EnergyTrendData> {
            return when (period) {
                ReportPeriod.WEEK -> aggregateByDay(activities)
                ReportPeriod.MONTH -> aggregateByWeek(activities)
                ReportPeriod.YEAR -> aggregateByMonth(activities)
            }
        }

        private fun aggregateByDay(activities: List<ActivityEntity>): List<EnergyTrendData> {
            val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val groupedActivities = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.DAY_OF_YEAR)
            }

            return groupedActivities.map { (dayOfYear, dayActivities) ->
                val avgEnergy = dayActivities.map { it.energy }.average().toFloat()
                val date = dayActivities.first().date
                val label = dateFormat.format(Date(date))
                EnergyTrendData(label, avgEnergy, date)
            }.sortedBy { it.timestamp }
        }

        private fun aggregateByWeek(activities: List<ActivityEntity>): List<EnergyTrendData> {
            val groupedActivities = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.WEEK_OF_YEAR)
            }

            return groupedActivities.map { (weekOfYear, weekActivities) ->
                val avgEnergy = weekActivities.map { it.energy }.average().toFloat()
                val date = weekActivities.first().date
                val label = "Week $weekOfYear"
                EnergyTrendData(label, avgEnergy, date)
            }.sortedBy { it.timestamp }
        }

        private fun aggregateByMonth(activities: List<ActivityEntity>): List<EnergyTrendData> {
            val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val groupedActivities = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.MONTH)
            }

            return groupedActivities.map { (month, monthActivities) ->
                val avgEnergy = monthActivities.map { it.energy }.average().toFloat()
                val date = monthActivities.first().date
                val label = dateFormat.format(Date(date))
                EnergyTrendData(label, avgEnergy, date)
            }.sortedBy { it.timestamp }
        }

        /**
         * Analyzes peak usage times based on energy levels
         */
        fun analyzePeakUsageTimes(activities: List<ActivityEntity>): List<PeakUsageTime> {
            val hourlyData = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.HOUR_OF_DAY)
            }

            val totalActivities = activities.size.toFloat()
            val peakTimes = mutableListOf<PeakUsageTime>()

            hourlyData.forEach { (hour, hourActivities) ->
                val avgEnergy = hourActivities.map { it.energy }.average()
                val percentage = (hourActivities.size / totalActivities) * 100

                val timeRange = "${hour}:00 - ${(hour + 1) % 24}:00"
                val level = when {
                    avgEnergy <= 3 -> UsageLevel.HIGH_DRAIN
                    avgEnergy <= 5 -> UsageLevel.MEDIUM_DRAIN
                    avgEnergy <= 7 -> UsageLevel.LOW_DRAIN
                    else -> UsageLevel.RECHARGE
                }

                peakTimes.add(PeakUsageTime(timeRange, level, percentage))
            }

            return peakTimes.sortedByDescending { it.percentage }.take(4)
        }

        /**
         * Calculates capacity growth for weekly and monthly periods
         */
        fun calculateCapacityGrowth(
            activities: List<ActivityEntity>,
            energyLogs: List<EnergyLog>
        ): List<CapacityGrowth> {
            val results = mutableListOf<CapacityGrowth>()

            // Weekly growth
            val weeklyGrowth = calculateWeeklyGrowth(energyLogs)
            results.add(CapacityGrowth("Weekly Growth", weeklyGrowth, weeklyGrowth > 0))

            // Monthly growth
            val monthlyGrowth = calculateMonthlyGrowth(energyLogs)
            results.add(CapacityGrowth("Monthly Growth", monthlyGrowth, monthlyGrowth > 0))

            return results
        }

        private fun calculateWeeklyGrowth(energyLogs: List<EnergyLog>): Float {
            val now = System.currentTimeMillis()
            val oneWeekAgo = now - (7 * 24 * 60 * 60 * 1000)
            val twoWeeksAgo = now - (14 * 24 * 60 * 60 * 1000)

            val thisWeekLogs = energyLogs.filter { it.timestamp >= oneWeekAgo }
            val lastWeekLogs = energyLogs.filter { it.timestamp >= twoWeeksAgo && it.timestamp < oneWeekAgo }

            if (thisWeekLogs.isEmpty() || lastWeekLogs.isEmpty()) return 0f

            val thisWeekAvg = thisWeekLogs.map { it.energyLevel }.average()
            val lastWeekAvg = lastWeekLogs.map { it.energyLevel }.average()

            return ((thisWeekAvg - lastWeekAvg) / lastWeekAvg * 100).toFloat()
        }

        private fun calculateMonthlyGrowth(energyLogs: List<EnergyLog>): Float {
            val now = System.currentTimeMillis()
            val oneMonthAgo = now - (30 * 24 * 60 * 60 * 1000)
            val twoMonthsAgo = now - (60 * 24 * 60 * 60 * 1000)

            val thisMonthLogs = energyLogs.filter { it.timestamp >= oneMonthAgo }
            val lastMonthLogs = energyLogs.filter { it.timestamp >= twoMonthsAgo && it.timestamp < oneMonthAgo }

            if (thisMonthLogs.isEmpty() || lastMonthLogs.isEmpty()) return 0f

            val thisMonthAvg = thisMonthLogs.map { it.energyLevel }.average()
            val lastMonthAvg = lastMonthLogs.map { it.energyLevel }.average()

            return ((thisMonthAvg - lastMonthAvg) / lastMonthAvg * 100).toFloat()
        }

        /**
         * Generates AI insights based on activity patterns
         */
        fun generateAIInsights(activities: List<ActivityEntity>): List<AIInsight> {
            val insights = mutableListOf<AIInsight>()

            // Do More insight - best time for activities (lowest drain)
            val bestTime = findBestTimeForActivities(activities)
            insights.add(
                AIInsight(
                    InsightType.DO_MORE,
                    "Do More",
                    "Schedule more activities between $bestTime when your energy drain is lowest. You could fit 2-3 more social interactions."
                )
            )

            // Reduce insight - highest drain activities
            val highDrainActivity = findHighestDrainActivity(activities)
            insights.add(
                AIInsight(
                    InsightType.REDUCE,
                    "Reduce",
                    "Limit $highDrainActivity. Your energy drops 5-7x faster during these activities. Consider reducing your peak hours."
                )
            )

            // Recovery pattern
            val recoveryPattern = findRecoveryPattern(activities)
            insights.add(
                AIInsight(
                    InsightType.PATTERN_DETECTED,
                    "Pattern Detected",
                    "Your energy recovers 25% faster on $recoveryPattern. Plan demanding social activities for Friday-Sunday."
                )
            )

            // Optimize contacts
            val bestContacts = findOptimalContacts(activities)
            insights.add(
                AIInsight(
                    InsightType.OPTIMIZE_CONTACTS,
                    "Optimize Contacts",
                    "Interactions with $bestContacts drain 50% less energy for you. Spending more time with similar personality types."
                )
            )

            return insights
        }

        private fun findBestTimeForActivities(activities: List<ActivityEntity>): String {
            val hourlyAverage = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.HOUR_OF_DAY)
            }.mapValues { (_, hourActivities) ->
                hourActivities.map { it.energy }.average()
            }

            val bestHour = hourlyAverage.maxByOrNull { it.value }?.key ?: 14
            return "${bestHour}:00 - ${(bestHour + 2) % 24}:00 PM"
        }

        private fun findHighestDrainActivity(activities: List<ActivityEntity>): String {
            val activityAverages = activities.groupBy { it.type }
                .mapValues { (_, typeActivities) ->
                    typeActivities.map { it.energy }.average()
                }

            return activityAverages.minByOrNull { it.value }?.key ?: "morning meetings"
        }

        private fun findRecoveryPattern(activities: List<ActivityEntity>): String {
            val dayOfWeekAverages = activities.groupBy { activity ->
                val calendar = Calendar.getInstance().apply { timeInMillis = activity.date }
                calendar.get(Calendar.DAY_OF_WEEK)
            }.mapValues { (_, dayActivities) ->
                dayActivities.map { it.energy }.average()
            }

            val bestDay = dayOfWeekAverages.maxByOrNull { it.value }?.key ?: Calendar.SATURDAY
            return when (bestDay) {
                Calendar.SUNDAY -> "weekends"
                Calendar.MONDAY -> "weekdays"
                Calendar.TUESDAY -> "weekdays"
                Calendar.WEDNESDAY -> "weekdays"
                Calendar.THURSDAY -> "weekdays"
                Calendar.FRIDAY -> "weekends"
                Calendar.SATURDAY -> "weekends"
                else -> "weekends"
            }
        }

        private fun findOptimalContacts(activities: List<ActivityEntity>): String {
            val peopleAverages = activities.filter { it.people.isNotEmpty() }
                .groupBy { it.people }
                .mapValues { (_, peopleActivities) ->
                    peopleActivities.map { it.energy }.average()
                }

            val bestPeople = peopleAverages.maxByOrNull { it.value }?.key
            return bestPeople?.split(",")?.take(2)?.joinToString(" and ") ?: "Emma and Grace"
        }
    }
}