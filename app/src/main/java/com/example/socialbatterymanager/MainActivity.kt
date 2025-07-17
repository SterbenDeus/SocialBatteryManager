package com.example.socialbatterymanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.socialbatterymanager.preferences.PreferencesManager
import com.example.socialbatterymanager.sync.SyncManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var syncManager: SyncManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesManager = PreferencesManager(this)
        syncManager = SyncManager(this)
        
        // Apply theme before setting content
        applyTheme()
        
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)
        
        // Check onboarding status and navigate accordingly
        checkOnboardingStatus(navController)
        
        // Initialize sync
        initializeSync()
        
        // Observe theme changes
        observeThemeChanges()
    }
    
    private fun initializeSync() {
        lifecycleScope.launch {
            try {
                syncManager.schedulePeriodicSync()
            } catch (e: Exception) {
                // Handle sync initialization error
                e.printStackTrace()
            }
        }
    }
    
    private fun checkOnboardingStatus(navController: androidx.navigation.NavController) {
        lifecycleScope.launch {
            preferencesManager.onboardingCompletedFlow.collect { completed ->
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
    
    private fun applyTheme() {
        lifecycleScope.launch {
            preferencesManager.themeFlow.collect { theme ->
                when (theme) {
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
            }
        }
    }
    
    private fun observeThemeChanges() {
        lifecycleScope.launch {
            preferencesManager.themeFlow.collect { theme ->
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
}
