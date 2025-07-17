# Authentication Setup Instructions

## Firebase Configuration

To complete the authentication setup, you need to:

1. **Create a Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or select an existing one
   - Enable Authentication service

2. **Configure Authentication Providers**
   - Enable Email/Password authentication
   - Enable Google Sign-In
   - For SSO, configure additional providers as needed

3. **Download Configuration Files**
   - Download `google-services.json` from Firebase Console
   - Replace the placeholder `app/google-services.json` with the real file

4. **Update Web Client ID**
   - In the Firebase Console, go to Authentication > Sign-in method > Google
   - Copy the Web client ID
   - Replace the placeholder value in `app/src/main/res/values/strings.xml`:
     ```xml
     <string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID</string>
     ```

## Authentication Features

The implementation includes:

- **Email/Password Authentication**: Users can sign up and sign in with email and password
- **Google Sign-In**: Users can authenticate with their Google account
- **SSO Support**: Framework ready for additional SSO providers
- **User State Management**: UserViewModel manages authenticated user state
- **Navigation Integration**: Automatic navigation based on authentication state

## Testing

To test the authentication:

1. Set up Firebase project as described above
2. Run the app
3. Try signing up with email/password
4. Try signing in with Google
5. Test logout functionality
6. Verify navigation flows work correctly

## Files Modified

- `AuthRepository.kt`: Firebase authentication logic
- `UserViewModel.kt`: User state management
- `LoginFragment.kt`: Login UI and authentication handling
- `MainActivity.kt`: Authentication state checking and navigation
- `fragment_login.xml`: Login UI layout
- `nav_graph.xml`: Navigation flow with authentication
- `build.gradle.kts`: Firebase dependencies
- `google-services.json`: Firebase configuration (placeholder)