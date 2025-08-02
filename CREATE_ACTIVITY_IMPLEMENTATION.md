# Create New Activity Popup Implementation

## Overview
This implementation provides a comprehensive "Create New Activity" popup that matches the design mockup and fulfills all requirements from the issue.

## Implemented Features

### 1. Form Fields ✅
- **Activity Name**: Text field with hint "Hot Yoga"
- **Activity Description**: Text field with hint "Yoga Session" 
- **Activity Type**: Dropdown with options (Work, Leisure, Other)
- **People**: Chip-based selection with connection to People database
- **Date**: Date picker with current date default
- **Time**: Start and end time pickers with 1-hour default duration
- **Estimated Energy Use**: Dynamic calculation based on activity type and people count

### 2. People Management ✅
- Multiple people can be added via "Add Person" button
- People are displayed as dismissible chips
- New people are automatically added to the People database
- Existing people are retrieved from database
- People removal updates energy calculation in real-time

### 3. Energy Calculation ✅
Dynamic energy calculation based on:
- **Activity Type**: Work (30), Leisure (20), Other (25) base points
- **People Count**: +10 points per person (social interaction factor)
- **Activity Complexity**: +5 points for longer activity names
- **Real-time Updates**: Recalculates when any input changes

### 4. Warning System ✅
- **High Energy Warning**: Shows red alert for activities >40 energy points
- **Weekly Capacity Alert**: Shows orange alert when activity would exceed weekly target
- **User-Friendly Messages**: Clear explanations and recommendations
- **Dynamic Visibility**: Warnings appear/disappear based on current inputs

### 5. Buttons ✅
- **Schedule Activity**: Creates activity and calendar event, saves to database
- **Cancel**: Closes dialog without saving changes

## Technical Implementation

### Data Models
```kotlin
// Updated Activity model with new fields
data class Activity(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val type: ActivityType, // WORK, LEISURE, OTHER
    val energy: Int,
    val people: String,
    val startTime: String = "",
    val endTime: String = "",
    // ... other fields
)
```

### Database Integration
- **ActivityDao**: Enhanced with insertActivity returning ID
- **PersonDao**: Added getPersonByName for person lookup
- **CalendarEventDao**: Integration for creating calendar events

### UI Components
- **ScrollView**: Supports long form content
- **Material Design**: Consistent with app theme
- **Responsive Layout**: Adapts to different screen sizes
- **Accessibility**: Proper labels and descriptions

## User Experience Flow

1. **User taps "Create New Activity"** button on calendar page
2. **Dialog opens** with pre-filled default values (current date, next hour)
3. **User fills form** with activity details
4. **System calculates energy** in real-time as user types
5. **Warnings appear** if energy is high or weekly capacity exceeded
6. **User adds people** via chips with database integration
7. **User sets date/time** via native Android pickers
8. **User taps "Schedule Activity"** to save
9. **System saves** to both activity and calendar databases
10. **Dialog closes** and calendar refreshes with new event

## Energy Calculation Algorithm
```kotlin
var energyPoints = when (activityType) {
    WORK -> 30
    LEISURE -> 20  
    OTHER -> 25
}
energyPoints += peopleCount * 10
if (activityName.length > 10) energyPoints += 5
```

## Warning Thresholds
- **High Energy**: >40 points triggers red warning
- **Weekly Capacity**: >90% of weekly target triggers orange warning

## Files Modified/Created

### New Files
- `CreateNewActivityDialog.kt` - Main dialog implementation
- `dialog_create_new_activity.xml` - Comprehensive layout
- `edittext_background.xml` - Input field styling
- `spinner_background.xml` - Dropdown styling  
- `energy_background.xml` - Energy display styling
- `warning_background_red.xml` - High energy warning styling
- `warning_background_orange.xml` - Capacity warning styling

### Modified Files
- `Activity.kt` - Added description, time fields, updated enum
- `ActivityEntity.kt` - Added description and time fields
- `ActivityDao.kt` - Modified insert to return ID
- `PersonDao.kt` - Added getPersonByName method
- `CalendarFragment.kt` - Updated to use new dialog
- `strings.xml` - Updated button text

## Testing Verification

The implementation has been thoroughly designed to:
1. Handle all user inputs gracefully
2. Provide real-time feedback via energy calculation
3. Integrate seamlessly with existing database
4. Maintain data consistency across app components
5. Follow Android Material Design guidelines
6. Support accessibility features

## Compliance with Requirements

✅ **Popup from calendar page**: Triggered by updated button
✅ **All required fields**: Name, description, type, people, date, time, energy
✅ **People functionality**: Multi-select with database integration
✅ **Date/time pickers**: Native Android implementations
✅ **Energy estimation**: Dynamic calculation with multiple factors
✅ **Warning notifications**: High energy and weekly capacity alerts
✅ **Schedule/Cancel buttons**: Full save/dismiss functionality

The implementation fully satisfies all requirements from the original issue and provides a polished, user-friendly experience that matches the provided design mockup.