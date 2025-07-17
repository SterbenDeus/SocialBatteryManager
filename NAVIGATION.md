# Navigation & Menu Implementation

## Overview
This implementation provides a complete navigation system for the Social Battery Manager app with bottom navigation and badge support.

## Components

### 1. Navigation Graph (`nav_graph.xml`)
- Defines all 5 main fragments: Home, Calendar, People, Activities, Reports
- Sets Home as the start destination
- Each fragment has a unique ID that matches the menu items

### 2. Bottom Navigation Menu (`bottom_nav_menu.xml`)
- Contains 5 menu items corresponding to the main fragments
- Each item has an icon and title
- Menu item IDs match the navigation fragment IDs for automatic navigation

### 3. MainActivity Badge System
- Supports showing numbered badges on menu items
- Provides methods to update, clear, and toggle badges
- Example badges are set up for Activities (3) and Reports (1)

## Features

### Navigation
- ✅ Links to all main fragments (Home, Calendar, People, Activities, Reports)
- ✅ Automatic current page highlighting via `setupWithNavController()`
- ✅ Smooth transitions between fragments

### Badge/Notification System
- ✅ Numbered badges for notifications
- ✅ Visibility toggle for badges
- ✅ Helper methods for updating from fragments

### Custom Icons
- ✅ Activities: Information/activity icon
- ✅ Reports: Bar chart icon
- ✅ Home: House icon
- ✅ Calendar: Calendar icon
- ✅ People: People icon

## Usage

### Updating Badges from Fragments
```kotlin
// In a fragment, get MainActivity and update badge
(activity as MainActivity).updateBadge(R.id.activitiesFragment, 5)

// Clear a badge
(activity as MainActivity).clearBadge(R.id.reportsFragment)

// Show/hide badge without number
(activity as MainActivity).setBadgeVisible(R.id.peopleFragment, true)
```

### Navigation Flow
The navigation automatically handles:
1. Fragment transitions
2. Back button behavior
3. Current page highlighting
4. Badge persistence during navigation

## Files Modified/Created

### Modified
- `MainActivity.kt` - Added badge support and navigation setup
- `nav_graph.xml` - Added Activities and Reports fragments
- `bottom_nav_menu.xml` - Added Activities and Reports menu items
- `activity_main.xml` - Added margin to prevent bottom nav overlap
- `fragment_reports.xml` - Added basic content layout
- `ReportsFragment.kt` - Fixed to be proper Fragment implementation
- `build.gradle.kts` - Added Material Design Components dependency

### Created
- `ic_activities.xml` - Icon for Activities tab
- `ic_reports.xml` - Icon for Reports tab
- `NavigationTest.kt` - Basic navigation configuration test