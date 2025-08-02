package com.example.socialbatterymanager.reports

import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.EnergyLog
import java.util.*
import kotlin.random.Random

/**
 * Utility class to generate sample data for testing the reports functionality
 */
object SampleDataGenerator {

    /**
     * Generate sample activities for the last N days
     */
    fun generateSampleActivities(days: Int = 30): List<ActivityEntity> {
        val activities = mutableListOf<ActivityEntity>()
        val random = Random(42) // Fixed seed for consistent data
        val now = System.currentTimeMillis()
        
        val activityTypes = listOf("Meeting", "Social Event", "Call", "Presentation", "Workshop", "Conference")
        val moods = listOf("Energetic", "Happy", "Neutral", "Tired", "Stressed", "Overwhelmed")
        val people = listOf("Emma", "Grace", "John", "Sarah", "Mike", "Lisa", "Tom", "Anna")

        for (i in 0 until days) {
            val dayStart = now - (i * 24 * 60 * 60 * 1000)
            val activitiesPerDay = random.nextInt(1, 6) // 1-5 activities per day

            for (j in 0 until activitiesPerDay) {
                val hourOffset = random.nextInt(0, 24) * 60 * 60 * 1000
                val activityTime = dayStart - hourOffset

                activities.add(
                    ActivityEntity(
                        id = activities.size + 1,
                        name = "${activityTypes.random(random)} ${j + 1}",
                        type = activityTypes.random(random),
                        energy = random.nextInt(1, 11), // 1-10 energy scale
                        people = people.shuffled(random).take(random.nextInt(1, 4)).joinToString(", "),
                        mood = moods.random(random),
                        notes = "Sample activity note ${j + 1}",
                        date = activityTime,
                        duration = random.nextLong(15, 180), // 15-180 minutes
                        socialInteractionLevel = random.nextInt(1, 11),
                        stressLevel = random.nextInt(1, 11)
                    )
                )
            }
        }

        return activities.sortedByDescending { it.date }
    }

    /**
     * Generate sample energy logs for the last N days
     */
    fun generateSampleEnergyLogs(days: Int = 30): List<EnergyLog> {
        val energyLogs = mutableListOf<EnergyLog>()
        val random = Random(42) // Fixed seed for consistent data
        val now = System.currentTimeMillis()
        
        var currentEnergyLevel = 70 // Starting energy level

        for (i in 0 until days) {
            val dayStart = now - (i * 24 * 60 * 60 * 1000)
            val logsPerDay = random.nextInt(3, 8) // 3-7 energy logs per day

            for (j in 0 until logsPerDay) {
                val hourOffset = random.nextInt(0, 24) * 60 * 60 * 1000
                val logTime = dayStart - hourOffset

                // Simulate energy changes
                val changeAmount = random.nextInt(-20, 15) // Energy can decrease or increase
                currentEnergyLevel = (currentEnergyLevel + changeAmount).coerceIn(0, 100)

                energyLogs.add(
                    EnergyLog(
                        id = energyLogs.size + 1,
                        energyLevel = currentEnergyLevel,
                        timestamp = logTime,
                        changeAmount = changeAmount,
                        reason = when {
                            changeAmount > 0 -> "Rest/Recovery"
                            changeAmount < -10 -> "High stress activity"
                            else -> "Normal activity"
                        }
                    )
                )
            }
        }

        return energyLogs.sortedByDescending { it.timestamp }
    }

    /**
     * Generate sample peak usage times
     */
    fun generateSamplePeakUsageTimes(): List<PeakUsageTime> {
        return listOf(
            PeakUsageTime("9:00 AM - 12:00 PM", UsageLevel.HIGH_DRAIN, 32.5f),
            PeakUsageTime("2:00 PM - 5:00 PM", UsageLevel.MEDIUM_DRAIN, 28.3f),
            PeakUsageTime("7:00 PM - 10:00 PM", UsageLevel.LOW_DRAIN, 22.1f),
            PeakUsageTime("10:00 PM - 12:00 AM", UsageLevel.RECHARGE, 17.1f)
        )
    }

    /**
     * Generate sample AI insights
     */
    fun generateSampleAIInsights(): List<AIInsight> {
        return listOf(
            AIInsight(
                type = InsightType.DO_MORE,
                title = "Do More",
                description = "Schedule more activities between 7-9 PM when your energy drain is lowest. You could fit 2-3 more social interactions."
            ),
            AIInsight(
                type = InsightType.REDUCE,
                title = "Reduce",
                description = "Limit morning meetings. Your energy drops 5-7x faster during these activities. Consider reducing your peak hours."
            ),
            AIInsight(
                type = InsightType.PATTERN_DETECTED,
                title = "Pattern Detected",
                description = "Your energy recovers 25% faster on weekends. Plan demanding social activities for Friday-Sunday."
            ),
            AIInsight(
                type = InsightType.OPTIMIZE_CONTACTS,
                title = "Optimize Contacts",
                description = "Interactions with Emma and Grace drain 50% less energy for you. Spending more time with similar personality types."
            )
        )
    }

    /**
     * Generate sample capacity growth data
     */
    fun generateSampleCapacityGrowth(): List<CapacityGrowth> {
        return listOf(
            CapacityGrowth("Weekly Growth", 15.0f, true),
            CapacityGrowth("Monthly Growth", 8.0f, true)
        )
    }
}