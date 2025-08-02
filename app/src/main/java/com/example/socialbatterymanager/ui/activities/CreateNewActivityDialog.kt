package com.example.socialbatterymanager.ui.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.model.Activity
import com.example.socialbatterymanager.model.ActivityType
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CreateNewActivityDialog : DialogFragment() {

    private lateinit var database: AppDatabase
    private lateinit var etActivityName: EditText
    private lateinit var etActivityDescription: EditText
    private lateinit var spinnerActivityType: Spinner
    private lateinit var chipGroupPeople: ChipGroup
    private lateinit var btnAddPerson: Button
    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var tvEnergyPoints: TextView
    private lateinit var layoutHighEnergyWarning: LinearLayout
    private lateinit var layoutWeeklyCapacityWarning: LinearLayout
    private lateinit var tvWeeklyCapacityMessage: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnScheduleActivity: Button

    private val selectedPeople = mutableListOf<Person>()
    private val calendar = Calendar.getInstance()
    private var selectedDate = System.currentTimeMillis()
    private var startTime = ""
    private var endTime = ""
    
    private var onActivityCreated: ((Activity) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_create_new_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        database = AppDatabase.getDatabase(requireContext())
        initViews(view)
        setupViews()
        calculateEnergyPoints()
    }

    private fun initViews(view: View) {
        etActivityName = view.findViewById(R.id.etActivityName)
        etActivityDescription = view.findViewById(R.id.etActivityDescription)
        spinnerActivityType = view.findViewById(R.id.spinnerActivityType)
        chipGroupPeople = view.findViewById(R.id.chipGroupPeople)
        btnAddPerson = view.findViewById(R.id.btnAddPerson)
        etDate = view.findViewById(R.id.etDate)
        etStartTime = view.findViewById(R.id.etStartTime)
        etEndTime = view.findViewById(R.id.etEndTime)
        tvEnergyPoints = view.findViewById(R.id.tvEnergyPoints)
        layoutHighEnergyWarning = view.findViewById(R.id.layoutHighEnergyWarning)
        layoutWeeklyCapacityWarning = view.findViewById(R.id.layoutWeeklyCapacityWarning)
        tvWeeklyCapacityMessage = view.findViewById(R.id.tvWeeklyCapacityMessage)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnScheduleActivity = view.findViewById(R.id.btnScheduleActivity)
    }

    private fun setupViews() {
        // Setup activity type spinner
        val activityTypes = arrayOf("Work", "Leisure", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activityTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerActivityType.adapter = adapter
        spinnerActivityType.setSelection(1) // Default to Leisure
        
        // Set default date and time
        setupDefaultDateTime()
        
        // Setup click listeners
        etDate.setOnClickListener { showDatePicker() }
        etStartTime.setOnClickListener { showTimePicker(true) }
        etEndTime.setOnClickListener { showTimePicker(false) }
        btnAddPerson.setOnClickListener { showAddPersonDialog() }
        btnCancel.setOnClickListener { dismiss() }
        btnScheduleActivity.setOnClickListener { createActivity() }
        
        // Add listeners for energy calculation
        etActivityName.setOnTextChangedListener { calculateEnergyPoints() }
        etActivityDescription.setOnTextChangedListener { calculateEnergyPoints() }
        spinnerActivityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                calculateEnergyPoints()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDefaultDateTime() {
        calendar.timeInMillis = selectedDate
        
        // Set current date
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))
        
        // Set default start time (current hour + 1)
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        calendar.set(Calendar.MINUTE, 0)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        startTime = timeFormat.format(calendar.time)
        etStartTime.setText(startTime)
        
        // Set default end time (start time + 1 hour)
        calendar.add(Calendar.HOUR_OF_DAY, 1)
        endTime = timeFormat.format(calendar.time)
        etEndTime.setText(endTime)
    }

    private fun showDatePicker() {
        calendar.timeInMillis = selectedDate
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = timeFormat.format(calendar.time)
                
                if (isStartTime) {
                    startTime = formattedTime
                    etStartTime.setText(formattedTime)
                } else {
                    endTime = formattedTime
                    etEndTime.setText(formattedTime)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }

    private fun showAddPersonDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Enter person's name"
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Add Person")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val personName = editText.text.toString().trim()
                if (personName.isNotEmpty()) {
                    addPersonToActivity(personName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addPersonToActivity(personName: String) {
        lifecycleScope.launch {
            // Check if person already exists in database
            var person = database.personDao().getPersonByName(personName)
            
            if (person == null) {
                // Create new person if not exists
                person = Person(name = personName)
                val personId = database.personDao().insertPerson(person)
                person = person.copy(id = personId.toInt())
            }
            
            // Add to selected people if not already added
            if (!selectedPeople.any { it.id == person.id }) {
                selectedPeople.add(person)
                addPersonChip(person)
                calculateEnergyPoints()
            }
        }
    }

    private fun addPersonChip(person: Person) {
        val chip = Chip(requireContext())
        chip.text = person.name
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            selectedPeople.remove(person)
            chipGroupPeople.removeView(chip)
            calculateEnergyPoints()
        }
        chipGroupPeople.addView(chip)
    }

    private fun calculateEnergyPoints() {
        val activityType = getSelectedActivityType()
        val peopleCount = selectedPeople.size
        
        // Base energy calculation
        var energyPoints = when (activityType) {
            ActivityType.WORK -> 30
            ActivityType.LEISURE -> 20
            ActivityType.OTHER -> 25
        }
        
        // Add energy based on number of people (social interaction)
        energyPoints += peopleCount * 10
        
        // Add energy based on activity name complexity/length
        val activityName = etActivityName.text.toString()
        if (activityName.length > 10) {
            energyPoints += 5
        }
        
        tvEnergyPoints.text = energyPoints.toString()
        
        // Show warnings
        showWarnings(energyPoints)
    }

    private fun showWarnings(energyPoints: Int) {
        // High energy warning
        if (energyPoints > 40) {
            layoutHighEnergyWarning.visibility = View.VISIBLE
        } else {
            layoutHighEnergyWarning.visibility = View.GONE
        }
        
        // Weekly capacity warning (simplified calculation for demo)
        val currentWeeklyUsage = 70 // This would come from actual data
        val projectedUsage = currentWeeklyUsage + energyPoints
        val weeklyTarget = 100 // This would come from user preferences
        
        if (projectedUsage > weeklyTarget * 0.9) { // 90% of target
            val remainingCapacity = ((weeklyTarget - projectedUsage).toFloat() / weeklyTarget * 100).toInt()
            tvWeeklyCapacityMessage.text = "This activity will bring your weekly capacity down to $remainingCapacity%. You may want to reschedule or reduce intensity."
            layoutWeeklyCapacityWarning.visibility = View.VISIBLE
        } else {
            layoutWeeklyCapacityWarning.visibility = View.GONE
        }
    }

    private fun getSelectedActivityType(): ActivityType {
        return when (spinnerActivityType.selectedItemPosition) {
            0 -> ActivityType.WORK
            1 -> ActivityType.LEISURE
            2 -> ActivityType.OTHER
            else -> ActivityType.LEISURE
        }
    }

    private fun createActivity() {
        val name = etActivityName.text.toString().trim()
        val description = etActivityDescription.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an activity name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val activity = Activity(
            name = name,
            description = description,
            type = getSelectedActivityType(),
            energy = tvEnergyPoints.text.toString().toIntOrNull() ?: 0,
            people = selectedPeople.joinToString(", ") { it.name },
            mood = "",
            notes = "",
            date = selectedDate,
            startTime = startTime,
            endTime = endTime
        )
        
        onActivityCreated?.invoke(activity)
        dismiss()
    }

    fun setOnActivityCreatedListener(listener: (Activity) -> Unit) {
        onActivityCreated = listener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance(): CreateNewActivityDialog {
            return CreateNewActivityDialog()
        }
    }
}

// Extension function for EditText text change listener
private fun EditText.setOnTextChangedListener(afterTextChanged: () -> Unit) {
    this.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) {
            afterTextChanged()
        }
    })
}