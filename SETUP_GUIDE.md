# TravelFoodie Setup Guide

This guide will help you set up and run the TravelFoodie Android application in Android Studio.

## Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio** (Hedgehog 2023.1.1 or later)
   - Download from: https://developer.android.com/studio

2. **JDK 17**
   - Android Studio usually includes this, but you can verify:
   - File → Project Structure → SDK Location → JDK location

3. **Android SDK**
   - SDK Platform 34 (Android 14)
   - Android SDK Build-Tools 34.0.0
   - Android SDK Platform-Tools

## Step-by-Step Setup

### Step 1: Extract the Project

1. Extract the `TravelFoodie.zip` file to your desired location
2. Note the path where you extracted it

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Click "Open" on the welcome screen (or File → Open)
3. Navigate to the extracted `TravelFoodie` folder
4. Click "OK"
5. Wait for Android Studio to index the project and download dependencies

### Step 3: Configure API Keys

The project requires several API keys to function properly. Follow these steps:

#### 3.1 Edit local.properties

1. Open the file `local.properties` in the project root
2. Replace the placeholder values with your actual API keys:

```properties
sdk.dir=/path/to/your/Android/Sdk

# Replace these with your actual API keys
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
PLACES_API_KEY=YOUR_GOOGLE_PLACES_API_KEY_HERE
KAKAO_API_KEY=YOUR_KAKAO_API_KEY_HERE
NAVER_CLIENT_ID=YOUR_NAVER_CLIENT_ID_HERE
NAVER_CLIENT_SECRET=YOUR_NAVER_CLIENT_SECRET_HERE
```

#### 3.2 How to Get API Keys

**Google Maps & Places API:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the following APIs:
   - Maps SDK for Android
   - Places API
4. Go to "Credentials" → "Create Credentials" → "API Key"
5. Copy the API key
6. (Optional) Restrict the key to Android apps with your package name: `com.travelfoodie`

