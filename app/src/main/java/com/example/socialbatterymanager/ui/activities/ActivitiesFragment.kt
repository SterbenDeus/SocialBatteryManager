package com.example.socialbatterymanager.ui.activities

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
import kotlinx.coroutines.launch

class ActivitiesFragment : Fragment() {

    private lateinit var rvActivities: RecyclerView
    private lateinit var adapter: ActivitiesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activities, container, false)

        rvActivities = view.findViewById(R.id.rvActivities)
        adapter = ActivitiesAdapter()
        rvActivities.layoutManager = LinearLayoutManager(requireContext())
        rvActivities.adapter = adapter

        view.findViewById<Button>(R.id.btnAddActivity).setOnClickListener {
            Toast.makeText(requireContext(), "Add Activity clicked!", Toast.LENGTH_SHORT).show()
            // TODO: Show Add Activity dialog and insert to DB
        }

        // Load activities from DB (collect with lifecycleScope)
        val db = AppDatabase.getDatabase(requireContext())
        lifecycleScope.launch {
            db.activityDao().getAllActivities().collect { activities ->
                adapter.submitList(activities)
            }
        }

        return view
    }
}
