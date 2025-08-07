# Notification Strategy

The app alerts users when their social battery drops below a configured threshold. These notifications are dispatched by
`EnergyReminderWorker` and follow these guidelines:

- **Channel**: Notifications are posted to the `social_battery_reminders` channel.
- **Unique IDs**: Each reminder uses a unique ID derived from `System.currentTimeMillis()` so multiple alerts don't overwrite
  one another.
- **App Launch**: Tapping a notification opens `MainActivity` via a `PendingIntent`, ensuring users land inside the app.
- **Auto-cancel**: Notifications dismiss themselves when tapped to avoid clutter.

Maintain this strategy when introducing new notifications to keep a consistent user experience.
