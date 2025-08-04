package com.example.socialbatterymanager.reports

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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.EnergyLog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private val viewModel: ReportsViewModel by viewModels()
    
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

        // Initialize views
        initializeViews(view)
        
        // Setup adapters and RecyclerViews
        setupRecyclerViews()
        
        // Setup period buttons
        setupPeriodButtons()

        // Observe data
        viewModel.reportData.observe(viewLifecycleOwner) { (activitiesData, energyLogsData) ->
            updateEnergyChart(activitiesData)
            updateEfficiencyChart(activitiesData)
            updatePeakUsageTimes(activitiesData)
            updateCapacityGrowth(activitiesData, energyLogsData)
            updateAIInsights(activitiesData)
        }
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
                energyPeriodLabel.text = getString(R.string.energy_usage_daily)
                energySubLabel.text = getString(R.string.period_last_7_days)
            }
            ReportPeriod.MONTH -> {
                monthButton.setBackgroundColor(Color.parseColor("#2196F3"))
                monthButton.setTextColor(Color.WHITE)
                energyPeriodLabel.text = getString(R.string.energy_usage_weekly)
                energySubLabel.text = getString(R.string.period_last_4_weeks)
            }
            ReportPeriod.YEAR -> {
                yearButton.setBackgroundColor(Color.parseColor("#2196F3"))
                yearButton.setTextColor(Color.WHITE)
                energyPeriodLabel.text = getString(R.string.energy_usage_monthly)
                energySubLabel.text = getString(R.string.period_last_12_months)
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
        val dateRange = getDateRangeForPeriod(currentPeriod)
        viewModel.loadReportData(dateRange.first, dateRange.second)
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

        val dataSet = LineDataSet(entries, getString(R.string.chart_energy_level_label))
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

        val dataSet = BarDataSet(entries, getString(R.string.chart_energy_efficiency_label))
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

        peakUsageAdapter.updatePeakUsages(peakTimes)
    }

    private fun updateCapacityGrowth(activities: List<ActivityEntity>, energyLogs: List<EnergyLog>) {
        val growthData = ReportDataAnalyzer.calculateCapacityGrowth(activities, energyLogs)

        growthData.forEach { growth ->
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

        insightsAdapter.updateInsights(insights)
    }

    // Keep export functionality from original implementation
    private fun exportToCSV() {
        val dateRange = getDateRangeForPeriod(currentPeriod)
        viewModel.getActivitiesForPeriod(dateRange.first, dateRange.second) { activities ->
            try {
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

                startActivity(Intent.createChooser(intent, getString(R.string.export_csv_title)))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_export_csv, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToPDF() {
        val dateRange = getDateRangeForPeriod(currentPeriod)
        viewModel.getActivitiesForPeriod(dateRange.first, dateRange.second) { activities ->
            try {
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

                startActivity(Intent.createChooser(intent, getString(R.string.export_report_title)))

            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_export_report, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
