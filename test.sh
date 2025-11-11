#!/bin/bash

# Quick Test Script for Android Karaoke MVP
# This script helps you quickly test different components

echo "🎤 Android Karaoke MVP - Testing Script"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command_exists python3; then
    echo -e "${RED}❌ Python3 not found${NC}"
    exit 1
fi

if ! command_exists java; then
    echo -e "${RED}❌ Java not found${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Prerequisites OK${NC}"

show_menu() {
    echo ""
    echo "Select testing option:"
    echo "1) Start Backend Server (for manual testing)"
    echo "2) Test Backend API"
    echo "3) Run Android Unit Tests"
    echo "4) Run Android Instrumented Tests"
    echo "5) Build Android APK"
    echo "6) Install APK on device"
    echo "7) Full Test Suite"
    echo "8) Generate Test Audio Files"
    echo "0) Exit"
    echo -n "Enter choice [0-8]: "
}

start_backend() {
    echo -e "${YELLOW}Starting Backend Server...${NC}"
    cd backend
    
    # Install dependencies if needed
    if [ ! -d "venv" ]; then
        echo "Creating virtual environment..."
        python3 -m venv venv
        source venv/bin/activate
        pip install fastapi uvicorn python-multipart
    else
        source venv/bin/activate
    fi
    
    echo -e "${GREEN}Starting server at http://localhost:8000${NC}"
    python src/main.py &
    SERVER_PID=$!
    echo "Server PID: $SERVER_PID"
    echo "Press any key to stop server..."
    read -n 1
    kill $SERVER_PID
    cd ..
}

test_backend_api() {
    echo -e "${YELLOW}Testing Backend API...${NC}"
    
    echo "1. Health Check:"
    curl -s http://localhost:8000/api/v1/health | jq '.' || echo "❌ Health check failed"
    
    echo -e "\n2. Get all processing jobs:"
    curl -s http://localhost:8000/api/v1/processing | jq '.' || echo "❌ Get processing jobs failed"
    
    echo -e "\n${GREEN}✅ API tests completed${NC}"
}

run_android_unit_tests() {
    echo -e "${YELLOW}Running Android Unit Tests...${NC}"
    cd android
    
    ./gradlew test --info
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Unit tests passed${NC}"
    else
        echo -e "${RED}❌ Unit tests failed${NC}"
    fi
    
    cd ..
}

run_android_instrumented_tests() {
    echo -e "${YELLOW}Running Android Instrumented Tests...${NC}"
    cd android
    
    # Check if device/emulator is connected
    adb devices | grep -v "List" | grep "device"
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ No Android device connected${NC}"
        echo "Please connect a device or start an emulator"
        cd ..
        return 1
    fi
    
    ./gradlew connectedAndroidTest
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Instrumented tests passed${NC}"
    else
        echo -e "${RED}❌ Instrumented tests failed${NC}"
    fi
    
    cd ..
}

build_android_apk() {
    echo -e "${YELLOW}Building Android APK...${NC}"
    cd android
    
    ./gradlew assembleDebug
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ APK built successfully${NC}"
        echo "APK location: android/app/build/outputs/apk/debug/app-debug.apk"
    else
        echo -e "${RED}❌ APK build failed${NC}"
    fi
    
    cd ..
}

install_apk() {
    echo -e "${YELLOW}Installing APK on device...${NC}"
    
    APK_PATH="android/app/build/outputs/apk/debug/app-debug.apk"
    
    if [ ! -f "$APK_PATH" ]; then
        echo -e "${RED}❌ APK not found. Build it first.${NC}"
        return 1
    fi
    
    adb install -r "$APK_PATH"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ APK installed successfully${NC}"
        echo "You can now test the app on your device"
    else
        echo -e "${RED}❌ APK installation failed${NC}"
    fi
}

full_test_suite() {
    echo -e "${YELLOW}Running Full Test Suite...${NC}"
    
    echo "Step 1: Android Unit Tests"
    run_android_unit_tests
    
    echo -e "\nStep 2: Building APK"
    build_android_apk
    
    echo -e "\nStep 3: Backend API Tests (if server is running)"
    test_backend_api
    
    echo -e "${GREEN}✅ Full test suite completed${NC}"
}

generate_test_audio() {
    echo -e "${YELLOW}Generating Test Audio Files...${NC}"
    
    mkdir -p test_audio
    
    echo "Creating test audio files using ffmpeg..."
    
    # Short test file (if ffmpeg available)
    if command_exists ffmpeg; then
        ffmpeg -f lavfi -i "sine=frequency=440:duration=10" -ar 44100 test_audio/test_short.wav -y
        ffmpeg -f lavfi -i "sine=frequency=220:duration=30" -ar 44100 test_audio/test_medium.wav -y
        echo -e "${GREEN}✅ Test audio files created in test_audio/${NC}"
    else
        echo -e "${YELLOW}⚠️ ffmpeg not found. Creating dummy files...${NC}"
        touch test_audio/dummy_audio.mp3
        echo "Please manually add real audio files to test_audio/ directory"
    fi
}

# Main script
echo ""
while true; do
    show_menu
    read choice
    case $choice in
        1) start_backend ;;
        2) test_backend_api ;;
        3) run_android_unit_tests ;;
        4) run_android_instrumented_tests ;;
        5) build_android_apk ;;
        6) install_apk ;;
        7) full_test_suite ;;
        8) generate_test_audio ;;
        0) echo "Goodbye!"; exit 0 ;;
        *) echo -e "${RED}Invalid option${NC}" ;;
    esac
done