#!/usr/bin/env python3

import sys
import os
import traceback

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

def test_demucs_detailed():
    """Test Demucs step by step to find where lameenc is needed"""
    try:
        import torch
        print("✅ PyTorch imported successfully")
        
        from demucs.pretrained import get_model
        print("✅ Demucs imported successfully")
        
        print("📥 Loading htdemucs model...")
        model = get_model("htdemucs")
        print(f"✅ Model loaded: {model.__class__.__name__}")
        
        print("🧪 Testing audio loading...")
        from demucs import audio
        print("✅ Demucs audio module imported")
        
        # Test with a small dummy audio
        print("🎵 Creating dummy audio...")
        import torch
        dummy_audio = torch.randn(2, 44100)  # 1 second of stereo audio
        print(f"✅ Created dummy audio: {dummy_audio.shape}")
        
        print("🔄 Testing model inference...")
        with torch.no_grad():
            sources = model(dummy_audio[None])  # Add batch dimension
        print(f"✅ Model inference successful: {sources.shape}")
        
        print("💾 Testing audio saving...")
        import soundfile as sf
        
        # Save to a temporary file
        test_output = "/tmp/test_vocals.wav"
        sf.write(test_output, sources[0, 0].detach().cpu().numpy().T, model.samplerate)
        print(f"✅ Audio saved successfully to {test_output}")
        
        print("\n🎉 Full Demucs pipeline is working!")
        return True
        
    except Exception as e:
        print(f"❌ Error at step: {e}")
        print("📋 Full traceback:")
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("🧪 Testing detailed Demucs integration...")
    success = test_demucs_detailed()
    sys.exit(0 if success else 1)