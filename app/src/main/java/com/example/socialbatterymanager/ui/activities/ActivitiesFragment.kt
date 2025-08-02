package com.example.socialbatterymanager.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.repository.DataRepository
import com.example.socialbatterymanager.data.repository.SecurityManager
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.model.Activity
import com.example.socialbatterymanager.model.toEntity
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {

    private lateinit var rvActivities: RecyclerView
    private lateinit var adapter: ActivitiesAdapter

    private lateinit var dataRepository: DataRepository

    private lateinit var database: AppDatabase


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        database = AppDatabase.getDatabase(requireContext())

        rvActivities = view.findViewById(R.id.rvActivities)
        adapter = ActivitiesAdapter { activity: Activity ->
            onActivityClick(activity)
        }
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = adapter

        // Initialize data repository with encryption support
        val securityManager = SecurityManager.getInstance(requireContext())
        val passphrase = if (securityManager.isEncryptionEnabled()) {
            securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
        } else {
            null
        }
        dataRepository = DataRepository.getInstance(requireContext(), passphrase)

        view.findViewById<Button>(R.id.btnAddActivity).setOnClickListener {
            showAddActivityDialog()
        }

        // Load activities from DB
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                database.activityDao().getAllActivities().collect { activities ->
                    adapter.submitList(activities)
                }
            }
        }

        return view
    }

    private fun showAddActivityDialog() {
        val dialog = CreateNewActivityDialog()
        dialog.setOnActivityCreatedListener { activity ->
            lifecycleScope.launch {
                try {
                    dataRepository.insertActivity(activity.toEntity())
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.activity_add_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.activity_add_error, e.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show(parentFragmentManager, "CreateNewActivityDialog")
    }

    private fun onActivityClick(activity: Activity) {
        val options = arrayOf(
            getString(R.string.edit),
            getString(R.string.delete),
            getString(R.string.mark_as_used)
        )
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
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.activity_update_success),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.activity_update_error, e.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.show()
    }

    private fun deleteActivity(activity: Activity) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_activity_title))
            .setMessage(
                getString(
                    R.string.delete_activity_confirmation,
                    activity.name
                )
            )
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.activityDao().deleteActivity(activity.toEntity())
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.activity_delete_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.activity_delete_error, e.message ?: ""),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun markAsUsed(activity: Activity) {
        lifecycleScope.launch {
            try {
                database.activityDao().incrementUsageCount(activity.id)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.activity_marked_as_used),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.activity_usage_error, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
