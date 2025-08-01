@file:Suppress("OverrideDeprecatedMigration")

package com.example.socialbatterymanager.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.ui.activities.CreateNewActivityDialog
import kotlinx.coroutines.launch
import java.util.Calendar

@Suppress("OverrideDeprecatedMigration")
class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    
    private lateinit var calendarView: CalendarView
    private lateinit var eventsRecyclerView: RecyclerView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var calendarIntegration: CalendarIntegration
    private lateinit var database: AppDatabase
    
    private var selectedDate: Long = System.currentTimeMillis()
    
    companion object {
        private const val CALENDAR_PERMISSION_REQUEST = 1001
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize database and calendar integration
        database = AppDatabase.getDatabase(requireContext())
        calendarIntegration = CalendarIntegration(requireContext(), database.calendarEventDao())
        
        // Initialize views
        calendarView = view.findViewById(R.id.calendarView)
        eventsRecyclerView = view.findViewById(R.id.rvEvents)
        
        // Setup RecyclerView
        calendarAdapter = CalendarAdapter { event ->
            // Handle event click
            Toast.makeText(requireContext(), "Event: ${event.title}", Toast.LENGTH_SHORT).show()
        }
        
        eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsRecyclerView.adapter = calendarAdapter
        
        // Setup calendar view
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            selectedDate = calendar.timeInMillis
            loadEventsForSelectedDate()
        }
        
        // Setup buttons
        view.findViewById<Button>(R.id.btnAddCalendarEvent).setOnClickListener {
            showCreateNewActivityDialog()
        }
        
        view.findViewById<Button>(R.id.btnImportEvents).setOnClickListener {
            requestCalendarPermissionAndImport()
        }
        
        // Load events for current date
        loadEventsForSelectedDate()
    }
    
    private fun loadEventsForSelectedDate() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis
            
            val events = database.calendarEventDao().getEventsForDay(startOfDay, endOfDay)
            calendarAdapter.submitList(events)
        }
    }
    
    private fun showCreateNewActivityDialog() {
        val dialog = CreateNewActivityDialog.newInstance()
        dialog.setOnActivityCreatedListener { activity ->
            // Save the activity and create a calendar event
            lifecycleScope.launch {
                try {
                    // Save activity to database
                    val activityId = database.activityDao().insertActivity(activity.toEntity())
                    
                    // Create calendar event
                    val calendarEvent = CalendarEvent(
                        title = activity.name,
                        description = activity.description,
                        startTime = activity.date,
                        endTime = activity.date + (60 * 60 * 1000), // Default 1 hour duration
                        location = "",
                        source = "manual",
                        isImported = false
                    )
                    
                    database.calendarEventDao().insert(calendarEvent)
                    
                    // Refresh the calendar view
                    loadEventsForSelectedDate()
                    
                    Toast.makeText(
                        requireContext(),
                        "Activity '${activity.name}' scheduled successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error creating activity: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show(parentFragmentManager, "CreateNewActivityDialog")
    }

    private fun createSampleEvent() {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 0)
            val startTime = calendar.timeInMillis
            
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            val endTime = calendar.timeInMillis
            
            calendarIntegration.createManualEvent(
                title = "Sample Event",
                description = "This is a sample calendar event",
                startTime = startTime,
                endTime = endTime,
                location = "Office"
            )
            
            loadEventsForSelectedDate()
            Toast.makeText(requireContext(), "Sample event created!", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun requestCalendarPermissionAndImport() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALENDAR) 
            != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST
            )
        } else {
            importCalendarEvents()
        }
    }
    
    private fun importCalendarEvents() {
        lifecycleScope.launch {
            try {
                val importedEvents = calendarIntegration.importDeviceCalendarEvents()
                loadEventsForSelectedDate()
                Toast.makeText(
                    requireContext(), 
                    "Imported ${importedEvents.size} events", 
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(), 
                    "Error importing events: ${e.message}", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    @Suppress("OverrideDeprecatedMigration")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CALENDAR_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importCalendarEvents()
            } else {
                Toast.makeText(
                    requireContext(), 
                    "Calendar permission denied. Cannot import events.", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
