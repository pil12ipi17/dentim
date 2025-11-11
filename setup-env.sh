#!/bin/bash

# Setup script for Android Karaoke project
echo "🎤 Setting up Android Karaoke development environment..."

# Set JAVA_HOME to Homebrew OpenJDK 17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# Add Java to PATH
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java version
echo "☕ Java version:"
java -version

echo "🎯 JAVA_HOME: $JAVA_HOME"

# Navigate to project directory
cd "$(dirname "$0")"

echo ""
echo "✅ Environment setup complete!"
echo ""
echo "📱 To build the Android app:"
echo "   cd android && ./gradlew assembleDebug"
echo ""
echo "🚀 To start the backend server:"
echo "   cd backend && source venv/bin/activate && python src/main.py"
echo ""
echo "🎵 Ready for karaoke development! 🎵"