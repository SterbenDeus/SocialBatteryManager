package com.example.socialbatterymanager.profile

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.model.User

class EditProfileDialog : DialogFragment() {

    interface EditProfileListener {
        fun onProfileUpdated(user: User)
    }

    private lateinit var listener: EditProfileListener
    private var currentUser: User? = null

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var capacitySeekBar: SeekBar
    private lateinit var capacityText: TextView
    private lateinit var warningSeekBar: SeekBar
    private lateinit var warningText: TextView
    private lateinit var moodSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    companion object {
        fun newInstance(user: User): EditProfileDialog {
            val dialog = EditProfileDialog()
            val args = Bundle()
            args.putString("user_id", user.id)
            args.putString("user_name", user.name)
            args.putString("user_email", user.email)
            args.putInt("user_capacity", user.batteryCapacity)
            args.putInt("user_warning", user.warningLevel)
            args.putString("user_mood", user.currentMood)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EditProfileListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement EditProfileListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_edit_profile, container, false)
        
        initializeViews(view)
        setupListeners()
        loadUserData()
        
        return view
    }

    private fun initializeViews(view: View) {
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        capacitySeekBar = view.findViewById(R.id.capacitySeekBar)
        capacityText = view.findViewById(R.id.capacityText)
        warningSeekBar = view.findViewById(R.id.warningSeekBar)
        warningText = view.findViewById(R.id.warningText)
        moodSpinner = view.findViewById(R.id.moodSpinner)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
    }

    private fun setupListeners() {
        capacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                capacityText.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        warningSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningText.text = "$progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            saveProfile()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun loadUserData() {
        arguments?.let { args ->
            val name = args.getString("user_name", "")
            val email = args.getString("user_email", "")
            val capacity = args.getInt("user_capacity", 100)
            val warning = args.getInt("user_warning", 30)
            val mood = args.getString("user_mood", "neutral")

            currentUser = User(
                id = args.getString("user_id", ""),
                name = name,
                email = email,
                batteryCapacity = capacity,
                warningLevel = warning,
                currentMood = mood
            )

            nameEditText.setText(name)
            emailEditText.setText(email)
            capacitySeekBar.progress = capacity
            capacityText.text = "$capacity%"
            warningSeekBar.progress = warning
            warningText.text = "$warning%"
        }
    }

    private fun saveProfile() {
        currentUser?.let { user ->
            val updatedUser = user.copy(
                name = nameEditText.text.toString(),
                email = emailEditText.text.toString(),
                batteryCapacity = capacitySeekBar.progress,
                warningLevel = warningSeekBar.progress,
                currentMood = getMoodFromSpinner(),
                lastUpdated = System.currentTimeMillis()
            )
            
            listener.onProfileUpdated(updatedUser)
            dismiss()
        }
    }

    private fun getMoodFromSpinner(): String {
        val moods = arrayOf("energetic", "happy", "neutral", "tired", "stressed", "overwhelmed")
        return moods.getOrNull(moodSpinner.selectedItemPosition) ?: "neutral"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}