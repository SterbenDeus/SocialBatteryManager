package com.example.socialbatterymanager.ui.activities

import android.app.AlertDialog
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.core.widget.doAfterTextChanged
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.model.Activity
import com.example.socialbatterymanager.model.ActivityType

class EditActivityDialog(
    private val context: Context,
    private val activity: Activity? = null,
    private val onSave: (Activity) -> Unit
) {
    
    fun show() {
        val dialogView = android.view.LayoutInflater.from(context).inflate(R.layout.dialog_edit_activity, null)
        
        val etName = dialogView.findViewById<EditText>(R.id.etActivityName)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerActivityType)
        val etEnergy = dialogView.findViewById<EditText>(R.id.etActivityEnergy)
        val etPeople = dialogView.findViewById<EditText>(R.id.etActivityPeople)
        val etMood = dialogView.findViewById<EditText>(R.id.etActivityMood)
        val etNotes = dialogView.findViewById<EditText>(R.id.etActivityNotes)
        val etRating = dialogView.findViewById<EditText>(R.id.etActivityRating)
        
        // Setup spinner
        val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ActivityType.values())
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        
        // Populate fields if editing
        activity?.let {
            etName.setText(it.name as CharSequence)
            spinnerType.setSelection(it.type.ordinal)
            etEnergy.setText(it.energy.toString() as CharSequence)
            etPeople.setText(it.people as CharSequence)
            etMood.setText(it.mood as CharSequence)
            etNotes.setText(it.notes as CharSequence)
            etRating.setText(it.rating.toString() as CharSequence)
        }
        
        val dialog = AlertDialog.Builder(context)
            .setTitle(if (activity == null) "Add Activity" else "Edit Activity")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val type = ActivityType.values()[spinnerType.selectedItemPosition]
                val energy = etEnergy.text.toString().toIntOrNull() ?: 0
                val people = etPeople.text.toString().trim()
                val mood = etMood.text.toString().trim()
                val notes = etNotes.text.toString().trim()
                val rating = etRating.text.toString().toFloatOrNull() ?: 0.0f
                
                if (name.isNotEmpty()) {
                    val newActivity = Activity(
                        id = activity?.id ?: 0,
                        name = name,
                        type = type,
                        energy = energy,
                        people = people,
                        mood = mood,
                        notes = notes,
                        date = activity?.date ?: System.currentTimeMillis(),
                        usageCount = activity?.usageCount ?: 0,
                        rating = rating
                    )
                    onSave(newActivity)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
    }
}