## Calendar UI Implementation

### Main Calendar Layout (fragment_calendar.xml)
The calendar interface includes:
- Title: "Calendar"
- Two buttons: "Add Event" and "Import Events"
- CalendarView for date selection
- RecyclerView for showing events

### Event Item Layout (item_calendar_event.xml)
Each event displays:
- Event title (bold)
- Source badge (Google/Teams/Manual)
- Time range (HH:mm format)
- Description (when available)

### Key UI Features:
1. **Calendar View**: Standard Android CalendarView with date selection
2. **Event List**: RecyclerView showing events for selected date
3. **Action Buttons**: Import events and add manual events
4. **Source Badges**: Color-coded badges showing event source
5. **Material Design**: Following app's color scheme and typography

### Color Scheme:
- Background: jscc_gold_start (#c8a04a)
- Text: jscc_charcoal (#393f44)
- Buttons: jscc_charcoal background, white text
- Event items: White background with charcoal text

### Layout Structure:
```
LinearLayout (vertical, gold background)
├── TextView (Calendar title)
├── LinearLayout (horizontal, buttons)
│   ├── Button (Add Event)
│   └── Button (Import Events)
├── CalendarView
├── TextView (Events for Selected Day)
└── RecyclerView (Events list)
```

### Responsive Design:
- Buttons share equal width using layout_weight
- RecyclerView fills remaining space
- Proper padding and margins for touch targets