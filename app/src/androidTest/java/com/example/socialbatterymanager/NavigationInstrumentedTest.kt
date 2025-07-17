package com.example.socialbatterymanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test for navigation setup
 */
@RunWith(AndroidJUnit4::class)
class NavigationInstrumentedTest {
    
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.socialbatterymanager", appContext.packageName)
    }
    
    @Test
    fun navigation_resources_exist() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val resources = appContext.resources
        
        // Check navigation graph exists
        val navGraphId = resources.getIdentifier("nav_graph", "navigation", appContext.packageName)
        assertTrue("Navigation graph should exist", navGraphId != 0)
        
        // Check menu exists
        val menuId = resources.getIdentifier("bottom_nav_menu", "menu", appContext.packageName)
        assertTrue("Bottom navigation menu should exist", menuId != 0)
        
        // Check all icons exist
        val homeIcon = resources.getIdentifier("ic_home", "drawable", appContext.packageName)
        assertTrue("Home icon should exist", homeIcon != 0)
        
        val calendarIcon = resources.getIdentifier("ic_calendar", "drawable", appContext.packageName)
        assertTrue("Calendar icon should exist", calendarIcon != 0)
        
        val peopleIcon = resources.getIdentifier("ic_people", "drawable", appContext.packageName)
        assertTrue("People icon should exist", peopleIcon != 0)
        
        val activitiesIcon = resources.getIdentifier("ic_activities", "drawable", appContext.packageName)
        assertTrue("Activities icon should exist", activitiesIcon != 0)
        
        val reportsIcon = resources.getIdentifier("ic_reports", "drawable", appContext.packageName)
        assertTrue("Reports icon should exist", reportsIcon != 0)
    }
}