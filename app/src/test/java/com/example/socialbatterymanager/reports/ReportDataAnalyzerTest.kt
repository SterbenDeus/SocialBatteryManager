package com.example.socialbatterymanager.reports

import com.example.socialbatterymanager.data.model.ActivityEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class ReportDataAnalyzerTest {

    @Test
    fun aggregateEnergyData_groupsByDay() {
        Locale.setDefault(Locale.US)
        val cal = Calendar.getInstance()
        cal.set(2023, Calendar.JANUARY, 2, 10, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val monday = cal.timeInMillis
        cal.set(2023, Calendar.JANUARY, 3, 10, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val tuesday = cal.timeInMillis

        val activities = listOf(
            ActivityEntity(name = "A", type = "work", energy = 4, people = "", mood = "", notes = "", date = monday),
            ActivityEntity(name = "B", type = "work", energy = 6, people = "", mood = "", notes = "", date = tuesday)
        )

        val result = ReportDataAnalyzer.aggregateEnergyData(activities, ReportPeriod.WEEK)

        assertEquals(listOf("Mon", "Tue"), result.map { it.label })
        assertEquals(listOf(4f, 6f), result.map { it.value })
    }

    private fun activityAt(hour: Int, energy: Int): ActivityEntity {
        val cal = Calendar.getInstance()
        cal.set(2023, Calendar.JANUARY, 1, hour, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return ActivityEntity(
            name = "A",
            type = "work",
            energy = energy,
            people = "",
            mood = "",
            notes = "",
            date = cal.timeInMillis
        )
    }

    @Test
    fun analyzePeakUsageTimes_classifiesUsageLevels() {
        val activities = mutableListOf<ActivityEntity>().apply {
            repeat(3) { add(activityAt(9, 2)) }
            repeat(2) { add(activityAt(14, 8)) }
            add(activityAt(18, 4))
        }

        val peakTimes = ReportDataAnalyzer.analyzePeakUsageTimes(activities)

        assertEquals("9:00 - 10:00", peakTimes[0].timeRange)
        assertEquals(UsageLevel.HIGH_DRAIN, peakTimes[0].level)
        assertEquals(50f, peakTimes[0].percentage, 0.1f)
        assertEquals(UsageLevel.RECHARGE, peakTimes[1].level)
        assertEquals(UsageLevel.MEDIUM_DRAIN, peakTimes[2].level)
    }

    @Test
    fun generateAIInsights_returnsAllTypes() {
        val insights = ReportDataAnalyzer.generateAIInsights(emptyList())
        assertEquals(4, insights.size)
        val types = insights.map { it.type }.toSet()
        val expected = setOf(
            InsightType.DO_MORE,
            InsightType.REDUCE,
            InsightType.PATTERN_DETECTED,
            InsightType.OPTIMIZE_CONTACTS
        )
        assertEquals(expected, types)
    }
}
