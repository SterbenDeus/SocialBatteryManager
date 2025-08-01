package com.example.socialbatterymanager.reports

import org.junit.Test
import org.junit.Assert.*

/**
 * Basic test for report models and data analysis
 */
class ReportTest {

    @Test
    fun testReportPeriodEnum() {
        assertEquals("Week", ReportPeriod.WEEK.displayName)
        assertEquals("Month", ReportPeriod.MONTH.displayName)
        assertEquals("Year", ReportPeriod.YEAR.displayName)
    }

    @Test
    fun testUsageLevelEnum() {
        assertEquals("High Drain", UsageLevel.HIGH_DRAIN.displayName)
        assertEquals("Medium Drain", UsageLevel.MEDIUM_DRAIN.displayName)
        assertEquals("Low Drain", UsageLevel.LOW_DRAIN.displayName)
        assertEquals("Recharge", UsageLevel.RECHARGE.displayName)
    }

    @Test
    fun testInsightTypeEnum() {
        assertEquals("Do More", InsightType.DO_MORE.displayName)
        assertEquals("Reduce", InsightType.REDUCE.displayName)
        assertEquals("Pattern Detected", InsightType.PATTERN_DETECTED.displayName)
        assertEquals("Optimize Contacts", InsightType.OPTIMIZE_CONTACTS.displayName)
    }
}