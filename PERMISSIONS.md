# Permission Usage

| Permission | Feature | Toggle | Rationale |
|------------|---------|--------|-----------|
| `READ_CONTACTS` | Import contacts into app | `FEATURE_CONTACTS_IMPORT` | Used to import contact info for people management. |
| `READ_CALENDAR` | Import calendar events | `FEATURE_CALENDAR_IMPORT` | Used to import calendar events to manage schedules. |
| `READ_MEDIA_IMAGES` | Select profile photos | – | Allows users to choose images from device storage for their profile. |
| `CAMERA` | Capture profile photos | – | Lets users take profile photos within the app. |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Data sync | – | Required for syncing and checking connectivity. |
| `USE_BIOMETRIC` | Biometric authentication | – | Enables biometric login during onboarding. |
| `POST_NOTIFICATIONS` | Reminder notifications | `FEATURE_NOTIFICATIONS` | Needed to deliver reminders and alerts to the user. |

Unused permissions `USE_FINGERPRINT` and `ACTIVITY_RECOGNITION` have been removed.

## Play Console Declarations

- **Privacy Policy**: Explain how contact and calendar data are used only to populate the app and are never shared.
- **Data Safety**: Declare access to contacts, calendar events, images/media, camera, and app activity for notification scheduling. All data stays on device unless sync is enabled.
