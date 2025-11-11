#!/usr/bin/env python3
"""Test script for Karaoke Backend API"""

import requests
import io
import time

BASE_URL = "http://localhost:8000/api/v1"

def test_health():
    """Test health endpoint"""
    print("🏥 Testing Health Endpoint...")
    response = requests.get(f"{BASE_URL}/health")
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")
    return response.status_code == 200

def test_upload():
    """Test upload endpoint"""
    print("\n📤 Testing Upload Endpoint...")
    
    # Create fake audio file data
    fake_audio = io.BytesIO(b"fake audio content for testing")
    fake_audio.name = "test.mp3"
    
    files = {
        'file': ('test.mp3', fake_audio, 'audio/mpeg')
    }
    data = {
        'ai_model': 'demucs'
    }
    
    response = requests.post(f"{BASE_URL}/upload", files=files, data=data)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")
    
    if response.status_code == 200:
        job_data = response.json()
        job_id = job_data['job_id']
        print(f"Job created: {job_id}")
        return job_id
    return None

def test_processing_status(job_id):
    """Test processing status endpoint"""
    print(f"\n⏳ Testing Processing Status for job {job_id}...")
    
    for i in range(6):  # Check status 6 times (12 seconds total)
        response = requests.get(f"{BASE_URL}/processing/{job_id}/status")
        print(f"Status: {response.status_code}")
        
        if response.status_code == 200:
            status_data = response.json()
            print(f"Job Status: {status_data['status']}, Progress: {status_data['progress']}%")
            
            if status_data['status'] == 'COMPLETED':
                print("✅ Processing completed!")
                return True
            elif status_data['status'] == 'FAILED':
                print("❌ Processing failed!")
                return False
        
        time.sleep(2)
    
    return False

def test_all_jobs():
    """Test getting all jobs"""
    print("\n📋 Testing All Jobs Endpoint...")
    response = requests.get(f"{BASE_URL}/processing")
    print(f"Status: {response.status_code}")
    print(f"Jobs count: {len(response.json()) if response.status_code == 200 else 0}")

def main():
    """Run all tests"""
    print("🎤 Karaoke Backend API Test Suite")
    print("=" * 40)
    
    # Test health
    if not test_health():
        print("❌ Health check failed!")
        return
    
    # Test upload
    job_id = test_upload()
    if not job_id:
        print("❌ Upload failed!")
        return
    
    # Test processing status
    test_processing_status(job_id)
    
    # Test all jobs
    test_all_jobs()
    
    print("\n✅ API tests completed!")

if __name__ == "__main__":
    main()