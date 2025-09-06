package com.example.socialbatterymanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.socialbatterymanager.BuildConfig
import com.example.socialbatterymanager.shared.preferences.PreferencesManager
import com.example.socialbatterymanager.sync.SyncManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var syncManager: SyncManager
    private lateinit var bottomNavigationView: BottomNavigationView

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // No-op; permission result handled silently
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = PreferencesManager(this)
        syncManager = SyncManager(this)

        // Show a splash screen while preferences load
        setContentView(R.layout.activity_splash)

        lifecycleScope.launch {
            // Set initial theme before inflating the main layout
            val initialTheme = try {
                preferencesManager.themeFlow
                    .catch { e ->
                        if (e is IOException) {
                            emit(PreferencesManager.THEME_SYSTEM)
                        } else {
                            throw e
                        }
                    }
                    .first()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load theme preference", e)
                FirebaseCrashlytics.getInstance().recordException(
                    RuntimeException("Failed to load theme preference", e)
                )
                PreferencesManager.THEME_SYSTEM
            }

            when (initialTheme) {
                PreferencesManager.THEME_LIGHT -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                PreferencesManager.THEME_DARK -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                PreferencesManager.THEME_SYSTEM -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            setContentView(R.layout.activity_main)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController

            bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView.setupWithNavController(navController)

            requestNotificationPermissionIfNeeded()

            // Check onboarding status and navigate accordingly
            checkOnboardingStatus(navController)

            // Initialize sync
            initializeSync()

            // Observe theme changes for runtime updates
            observeThemeChanges()
        }
    }
    
    private fun initializeSync() {
        lifecycleScope.launch {
            try {
                syncManager.schedulePeriodicSync()
            } catch (e: Exception) {
                // Handle sync initialization error
                Log.e("MainActivity", "Failed to schedule periodic sync", e)
                FirebaseCrashlytics.getInstance().recordException(
                    RuntimeException("Failed to schedule periodic sync", e)
                )
            }
        }
    }
    
    private fun checkOnboardingStatus(navController: androidx.navigation.NavController) {
        lifecycleScope.launch {
            preferencesManager.onboardingCompletedFlow
                .catch { e ->
                    if (e is IOException) {
                        emit(false)
                    } else {
                        throw e
                    }
                }
                .collect { completed ->
                    if (completed) {
                        // User has completed onboarding, navigate to home if currently on onboarding
                        if (navController.currentDestination?.id == R.id.onboardingFragment) {
                            navController.navigate(R.id.action_onboarding_to_home)
                        }
                    } else {
                        // User hasn't completed onboarding, navigate to onboarding if not already there
                        if (navController.currentDestination?.id != R.id.onboardingFragment) {
                            navController.navigate(R.id.onboardingFragment)
                        }
                    }
                }
        }
    }
    
    private fun observeThemeChanges() {
        lifecycleScope.launch {
            preferencesManager.themeFlow
                .catch { e ->
                    if (e is IOException) {
                        emit(PreferencesManager.THEME_SYSTEM)
                    } else {
                        throw e
                    }
                }
                .collect { theme ->
                    when (theme) {
                        PreferencesManager.THEME_LIGHT -> {
                            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }
                        }
                        PreferencesManager.THEME_DARK -> {
                            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }
                        }
                        PreferencesManager.THEME_SYSTEM -> {
                            if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            }
                        }
                    }
                }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (!BuildConfig.FEATURE_NOTIFICATIONS) return

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.notification_permission_title)
                    .setMessage(R.string.notification_permission_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}