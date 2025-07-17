package com.example.socialbatterymanager.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
<<<<<<< HEAD
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.utils.ErrorHandler
import com.example.socialbatterymanager.utils.NetworkConnectivityManager
import kotlinx.coroutines.launch
=======
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.ActivityEntity
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.model.EnergyLog
import com.example.socialbatterymanager.notification.EnergyReminderWorker
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
>>>>>>> copilot/fix-5

class HomeFragment : Fragment() {

    private lateinit var energyBatteryView: EnergyBatteryView
    private lateinit var btnAddEnergy: Button
    private lateinit var btnRemoveEnergy: Button
    private lateinit var chipGroupMood: ChipGroup
    private lateinit var tvWeeklyStats: TextView
    private lateinit var recyclerViewActivities: RecyclerView
    private lateinit var fabAddActivity: FloatingActionButton
    private lateinit var btnTestBattery: Button
    private lateinit var networkManager: NetworkConnectivityManager

    private lateinit var activityAdapter: ActivityAdapter
    private lateinit var database: AppDatabase

    // Current energy level
    private var currentEnergyLevel = 65

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize database
        database = AppDatabase.getDatabase(requireContext())

        // Initialize views
        energyBatteryView = view.findViewById(R.id.energyBatteryView)
        btnAddEnergy = view.findViewById(R.id.btnAddEnergy)
        btnRemoveEnergy = view.findViewById(R.id.btnRemoveEnergy)
        chipGroupMood = view.findViewById(R.id.chipGroupMood)
        tvWeeklyStats = view.findViewById(R.id.tvWeeklyStats)
        recyclerViewActivities = view.findViewById(R.id.recyclerViewActivities)
        fabAddActivity = view.findViewById(R.id.fabAddActivity)
        btnTestBattery = view.findViewById(R.id.btnTestBattery)
        
        // Initialize network manager
        networkManager = NetworkConnectivityManager(requireContext())

<<<<<<< HEAD
        setupAccessibility()
        updateUI()
        observeNetworkState()

        btnTestBattery.setOnClickListener {
            try {
                val nextLevel = testLevels[testIndex]
                animateBatteryLevelChange(nextLevel)
                testIndex = (testIndex + 1) % testLevels.size
            } catch (e: Exception) {
                ErrorHandler.handleException(view, e) {
                    // Retry action
                    val nextLevel = testLevels[testIndex]
                    animateBatteryLevelChange(nextLevel)
                    testIndex = (testIndex + 1) % testLevels.size
                }
            }
        }
        
        btnLogout.setOnClickListener {
            userViewModel.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
=======
        setupRecyclerView()
        setupMoodChips()
        setupClickListeners()
        setupNotifications()
        loadData()
>>>>>>> copilot/fix-5

        return view
    }
    
    private fun setupAccessibility() {
        // Set content descriptions for accessibility
        lottieBattery.contentDescription = getString(R.string.battery_level_description, batteryLevel)
        tvBatteryPercent.contentDescription = getString(R.string.battery_percent_description, batteryLevel)
        btnTestBattery.contentDescription = getString(R.string.test_battery_description)
        
        // Enable focus for accessibility
        lottieBattery.isFocusable = true
        tvBatteryPercent.isFocusable = true
        btnTestBattery.isFocusable = true
        
        // Set minimum touch target size for accessibility
        val minTouchSize = resources.getDimensionPixelSize(R.dimen.min_touch_target_size)
        btnTestBattery.minimumHeight = minTouchSize
        btnTestBattery.minimumWidth = minTouchSize
    }
    
    private fun observeNetworkState() {
        lifecycleScope.launch {
            networkManager.isConnected.collect { isConnected ->
                if (!isConnected) {
                    view?.let { ErrorHandler.showOfflineSnackbar(it) }
                }
            }
        }
    }

<<<<<<< HEAD
    private fun animateBatteryLevelChange(targetLevel: Int) {
        val startLevel = batteryLevel
        val animator = ValueAnimator.ofInt(startLevel, targetLevel)
        animator.duration = 600
        animator.addUpdateListener { valueAnimator ->
            val level = valueAnimator.animatedValue as Int
            batteryLevel = level
            updateUI()
        }
        animator.start()
    }

