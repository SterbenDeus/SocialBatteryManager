package com.example.socialbatterymanager.profile

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.model.User

class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profileImage: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var capacitySeekBar: SeekBar
    private lateinit var capacityText: TextView
    private lateinit var warningSeekBar: SeekBar
    private lateinit var warningText: TextView
    private lateinit var moodSpinner: Spinner
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button
    private lateinit var deleteAccountButton: Button

    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            saveProfileChanges()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        sharedPreferences = requireActivity().getSharedPreferences("profile_prefs", android.content.Context.MODE_PRIVATE)
        
        initializeViews(view)
        setupClickListeners()
        loadUserProfile()
        
        return view
    }

    private fun initializeViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        nameEditText = view.findViewById(R.id.nameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        capacitySeekBar = view.findViewById(R.id.capacitySeekBar)
        capacityText = view.findViewById(R.id.capacityText)
        warningSeekBar = view.findViewById(R.id.warningSeekBar)
        warningText = view.findViewById(R.id.warningText)
        moodSpinner = view.findViewById(R.id.moodSpinner)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        signOutButton = view.findViewById(R.id.signOutButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)
    }

    private fun setupClickListeners() {
        profileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        editProfileButton.setOnClickListener {
            toggleEditMode()
        }

        signOutButton.setOnClickListener {
            showSignOutDialog()
        }

        deleteAccountButton.setOnClickListener {
            showDeleteAccountDialog()
        }

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
    }

    private fun loadUserProfile() {
        val name = sharedPreferences.getString("user_name", "John Doe") ?: "John Doe"
        val email = sharedPreferences.getString("user_email", "john@example.com") ?: "john@example.com"
        val capacity = sharedPreferences.getInt("battery_capacity", 100)
        val warningLevel = sharedPreferences.getInt("warning_level", 30)
        val imageUri = sharedPreferences.getString("profile_image", null)

        currentUser = User(
            id = "1",
            name = name,
            email = email,
            photoUri = imageUri,
            batteryCapacity = capacity,
            warningLevel = warningLevel
        )

        updateUI()
    }

    private fun updateUI() {
        currentUser?.let { user ->
            nameEditText.setText(user.name)
            emailEditText.setText(user.email)
            capacitySeekBar.progress = user.batteryCapacity
            capacityText.text = "${user.batteryCapacity}%"
            warningSeekBar.progress = user.warningLevel
            warningText.text = "${user.warningLevel}%"
            
            user.photoUri?.let { uri ->
                selectedImageUri = Uri.parse(uri)
                profileImage.setImageURI(selectedImageUri)
            }
        }
    }

    private fun toggleEditMode() {
        val isEditMode = editProfileButton.text == "Edit Profile"
        
        if (isEditMode) {
            // Switch to edit mode
            nameEditText.isEnabled = true
            emailEditText.isEnabled = true
            capacitySeekBar.isEnabled = true
            warningSeekBar.isEnabled = true
            moodSpinner.isEnabled = true
            editProfileButton.text = "Save Changes"
        } else {
            // Save changes and switch to view mode
            saveProfileChanges()
            nameEditText.isEnabled = false
            emailEditText.isEnabled = false
            capacitySeekBar.isEnabled = false
            warningSeekBar.isEnabled = false
            moodSpinner.isEnabled = false
            editProfileButton.text = "Edit Profile"
        }
    }

    private fun saveProfileChanges() {
        val editor = sharedPreferences.edit()
        editor.putString("user_name", nameEditText.text.toString())
        editor.putString("user_email", emailEditText.text.toString())
        editor.putInt("battery_capacity", capacitySeekBar.progress)
        editor.putInt("warning_level", warningSeekBar.progress)
        selectedImageUri?.let { uri ->
            editor.putString("profile_image", uri.toString())
        }
        editor.apply()
        
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                performSignOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performSignOut() {
        // Clear user data
        sharedPreferences.edit().clear().apply()
        
        // Navigate to login screen or close app
        Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    private fun performDeleteAccount() {
        // Clear all user data
        sharedPreferences.edit().clear().apply()
        
        // Delete user from database/server
        // This would involve API calls in a real app
        
        Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }
}