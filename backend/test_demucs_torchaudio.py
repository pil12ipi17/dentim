#!/usr/bin/env python3

import sys
import os

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

def test_demucs_without_audio():
    """Test Demucs without using the audio module"""
    try:
        import torch
        print("✅ PyTorch imported successfully")
        
        from demucs.pretrained import get_model
        print("✅ Demucs imported successfully")
        
        print("📥 Loading htdemucs model...")
        model = get_model("htdemucs")
        print(f"✅ Model loaded: {model.__class__.__name__}")
        
        # Use torchaudio directly instead of demucs.audio
        print("🎵 Testing with torchaudio directly...")
        import torchaudio
        
        # Create a dummy waveform
        waveform = torch.randn(2, 44100 * 5)  # 5 seconds of stereo audio at 44.1kHz
        print(f"✅ Created dummy waveform: {waveform.shape}")
        
        # Test model inference
        print("🔄 Testing model inference...")
        from demucs.apply import apply_model
        with torch.no_grad():
            sources = apply_model(model, waveform[None])  # Add batch dimension
        print(f"✅ Model inference successful: {sources.shape}")
        print(f"✅ Model sources: {model.sources}")
        
        # Test saving with soundfile (уже установлен)
        print("💾 Testing audio saving with soundfile...")
        import soundfile as sf
        
        # Extract vocals (usually index 3 in htdemucs)
        vocals_idx = model.sources.index('vocals') if 'vocals' in model.sources else 0
        vocals = sources[0, vocals_idx]  # [channels, time]
        
        output_path = "/tmp/test_vocals_sf.wav"
        sf.write(output_path, vocals.detach().cpu().numpy().T, model.samplerate)
        print(f"✅ Vocals saved to {output_path}")
        
        # Test instrumental (combine non-vocal sources)
        instrumental = torch.zeros_like(vocals)
        for i, source_name in enumerate(model.sources):
            if source_name != 'vocals':
                instrumental += sources[0, i]
        
        instrumental_path = "/tmp/test_instrumental_sf.wav"
        sf.write(instrumental_path, instrumental.detach().cpu().numpy().T, model.samplerate)
        print(f"✅ Instrumental saved to {instrumental_path}")
        
        print("\n🎉 Demucs pipeline working without lameenc!")
        return True
        
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("🧪 Testing Demucs without audio module...")
    success = test_demucs_without_audio()
    sys.exit(0 if success else 1)