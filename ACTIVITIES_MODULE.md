# Activities Management Module

## Overview
This module provides complete activities management functionality for the Social Battery Manager app.

## Key Features

### 1. Activity Model
- **ActivityType Enum**: Defines WORK and SOCIAL activity types
- **Activity Data Class**: Complete model with all required fields
- **Conversion Functions**: Seamless conversion between Activity and ActivityEntity

### 2. Database Layer
- **ActivityEntity**: Updated Room entity with new fields (usageCount, rating)
- **ActivityDao**: Full CRUD operations including:
  - Insert, Update, Delete activities
  - Query activities by type
  - Increment usage count
  - Update rating
- **Database Migration**: Automatic migration from version 1 to 2

### 3. UI Components
- **ActivitiesFragment**: Main fragment with RecyclerView and add button
- **ActivitiesAdapter**: Custom adapter with click handling
- **EditActivityDialog**: Dialog for adding/editing activities
- **Navigation Integration**: Added to navigation graph and bottom menu

### 4. User Interactions
- **Add Activity**: Click "Add Activity" button to create new activities
- **Edit Activity**: Click on any activity to see options (Edit, Delete, Mark as Used)
- **Delete Activity**: Confirmation dialog for deleting activities
- **Usage Tracking**: Track how often activities are used
- **Rating System**: Rate activities from 0-5 stars

## Usage

### Adding an Activity
1. Navigate to Activities tab
2. Click "Add Activity" button
3. Fill in all fields (name, type, energy, people, mood, notes, rating)
4. Click "Save"

### Editing an Activity
1. Click on any activity in the list
2. Select "Edit" from the context menu
3. Modify any fields
4. Click "Save"

### Marking Activity as Used
1. Click on any activity in the list
2. Select "Mark as Used" from the context menu
3. Usage count will be incremented

## Database Schema
```sql
CREATE TABLE activities (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    energy INTEGER NOT NULL,
    people TEXT NOT NULL,
    mood TEXT NOT NULL,
    notes TEXT NOT NULL,
    date INTEGER NOT NULL,
    usageCount INTEGER NOT NULL DEFAULT 0,
    rating REAL NOT NULL DEFAULT 0.0
);
```

## Files Created/Modified
- `model/Activity.kt` - Activity model and enum
- `data/ActivityEntity.kt` - Updated Room entity
- `data/ActivityDao.kt` - Enhanced DAO with full CRUD
- `data/AppDatabase.kt` - Database with migration
- `ui/activities/ActivitiesFragment.kt` - Main fragment
- `ui/activities/ActivitiesAdapter.kt` - RecyclerView adapter
- `ui/activities/EditActivityDialog.kt` - Add/Edit dialog
- `res/layout/dialog_edit_activity.xml` - Dialog layout
- `res/navigation/nav_graph.xml` - Navigation setup
- `res/menu/bottom_nav_menu.xml` - Bottom navigation
- `test/java/model/ActivityTest.kt` - Unit tests

## Testing
Basic unit tests are included for model conversion functions. The tests verify:
- Activity to Entity conversion
- Entity to Activity conversion
- Proper enum handling