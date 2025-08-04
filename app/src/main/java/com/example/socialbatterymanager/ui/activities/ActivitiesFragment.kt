package com.example.socialbatterymanager.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.repository.DataRepository
import com.example.socialbatterymanager.data.repository.SecurityManager
import com.example.socialbatterymanager.model.Activity
import com.example.socialbatterymanager.model.toEntity

class ActivitiesFragment : Fragment() {

    private lateinit var rvActivities: RecyclerView
    private lateinit var adapter: ActivitiesAdapter

    private val viewModel: ActivitiesViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val securityManager = SecurityManager.getInstance(requireContext())
                val passphrase = if (securityManager.isEncryptionEnabled()) {
                    securityManager.getDatabasePassphrase() ?: securityManager.generateDatabasePassphrase()
                } else {
                    null
                }
                val repo = DataRepository.getInstance(requireContext(), passphrase)
                return ActivitiesViewModel(repo) as T
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        rvActivities = view.findViewById(R.id.rvActivities)
        adapter = ActivitiesAdapter { activity: Activity ->
            onActivityClick(activity)
        }
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = adapter

        view.findViewById<Button>(R.id.btnAddActivity).setOnClickListener {
            showAddActivityDialog()
        }

        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            adapter.submitList(activities)
        }

        return view
    }

    private fun showAddActivityDialog() {
        val dialog = CreateNewActivityDialog()
        dialog.setOnActivityCreatedListener { activity ->
            viewModel.insertActivity(activity.toEntity()) { success, error ->
                Toast.makeText(
                    requireContext(),
                    if (success)
                        getString(R.string.activity_add_success)
                    else
                        getString(R.string.activity_add_error, error ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
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
            viewModel.updateActivity(updatedActivity.toEntity()) { success, error ->
                Toast.makeText(
                    requireContext(),
                    if (success)
                        getString(R.string.activity_update_success)
                    else
                        getString(R.string.activity_update_error, error ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
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
                        val entity = activity.toEntity()
                        database.activityDao().deleteActivity(entity)
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
        viewModel.markAsUsed(activity.id) { success, error ->
            Toast.makeText(
                requireContext(),
                if (success)
                    getString(R.string.activity_marked_as_used)
                else
                    getString(R.string.activity_usage_error, error ?: ""),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
