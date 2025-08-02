package com.example.socialbatterymanager.features.notifications

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.NotificationEntity
import com.example.socialbatterymanager.data.model.NotificationType
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private lateinit var recyclerViewNotifications: RecyclerView
    private lateinit var tvNoNotifications: TextView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // Initialize database
        database = AppDatabase.getDatabase(requireContext())

        // Initialize views
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications)
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications)

        setupRecyclerView()
        loadNotifications()

        return view
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onNotificationClick = { notification ->
                handleNotificationClick(notification)
            },
            onRateActivity = { notification ->
                showRatingDialog(notification)
            },
            onSkipRating = { notification ->
                markNotificationAsRead(notification)
            }
        )

        recyclerViewNotifications.adapter = notificationAdapter
        recyclerViewNotifications.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            database.notificationDao().getAllNotifications().collect { notifications ->
                if (notifications.isEmpty()) {
                    tvNoNotifications.visibility = View.VISIBLE
                    recyclerViewNotifications.visibility = View.GONE
                } else {
                    tvNoNotifications.visibility = View.GONE
                    recyclerViewNotifications.visibility = View.VISIBLE
                    notificationAdapter.submitList(notifications)
                }
            }
        }
    }

    private fun handleNotificationClick(notification: NotificationEntity) {
        when (NotificationType.valueOf(notification.type)) {
            NotificationType.ENERGY_LOW -> {
                // Navigate to home or show energy tips
                markNotificationAsRead(notification)
            }
            NotificationType.BUSY_WEEK -> {
                // Navigate to calendar or show week overview  
                markNotificationAsRead(notification)
            }
            NotificationType.RATE_ACTIVITY -> {
                showRatingDialog(notification)
            }
        }
    }

    private fun showRatingDialog(notification: NotificationEntity) {
        if (notification.activityId == null) return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_rate_activity, null)

        val tvActivityName = dialogView.findViewById<TextView>(R.id.tvActivityName)
        val seekBarRating = dialogView.findViewById<SeekBar>(R.id.seekBarRating)
        val tvRatingLabel = dialogView.findViewById<TextView>(R.id.tvRatingLabel)
        val btnSubmitRating = dialogView.findViewById<Button>(R.id.btnSubmitRating)
        val btnSkip = dialogView.findViewById<Button>(R.id.btnSkip)

        // Load activity details
        lifecycleScope.launch {
            val activity = database.activityDao().getActivityById(notification.activityId)
            activity?.let {
                tvActivityName.text = it.name
            }
        }

        // Setup rating seekbar
        seekBarRating.max = 4 // 0-4 for 1-5 scale
        seekBarRating.progress = 2 // Default to middle (3)
        updateRatingLabel(tvRatingLabel, 3)

        seekBarRating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateRatingLabel(tvRatingLabel, progress + 1)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnSubmitRating.setOnClickListener {
            val rating = seekBarRating.progress + 1
            submitActivityRating(notification, rating)
            dialog.dismiss()
        }

        btnSkip.setOnClickListener {
            markNotificationAsRead(notification)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateRatingLabel(tvRatingLabel: TextView, rating: Int) {
        val labels = arrayOf("Very Draining", "Draining", "Neutral", "Energizing", "Very Energizing")
        tvRatingLabel.text = "$rating - ${labels[rating - 1]}"
    }

    private fun submitActivityRating(notification: NotificationEntity, rating: Int) {
        lifecycleScope.launch {
            notification.activityId?.let { activityId ->
                // Update activity rating and usage estimates
                val activity = database.activityDao().getActivityById(activityId)
                activity?.let {
                    val updatedActivity = it.copy(
                        rating = rating.toFloat(),
                        usageCount = it.usageCount + 1
                    )
                    database.activityDao().updateActivity(updatedActivity)
                }
            }
            markNotificationAsRead(notification)
        }
    }

    private fun markNotificationAsRead(notification: NotificationEntity) {
        lifecycleScope.launch {
            database.notificationDao().updateNotification(
                notification.copy(isRead = true)
            )
        }
    }
}