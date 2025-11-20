#!/usr/bin/env python3

import sys
import os
from pathlib import Path

# Add the backend directory to Python path
backend_dir = Path(__file__).parent / "backend"
sys.path.insert(0, str(backend_dir))

def process_audio_from_folder():
    """Process audio file from Audio folder using Demucs"""
    try:
        import torch
        from demucs.pretrained import get_model
        from demucs.apply import apply_model
        import soundfile as sf
        
        print("🎵 Обработка музыки из папки Audio...")
        
        # Найдем аудио файл в папке Audio
        audio_folder = Path("Audio")
        if not audio_folder.exists():
            print("❌ Папка Audio не найдена")
            return False
            
        # Найдем первый аудио файл
        audio_files = list(audio_folder.glob("*.mp3")) + list(audio_folder.glob("*.wav"))
        if not audio_files:
            print("❌ Аудио файлы не найдены в папке Audio")
            return False
            
        audio_file = audio_files[0]
        print(f"🎶 Найден файл: {audio_file.name}")
        
        # Загружаем модель Demucs
        print("📥 Загружаем модель htdemucs...")
        model = get_model("htdemucs")
        print(f"✅ Модель загружена: {model.sources}")
        
        # Загружаем аудио
        print(f"🔄 Загружаем аудио: {audio_file}")
        waveform_np, sample_rate = sf.read(str(audio_file))
        print(f"✅ Загружено: shape={waveform_np.shape}, sample_rate={sample_rate}")
        
        # Преобразуем в torch tensor [channels, time]
        if waveform_np.ndim == 1:  # моно
            waveform_np = waveform_np[:, None]
        waveform = torch.from_numpy(waveform_np.T).float()
        
        # Обеспечиваем стерео
        if waveform.shape[0] == 1:
            waveform = waveform.repeat(2, 1)
        elif waveform.shape[0] > 2:
            waveform = waveform[:2]
            
        print(f"✅ Подготовлено: {waveform.shape}")
        
        # Выполняем разделение
        print("🔄 Запускаем разделение Demucs...")
        with torch.no_grad():
            sources = apply_model(model, waveform[None])
        
        print(f"✅ Разделение завершено: {sources.shape}")
        
        # Создаем папку для результатов
        output_folder = audio_folder / f"{audio_file.stem}_separated"
        output_folder.mkdir(exist_ok=True)
        
        source_names = model.sources  # ['drums', 'bass', 'other', 'vocals']
        print(f"💾 Сохраняем {len(source_names)} дорожек...")
        
        # Сохраняем каждую дорожку
        for i, source_name in enumerate(source_names):
            output_file = output_folder / f"{source_name}.wav"
            source_audio = sources[0, i]  # [channels, time]
            sf.write(str(output_file), source_audio.detach().cpu().numpy().T, model.samplerate)
            print(f"✅ Сохранено: {output_file}")
        
        # Создаем инструментальную версию (без вокала)
        instrumental = torch.zeros_like(sources[0, 0])
        for i, source_name in enumerate(source_names):
            if source_name != "vocals":
                instrumental += sources[0, i]
        
        instrumental_file = output_folder / "instrumental.wav"
        sf.write(str(instrumental_file), instrumental.detach().cpu().numpy().T, model.samplerate)
        print(f"✅ Сохранена инструментальная: {instrumental_file}")
        
        print(f"\n🎉 Обработка завершена успешно!")
        print(f"📁 Результаты сохранены в: {output_folder}")
        print(f"🎤 Вокал: {output_folder}/vocals.wav")
        print(f"🎸 Инструментальная: {output_folder}/instrumental.wav")
        print(f"🥁 Барабаны: {output_folder}/drums.wav")
        print(f"🎸 Бас: {output_folder}/bass.wav")
        print(f"🎹 Остальное: {output_folder}/other.wav")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = process_audio_from_folder()
    sys.exit(0 if success else 1)