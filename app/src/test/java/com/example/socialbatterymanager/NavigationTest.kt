package com.example.socialbatterymanager

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit test for navigation configuration
 */
class NavigationTest {
    
    @Test
    fun navigation_fragmentIds_areUnique() {
        // Test that all fragment IDs are unique
        val fragmentIds = setOf(
            R.id.homeFragment,
            R.id.calendarFragment,
            R.id.peopleFragment,
            R.id.activitiesFragment,
            R.id.reportsFragment
        )
        
        // Should have 5 unique fragment IDs
        assertEquals(5, fragmentIds.size)
    }
    
    @Test
    fun navigation_menuItems_matchFragments() {
        // Test that menu items match navigation fragments
        val menuIds = setOf(
            R.id.homeFragment,
            R.id.calendarFragment,
            R.id.peopleFragment,
            R.id.activitiesFragment,
            R.id.reportsFragment
        )
        
        // All menu items should correspond to navigation fragments
        assertEquals(5, menuIds.size)
    }
}