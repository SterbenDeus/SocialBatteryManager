package com.example.socialbatterymanager.reports

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.ActivityDao
import com.example.socialbatterymanager.data.ActivityEntity
import com.example.socialbatterymanager.data.AppDatabase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
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
    private lateinit var trendsAdapter: TrendsAdapter
    private lateinit var energyChart: LineChart
    private lateinit var moodChart: PieChart
    private lateinit var trendsRecyclerView: RecyclerView

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

        // Initialize views
        energyChart = view.findViewById(R.id.energyChart)
        moodChart = view.findViewById(R.id.moodChart)
        trendsRecyclerView = view.findViewById(R.id.trendsRecyclerView)

        // Setup RecyclerView
        trendsAdapter = TrendsAdapter(emptyList())
        trendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        trendsRecyclerView.adapter = trendsAdapter

        // Setup export buttons
        view.findViewById<Button>(R.id.exportCsvButton).setOnClickListener {
            exportToCSV()
        }
        view.findViewById<Button>(R.id.exportPdfButton).setOnClickListener {
            exportToPDF()
        }

        // Load and display data
        loadReportData()
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
            val avgEnergy = activitiesForDate.map { it.energy }.average().toFloat()
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
            val avgEnergy = activitiesForDate.map { it.energy }.average()
            val mostCommonMood = activitiesForDate.groupBy { it.mood }
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
                    val avgEnergy = activitiesForDate.map { it.energy }.average()
                    val mostCommonMood = activitiesForDate.groupBy { it.mood }
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