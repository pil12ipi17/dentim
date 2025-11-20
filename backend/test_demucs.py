#!/usr/bin/env python3

import sys
import os

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

def test_demucs():
    """Test if Demucs can be imported and used directly"""
    try:
        import torch
        print("✅ PyTorch imported successfully")
        
        from demucs.pretrained import get_model
        print("✅ Demucs imported successfully")
        
        # Load the pre-trained model
        print("📥 Loading htdemucs model...")
        model = get_model("htdemucs")
        print(f"✅ Model loaded: {model.__class__.__name__}")
        
        from demucs import audio
        print("✅ Demucs audio module imported")
        
        import soundfile as sf
        print("✅ SoundFile imported")
        
        print("\n🎉 All Demucs dependencies are working!")
        print(f"Model sources: {model.sources}")
        print(f"Sample rate: {model.samplerate}")
        
        return True
        
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

if __name__ == "__main__":
    print("🧪 Testing Demucs integration...")
    success = test_demucs()
    sys.exit(0 if success else 1)