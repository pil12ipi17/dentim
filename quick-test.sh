#!/bin/bash

# Android Studio Quick Commands
# Run these commands after Android Studio setup is complete

echo "🎤 Android Karaoke - Quick Test Commands"
echo "======================================="

# Check if we're in the right directory
if [ ! -f "android/gradlew" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

echo "📂 Current directory: $(pwd)"
echo ""

# Function to run command with error handling
run_command() {
    local cmd="$1"
    local description="$2"
    
    echo "🔄 $description..."
    echo "Command: $cmd"
    echo ""
    
    if eval "$cmd"; then
        echo "✅ $description - SUCCESS"
    else
        echo "❌ $description - FAILED"
        
        # Special Java version check for Android commands
        if [[ $cmd == *"gradlew"* ]]; then
            echo ""
            echo "💡 Возможная причина: требуется Java 17"
            echo "   Текущая версия Java: $(java --version | head -n 1)"
            echo "   📖 См. инструкцию: JAVA_17_SETUP.md"
            echo "   🔧 Или используйте Android Studio (автоматически настроит Java)"
        fi
        
        return 1
    fi
    echo ""
}

echo "Select quick test option:"
echo "1) Build Android Project (./gradlew assembleDebug)"
echo "2) Run Android Unit Tests (./gradlew test)" 
echo "3) Run Android Instrumented Tests (./gradlew connectedAndroidTest)"
echo "4) Install APK on Device (./gradlew installDebug)"
echo "5) Start Backend Server (for testing with app)"
echo "6) Test Backend API (Python script)"
echo "7) Full Android Test Suite (build + unit tests)"
echo "8) Clean & Rebuild Project (./gradlew clean assembleDebug)"
echo "9) Check Gradle Tasks (./gradlew tasks)"
echo "0) Exit"

read -p "Enter choice [0-9]: " choice

case $choice in
    1)
        run_command "cd android && ./gradlew assembleDebug" "Building Android Project"
        ;;
    2)
        run_command "cd android && ./gradlew test" "Running Unit Tests"
        ;;
    3)
        run_command "cd android && ./gradlew connectedAndroidTest" "Running Instrumented Tests"
        echo "📱 Note: Make sure device/emulator is connected and USB debugging enabled"
        ;;
    4)
        run_command "cd android && ./gradlew installDebug" "Installing APK on Device"
        echo "📱 Note: Check your device for the installed app"
        ;;
    5)
        echo "🔄 Starting Backend Server..."
        echo "Server will start at: http://localhost:8000"
        echo "Press Ctrl+C to stop server"
        echo ""
        cd backend/src && source ../venv/bin/activate && uvicorn main:app --host 0.0.0.0 --port 8000 --reload
        ;;
    6)
        run_command "source backend/venv/bin/activate && python test_api.py" "Testing Backend API"
        ;;
    7)
        echo "🔄 Running Full Android Test Suite..."
        run_command "cd android && ./gradlew clean" "Cleaning Project"
        run_command "cd android && ./gradlew assembleDebug" "Building Project" 
        run_command "cd android && ./gradlew test" "Running Unit Tests"
        echo "✅ Full test suite completed!"
        ;;
    8)
        run_command "cd android && ./gradlew clean assembleDebug" "Clean & Rebuild"
        ;;
    9)
        run_command "cd android && ./gradlew tasks" "Listing Available Gradle Tasks"
        ;;
    0)
        echo "👋 Goodbye!"
        ;;
    *)
        echo "❌ Invalid choice. Please run the script again."
        ;;
esac