**Kakao API:**
1. Go to [Kakao Developers](https://developers.kakao.com/)
2. Sign in or create an account
3. Click "내 애플리케이션" (My Applications)
4. Click "애플리케이션 추가하기" (Add Application)
5. Enter app name and company name
6. Go to "앱 키" (App Keys) tab
7. Copy the "REST API 키" (REST API Key)

**Naver API:**
1. Go to [Naver Developers](https://developers.naver.com/)
2. Sign in or create an account
3. Click "Application" → "애플리케이션 등록" (Register Application)
4. Select "검색" (Search) API
5. Enter application details
6. Copy the "Client ID" and "Client Secret"

**Note:** If you don't have API keys yet, the app will still compile and run, but some features (restaurant search, maps) will use mock data instead.

### Step 4: Configure Firebase

#### 4.1 Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or "Create a project"
3. Enter project name (e.g., "TravelFoodie")
4. Follow the setup wizard

#### 4.2 Add Android App to Firebase

1. In your Firebase project, click "Add app" → Android icon
2. Enter package name: `com.travelfoodie`
3. (Optional) Enter app nickname: "TravelFoodie"
4. (Optional) Enter debug signing certificate SHA-1
   - To get SHA-1, run in terminal:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
5. Click "Register app"
6. Download `google-services.json`
7. Replace the placeholder `app/google-services.json` file with your downloaded file

#### 4.3 Enable Firebase Services

In your Firebase project console:

1. **Authentication:**
   - Go to "Build" → "Authentication"
   - Click "Get started"
   - Click "Sign-in method" tab
   - Enable "Google" provider
   - Enter support email
   - Click "Save"

2. **Realtime Database:**
   - Go to "Build" → "Realtime Database"
   - Click "Create Database"
   - Choose location (e.g., us-central1)
   - Start in "Test mode" (for development)
   - Click "Enable"

3. **Cloud Messaging:**
   - Automatically enabled when you add the app

4. **Storage:**
   - Go to "Build" → "Storage"
   - Click "Get started"
   - Start in "Test mode" (for development)
   - Click "Done"

#### 4.4 Set Security Rules (Important for Production)

**Realtime Database Rules:**
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**Storage Rules:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Step 5: Sync Project with Gradle Files

1. In Android Studio, click "File" → "Sync Project with Gradle Files"
2. Wait for the sync to complete
3. If you see any errors, check the "Build" tab at the bottom

### Step 6: Build the Project

1. Click "Build" → "Make Project" (or press Ctrl+F9 / Cmd+F9)
2. Wait for the build to complete
3. Check for any errors in the "Build" tab

### Step 7: Run the App

#### 7.1 Using an Emulator

1. Click "Tools" → "Device Manager"
2. Click "Create Device"
3. Select a device (e.g., Pixel 6)
4. Select a system image (API 34 recommended)
5. Click "Finish"
6. Click the "Run" button (green triangle) or press Shift+F10
7. Select your emulator from the list

#### 7.2 Using a Physical Device

1. Enable Developer Options on your Android device:
   - Go to Settings → About phone
   - Tap "Build number" 7 times
2. Enable USB Debugging:
   - Go to Settings → Developer options
   - Enable "USB debugging"
3. Connect your device via USB
4. Click the "Run" button (green triangle)
5. Select your device from the list

## Troubleshooting

### Issue: Gradle Sync Failed

**Solution:**
1. Click "File" → "Invalidate Caches / Restart"
2. Select "Invalidate and Restart"
3. Wait for Android Studio to restart and re-index

### Issue: SDK Not Found

**Solution:**
1. Click "File" → "Project Structure"
2. Go to "SDK Location"
3. Set the correct path to your Android SDK
4. Click "OK"

### Issue: Build Failed - Missing Dependencies

**Solution:**
1. Open "SDK Manager" (Tools → SDK Manager)
2. Go to "SDK Platforms" tab
3. Check "Android 14.0 (API 34)"
4. Go to "SDK Tools" tab
5. Check:
   - Android SDK Build-Tools 34
   - Android SDK Platform-Tools
   - Google Play services
6. Click "Apply" and wait for installation

### Issue: Firebase Authentication Not Working

**Solution:**
1. Verify `google-services.json` is in the `app/` directory
2. Check that the package name in Firebase matches: `com.travelfoodie`
3. Ensure Google Sign-In is enabled in Firebase Console
4. For physical devices, add your release SHA-1 to Firebase

### Issue: Maps Not Showing

**Solution:**
1. Verify `MAPS_API_KEY` in `local.properties`
2. Check that Maps SDK for Android is enabled in Google Cloud Console
3. Ensure the API key is not restricted, or add `com.travelfoodie` to allowed apps

### Issue: App Crashes on Launch

**Solution:**
1. Check Logcat for error messages (View → Tool Windows → Logcat)
2. Common causes:
   - Missing `google-services.json`
   - Invalid API keys
   - Missing permissions
3. Try "Build" → "Clean Project" then "Build" → "Rebuild Project"

## Testing the App

### 1. Test User Authentication

1. Launch the app
2. Click "Google로 로그인" (Sign in with Google)
3. Select a Google account
4. Enter a nickname
5. Click "프로필 만들기" (Create Profile)

### 2. Test Trip Creation

1. Go to "여행" (Trips) tab
2. Click the "+" button
3. Enter trip details:
   - Title: "서울 문화 여행"
   - Start date: Select a future date
   - End date: Select a date after start date
   - Theme: Select "문화" (Culture)
4. Click "저장" (Save)
5. Verify the trip appears in the list with D-day countdown

### 3. Test Attractions & Restaurants

1. After creating a trip, go to "명소" (Attractions) tab
2. Verify TOP 5 attractions are displayed
3. Go to "맛집" (Restaurants) tab
4. Verify TOP 10 restaurants are displayed

### 4. Test Notifications

1. Create a trip with start date within 7 days
2. Wait for scheduled notification (or manually trigger via AlarmManager)
3. Verify notification appears

### 5. Test Widget

1. Long-press on home screen
2. Select "Widgets"
3. Find "TravelFoodie" widget
4. Drag to home screen
5. Verify widget shows next trip and D-day

### 6. Test Shake Feature

1. Create a trip and add restaurants
2. Shake your device (physical device required)
3. Verify random 3 restaurants popup appears

## Project Structure Overview

```
TravelFoodie/
├── app/                    # Main application module
├── core/
│   ├── data/               # Database, API, repositories
│   ├── domain/             # Domain models
│   ├── sensors/            # Accelerometer handling
│   ├── sync/               # Firebase sync
│   └── ui/                 # Shared UI components
├── feature/
│   ├── trip/               # Trip management
│   ├── attraction/         # Attraction recommendations
│   ├── restaurant/         # Restaurant listings
│   ├── voice/              # Voice commands
│   ├── widget/             # Home widget
│   └── board/              # Chat (optional)
└── gradle/                 # Gradle configuration
```

## Development Tips

### 1. Enable Debug Logging

In `app/build.gradle.kts`, ensure debug build type has:
```kotlin
buildTypes {
    debug {
        isMinifyEnabled = false
        isDebuggable = true
    }
}
```

### 2. View Database

1. Install "Database Inspector" plugin (if not already installed)
2. Run the app on an emulator or rooted device
3. Go to "View" → "Tool Windows" → "App Inspection"
4. Select "Database Inspector" tab
5. Explore the `travelfoodie.db` database

### 3. Test API Calls

1. Open Logcat
2. Filter by "OkHttp" to see network requests
3. Verify API responses

### 4. Debug Notifications

1. Use "Notification Inspector" in Android Studio
2. Or manually trigger notifications via:
   ```kotlin
   // In your code
   val intent = Intent(context, AlarmReceiver::class.java).apply {
       putExtra("trip_title", "Test Trip")
       putExtra("notif_type", "D-7")
       putExtra("nickname", "Test User")
   }
   context.sendBroadcast(intent)
   ```

## Next Steps

After successfully running the app:

1. **Customize the UI:**
   - Modify colors in `app/src/main/res/values/colors.xml`
   - Update themes in `app/src/main/res/values/themes.xml`
   - Replace placeholder icons with custom icons

2. **Add More Features:**
   - Implement voice commands (STT/TTS)
   - Add OCR receipt scanning
   - Implement board/chat feature

3. **Improve Data:**
   - Integrate real AI for attraction recommendations
   - Use actual restaurant data from APIs
   - Add more regions and attractions

4. **Prepare for Release:**
   - Update `google-services.json` with production Firebase project
   - Generate release keystore
   - Enable ProGuard/R8 optimization
   - Test on multiple devices and Android versions

## Support

If you encounter any issues not covered in this guide:

1. Check the main `README.md` file
2. Review Android Studio's "Build" and "Logcat" tabs for error messages
3. Ensure all dependencies are up to date
4. Try "File" → "Invalidate Caches / Restart"

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Material Design Guidelines](https://material.io/design)

Happy coding! 🚀
