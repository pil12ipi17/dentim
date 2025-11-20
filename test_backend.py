#!/usr/bin/env python3
"""Test script for updated backend with Demucs integration."""

import requests
import time
import subprocess
import sys
import os
from pathlib import Path

BASE_URL = "http://localhost:8000"

def start_server():
    """Start the backend server."""
    backend_dir = Path(__file__).parent / "backend"
    venv_python = backend_dir / "venv" / "bin" / "python"
    main_py = backend_dir / "src" / "main.py"
    
    print("🚀 Starting backend server...")
    
    # Start server in background
    process = subprocess.Popen([
        str(venv_python), str(main_py)
    ], cwd=str(backend_dir))
    
    # Wait for server to start
    time.sleep(3)
    return process

def test_health_endpoints():
    """Test both health endpoints."""
    print("\n🏥 Testing Health Endpoints...")
    
    # Test contract endpoint
    try:
        response = requests.get(f"{BASE_URL}/health", timeout=5)
        print(f"✅ /health: {response.status_code} - {response.json()}")
    except Exception as e:
        print(f"❌ /health failed: {e}")
    
    # Test legacy endpoint
    try:
        response = requests.get(f"{BASE_URL}/api/v1/health", timeout=5)
        print(f"✅ /api/v1/health: {response.status_code} - {response.json()}")
    except Exception as e:
        print(f"❌ /api/v1/health failed: {e}")

def test_upload_endpoint():
    """Test file upload endpoint."""
    print("\n📤 Testing Upload Endpoint...")
    
    # Create test file
    test_file_content = b"fake audio content for testing"
    
    try:
        files = {
            "file": ("test.mp3", test_file_content, "audio/mpeg"),
        }
        data = {
            "quality": "HIGH",
        }
        
        response = requests.post(f"{BASE_URL}/api/v1/upload", files=files, data=data, timeout=10)
        print(f"Status: {response.status_code}")
        
        if response.status_code == 202:
            result = response.json()
            print(f"✅ Upload successful: {result}")
            return result.get("taskId")
        else:
            print(f"❌ Upload failed: {response.text}")
            return None
            
    except Exception as e:
        print(f"❌ Upload failed: {e}")
        return None

def test_task_status(task_id):
    """Test task status endpoint."""
    if not task_id:
        return
        
    print(f"\n📊 Testing Task Status for {task_id}...")
    
    for i in range(5):
        try:
            response = requests.get(f"{BASE_URL}/api/v1/tasks/{task_id}/status", timeout=5)
            if response.status_code == 200:
                result = response.json()
                print(f"Status {i+1}: {result}")
                if result.get("status") in ["COMPLETED", "ERROR", "CANCELLED"]:
                    break
            else:
                print(f"❌ Status check failed: {response.status_code}")
                
        except Exception as e:
            print(f"❌ Status check failed: {e}")
        
        time.sleep(2)

def main():
    """Run all tests."""
    print("🎤 Testing Updated Karaoke Backend with Demucs Integration")
    print("=" * 60)
    
    # Start server
    server_process = None
    try:
        server_process = start_server()
        
        # Run tests
        test_health_endpoints()
        task_id = test_upload_endpoint()
        test_task_status(task_id)
        
    finally:
        # Cleanup
        if server_process:
            print("\n🛑 Stopping server...")
            server_process.terminate()
            server_process.wait()
    
    print("\n✅ Testing completed!")

if __name__ == "__main__":
    main()