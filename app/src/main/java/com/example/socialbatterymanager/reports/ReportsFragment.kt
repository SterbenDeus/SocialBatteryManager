package com.example.socialbatterymanager.reports

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.database.EnergyLogDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.EnergyLog
import com.example.socialbatterymanager.data.database.AppDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var database: AppDatabase
    private lateinit var activityDao: ActivityDao
    private lateinit var energyLogDao: EnergyLogDao
    
    // Adapters
    private lateinit var peakUsageAdapter: PeakUsageAdapter
    private lateinit var insightsAdapter: InsightsAdapter
    
    // Charts
    private lateinit var energyChart: LineChart
    private lateinit var efficiencyChart: BarChart
    
    // RecyclerViews
    private lateinit var peakUsageRecyclerView: RecyclerView
    private lateinit var insightsRecyclerView: RecyclerView
    
    // Period buttons
    private lateinit var weekButton: Button
    private lateinit var monthButton: Button
    private lateinit var yearButton: Button
    
    // Text views
    private lateinit var energyPeriodLabel: TextView
    private lateinit var energySubLabel: TextView
    private lateinit var weeklyGrowthPercentage: TextView
    private lateinit var monthlyGrowthPercentage: TextView
    
    // Current selected period
    private var currentPeriod: ReportPeriod = ReportPeriod.WEEK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java,
            "social_battery_db"
        ).build()
        activityDao = database.activityDao()
        energyLogDao = database.energyLogDao()

        // Initialize views
        initializeViews(view)
        
        // Setup adapters and RecyclerViews
        setupRecyclerViews()
        
        // Setup period buttons
        setupPeriodButtons()

        // Load and display data
        loadReportData()
    }

    private fun initializeViews(view: View) {
        energyChart = view.findViewById(R.id.energyChart)
        efficiencyChart = view.findViewById(R.id.efficiencyChart)
        peakUsageRecyclerView = view.findViewById(R.id.peakUsageRecyclerView)
        insightsRecyclerView = view.findViewById(R.id.insightsRecyclerView)
        
        weekButton = view.findViewById(R.id.weekButton)
        monthButton = view.findViewById(R.id.monthButton)
        yearButton = view.findViewById(R.id.yearButton)
        
        energyPeriodLabel = view.findViewById(R.id.energyPeriodLabel)
        energySubLabel = view.findViewById(R.id.energySubLabel)
        weeklyGrowthPercentage = view.findViewById(R.id.weeklyGrowthPercentage)
        monthlyGrowthPercentage = view.findViewById(R.id.monthlyGrowthPercentage)
    }

    private fun setupRecyclerViews() {
        // Peak Usage RecyclerView
        peakUsageAdapter = PeakUsageAdapter(emptyList())
        peakUsageRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        peakUsageRecyclerView.adapter = peakUsageAdapter

        // AI Insights RecyclerView
        insightsAdapter = InsightsAdapter(emptyList())
        insightsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        insightsRecyclerView.adapter = insightsAdapter
    }

    private fun setupPeriodButtons() {
        weekButton.setOnClickListener { 
            selectPeriod(ReportPeriod.WEEK)
        }
        monthButton.setOnClickListener { 
            selectPeriod(ReportPeriod.MONTH)
        }
        yearButton.setOnClickListener { 
            selectPeriod(ReportPeriod.YEAR)
        }
        
        // Set initial selection
        selectPeriod(ReportPeriod.WEEK)
    }

    private fun selectPeriod(period: ReportPeriod) {
        currentPeriod = period
        
        // Update button styles
        resetButtonStyles()
        when (period) {
            ReportPeriod.WEEK -> {
                weekButton.setBackgroundColor(Color.parseColor("#2196F3"))
                weekButton.setTextColor(Color.WHITE)
                energyPeriodLabel.text = "Daily Energy Usage"
                energySubLabel.text = "Last 7 days"
            }
            ReportPeriod.MONTH -> {
                monthButton.setBackgroundColor(Color.parseColor("#2196F3"))
                monthButton.setTextColor(Color.WHITE)
                energyPeriodLabel.text = "Weekly Energy Usage"
                energySubLabel.text = "Last 4 weeks"
            }
            ReportPeriod.YEAR -> {
                yearButton.setBackgroundColor(Color.parseColor("#2196F3"))
                yearButton.setTextColor(Color.WHITE)
                energyPeriodLabel.text = "Monthly Energy Usage"
                energySubLabel.text = "Last 12 months"
            }
        }
        
        // Reload data for the new period
        loadReportData()
    }

    private fun resetButtonStyles() {
        val defaultColor = Color.parseColor("#E0E0E0")
        val defaultTextColor = Color.parseColor("#757575")
        
        weekButton.setBackgroundColor(defaultColor)
        weekButton.setTextColor(defaultTextColor)
        monthButton.setBackgroundColor(defaultColor)
        monthButton.setTextColor(defaultTextColor)
        yearButton.setBackgroundColor(defaultColor)
        yearButton.setTextColor(defaultTextColor)
    }

    private fun loadReportData() {
        lifecycleScope.launch {
            val dateRange = getDateRangeForPeriod(currentPeriod)
            val activities = activityDao.getActivitiesByDateRangeSync(dateRange.first, dateRange.second)
            val energyLogs = energyLogDao.getEnergyLogsByDateRangeSync(dateRange.first, dateRange.second)

            // Use sample data if no real data is available
            val activitiesData = if (activities.isEmpty()) {
                SampleDataGenerator.generateSampleActivities(
                    when (currentPeriod) {
                        ReportPeriod.WEEK -> 7
                        ReportPeriod.MONTH -> 30
                        ReportPeriod.YEAR -> 365
                    }
                )
            } else {
                activities
            }

            val energyLogsData = if (energyLogs.isEmpty()) {
                SampleDataGenerator.generateSampleEnergyLogs(
                    when (currentPeriod) {
                        ReportPeriod.WEEK -> 7
                        ReportPeriod.MONTH -> 30
                        ReportPeriod.YEAR -> 365
                    }
                )
            } else {
                energyLogs
            }

            // Update charts and data
            updateEnergyChart(activitiesData)
            updateEfficiencyChart(activitiesData)
            updatePeakUsageTimes(activitiesData)
            updateCapacityGrowth(activitiesData, energyLogsData)
            updateAIInsights(activitiesData)
        }
    }

    private fun getDateRangeForPeriod(period: ReportPeriod): Pair<Long, Long> {
        val endDate = System.currentTimeMillis()
        val startDate = when (period) {
            ReportPeriod.WEEK -> endDate - (7 * 24 * 60 * 60 * 1000)
            ReportPeriod.MONTH -> endDate - (30 * 24 * 60 * 60 * 1000)
            ReportPeriod.YEAR -> endDate - (365 * 24 * 60 * 60 * 1000)
        }
        return Pair(startDate, endDate)
    }

    private fun updateEnergyChart(activities: List<ActivityEntity>) {
        val trendData = ReportDataAnalyzer.aggregateEnergyData(activities, currentPeriod)
        
        val entries = trendData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.value)
        }

        val dataSet = LineDataSet(entries, "Energy Level")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#E3F2FD")

        val lineData = LineData(dataSet)
        energyChart.data = lineData
        energyChart.xAxis.valueFormatter = IndexAxisValueFormatter(trendData.map { it.label })
        energyChart.xAxis.granularity = 1f
        energyChart.description.text = ""
        energyChart.setDrawGridBackground(false)
        energyChart.axisRight.isEnabled = false
        energyChart.legend.isEnabled = false
        energyChart.invalidate()
    }

    private fun updateEfficiencyChart(activities: List<ActivityEntity>) {
        // Create efficiency data based on energy levels across time periods
        val efficiencyData = when (currentPeriod) {
            ReportPeriod.WEEK -> {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val values = if (activities.isNotEmpty()) {
                    // Calculate actual efficiency based on energy levels
                    calculateEfficiencyByDay(activities)
                } else {
                    listOf(75f, 82f, 88f, 85f, 90f, 78f, 80f)
                }
                Pair(days, values)
            }
            ReportPeriod.MONTH -> {
                val weeks = listOf("Week 1", "Week 2", "Week 3", "Week 4")
                val values = if (activities.isNotEmpty()) {
                    calculateEfficiencyByWeek(activities)
                } else {
                    listOf(75f, 82f, 88f, 85f)
                }
                Pair(weeks, values)
            }
            ReportPeriod.YEAR -> {
                val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val values = if (activities.isNotEmpty()) {
                    calculateEfficiencyByMonth(activities)
                } else {
                    listOf(70f, 75f, 80f, 85f, 88f, 90f, 85f, 82f, 78f, 75f, 72f, 74f)
                }
                Pair(months, values)
            }
        }

        val entries = efficiencyData.second.mapIndexed { index, value ->
            BarEntry(index.toFloat(), value)
        }

        val dataSet = BarDataSet(entries, "Energy Efficiency")
        dataSet.color = Color.parseColor("#4CAF50")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        efficiencyChart.data = barData
        efficiencyChart.xAxis.valueFormatter = IndexAxisValueFormatter(efficiencyData.first)
        efficiencyChart.xAxis.granularity = 1f
        efficiencyChart.description.text = ""
        efficiencyChart.setDrawGridBackground(false)
        efficiencyChart.axisRight.isEnabled = false
        efficiencyChart.legend.isEnabled = false
        efficiencyChart.invalidate()
    }

    private fun calculateEfficiencyByDay(activities: List<ActivityEntity>): List<Float> {
        // Group activities by day of week and calculate average energy efficiency
        val calendar = Calendar.getInstance()
        val dayGroups = activities.groupBy { activity ->
            calendar.timeInMillis = activity.date
            calendar.get(Calendar.DAY_OF_WEEK)
        }

        return (Calendar.MONDAY..Calendar.SUNDAY).map { dayOfWeek ->
            val dayActivities = dayGroups[dayOfWeek] ?: emptyList()
            if (dayActivities.isNotEmpty()) {
                (dayActivities.map { it.energy }.average() * 10).toFloat().coerceIn(0f, 100f)
            } else {
                75f // Default efficiency
            }
        }
    }

    private fun calculateEfficiencyByWeek(activities: List<ActivityEntity>): List<Float> {
        val calendar = Calendar.getInstance()
        val weekGroups = activities.groupBy { activity ->
            calendar.timeInMillis = activity.date
            calendar.get(Calendar.WEEK_OF_MONTH)
        }

        return (1..4).map { week ->
            val weekActivities = weekGroups[week] ?: emptyList()
            if (weekActivities.isNotEmpty()) {
                (weekActivities.map { it.energy }.average() * 10).toFloat().coerceIn(0f, 100f)
            } else {
                80f // Default efficiency
            }
        }
    }

    private fun calculateEfficiencyByMonth(activities: List<ActivityEntity>): List<Float> {
        val calendar = Calendar.getInstance()
        val monthGroups = activities.groupBy { activity ->
            calendar.timeInMillis = activity.date
            calendar.get(Calendar.MONTH)
        }

        return (0..11).map { month ->
            val monthActivities = monthGroups[month] ?: emptyList()
            if (monthActivities.isNotEmpty()) {
                (monthActivities.map { it.energy }.average() * 10).toFloat().coerceIn(0f, 100f)
            } else {
                75f // Default efficiency
            }
        }
    }

    private fun updatePeakUsageTimes(activities: List<ActivityEntity>) {
        val peakTimes = ReportDataAnalyzer.analyzePeakUsageTimes(activities)
        
        // Use sample data if analysis doesn't produce enough results
        val finalPeakTimes = if (peakTimes.size < 4) {
            SampleDataGenerator.generateSamplePeakUsageTimes()
        } else {
            peakTimes
        }
        
        peakUsageAdapter.updatePeakUsages(finalPeakTimes)
    }

    private fun updateCapacityGrowth(activities: List<ActivityEntity>, energyLogs: List<EnergyLog>) {
        val growthData = ReportDataAnalyzer.calculateCapacityGrowth(activities, energyLogs)
        
        // Use sample data if no meaningful growth data is available
        val finalGrowthData = if (growthData.isEmpty()) {
            SampleDataGenerator.generateSampleCapacityGrowth()
        } else {
            growthData
        }
        
        finalGrowthData.forEach { growth ->
            when (growth.period) {
                "Weekly Growth" -> {
                    val sign = if (growth.isPositive) "+" else "-"
                    weeklyGrowthPercentage.text = "$sign${String.format("%.0f", kotlin.math.abs(growth.growthPercentage))}%"
                    weeklyGrowthPercentage.setTextColor(
                        if (growth.isPositive) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                    )
                }
                "Monthly Growth" -> {
                    val sign = if (growth.isPositive) "+" else "-"
                    monthlyGrowthPercentage.text = "$sign${String.format("%.0f", kotlin.math.abs(growth.growthPercentage))}%"
                    monthlyGrowthPercentage.setTextColor(
                        if (growth.isPositive) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                    )
                }
            }
        }
    }

    private fun updateAIInsights(activities: List<ActivityEntity>) {
        val insights = ReportDataAnalyzer.generateAIInsights(activities)
        
        // Use sample insights if no meaningful insights are generated
        val finalInsights = if (insights.isEmpty()) {
            SampleDataGenerator.generateSampleAIInsights()
        } else {
            insights
        }
        
        insightsAdapter.updateInsights(finalInsights)
    }

    // Keep export functionality from original implementation
    private fun exportToCSV() {
        lifecycleScope.launch {
            try {
                val dateRange = getDateRangeForPeriod(currentPeriod)
                val activities = activityDao.getActivitiesByDateRangeSync(dateRange.first, dateRange.second)

                val file = File(requireContext().getExternalFilesDir(null), "social_battery_report.csv")
                val writer = FileWriter(file)

                // Write CSV header
                writer.append("Date,Activity,Type,Energy,Mood,People,Notes\n")

                // Write data
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                activities.forEach { activity ->
                    val date = dateFormat.format(Date(activity.date))
                    writer.append("$date,${activity.name},${activity.type},${activity.energy},${activity.mood},${activity.people},${activity.notes}\n")
                }

                writer.close()

                // Share file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(Intent.createChooser(intent, "Export CSV"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error exporting CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToPDF() {
        lifecycleScope.launch {
            try {
                val dateRange = getDateRangeForPeriod(currentPeriod)
                val activities = activityDao.getActivitiesByDateRangeSync(dateRange.first, dateRange.second)

                // Group activities by date for trends
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val trends = activities.groupBy { activity ->
                    val date = Date(activity.date)
                    dateFormat.format(date)
                }.map { (date, activitiesForDate) ->
                    val avgEnergy: Double = activitiesForDate.map { it.energy }.average()
                    val mostCommonMood: String = activitiesForDate.groupBy { it.mood }
                        .maxByOrNull { it.value.size }?.key ?: "Unknown"

                    TrendData(
                        date = date,
                        avgEnergy = avgEnergy,
                        avgMood = mostCommonMood,
                        activityCount = activitiesForDate.size
                    )
                }.sortedByDescending { it.date }

                // Create simple text report (PDF generation would require more complex implementation)
                val file = File(requireContext().getExternalFilesDir(null), "social_battery_report.txt")
                val writer = FileWriter(file)

                writer.append("Social Battery Manager Report\n")
                writer.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

                writer.append("DAILY TRENDS:\n")
                trends.forEach { trend ->
                    writer.append("${trend.date}: Energy: ${String.format("%.1f", trend.avgEnergy)}, Mood: ${trend.avgMood}, Activities: ${trend.activityCount}\n")
                }

                writer.append("\nDETAILED ACTIVITIES:\n")
                activities.forEach { activity ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(activity.date))
                    writer.append("$date - ${activity.name} (${activity.type}): Energy: ${activity.energy}, Mood: ${activity.mood}\n")
                }

                writer.close()

                // Share file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(Intent.createChooser(intent, "Export Report"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error exporting report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

    private fun loadReportData() {
        lifecycleScope.launch {
            val endDate = System.currentTimeMillis()
            val startDate = endDate - (30 * 24 * 60 * 60 * 1000) // Last 30 days

            val activities = activityDao.getActivitiesByDateRangeSync(startDate, endDate)

            // Update charts
            updateEnergyChart(activities)
            updateMoodChart(activities)

            // Update trends list
            updateTrendsList(activities)
        }
    }

    private fun updateEnergyChart(activities: List<ActivityEntity>) {
        val entries = mutableListOf<Entry>()
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val labels = mutableListOf<String>()

        // Group activities by date and calculate average energy
        val groupedByDate = activities.groupBy { activity ->
            val date = Date(activity.date)
            dateFormat.format(date)
        }

        groupedByDate.entries.forEachIndexed { index, (date, activitiesForDate) ->
            val avgEnergy: Float = activitiesForDate.map { it.energy }.average().toFloat()
            entries.add(Entry(index.toFloat(), avgEnergy))
            labels.add(date)
        }

        val dataSet = LineDataSet(entries, "Energy Level")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(Color.BLUE)

        val lineData = LineData(dataSet)
        energyChart.data = lineData
        energyChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        energyChart.xAxis.granularity = 1f
        energyChart.description.text = "Energy Trends Over Time"
        energyChart.invalidate()
    }

    private fun updateMoodChart(activities: List<ActivityEntity>) {
        val moodCounts = activities.groupBy { it.mood }.mapValues { it.value.size }
        val entries = moodCounts.map { (mood, count) ->
            PieEntry(count.toFloat(), mood)
        }

        val dataSet = PieDataSet(entries, "Mood Distribution")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)
        moodChart.data = pieData
        moodChart.description.text = "Mood Distribution"
        moodChart.invalidate()
    }

    private fun updateTrendsList(activities: List<ActivityEntity>) {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val trends = activities.groupBy { activity ->
            val date = Date(activity.date)
            dateFormat.format(date)
        }.map { (date, activitiesForDate) ->
            val avgEnergy: Double = activitiesForDate.map { it.energy }.average()
            val mostCommonMood: String = activitiesForDate.groupBy { it.mood }
                .maxByOrNull { it.value.size }?.key ?: "Unknown"

            TrendData(
                date = date,
                avgEnergy = avgEnergy,
                avgMood = mostCommonMood,
                activityCount = activitiesForDate.size
            )
        }.sortedByDescending { it.date }

        trendsAdapter.updateTrends(trends)
    }

    private fun exportToCSV() {
        lifecycleScope.launch {
            try {
                val endDate = System.currentTimeMillis()
                val startDate = endDate - (30 * 24 * 60 * 60 * 1000)
                val activities = activityDao.getActivitiesByDateRangeSync(startDate, endDate)

                val file = File(requireContext().getExternalFilesDir(null), "social_battery_report.csv")
                val writer = FileWriter(file)

                // Write CSV header
                writer.append("Date,Activity,Type,Energy,Mood,People,Notes\n")

                // Write data
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                activities.forEach { activity ->
                    val date = dateFormat.format(Date(activity.date))
                    writer.append("$date,${activity.name},${activity.type},${activity.energy},${activity.mood},${activity.people},${activity.notes}\n")
                }

                writer.close()

                // Share file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/csv"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(Intent.createChooser(intent, "Export CSV"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error exporting CSV: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToPDF() {
        lifecycleScope.launch {
            try {
                val endDate = System.currentTimeMillis()
                val startDate = endDate - (30 * 24 * 60 * 60 * 1000)
                val activities = activityDao.getActivitiesByDateRangeSync(startDate, endDate)

                // Group activities by date for trends
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val trends = activities.groupBy { activity ->
                    val date = Date(activity.date)
                    dateFormat.format(date)
                }.map { (date, activitiesForDate) ->
                    val avgEnergy: Double = activitiesForDate.map { it.energy }.average()
                    val mostCommonMood: String = activitiesForDate.groupBy { it.mood }
                        .maxByOrNull { it.value.size }?.key ?: "Unknown"

                    TrendData(
                        date = date,
                        avgEnergy = avgEnergy,
                        avgMood = mostCommonMood,
                        activityCount = activitiesForDate.size
                    )
                }.sortedByDescending { it.date }

                // Create simple text report (PDF generation would require more complex implementation)
                val file = File(requireContext().getExternalFilesDir(null), "social_battery_report.txt")
                val writer = FileWriter(file)

                writer.append("Social Battery Manager Report\n")
                writer.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n\n")

                writer.append("DAILY TRENDS:\n")
                trends.forEach { trend ->
                    writer.append("${trend.date}: Energy: ${String.format("%.1f", trend.avgEnergy)}, Mood: ${trend.avgMood}, Activities: ${trend.activityCount}\n")
                }

                writer.append("\nDETAILED ACTIVITIES:\n")
                activities.forEach { activity ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(activity.date))
                    writer.append("$date - ${activity.name} (${activity.type}): Energy: ${activity.energy}, Mood: ${activity.mood}\n")
                }

                writer.close()

                // Share file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                startActivity(Intent.createChooser(intent, "Export Report"))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error exporting report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}