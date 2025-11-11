# Quickstart Guide: Android Karaoke MVP

**Feature**: 001-android-karaoke-mvp  
**Last Updated**: 2025-11-11

## Overview

Это руководство поможет быстро настроить и запустить Android караоке приложение с Python сервером для обработки аудио.

## Prerequisites

### Development Environment
- **Android Studio**: Arctic Fox 2020.3.1 или новее
- **Android SDK**: API level 26+ (Android 8.0)
- **Python**: 3.11 или новее
- **Docker**: 20.10+ (для сервера)
- **Git**: для версионирования

### Hardware Requirements
- **Android Device/Emulator**: Android 8.0+ с минимум 2GB RAM
- **Development Machine**: 8GB+ RAM, поддержка GPU для ML ускорения (опционально)
- **Storage**: 5GB+ свободного места для ML моделей и аудиофайлов

## Quick Setup (10 минут)

### 1. Clone Repository
```bash
git clone <repository-url>
cd dentim
git checkout 001-android-karaoke-mvp
```

### 2. Backend Setup
```bash
cd backend

# Создать виртуальное окружение
python -m venv venv
source venv/bin/activate  # или venv\Scripts\activate на Windows

# Установить зависимости
pip install -r requirements.txt

# Скачать ML модели (это может занять время)
python scripts/download_models.py

# Запустить сервер
uvicorn src.main:app --reload --host 0.0.0.0 --port 8000
```

### 3. Android Setup
```bash
cd android

# Открыть в Android Studio
# File → Open → выбрать папку android/

# Или из командной строки
./gradlew assembleDebug
```

### 4. Проверка Setup
- Backend: откройте http://localhost:8000/docs в браузере
- Android: запустите app на эмуляторе или устройстве

## Development Workflow

### Backend Development
```bash
# Запуск с автоперезагрузкой
uvicorn src.main:app --reload

# Запуск тестов
pytest tests/ -v

# Проверка качества кода
flake8 src/
mypy src/
```

### Android Development
```bash
# Сборка и установка
./gradlew installDebug

# Запуск unit тестов
./gradlew test

# Запуск UI тестов
./gradlew connectedAndroidTest
```

## Testing the Integration

### End-to-End Test Scenario
1. **Запустить backend**: `uvicorn src.main:app`
2. **Запустить Android app** на устройстве
3. **Выбрать аудиофайл** в приложении (например, test.mp3)
4. **Выбрать качество** (рекомендуется MEDIUM для тестов)
5. **Запустить обработку** и наблюдать progress
6. **Воспроизвести минусовку** когда обработка завершится

### Sample Audio Files
Для тестирования можно использовать:
- Короткие тестовые файлы (30-60 сек) для быстрой проверки
- Файлы с четким вокалом для лучших результатов разделения
- Различные форматы: MP3, WAV, FLAC

## Configuration

### Backend Configuration
Файл: `backend/src/config.py`
```python
# Основные настройки
MAX_FILE_SIZE = 100 * 1024 * 1024  # 100MB
UPLOAD_DIR = "temp_uploads"
AI_MODEL = "demucs"  # или "spleeter"
```

### Android Configuration
Файл: `android/app/src/main/res/values/config.xml`
```xml
<string name="api_base_url">http://10.0.2.2:8000</string>
<integer name="max_file_size_mb">100</integer>
<integer name="upload_timeout_seconds">30</integer>
```

## Common Issues & Solutions

### Backend Issues

#### "Model not found"
```bash
# Убедитесь что модели скачаны
python scripts/download_models.py
ls models/  # должны быть demucs и spleeter папки
```

#### "Permission denied" в Docker
```bash
# Проверьте права доступа к папкам
chmod -R 755 temp_uploads/
chmod -R 755 models/
```

### Android Issues

#### "Network Security Config"
Добавьте в `android/app/src/main/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

#### "Room database migration"
```kotlin
// При изменении схемы БД
@Database(version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // Добавить migration если нужно
}
```

## Production Checklist

- [ ] Использовать HTTPS для API
- [ ] Настроить proper error tracking (Sentry/Crashlytics)
- [ ] Оптимизировать размер APK (R8/ProGuard)
- [ ] Настроить CI/CD pipeline
- [ ] Добавить аутентификацию пользователей
- [ ] Настроить rate limiting на API
- [ ] Оптимизировать ML модели для продакшена

## Next Steps

После успешного запуска MVP:
1. Запустить `/speckit.tasks` для создания детального плана задач
2. Реализовать основную функциональность по приоритету
3. Добавить advanced features (text synchronization, user auth)
4. Подготовить к production deployment