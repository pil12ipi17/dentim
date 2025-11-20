#!/usr/bin/env python3

import uvicorn
import sys
import os
import signal

# Disable shell update prompts
os.environ['SHELL_SESSION_DID_INIT'] = '1'
os.environ['DISABLE_AUTO_UPDATE'] = 'true'

def signal_handler(sig, frame):
    print('Server shutting down gracefully...')
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

# Import the FastAPI app
from src.main import app

if __name__ == "__main__":
    print("Starting Karaoke Processing Server...")
    print("Server will run on http://0.0.0.0:8000")
    print("Available at:")
    print("  - Local: http://127.0.0.1:8000")
    print("  - Android Emulator: http://10.0.2.2:8000")
    print("Press Ctrl+C to stop")
    
    uvicorn.run(
        app,
        host="0.0.0.0", 
        port=8000, 
        reload=False
    )