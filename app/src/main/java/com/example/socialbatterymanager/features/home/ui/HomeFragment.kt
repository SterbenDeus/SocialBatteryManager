package com.example.socialbatterymanager.features.home.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.shared.utils.ErrorHandler
import com.example.socialbatterymanager.shared.utils.NetworkConnectivityManager
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.EnergyLog
import com.example.socialbatterymanager.notification.EnergyReminderWorker
import com.example.socialbatterymanager.BuildConfig
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var energyBatteryView: EnergyBatteryView
    private lateinit var tvEnergyPercentage: TextView
    private lateinit var tvEnergyLabel: TextView
    private lateinit var llMoodOptions: LinearLayout
    private lateinit var tvRemainingHours: TextView
    private lateinit var tvBurnRate: TextView
    private lateinit var tvAverageEnergy: TextView
    private lateinit var tvPeakDay: TextView
    private lateinit var tvRecoveryNeeded: TextView
    private lateinit var btnLogActivity: Button
    private lateinit var btnPlanDay: Button
    private lateinit var btnViewBattery: Button
    private lateinit var btnGetTips: Button
    private lateinit var networkManager: NetworkConnectivityManager

    // Hidden views for backwards compatibility
    private lateinit var btnAddEnergy: Button
    private lateinit var btnRemoveEnergy: Button
    private lateinit var chipGroupMood: ChipGroup
    private lateinit var tvWeeklyStats: TextView
    private lateinit var recyclerViewActivities: RecyclerView
    private lateinit var fabAddActivity: FloatingActionButton
    private lateinit var btnTestBattery: Button

    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var database: AppDatabase

    // Current energy level
    private var currentEnergyLevel = 75
    private var selectedMood: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize database
        database = AppDatabase.getDatabase(requireContext())

        // Initialize views
        initializeViews(view)

        // Initialize network manager
        networkManager = NetworkConnectivityManager(requireContext())

        setupUI()
        setupMoodOptions()
        setupClickListeners()
        setupNotifications()
        loadData()
        observeNetworkState()

        return view
    }

    private fun initializeViews(view: View) {
        // New UI views
        energyBatteryView = view.findViewById(R.id.energyBatteryView)
        tvEnergyPercentage = view.findViewById(R.id.tvEnergyPercentage)
        tvEnergyLabel = view.findViewById(R.id.tvEnergyLabel)
        llMoodOptions = view.findViewById(R.id.llMoodOptions)
        tvRemainingHours = view.findViewById(R.id.tvRemainingHours)
        tvBurnRate = view.findViewById(R.id.tvBurnRate)
        tvAverageEnergy = view.findViewById(R.id.tvAverageEnergy)
        tvPeakDay = view.findViewById(R.id.tvPeakDay)
        tvRecoveryNeeded = view.findViewById(R.id.tvRecoveryNeeded)
        btnLogActivity = view.findViewById(R.id.btnLogActivity)
        btnPlanDay = view.findViewById(R.id.btnPlanDay)
        btnViewBattery = view.findViewById(R.id.btnViewBattery)
        btnGetTips = view.findViewById(R.id.btnGetTips)

        // Hidden backwards compatibility views
        btnAddEnergy = view.findViewById(R.id.btnAddEnergy)
        btnRemoveEnergy = view.findViewById(R.id.btnRemoveEnergy)
        chipGroupMood = view.findViewById(R.id.chipGroupMood)
        tvWeeklyStats = view.findViewById(R.id.tvWeeklyStats)
        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities)
        fabAddActivity = view.findViewById(R.id.fabAddActivity)
        btnTestBattery = view.findViewById(R.id.btnTestBattery)
    }

    private fun setupUI() {
        updateEnergyDisplay()
        calculateEnergyInsights()
        calculateWeeklyForecast()
    }

    private fun updateEnergyDisplay() {
        energyBatteryView.setBatteryLevel(currentEnergyLevel, false)
        tvEnergyPercentage.text = "$currentEnergyLevel%"
        tvEnergyLabel.text = energyBatteryView.getEnergyLevelLabel()
        
        // Update percentage color based on energy level
        val colorResId = when {
            currentEnergyLevel >= 80 -> R.color.energy_high
            currentEnergyLevel >= 60 -> R.color.energy_medium
            currentEnergyLevel >= 30 -> R.color.energy_low
            else -> R.color.energy_recharge
        }
        tvEnergyPercentage.setTextColor(requireContext().getColor(colorResId))
    }

    private fun setupMoodOptions() {
        val moods = resources.getStringArray(R.array.mood_options)
        
        moods.forEach { mood ->
            val button = Button(requireContext())
            button.text = mood.split(" ")[0] // Just the emoji
            button.setBackgroundResource(R.drawable.mood_button_background)
            button.setPadding(16, 16, 16, 16)
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(4, 4, 4, 4)
            button.layoutParams = layoutParams
            
            button.setOnClickListener {
                selectMood(mood, button)
            }
            
            llMoodOptions.addView(button)
        }
    }

    private fun selectMood(mood: String, selectedButton: Button) {
        // Reset all mood buttons
        for (i in 0 until llMoodOptions.childCount) {
            val child = llMoodOptions.getChildAt(i)
            if (child is Button) {
                child.setBackgroundResource(R.drawable.mood_button_background)
            }
        }
        
        // Highlight selected button
        selectedButton.setBackgroundResource(R.drawable.mood_button_selected)
        selectedMood = mood
        
        // Log mood change
        logMoodChange(mood)
    }

    private fun calculateEnergyInsights() {
        lifecycleScope.launch {
            // Calculate remaining hours based on current energy and average burn rate
            val avgBurnRate = calculateAverageBurnRate()
            val remainingHours = if (avgBurnRate > 0) {
                (currentEnergyLevel / avgBurnRate * 0.5) // Rough estimate
            } else {
                8.0 // Default if no data
            }
            
            tvRemainingHours.text = String.format("%.1fh", remainingHours)
            tvBurnRate.text = String.format("%.1fh", avgBurnRate)
        }
    }

    private suspend fun calculateAverageBurnRate(): Double {
        val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        val activityList = database.activityDao().getAllActivities().first()

        val weeklyActivities = activityList.filter { it.date >= weekStart && it.energy < 0 }
        val totalBurn = weeklyActivities.sumOf { abs(it.energy.toDouble()) }

        return if (weeklyActivities.isNotEmpty()) totalBurn / weeklyActivities.size else 2.0
    }

    private fun calculateWeeklyForecast() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)

                database.energyLogDao().getEnergyLogsAfter(weekStart).collect { energyLogs ->
                    if (energyLogs.isNotEmpty()) {
                        // Calculate average energy
                        val avgEnergy = energyLogs.map { it.energyLevel }.average()
                        tvAverageEnergy.text = "${avgEnergy.toInt()}%"

                        // Find peak day (day with most activities)
                        val peakDay = findPeakDay()
                        tvPeakDay.text = peakDay

                        // Calculate recovery needed
                        val recoveryDays = calculateRecoveryNeeded(avgEnergy.toInt())
                        tvRecoveryNeeded.text = "$recoveryDays days"
                    }
                }
            }
        }
    }

    private suspend fun findPeakDay(): String {
        val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault())
        val now = System.currentTimeMillis()
        val weekStart = now - TimeUnit.DAYS.toMillis(7)

        val activities = database.activityDao().getActivitiesByDateRangeSync(weekStart, now)
        val countsByDay = activities.groupingBy { activity ->
            dayFormatter.format(Date(activity.date))
        }.eachCount()

        return countsByDay.maxByOrNull { it.value }?.key ?: dayFormatter.format(Date())
    }

    private fun calculateRecoveryNeeded(avgEnergy: Int): Int {
        return when {
            avgEnergy >= 80 -> 1
            avgEnergy >= 60 -> 2
            avgEnergy >= 30 -> 3
            else -> 4
        }
    }

    private fun setupClickListeners() {
        btnLogActivity.setOnClickListener {
            // Open activity logging dialog
            val dialog = AddActivityDialog(requireContext()) { newActivity ->
                lifecycleScope.launch {
                    database.activityDao().insertActivity(newActivity)
                    updateEnergyLevel(newActivity.energy)
                }
            }
            dialog.show()
        }

        // Hide planning feature in release builds until implemented
        if (!BuildConfig.DEBUG) {
            btnPlanDay.visibility = View.GONE
        } else {
            btnPlanDay.isEnabled = false
        }

        btnViewBattery.setOnClickListener {
            // Show detailed battery info
            showBatteryDetails()
        }

        btnGetTips.setOnClickListener {
            // Show energy management tips
            showEnergyTips()
        }

        // Backwards compatibility click listeners
        btnAddEnergy.setOnClickListener {
            updateEnergyLevel(5)
        }

        btnRemoveEnergy.setOnClickListener {
            updateEnergyLevel(-5)
        }

        fabAddActivity.setOnClickListener {
            val dialog = AddActivityDialog(requireContext()) { newActivity ->
                lifecycleScope.launch {
                    database.activityDao().insertActivity(newActivity)
                    updateEnergyLevel(newActivity.energy)
                }
            }
            dialog.show()
        }

        btnTestBattery.setOnClickListener {
            val testLevels = listOf(95, 75, 55, 35, 15)
            val randomLevel = testLevels.random()
            currentEnergyLevel = randomLevel
            updateEnergyDisplay()
            calculateEnergyInsights()
            calculateWeeklyForecast()
        }
    }

    private fun showBatteryDetails() {
        val message = getString(
            R.string.battery_details_message,
            currentEnergyLevel,
            energyBatteryView.getEnergyLevelLabel()
        )

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.battery_details_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showEnergyTips() {
        val tipsResId = when {
            currentEnergyLevel >= 80 -> R.string.energy_tip_high
            currentEnergyLevel >= 60 -> R.string.energy_tip_medium
            currentEnergyLevel >= 30 -> R.string.energy_tip_low
            else -> R.string.energy_tip_recharge
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.energy_tips_title)
            .setMessage(getString(tipsResId))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load latest energy level
            val latestEnergyLog = database.energyLogDao().getLatestEnergyLog()
            if (latestEnergyLog != null) {
                currentEnergyLevel = latestEnergyLog.energyLevel
                updateEnergyDisplay()
            }

            calculateEnergyInsights()
            calculateWeeklyForecast()
        }
    }

    private fun updateEnergyLevel(change: Int) {
        val newLevel = (currentEnergyLevel + change).coerceIn(0, 100)
        currentEnergyLevel = newLevel
        updateEnergyDisplay()
        
        // Recalculate insights
        calculateEnergyInsights()

        // Log the energy change
        lifecycleScope.launch {
            val energyLog = EnergyLog(
                energyLevel = newLevel,
                timestamp = System.currentTimeMillis(),
                changeAmount = change,
                reason = if (change > 0) "Energy added" else "Energy removed"
            )
            database.energyLogDao().insertEnergyLog(energyLog)
        }
    }

    private fun setupNotifications() {
        // Schedule daily reminder notifications
        val reminderWork = PeriodicWorkRequestBuilder<EnergyReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(requireContext()).enqueue(reminderWork)
    }

    private fun logMoodChange(mood: String) {
        // Log mood change as an energy log entry
        lifecycleScope.launch {
            val energyLog = EnergyLog(
                energyLevel = currentEnergyLevel,
                timestamp = System.currentTimeMillis(),
                changeAmount = 0,
                reason = "Mood: $mood"
            )
            database.energyLogDao().insertEnergyLog(energyLog)
        }
    }

    private fun observeNetworkState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkManager.isConnected.collect { isConnected ->
                    if (!isConnected) {
                        view?.let { ErrorHandler.showOfflineSnackbar(it) }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkManager.unregister()
    }
}
