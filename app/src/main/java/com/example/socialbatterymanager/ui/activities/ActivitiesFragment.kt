package com.example.socialbatterymanager.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.AppDatabase
import com.example.socialbatterymanager.model.Activity
import com.example.socialbatterymanager.model.toEntity
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {

    private lateinit var rvActivities: RecyclerView
    private lateinit var adapter: ActivitiesAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        database = AppDatabase.getDatabase(requireContext())
        
        rvActivities = view.findViewById(R.id.rvActivities)
        adapter = ActivitiesAdapter { activity ->
            onActivityClick(activity)
        }
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = adapter

        view.findViewById<Button>(R.id.btnAddActivity).setOnClickListener {
            showAddActivityDialog()
        }

        // Load activities from DB
        lifecycleScope.launch {
            database.activityDao().getAllActivities().collect { activities ->
                adapter.submitList(activities)
            }
        }

        return view
    }

    private fun showAddActivityDialog() {
        EditActivityDialog(requireContext()) { activity ->
            lifecycleScope.launch {
                try {
                    database.activityDao().insertActivity(activity.toEntity())
                    Toast.makeText(requireContext(), "Activity added successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error adding activity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.show()
    }

    private fun onActivityClick(activity: Activity) {
        val options = arrayOf("Edit", "Delete", "Mark as Used")
        
        AlertDialog.Builder(requireContext())
            .setTitle(activity.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editActivity(activity)
                    1 -> deleteActivity(activity)
                    2 -> markAsUsed(activity)
                }
            }
            .show()
    }

    private fun editActivity(activity: Activity) {
        EditActivityDialog(requireContext(), activity) { updatedActivity ->
            lifecycleScope.launch {
                try {
                    database.activityDao().updateActivity(updatedActivity.toEntity())
                    Toast.makeText(requireContext(), "Activity updated successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error updating activity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.show()
    }

    private fun deleteActivity(activity: Activity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete '${activity.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.activityDao().deleteActivity(activity.toEntity())
                        Toast.makeText(requireContext(), "Activity deleted successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error deleting activity: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markAsUsed(activity: Activity) {
        lifecycleScope.launch {
            try {
                database.activityDao().incrementUsageCount(activity.id)
                Toast.makeText(requireContext(), "Activity marked as used!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating usage: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
