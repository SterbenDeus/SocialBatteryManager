package com.example.socialbatterymanager.calendar

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialbatterymanager.R

class CalendarFragment : Fragment(R.layout.fragment_calendar) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btnAddCalendarEvent).setOnClickListener {
            Toast.makeText(requireContext(), "Add to Calendar clicked!", Toast.LENGTH_SHORT).show()
            // TODO: Open dialog or add event logic here
        }
    }
}
