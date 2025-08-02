package com.example.socialbatterymanager.features.privacy.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.Toast
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.BlockedUser
import com.example.socialbatterymanager.data.model.PrivacySettings
import com.example.socialbatterymanager.data.model.VisibilityLevel
import com.example.socialbatterymanager.features.auth.data.AuthRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.privacyDataStore by preferencesDataStore(name = "privacy_preferences")

class PrivacySettingsFragment : Fragment() {

    // UI Components
    private lateinit var privacyLevelRadioGroup: RadioGroup
    private lateinit var everyoneRadioButton: RadioButton
    private lateinit var friendsOnlyRadioButton: RadioButton
    private lateinit var closeFriendsRadioButton: RadioButton
    private lateinit var onlyMeRadioButton: RadioButton
    private lateinit var moodStatusSwitch: Switch
    private lateinit var energyLevelSwitch: Switch
    private lateinit var activityPatternsSwitch: Switch
    private lateinit var addUserButton: Button
    private lateinit var blockedUsersRecyclerView: RecyclerView

    // Data
    private var currentPrivacySettings: PrivacySettings? = null
    private val blockedUsers = mutableListOf<BlockedUser>()
    private lateinit var blockedUsersAdapter: BlockedUsersAdapter
    private val authRepository = AuthRepository()

