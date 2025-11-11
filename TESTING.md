# Testing Guide for Android Karaoke MVP

## Overview
This document outlines comprehensive testing strategies for the Android Karaoke MVP application that separates vocals from instrumental tracks using AI.

## Project Architecture
- **Frontend**: Android app (Kotlin, MVVM, Room, Retrofit)  
- **Backend**: Python FastAPI server with Demucs/Spleeter AI models
- **Communication**: REST API + WebSocket for real-time updates

## Testing Phases

### Phase 1: Backend Testing (Python Server)
### Phase 2: Android Unit Testing
### Phase 3: Android Integration Testing  
### Phase 4: End-to-End Testing
### Phase 5: User Acceptance Testing

---

## Phase 1: Backend Testing (Python Server)

### Prerequisites
1. Install Python dependencies:
```bash
cd backend
pip install -r requirements.txt
```

2. Start the FastAPI server:
```bash
uvicorn src.main:app --host 0.0.0.0 --port 8000 --reload
```

### Backend Tests

#### 1.1 API Health Check
```bash
curl http://localhost:8000/api/v1/health
```
Expected: `{"success": true, "message": "Server is running"}`

#### 1.2 File Upload Test
```bash
curl -X POST "http://localhost:8000/api/v1/upload" \
  -F "file=@test_audio.mp3" \
  -F "ai_model=demucs"
```
Expected: JSON response with job_id

#### 1.3 Processing Status Check
```bash
curl http://localhost:8000/api/v1/processing/{job_id}/status
```
Expected: Status progression (PENDING → PROCESSING → COMPLETED)

#### 1.4 WebSocket Connection Test
Use a WebSocket client to test real-time updates:
```javascript
const ws = new WebSocket('ws://localhost:8000/ws');
ws.onmessage = (event) => console.log(event.data);
```

---

## Phase 2: Android Unit Testing

### 2.1 Utilities Testing

Run utility tests:
```bash
cd android
./gradlew test
```

### 2.2 Repository Testing
Test data layer components:
```bash
./gradlew testDebugUnitTest --tests "*Repository*"
```

### 2.3 ViewModel Testing  
Test business logic:
```bash
./gradlew testDebugUnitTest --tests "*ViewModel*"
```

### 2.4 Use Case Testing
Test domain layer:
```bash
./gradlew testDebugUnitTest --tests "*UseCase*"
```

---

## Phase 3: Android Integration Testing

### 3.1 Database Testing
```bash
./gradlew connectedAndroidTest --tests "*Database*"
```

### 3.2 Network Testing
```bash
./gradlew connectedAndroidTest --tests "*Api*"
```

### 3.3 UI Testing
```bash
./gradlew connectedAndroidTest --tests "*Fragment*"
```

---

## Phase 4: End-to-End Testing

### 4.1 Complete Upload Flow
1. Launch Android app
2. Select audio file (MP3/WAV)
3. Choose AI model (Demucs/Spleeter)
4. Upload and monitor progress
5. Verify processing completion
6. Check file downloads

### 4.2 Error Scenarios
- Network disconnection during upload
- Invalid file formats
- Large file handling (>100MB)
- Server overload scenarios

---

## Phase 5: E2E Testing

### Complete Flow Testing
1. Start backend server
2. Install Android app
3. Test complete upload → processing → playback flow
4. Verify WebSocket real-time updates
5. Test error scenarios and edge cases

---

## Android Studio Setup

### Initial Setup (Required for Android Testing)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Open Project in Android Studio**
   ```bash
   # Open Android Studio and select "Open an Existing Project"
   # Navigate to: /Users/denis/AndroidKaraoke/dentim/android/
   ```

3. **SDK Setup (Automatic)**
   - Android Studio will automatically detect missing SDK components
   - Accept the license agreements when prompted
   - Wait for SDK download and installation to complete
   - The `local.properties` file will be automatically updated

4. **Sync Project**
   - Android Studio will automatically sync the project
   - Wait for Gradle sync to complete
   - Resolve any dependency issues if they arise

5. **Connect Device or Start Emulator**
   - **Physical Device**: Enable USB debugging in Developer Options
   - **Emulator**: Use AVD Manager to create a virtual device (API 34+ recommended)

### Verification
Once setup is complete, verify everything works:
```bash
cd android
./gradlew assembleDebug  # Should build successfully
```

---

## Test Automation Scripts

### `test.sh` - Main Testing Script
Automated testing menu with the following options:
- Start/stop backend server  
- Run backend API tests
- Run Android unit tests
- Run Android instrumented tests
- Build and install APK
- Generate test audio files
- Full test suite execution

### Usage Examples
```bash
# Quick start - run automated testing menu
./test.sh

# Manual backend testing
source backend/venv/bin/activate
python test_api.py

# Manual Android testing (after Android Studio setup)
cd android
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
./gradlew assembleDebug          # Build APK
./gradlew installDebug           # Install on connected device
```

### Android Studio Testing
Within Android Studio:
1. **Run Unit Tests**: Right-click on `test` folder → "Run All Tests"
2. **Run Instrumented Tests**: Right-click on `androidTest` folder → "Run All Tests"
3. **Run App**: Click the green play button or Shift+F10
4. **Debug**: Set breakpoints and click the debug button

---

## Test Data

### Sample Audio Files
Create test files in `android/app/src/test/resources/`:
- `test_song_short.mp3` (30 seconds, ~1MB)
- `test_song_medium.wav` (2 minutes, ~20MB)
- `test_song_long.flac` (5 minutes, ~50MB)
- `test_invalid.txt` (invalid format)

### Mock Data
- Mock processing responses
- Mock WebSocket messages  
- Mock database entries

---

## Testing Tools

### Android Testing
- **JUnit 4/5**: Unit testing framework
- **Mockito**: Mocking framework  
- **Espresso**: UI testing
- **Room Testing**: Database testing
- **Hilt Testing**: DI testing

### Backend Testing
- **pytest**: Python testing framework
- **httpx**: HTTP client testing
- **websockets**: WebSocket testing
- **FastAPI TestClient**: API testing

### Manual Testing
- **Android Studio Emulator**: Various device configurations
- **Physical Devices**: Different Android versions
- **Postman**: API testing
- **WebSocket King**: WebSocket testing

---

## Continuous Integration

### GitHub Actions Setup
```yaml
name: Android CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Setup Android SDK
      uses: android-actions/setup-android@v2
    - name: Run Unit Tests
      run: ./gradlew test
    - name: Run Instrumented Tests
      run: ./gradlew connectedAndroidTest
```

---

## Performance Benchmarks

### Target Metrics
- **App Launch**: < 3 seconds
- **File Upload**: Progress updates every 500ms
- **Processing Time**: Demucs ~2-5min, Spleeter ~30sec-2min
- **Memory Usage**: < 200MB peak
- **APK Size**: < 50MB

---

## Bug Reporting Template

```
**Environment:**
- Android Version: 
- Device Model:
- App Version:

**Steps to Reproduce:**
1. 
2. 
3. 

**Expected Result:**

**Actual Result:**

**Logs/Screenshots:**
```