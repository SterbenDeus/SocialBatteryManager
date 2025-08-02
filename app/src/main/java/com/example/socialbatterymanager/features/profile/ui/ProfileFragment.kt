package com.example.socialbatterymanager.features.profile.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.User
import com.example.socialbatterymanager.BuildConfig
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "profile_preferences")

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
    private lateinit var notificationsSwitch: Switch
    private lateinit var editProfileButton: Button
    private lateinit var signOutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var recalibrationButton: Button
    private lateinit var surveyButton: Button
    private lateinit var privacySettingsButton: Button

    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    // DataStore keys
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val EMAIL_KEY = stringPreferencesKey("user_email")
    private val CAPACITY_KEY = intPreferencesKey("battery_capacity")
    private val WARNING_KEY = intPreferencesKey("warning_level")
    private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
    private val MOOD_KEY = stringPreferencesKey("current_mood")
    private val PHOTO_KEY = stringPreferencesKey("profile_photo")

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            saveImageUri(it.toString())
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                profileImage.setImageURI(uri)
                saveImageUri(uri.toString())
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showImageSourceDialog()
        } else {
            Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        sharedPreferences = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        
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
        notificationsSwitch = view.findViewById(R.id.notificationsSwitch)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        signOutButton = view.findViewById(R.id.signOutButton)
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton)
        recalibrationButton = view.findViewById(R.id.recalibrationButton)
        surveyButton = view.findViewById(R.id.surveyButton)
        privacySettingsButton = view.findViewById(R.id.privacySettingsButton)
    }

    private fun setupClickListeners() {
        profileImage.setOnClickListener {
            checkPermissionAndShowImageOptions()
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

        recalibrationButton.setOnClickListener {
            showRecalibrationDialog()
        }

        // Hide survey feature in release builds until implemented
        if (!BuildConfig.DEBUG) {
            surveyButton.visibility = View.GONE
        } else {
            surveyButton.isEnabled = false
        }

        privacySettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_privacySettingsFragment)
        }

        capacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                capacityText.text = getString(R.string.percent_format, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        warningSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                warningText.text = getString(R.string.percent_format, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun checkPermissionAndShowImageOptions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                showImageSourceDialog()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Toast.makeText(requireContext(), getString(R.string.permission_access_photos), Toast.LENGTH_LONG).show()
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(
            getString(R.string.choose_from_gallery),
            getString(R.string.take_photo),
            getString(R.string.cancel)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.select_image))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*")
                    1 -> openCamera()
                    2 -> {} // Cancel
                }
            }
            .show()
    }

    private fun openCamera() {
        val photoFile = java.io.File(
            requireContext().getExternalFilesDir(null),
            "profile_photo_${System.currentTimeMillis()}.jpg"
        )
        selectedImageUri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        cameraLauncher.launch(selectedImageUri)
    }

    private fun loadUserProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                requireContext().dataStore.data.map { preferences ->
                    User(
                        id = "1",
                        name = preferences[NAME_KEY] ?: getString(R.string.default_user_name),
                        email = preferences[EMAIL_KEY] ?: getString(R.string.default_user_email),
                        photoUri = preferences[PHOTO_KEY],
                        batteryCapacity = preferences[CAPACITY_KEY] ?: 100,
                        warningLevel = preferences[WARNING_KEY] ?: 30,
                        currentMood = preferences[MOOD_KEY] ?: getString(R.string.default_mood),
                        notificationsEnabled = preferences[NOTIFICATIONS_KEY] ?: true
                    )
                }.collect { user ->
                    currentUser = user
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        currentUser?.let { user ->
            nameEditText.setText(user.name)
            emailEditText.setText(user.email)
            capacitySeekBar.progress = user.batteryCapacity
            capacityText.text = getString(R.string.percent_format, user.batteryCapacity)
            warningSeekBar.progress = user.warningLevel
            warningText.text = getString(R.string.percent_format, user.warningLevel)
            notificationsSwitch.isChecked = user.notificationsEnabled
            
            user.photoUri?.let { uri ->
                selectedImageUri = Uri.parse(uri)
                profileImage.setImageURI(selectedImageUri)
            }
        }
    }

    private fun toggleEditMode() {
        val isEditMode = editProfileButton.text == getString(R.string.edit_profile)
        
        if (isEditMode) {
            // Switch to edit mode
            nameEditText.isEnabled = true
            emailEditText.isEnabled = true
            capacitySeekBar.isEnabled = true
            warningSeekBar.isEnabled = true
            moodSpinner.isEnabled = true
            notificationsSwitch.isEnabled = true
            editProfileButton.text = getString(R.string.save_changes)
        } else {
            // Save changes and switch to view mode
            saveProfileChanges()
            nameEditText.isEnabled = false
            emailEditText.isEnabled = false
            capacitySeekBar.isEnabled = false
            warningSeekBar.isEnabled = false
            moodSpinner.isEnabled = false
            notificationsSwitch.isEnabled = false
            editProfileButton.text = getString(R.string.edit_profile)
        }
    }

    private fun saveProfileChanges() {
        lifecycleScope.launch {
            requireContext().dataStore.edit { preferences ->
                preferences[NAME_KEY] = nameEditText.text.toString()
                preferences[EMAIL_KEY] = emailEditText.text.toString()
                preferences[CAPACITY_KEY] = capacitySeekBar.progress
                preferences[WARNING_KEY] = warningSeekBar.progress
                preferences[NOTIFICATIONS_KEY] = notificationsSwitch.isChecked
                preferences[MOOD_KEY] = getMoodFromSpinner()
            }
            Toast.makeText(requireContext(), getString(R.string.profile_updated_success), Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUri(uri: String) {
        lifecycleScope.launch {
            requireContext().dataStore.edit { preferences ->
                preferences[PHOTO_KEY] = uri
            }
        }
    }

    private fun getMoodFromSpinner(): String {
        val moods = resources.getStringArray(R.array.mood_options)
        return moods.getOrNull(moodSpinner.selectedItemPosition)?.lowercase()
            ?: getString(R.string.default_mood)
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                performSignOut()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showRecalibrationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.battery_recalibration_title))
            .setMessage(getString(R.string.battery_recalibration_message))
            .setPositiveButton(getString(R.string.recalibrate)) { _, _ ->
                performRecalibration()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun performSignOut() {
        lifecycleScope.launch {
            requireContext().dataStore.edit { preferences ->
                preferences.clear()
            }
            Toast.makeText(requireContext(), getString(R.string.sign_out_success), Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun performDeleteAccount() {
        lifecycleScope.launch {
            requireContext().dataStore.edit { preferences ->
                preferences.clear()
            }
            // In a real app, this would also delete from server/database
            Toast.makeText(requireContext(), getString(R.string.account_deleted_success), Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    private fun performRecalibration() {
        lifecycleScope.launch {
            requireContext().dataStore.edit { preferences ->
                preferences[CAPACITY_KEY] = 100
                preferences[WARNING_KEY] = 30
            }
            loadUserProfile()
            Toast.makeText(requireContext(), getString(R.string.battery_recalibrated_success), Toast.LENGTH_SHORT).show()
        }
    }
}