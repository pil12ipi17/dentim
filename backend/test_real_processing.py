#!/usr/bin/env python3

import sys
import os

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

def test_real_audio_processing():
    """Test real audio file processing"""
    try:
        import torch
        from demucs.pretrained import get_model
        from demucs.apply import apply_model
        import soundfile as sf
        
        print("🧪 Testing real audio processing...")
        
        # Load model
        print("📥 Loading htdemucs model...")
        model = get_model("htdemucs")
        print(f"✅ Model loaded: {model.sources}")
        
        # Load the test audio file
        audio_file = "/tmp/test_audio.mp3"
        if not os.path.exists(audio_file):
            print(f"❌ Test file {audio_file} not found")
            return False
            
        print(f"🎵 Loading audio: {audio_file}")
        waveform_np, sample_rate = sf.read(audio_file)
        print(f"✅ Loaded audio: shape={waveform_np.shape}, sample_rate={sample_rate}")
        
        # Convert to torch tensor and ensure correct shape [channels, time]
        if waveform_np.ndim == 1:  # mono
            waveform_np = waveform_np[:, None]  # [time, 1]
        waveform = torch.from_numpy(waveform_np.T).float()  # [channels, time]
        
        # Ensure stereo
        if waveform.shape[0] == 1:
            waveform = waveform.repeat(2, 1)
        elif waveform.shape[0] > 2:
            waveform = waveform[:2]
            
        print(f"✅ Prepared waveform: {waveform.shape}")
        
        # Resample if needed (skip for now to avoid torchaudio issues)
        if sample_rate != model.samplerate:
            print(f"⚠️ Sample rate mismatch: {sample_rate}Hz vs {model.samplerate}Hz")
            print("ℹ️ Proceeding without resampling for test")
        
        # Run separation
        print("🔄 Running Demucs separation...")
        with torch.no_grad():
            sources = apply_model(model, waveform[None])
        
        print(f"✅ Separation complete: {sources.shape}")
        
        # Save results
        output_dir = "/tmp/demucs_test_output"
        os.makedirs(output_dir, exist_ok=True)
        
        source_names = model.sources
        print(f"💾 Saving {len(source_names)} sources...")
        
        # Save vocals
        if "vocals" in source_names:
            vocals_idx = source_names.index("vocals")
            vocals_file = f"{output_dir}/vocals.wav"
            vocals = sources[0, vocals_idx]  # [channels, time]
            sf.write(vocals_file, vocals.detach().cpu().numpy().T, model.samplerate)
            print(f"✅ Saved vocals: {vocals_file}")
        
        # Save instrumental
        instrumental = torch.zeros_like(sources[0, 0])
        for i, source_name in enumerate(source_names):
            if source_name != "vocals":
                instrumental += sources[0, i]
        
        instrumental_file = f"{output_dir}/instrumental.wav"
        sf.write(instrumental_file, instrumental.detach().cpu().numpy().T, model.samplerate)
        print(f"✅ Saved instrumental: {instrumental_file}")
        
        print("\n🎉 Real audio processing successful!")
        print(f"📁 Output files in: {output_dir}")
        return True
        
    except Exception as e:
        print(f"❌ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = test_real_audio_processing()
    sys.exit(0 if success else 1)