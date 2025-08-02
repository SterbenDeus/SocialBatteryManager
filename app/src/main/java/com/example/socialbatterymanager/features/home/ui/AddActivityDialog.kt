package com.example.socialbatterymanager.features.home.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.ActivityEntity

class AddActivityDialog(
    private val context: Context,
    private val onActivityAdded: (ActivityEntity) -> Unit,
    private val existingActivity: ActivityEntity? = null
) {

    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_activity, null)
        
        val etName = view.findViewById<EditText>(R.id.etActivityName)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerActivityType)
        val spinnerEnergy = view.findViewById<Spinner>(R.id.spinnerEnergyImpact)
        val etPeople = view.findViewById<EditText>(R.id.etPeople)
        val spinnerMood = view.findViewById<Spinner>(R.id.spinnerMood)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)

        // Setup spinners
        setupSpinners(spinnerType, spinnerEnergy, spinnerMood)

        // Pre-fill if editing existing activity
        existingActivity?.let { activity ->
            etName.setText(activity.name)
            etPeople.setText(activity.people)
            etNotes.setText(activity.notes)
            
            // Set spinner selections
            val typeAdapter = spinnerType.adapter as ArrayAdapter<String>
            spinnerType.setSelection(typeAdapter.getPosition(activity.type))
            
            val energyAdapter = spinnerEnergy.adapter as ArrayAdapter<String>
            spinnerEnergy.setSelection(energyAdapter.getPosition(activity.energy.toString()))
            
            val moodAdapter = spinnerMood.adapter as ArrayAdapter<String>
            spinnerMood.setSelection(moodAdapter.getPosition(activity.mood))
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(
                if (existingActivity == null)
                    context.getString(R.string.add_activity_title)
                else
                    context.getString(R.string.edit_activity_title)
            )
            .setView(view)
            .setPositiveButton(context.getString(R.string.save)) { _, _ ->
                val name = etName.text.toString().trim()
                val type = spinnerType.selectedItem.toString()
                val energy = spinnerEnergy.selectedItem.toString().toIntOrNull() ?: 0
                val people = etPeople.text.toString().trim()
                val mood = spinnerMood.selectedItem.toString()
                val notes = etNotes.text.toString().trim()

                if (name.isNotEmpty()) {
                    val activity = ActivityEntity(
                        id = existingActivity?.id ?: 0,
                        name = name,
                        type = type,
                        energy = energy,
                        people = people,
                        mood = mood,
                        notes = notes,
                        date = existingActivity?.date ?: System.currentTimeMillis()
                    )
                    onActivityAdded(activity)
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_activity_name_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()

        dialog.show()
    }

    private fun setupSpinners(
        typeSpinner: Spinner,
        energySpinner: Spinner,
        moodSpinner: Spinner
    ) {
        // Activity types
        val types = context.resources.getStringArray(R.array.activity_type_options)
        val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        // Energy impact values
        val energyValues = context.resources.getStringArray(R.array.energy_impact_values)
        val energyAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, energyValues)
        energyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        energySpinner.adapter = energyAdapter
        energySpinner.setSelection(5) // Default to 0

        // Mood options
        val moods = context.resources.getStringArray(R.array.mood_dialog_options)
        val moodAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, moods)
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        moodSpinner.adapter = moodAdapter
        moodSpinner.setSelection(1) // Default to Neutral
    }
}