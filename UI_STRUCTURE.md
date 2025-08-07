# Profile Page UI Structure

```
┌─────────────────────────────────────────────────────┐
│                   Profile Page                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│              ┌─────────────┐                        │
│              │             │                        │
│              │  Profile    │                        │
│              │   Photo     │                        │
│              │             │                        │
│              └─────────────┘                        │
│              "Tap to change photo"                   │
│                                                     │
├─────────────────────────────────────────────────────┤
│           Personal Information                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Name: [John Doe                    ]               │
│                                                     │
│  Email: [john@example.com           ]               │
│                                                     │
├─────────────────────────────────────────────────────┤
│              Battery Settings                       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Battery Capacity                        100%      │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│
│                                                     │
│  Warning Level                           30%       │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━│
│                                                     │
├─────────────────────────────────────────────────────┤
│               Current Mood                          │
├─────────────────────────────────────────────────────┤
│                                                     │
│  [Neutral                    ▼]                     │
│                                                     │
├─────────────────────────────────────────────────────┤
│                Settings                             │
├─────────────────────────────────────────────────────┤
│                                                     │
│  Enable Notifications                    [ON]       │
│                                                     │
├─────────────────────────────────────────────────────┤
│                  Tools                              │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │        Recalibrate Battery                     │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │          Take Survey                           │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
├─────────────────────────────────────────────────────┤
│               Action Buttons                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │           Edit Profile                         │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │            Sign Out                            │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │         Delete Account                         │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## Interactive Elements

### Profile Photo
- **Tap**: Opens image selection dialog
- **Options**: Camera or Gallery
- **Permissions**: Requests storage and camera permissions at runtime

### Edit Profile Button
- **View Mode**: Shows "Edit Profile" 
- **Edit Mode**: Shows "Save Changes"
- **Behavior**: Toggles field editability

### Seekbars
- **Battery Capacity**: 0-100% with live preview
- **Warning Level**: 0-100% with live preview
- **Visual**: Shows percentage value next to slider

### Mood Spinner
- **Options**: Energetic, Happy, Neutral, Tired, Stressed, Overwhelmed
- **Selection**: Dropdown with current mood highlighted

### Settings Switch
- **Notifications**: ON/OFF toggle
- **Immediate**: Changes saved instantly

### Tool Buttons
- **Recalibrate**: Resets battery settings to defaults
- **Survey**: Opens survey tools (placeholder)

### Account Actions
- **Sign Out**: Confirmation dialog → Clear data → Exit
- **Delete Account**: Warning dialog → Permanent deletion

## Navigation Integration

```
Bottom Navigation:
[Home] [Calendar] [People] [Profile*]
                              ↑
                       Active tab
```

The profile page is integrated into the app's bottom navigation as the fourth tab, making it easily accessible from any screen in the app.