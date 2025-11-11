# Android Studio Setup & Testing Guide

## Quick Start After Android Studio Installation

### 1. Open Project in Android Studio
```bash
# Start Android Studio
# Select: "Open an Existing Project"
# Navigate to: /Users/denis/AndroidKaraoke/dentim/android/
```

### 2. Initial Setup (Automatic)
- ✅ Android Studio will detect and download required SDK components
- ✅ Accept license agreements when prompted  
- ✅ Wait for Gradle sync to complete
- ✅ The `local.properties` file will be updated automatically

### 3. Connect Device or Start Emulator
**Option A: Physical Device**
```bash
# Enable Developer Options on your Android device
# Enable USB Debugging
# Connect device via USB
# Allow USB debugging when prompted
```

**Option B: Emulator**
```bash
# In Android Studio: Tools → AVD Manager
# Create Virtual Device → Choose device model → Choose system image (API 34+)
# Launch emulator
```

### 4. Quick Testing Commands

**Use the automated script:**
```bash
./quick-test.sh
```

**Or run commands manually:**
```bash
# Build the project
cd android && ./gradlew assembleDebug

# Run unit tests
cd android && ./gradlew test

# Run instrumented tests (requires connected device)
cd android && ./gradlew connectedAndroidTest

# Install APK on device
cd android && ./gradlew installDebug
```

### 5. Backend Testing
```bash
# Start backend server (in separate terminal)
./test.sh
# Select option 1 to start backend

# Test API endpoints
./test.sh  
# Select option 2 to test backend API
```

### 6. Full Testing Workflow
```bash
# 1. Start backend server
./quick-test.sh → Select option 5

# 2. In another terminal, build and install Android app
./quick-test.sh → Select option 7 (full test suite)

# 3. Install app on device
./quick-test.sh → Select option 4

# 4. Test the app manually on device/emulator
```

## Troubleshooting

### Common Issues & Solutions

**Gradle sync failed:**
```bash
cd android && ./gradlew clean
# Then sync again in Android Studio
```

**SDK not found:**
- Open Android Studio
- Go to File → Project Structure → SDK Location
- Verify Android SDK location is correct

**Device not detected:**
```bash
# Check ADB connection
adb devices
# If empty, enable USB debugging and reconnect
```

**Build errors:**
```bash
# Clean and rebuild
cd android && ./gradlew clean assembleDebug
```

## Available Scripts

- `./test.sh` - Full testing menu (backend + Android)
- `./quick-test.sh` - Quick Android testing commands
- `./test_api.py` - Backend API testing (Python)

## Project Structure
```
android/
├── app/src/main/java/com/dentim/karaoke/  # Main source code
├── app/src/test/java/                      # Unit tests  
├── app/src/androidTest/java/               # Instrumented tests
├── build.gradle.kts                       # Project config
└── gradlew                                 # Gradle wrapper

backend/
├── src/main.py                            # FastAPI server
├── venv/                                  # Python virtual environment
└── requirements.txt                       # Python dependencies
```

## Next Steps

After successful setup:
1. ✅ Android Studio opened project successfully
2. ✅ Gradle sync completed without errors
3. ✅ Backend server running (`./quick-test.sh` option 5)
4. ✅ Android app builds successfully (`./quick-test.sh` option 1)
5. ✅ Unit tests pass (`./quick-test.sh` option 2)
6. ✅ App installs on device/emulator (`./quick-test.sh` option 4)
7. 🎤 **Test the full Karaoke experience!**