#!/usr/bin/env python3
"""
Simple test server to serve audio files for Android app testing
"""

from fastapi import FastAPI
from fastapi.responses import FileResponse
import uvicorn
from pathlib import Path

app = FastAPI()

# Sample audio file path
VOCALS_FILE = "/Users/denis/AndroidKaraoke/dentim/backend/outputs/de6d7fa9-f22a-48f9-bc05-6af177e57560/htdemucs/de6d7fa9-f22a-48f9-bc05-6af177e57560/vocals.wav"
INSTRUMENTAL_FILE = "/Users/denis/AndroidKaraoke/dentim/backend/outputs/de6d7fa9-f22a-48f9-bc05-6af177e57560/htdemucs/de6d7fa9-f22a-48f9-bc05-6af177e57560/no_vocals.wav"

@app.get("/api/v1/health")
async def health():
    return {"status": "ok", "message": "Audio test server running"}

@app.get("/api/v1/download/{task_id}/vocals")
async def get_vocals(task_id: str):
    """Serve vocals file for testing"""
    if Path(VOCALS_FILE).exists():
        return FileResponse(
            path=VOCALS_FILE,
            filename=f"vocals.wav",
            media_type="audio/wav"
        )
    return {"error": "File not found"}

@app.get("/api/v1/download/{task_id}/instrumental")
async def get_instrumental(task_id: str):
    """Serve instrumental file for testing"""
    if Path(INSTRUMENTAL_FILE).exists():
        return FileResponse(
            path=INSTRUMENTAL_FILE,
            filename=f"instrumental.wav",
            media_type="audio/wav"
        )
    return {"error": "File not found"}

@app.get("/api/v1/processing")
async def get_processing():
    """Return sample processing data"""
    return [{
        "task_id": "test-vocals",
        "status": "COMPLETED",
        "progress": 100,
        "filename": "Test Song.mp3",
        "vocals_ready": True,
        "instrumental_ready": True
    }]

if __name__ == "__main__":
    print("Starting audio test server on port 8000...")
    print(f"Vocals file: {VOCALS_FILE}")
    print(f"Instrumental file: {INSTRUMENTAL_FILE}")
    print(f"Vocals exists: {Path(VOCALS_FILE).exists()}")
    print(f"Instrumental exists: {Path(INSTRUMENTAL_FILE).exists()}")
    
    uvicorn.run(app, host="0.0.0.0", port=8000)