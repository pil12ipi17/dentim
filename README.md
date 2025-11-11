# Android Karaoke MVP 🎤🎵

AI-powered karaoke application with voice separation technology.

## 🏗️ Architecture

### Backend (Python FastAPI)
- **AI Voice Separation**: Demucs/Spleeter integration for isolating vocals
- **REST API**: Upload, process, and retrieve audio files
- **Mock Processing**: Ready for real AI model integration
- **Health Monitoring**: Status endpoints for system monitoring

### Android Client (Kotlin)
- **Clean Architecture**: Data/Domain/Presentation layers
- **Modern Android**: Hilt DI, Room Database, Retrofit, Navigation
- **MVVM Pattern**: ViewModels with StateFlow for reactive UI
- **File Management**: Upload audio files with progress tracking
- **Real-time Updates**: Processing status monitoring

## 🚀 Quick Start

### Prerequisites
- **Java 17** (via Homebrew: `brew install openjdk@17`)
- **Python 3.11+** 
- **Android Studio** (latest)
- **Android SDK 34+**

### 1. Setup Environment
```bash
# Run setup script
./setup-env.sh

# Or manually:
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
```

### 2. Backend Setup
```bash
cd backend

# Create virtual environment
python -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Start server
python src/main.py
```
Server will be available at `http://localhost:8000`

### 3. Android Setup
```bash
cd android

# Build project
./gradlew assembleDebug

# Or open in Android Studio:
# File → Open → android/
```

## 📱 Android Studio Setup

1. **Open Project**: `File → Open → /path/to/dentim/android/`
2. **Gradle Sync**: Wait for automatic sync to complete
3. **Run Configuration**: 
   - Select `app` from dropdown
   - Choose device/emulator
   - Click ▶️ Run

### Troubleshooting
- **Java Issues**: Ensure `JAVA_HOME=/opt/homebrew/opt/openjdk@17`
- **Gradle Issues**: Run `./gradlew clean` then rebuild
- **JDK Image Error**: Already disabled in `gradle.properties`

## 🧪 Testing

### Backend Tests
```bash
cd backend
source venv/bin/activate
pytest tests/ -v
```

### Android Tests
```bash
cd android
./gradlew test
```

## 📖 API Documentation

### Backend Endpoints
- `GET /health` - Server health check
- `POST /upload` - Upload audio file for processing
- `GET /status/{job_id}` - Check processing status
- `GET /download/{job_id}/{type}` - Download processed audio
- `POST /cancel/{job_id}` - Cancel processing job

### API Testing
```bash
# Health check
curl http://localhost:8000/health

# Upload file
curl -X POST "http://localhost:8000/upload" \
  -F "file=@audio.mp3" \
  -F "ai_model=demucs"
```

## 🛠️ Tech Stack

### Backend
- **FastAPI** - Modern Python web framework
- **Uvicorn** - ASGI server
- **Python-multipart** - File upload support
- **Pytest** - Testing framework

### Android
- **Kotlin** - Primary language
- **Hilt** - Dependency injection
- **Room** - Local database
- **Retrofit** - HTTP client
- **Navigation** - Fragment navigation
- **ViewBinding** - View binding
- **Coroutines** - Async programming

## 🎵 Ready to Rock! 🎵

Your Android Karaoke MVP is ready for development. Start the backend server, open Android Studio, and let's build something amazing! 🚀