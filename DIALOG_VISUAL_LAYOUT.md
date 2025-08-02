# Create New Activity Dialog - Visual Layout

```
┌─────────────────────────────────────┐
│           Create New Activity        │
│    Plan your activity and track     │
│       energy consumption           │
├─────────────────────────────────────┤
│                                     │
│ Activity Name                       │
│ ┌─────────────────────────────────┐ │
│ │ Hot Yoga                        │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Activity Description                │
│ ┌─────────────────────────────────┐ │
│ │ Yoga Session                    │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Activity Type                       │
│ ┌─────────────────────────────────┐ │
│ │ Leisure            ▼            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ People Involved                     │
│ ┌─Grace Lee ×┐ ┌─Emma Thompson ×┐   │
│ │            │ │               │   │
│ └────────────┘ └───────────────┘   │
│ + Add Person                        │
│                                     │
│ Date              Time              │
│ ┌─────────────┐ ┌───────┐ ┌───────┐│
│ │ 10/30/2025  │ │07:00PM│ │08:00PM││
│ └─────────────┘ └───────┘ └───────┘│
│                                     │
│ Estimated Energy Use                │
│ ┌─────────────────────────────────┐ │
│ │ 50  energy points         🔋    │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ⚠️ High Energy Activity             │
│ ┌─────────────────────────────────┐ │
│ │ This activity uses a lot of     │ │
│ │ energy (over 40). Consider      │ │
│ │ taking breaks during session.   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ⚠️ Weekly Capacity Alert            │
│ ┌─────────────────────────────────┐ │
│ │ This activity will bring your   │ │
│ │ weekly capacity down below      │ │
│ │ target (30%). You may want to   │ │
│ │ reschedule or reduce intensity. │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────┐ ┌─────────────────┐ │
│ │   Cancel    │ │ Schedule        │ │
│ │             │ │ Activity        │ │
│ └─────────────┘ └─────────────────┘ │
└─────────────────────────────────────┘
```

## Dialog Features Demonstrated

✅ **Header**: Title and subtitle matching design
✅ **Form Fields**: All required inputs with proper styling  
✅ **People Chips**: Grace Lee and Emma Thompson with X buttons
✅ **Date/Time**: Properly formatted with pickers
✅ **Energy Display**: 50 points with battery icon
✅ **Warnings**: Both high energy and capacity alerts shown
✅ **Buttons**: Cancel (text) and Schedule Activity (primary)

## Dynamic Behavior

- **Energy calculation updates** as user types and adds people
- **Warnings appear/disappear** based on energy levels
- **People chips are dismissible** with X buttons
- **Date/time pickers** open native Android dialogs
- **Form validation** prevents empty activity names
- **Database integration** saves activities and creates events

## Styling Features

- **Rounded corners** on all input fields
- **Proper spacing** between sections
- **Color-coded warnings** (red for high energy, orange for capacity)
- **Material Design components** (chips, buttons, spinners)
- **Responsive layout** with ScrollView for smaller screens