package com.example.socialbatterymanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.socialbatterymanager.auth.AuthRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.badge.BadgeDrawable

class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigationView: BottomNavigationView

    private val authRepository = AuthRepository()

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)
        

        // Set up badges for demonstration
        setupBadges()
    }
    
    private fun setupBadges() {
        // Example: Add badge to Activities tab
        val activitiesBadge = bottomNavigationView.getOrCreateBadge(R.id.activitiesFragment)
        activitiesBadge.isVisible = true
        activitiesBadge.number = 3
        
        // Example: Add badge to Reports tab  
        val reportsBadge = bottomNavigationView.getOrCreateBadge(R.id.reportsFragment)
        reportsBadge.isVisible = true
        reportsBadge.number = 1
    }
    
    // Method to update badge count
    fun updateBadge(menuItemId: Int, count: Int) {
        val badge = bottomNavigationView.getBadge(menuItemId)
        if (count > 0) {
            if (badge == null) {
                val newBadge = bottomNavigationView.getOrCreateBadge(menuItemId)
                newBadge.isVisible = true
                newBadge.number = count
            } else {
                badge.isVisible = true
                badge.number = count
            }
        } else {
            badge?.isVisible = false
        }
    }
    
    // Method to clear badge
    fun clearBadge(menuItemId: Int) {
        bottomNavigationView.getBadge(menuItemId)?.isVisible = false
    }
    
    // Method to show/hide badge without number
    fun setBadgeVisible(menuItemId: Int, visible: Boolean) {
        val badge = bottomNavigationView.getOrCreateBadge(menuItemId)
        badge.isVisible = visible

        // Listen for navigation changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    bottomNavigationView.visibility = android.view.View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = android.view.View.VISIBLE
                }
            }
        }
        
        // Check if user is already logged in
        if (authRepository.isUserLoggedIn()) {
            navController.navigate(R.id.homeFragment)
        }

    }
}