    // DataStore keys
    private val VISIBILITY_LEVEL_KEY = stringPreferencesKey("visibility_level")
    private val MOOD_STATUS_ENABLED_KEY = booleanPreferencesKey("mood_status_enabled")
    private val ENERGY_LEVEL_ENABLED_KEY = booleanPreferencesKey("energy_level_enabled")
    private val ACTIVITY_PATTERNS_ENABLED_KEY = booleanPreferencesKey("activity_patterns_enabled")
    private val BLOCKED_USERS_KEY = stringPreferencesKey("blocked_users_json")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_settings, container, false)
        
        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadPrivacySettings()
        
        return view
    }

    private fun initializeViews(view: View) {
        privacyLevelRadioGroup = view.findViewById(R.id.privacyLevelRadioGroup)
        everyoneRadioButton = view.findViewById(R.id.everyoneRadioButton)
        friendsOnlyRadioButton = view.findViewById(R.id.friendsOnlyRadioButton)
        closeFriendsRadioButton = view.findViewById(R.id.closeFriendsRadioButton)
        onlyMeRadioButton = view.findViewById(R.id.onlyMeRadioButton)
        moodStatusSwitch = view.findViewById(R.id.moodStatusSwitch)
        energyLevelSwitch = view.findViewById(R.id.energyLevelSwitch)
        activityPatternsSwitch = view.findViewById(R.id.activityPatternsSwitch)
        addUserButton = view.findViewById(R.id.addUserButton)
        blockedUsersRecyclerView = view.findViewById(R.id.blockedUsersRecyclerView)
    }

    private fun setupRecyclerView() {
        blockedUsersAdapter = BlockedUsersAdapter(blockedUsers) { blockedUser ->
            showRemoveUserDialog(blockedUser)
        }
        blockedUsersRecyclerView.adapter = blockedUsersAdapter
        blockedUsersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClickListeners() {
        // Privacy level radio group listener
        privacyLevelRadioGroup.setOnCheckedChangeListener { _, _ ->
            savePrivacySettings()
        }

        // Permission switches listeners
        moodStatusSwitch.setOnCheckedChangeListener { _, _ ->
            savePrivacySettings()
        }

        energyLevelSwitch.setOnCheckedChangeListener { _, _ ->
            savePrivacySettings()
        }

        activityPatternsSwitch.setOnCheckedChangeListener { _, _ ->
            savePrivacySettings()
        }

        // Add user button
        addUserButton.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun loadPrivacySettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                requireContext().privacyDataStore.data.map { preferences ->
                    val userId = authRepository.currentUser?.uid ?: ""
                    PrivacySettings(
                        userId = userId,
                        moodVisibilityLevel = VisibilityLevel.valueOf(
                            preferences[VISIBILITY_LEVEL_KEY] ?: VisibilityLevel.FRIENDS_ONLY.name
                        ),
                        moodStatusEnabled = preferences[MOOD_STATUS_ENABLED_KEY] ?: true,
                        energyLevelEnabled = preferences[ENERGY_LEVEL_ENABLED_KEY] ?: true,
                        activityPatternsEnabled = preferences[ACTIVITY_PATTERNS_ENABLED_KEY] ?: false
                    )
                }.collect { settings ->
                    currentPrivacySettings = settings
                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {
        currentPrivacySettings?.let { settings ->
            // Update radio buttons
            when (settings.moodVisibilityLevel) {
                VisibilityLevel.EVERYONE -> everyoneRadioButton.isChecked = true
                VisibilityLevel.FRIENDS_ONLY -> friendsOnlyRadioButton.isChecked = true
                VisibilityLevel.CLOSE_FRIENDS -> closeFriendsRadioButton.isChecked = true
                VisibilityLevel.ONLY_ME -> onlyMeRadioButton.isChecked = true
            }

            // Update switches
            moodStatusSwitch.isChecked = settings.moodStatusEnabled
            energyLevelSwitch.isChecked = settings.energyLevelEnabled
            activityPatternsSwitch.isChecked = settings.activityPatternsEnabled
        }
    }

    private fun savePrivacySettings() {
        lifecycleScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            val visibilityLevel = when {
                everyoneRadioButton.isChecked -> VisibilityLevel.EVERYONE
                friendsOnlyRadioButton.isChecked -> VisibilityLevel.FRIENDS_ONLY
                closeFriendsRadioButton.isChecked -> VisibilityLevel.CLOSE_FRIENDS
                onlyMeRadioButton.isChecked -> VisibilityLevel.ONLY_ME
                else -> VisibilityLevel.FRIENDS_ONLY
            }

            requireContext().privacyDataStore.edit { preferences ->
                preferences[USER_ID_KEY] = userId
                preferences[VISIBILITY_LEVEL_KEY] = visibilityLevel.name
                preferences[MOOD_STATUS_ENABLED_KEY] = moodStatusSwitch.isChecked
                preferences[ENERGY_LEVEL_ENABLED_KEY] = energyLevelSwitch.isChecked
                preferences[ACTIVITY_PATTERNS_ENABLED_KEY] = activityPatternsSwitch.isChecked
            }

            currentPrivacySettings = PrivacySettings(
                userId = userId,
                moodVisibilityLevel = visibilityLevel,
                moodStatusEnabled = moodStatusSwitch.isChecked,
                energyLevelEnabled = energyLevelSwitch.isChecked,
                activityPatternsEnabled = activityPatternsSwitch.isChecked
            )

            Toast.makeText(
                requireContext(),
                getString(R.string.privacy_settings_updated),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_blocked_user, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditText)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.block_user_title))
            .setMessage(getString(R.string.block_user_message))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.block)) { _, _ ->
                val name = nameEditText.text.toString().trim()
                val email = emailEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    addBlockedUser(name, email)
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_enter_name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showRemoveUserDialog(blockedUser: BlockedUser) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.remove_blocked_user_title))
            .setMessage(
                getString(
                    R.string.remove_blocked_user_message,
                    blockedUser.blockedUserName
                )
            )
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                removeBlockedUser(blockedUser)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun addBlockedUser(name: String, email: String) {
        val userId = authRepository.currentUser?.uid ?: return
        val blockedUser = BlockedUser(
            userId = userId,
            blockedUserId = email.ifEmpty { "user_${System.currentTimeMillis()}" },
            blockedUserName = name,
            blockedUserEmail = email.ifEmpty { null }
        )

        blockedUsers.add(blockedUser)
        blockedUsersAdapter.notifyItemInserted(blockedUsers.size - 1)
        
        // In real app, save to database for this user
        Toast.makeText(
            requireContext(),
            getString(R.string.blocked_user_added, name),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun removeBlockedUser(blockedUser: BlockedUser) {
        val userId = authRepository.currentUser?.uid ?: return
        val position = blockedUsers.indexOfFirst {
            it.blockedUserId == blockedUser.blockedUserId && it.userId == userId
        }
        if (position != -1) {
            blockedUsers.removeAt(position)
            blockedUsersAdapter.notifyItemRemoved(position)

            // In real app, remove from database for this user
            Toast.makeText(
                requireContext(),
                getString(
                    R.string.blocked_user_removed,
                    blockedUser.blockedUserName
                ),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getSelectedVisibilityLevel(): VisibilityLevel {
        return when {
            everyoneRadioButton.isChecked -> VisibilityLevel.EVERYONE
            friendsOnlyRadioButton.isChecked -> VisibilityLevel.FRIENDS_ONLY
            closeFriendsRadioButton.isChecked -> VisibilityLevel.CLOSE_FRIENDS
            onlyMeRadioButton.isChecked -> VisibilityLevel.ONLY_ME
            else -> VisibilityLevel.FRIENDS_ONLY
        }
    }
}