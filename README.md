# Auto Reload Chrome - Android App

An Android application that auto-reloads Chrome web pages every 2-3 seconds using a floating button overlay.

## Features

- Floating button that stays on top of all apps
- Auto-reloads Chrome pages every 2.5 seconds
- Simple Start/Stop controls
- Works with Chrome browser

## How to Build

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to this folder and open
4. Click "Build" -> "Make Project"
5. Click "Run" to install on your device

## How to Use

1. **Install the app** on your Android device
2. **Grant Overlay Permission**:
   - Open the app
   - Click "Start Floating Button"
   - Grant the overlay permission when prompted
3. **Enable Accessibility Service**:
   - Click "Open Accessibility Settings"
   - Find "Auto Reload Chrome" in the list
   - Enable it
4. **Start Using**:
   - Go to Chrome browser
   - Open any website
   - Tap the floating blue dot
   - Click "Start" button
   - The page will now auto-reload every 2-3 seconds!

## Requirements

- Android 8.0 (API 26) or higher
- Chrome browser installed
- Overlay permission granted
- Accessibility service enabled

## Permissions

- **SYSTEM_ALERT_WINDOW**: To display floating button over other apps
- **FOREGROUND_SERVICE**: To keep the floating service running
- **BIND_ACCESSIBILITY_SERVICE**: To perform reload gestures in Chrome

## Note

This app is intended for testing and development purposes. Auto-reloading pages frequently may consume additional data and battery.