    private fun updateUI() {
        lottieBattery.progress = batteryLevel / 100f
        setLottieBatteryColor(batteryLevel)
        tvBatteryPercent.text = "$batteryLevel%"
        
        // Update accessibility descriptions
        lottieBattery.contentDescription = getString(R.string.battery_level_description, batteryLevel)
        tvBatteryPercent.contentDescription = getString(R.string.battery_percent_description, batteryLevel)
        
        // Announce battery level changes for accessibility
        tvBatteryPercent.announceForAccessibility(getString(R.string.battery_level_changed, batteryLevel))
    }

    private fun setLottieBatteryColor(level: Int) {
        val color = when {
            level >= 80 -> "#4CAF50".toColorInt()    // Green
            level >= 60 -> "#FFEB3B".toColorInt()    // Yellow
            level >= 40 -> "#FF9800".toColorInt()    // Orange
            else -> "#F44336".toColorInt()           // Red
        }
        lottieBattery.addValueCallback(
            KeyPath("BatteryFill"),
            LottieProperty.COLOR_FILTER,
            LottieValueCallback(PorterDuffColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP))
=======
    private fun setupRecyclerView() {
        activityAdapter = ActivityAdapter(
            onItemClick = { activity ->
                // Edit activity
                val dialog = AddActivityDialog(requireContext(), { updatedActivity ->
                    lifecycleScope.launch {
                        database.activityDao().updateActivity(updatedActivity)
                        updateEnergyLevel(updatedActivity.energy)
                    }
                }, activity)
                dialog.show()
            },
            onItemLongClick = { activity ->
                // Delete activity
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Activity")
                    .setMessage("Are you sure you want to delete '${activity.name}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            database.activityDao().deleteActivity(activity)
                            // Reverse the energy impact
                            updateEnergyLevel(-activity.energy)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
>>>>>>> copilot/fix-5
        )

        recyclerViewActivities.adapter = activityAdapter
        recyclerViewActivities.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupMoodChips() {
        val moods = arrayOf("😊 Happy", "😐 Neutral", "😔 Sad", "😴 Tired", "😤 Stressed", "😎 Energetic")
        
        moods.forEach { mood ->
            val chip = Chip(requireContext())
            chip.text = mood
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck other chips
                    for (i in 0 until chipGroupMood.childCount) {
                        val otherChip = chipGroupMood.getChildAt(i) as Chip
                        if (otherChip != chip) {
                            otherChip.isChecked = false
                        }
                    }
                    // Log mood change
                    logMoodChange(mood)
                }
            }
            chipGroupMood.addView(chip)
        }
    }

    private fun setupClickListeners() {
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
            updateEnergyLevel(randomLevel - currentEnergyLevel)
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load latest energy level
            val latestEnergyLog = database.energyLogDao().getLatestEnergyLog()
            if (latestEnergyLog != null) {
                currentEnergyLevel = latestEnergyLog.energyLevel
                energyBatteryView.setBatteryLevel(currentEnergyLevel, false)
            }

            // Observe activities
            database.activityDao().getAllActivities().collect { activities ->
                activityAdapter.submitList(activities)
            }
        }

        lifecycleScope.launch {
            // Load weekly stats
            val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            
            combine(
                database.energyLogDao().getEnergyLogsAfter(weekStart),
                database.activityDao().getAllActivities()
            ) { energyLogs, activities ->
                val weeklyActivities = activities.filter { it.date >= weekStart }
                val totalEnergyGain = weeklyActivities.filter { it.energy > 0 }.sumOf { it.energy }
                val totalEnergyLoss = weeklyActivities.filter { it.energy < 0 }.sumOf { it.energy }
                
                "This week: +$totalEnergyGain / $totalEnergyLoss energy, ${weeklyActivities.size} activities"
            }.collect { stats ->
                tvWeeklyStats.text = stats
            }
        }
    }

    private fun updateEnergyLevel(change: Int) {
        val newLevel = (currentEnergyLevel + change).coerceIn(0, 100)
        currentEnergyLevel = newLevel
        energyBatteryView.setBatteryLevel(newLevel, true)

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
    
    override fun onDestroyView() {
        super.onDestroyView()
        networkManager.unregister()
    }
}
