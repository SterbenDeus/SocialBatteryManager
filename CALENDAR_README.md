# Calendar Implementation

This document describes the calendar functionality implemented for the Social Battery Manager app.

## Overview

The calendar feature allows users to:
- View events in a calendar interface
- Import events from device calendar (Google, Teams, Outlook)
- Create manual events
- Store events locally in Room database
- View events for selected dates

## Files Implemented

### Core Files
- `calendar/CalendarFragment.kt` - Main calendar fragment with UI logic
- `calendar/CalendarAdapter.kt` - RecyclerView adapter for displaying events
- `calendar/CalendarIntegration.kt` - Integration with external calendars
- `data/CalendarEvent.kt` - Room entity for calendar events
- `data/CalendarEventDao.kt` - Room DAO for calendar operations

### UI Files
- `res/layout/fragment_calendar.xml` - Main calendar layout
- `res/layout/item_calendar_event.xml` - Individual event item layout
- `res/drawable/rounded_background.xml` - Background for event badges

### Test Files
- `androidTest/calendar/CalendarIntegrationTest.kt` - Integration tests
- `test/calendar/CalendarEventTest.kt` - Unit tests

## Features

### 1. Calendar View
- Full calendar view using Android's CalendarView
- Date selection to view events for specific days
- Material Design styling

### 2. Event Storage
- Local storage using Room database
- CalendarEvent entity with fields:
  - id, title, description
  - startTime, endTime
  - location, source
  - externalId, isImported

### 3. Event Import
- Device calendar integration
- Permission handling for READ_CALENDAR
- Support for Google, Teams, Outlook calendars
- Source detection based on calendar name

### 4. Event Display
- RecyclerView showing events for selected date
- Time formatting (HH:mm)
- Source badges (Google, Teams, Manual, etc.)
- Event descriptions (when available)

### 5. Manual Event Creation
- Create events manually through the app
- Sample event creation for testing
- Future: Dialog for full event creation

## Database Schema

```sql
CREATE TABLE calendar_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    startTime INTEGER NOT NULL,
    endTime INTEGER NOT NULL,
    location TEXT DEFAULT '',
    source TEXT DEFAULT '',
    externalId TEXT DEFAULT '',
    isImported INTEGER DEFAULT 0
);
```

## Permissions

The app requires:
```xml
<uses-permission android:name="android.permission.READ_CALENDAR" />
```

## Usage

1. Navigate to Calendar tab
2. Select a date on the calendar
3. View events for that date in the list below
4. Click "Import Events" to import from device calendar
5. Click "Add Event" to create a sample event

## Future Enhancements

- Full event creation dialog
- Event editing and deletion
- Google Calendar API integration
- Microsoft Teams Calendar API integration
- Event notifications
- Calendar synchronization
- Event filtering and search