#!/bin/bash

# Disable shell session save/restore
export SHELL_SESSION_DID_INIT=1
export DISABLE_AUTO_UPDATE=true

cd /Users/denis/AndroidKaraoke/dentim/backend

echo "Starting server in background..."
/Users/denis/AndroidKaraoke/dentim/backend/venv/bin/python start_server_noupdate.py &
SERVER_PID=$!

echo "Server PID: $SERVER_PID"
echo "Waiting for server to start..."
sleep 5

echo "Testing server health..."
curl http://127.0.0.1:8000/health

echo "Processing music file..."
curl -X POST "http://127.0.0.1:8000/api/v1/upload" \
     -F "file=@/Users/denis/AndroidKaraoke/dentim/Audio/1.valerij-meladze-sera-albom-sera.mp3;type=audio/mpeg" \
     -F 'ai_model=demucs'

echo "Stopping server..."
kill $SERVER_PID