# Research: Android Karaoke MVP

**Date**: 2025-11-11  
**Feature**: 001-android-karaoke-mvp

## AI Voice Separation Technology

**Decision**: Использовать Demucs как основную ИИ-модель с fallback на Spleeter

**Rationale**: 
- Demucs показывает лучшее качество разделения вокала в современных тестах
- Facebook Research активно поддерживает проект
- Хорошая документация и готовые pre-trained модели
- Spleeter от Deezer как запасной вариант для случаев, где Demucs не работает

**Alternatives considered**:
- Spleeter (старше, но стабильный)
- Open-Unmix (исследовательский, менее стабильный)
- Proprietary APIs (дорогие для MVP)

## Android Architecture Pattern

**Decision**: MVVM с Repository Pattern и Room Database

**Rationale**:
- Стандартная рекомендуемая архитектура Google для Android
- Хорошее разделение ответственности
- Room обеспечивает типобезопасную работу с локальной БД
- ViewModel автоматически обрабатывает lifecycle

**Alternatives considered**:
- MVP (устаревший подход)
- MVI (слишком сложно для MVP)
- Clean Architecture (избыточно для простого приложения)

## Backend Framework Selection

**Decision**: FastAPI с async/await поддержкой

**Rationale**:
- Высокая производительность для IO-операций (загрузка файлов)
- Автогенерация OpenAPI документации
- Отличная поддержка типов Python
- Простота integration с ML библиотеками

**Alternatives considered**:
- Django REST Framework (тяжеловесный для простого API)
- Flask (требует больше настройки для async)
- Express.js (но требует Node.js вместо Python для ML)

## File Upload Strategy

**Decision**: Multipart upload с progress tracking через WebSocket

**Rationale**:
- Стандартный HTTP multipart для совместимости
- WebSocket для real-time прогресса без polling
- Возможность resume upload в будущих версиях

**Alternatives considered**:
- Polling для прогресса (создает лишнюю нагрузку)
- Chunked upload (сложнее для MVP)
- Server-Sent Events (меньше контроля чем WebSocket)

## Audio Quality Handling

**Decision**: FFmpeg для конверсии между форматами качества

**Rationale**:
- Универсальное решение для аудио обработки
- Поддержка всех нужных форматов
- Хорошая оптимизация производительности
- Стандарт индустрии

**Alternatives considered**:
- Pydub (Python-only, медленнее)
- SoX (меньше поддерживаемых форматов)
- Native Android MediaPlayer (ограничен мобильной платформой)

## Testing Strategy

**Decision**: Unit tests + Contract tests + E2E automation

**Rationale**:
- Unit tests для бизнес-логики (80% покрытия)
- Contract tests для API совместимости Android ↔ Backend  
- E2E tests для критических пользовательских сценариев
- Mock AI модели в тестах для скорости

**Alternatives considered**:
- Только unit tests (недостаточно для интеграции)
- Manual testing only (не масштабируется)
- Property-based testing (избыточно для MVP)

## Deployment & Infrastructure

**Decision**: Docker для backend, Android APK для клиента

**Rationale**:
- Docker упрощает развертывание Python + ML зависимостей
- Стандартный Android build процесс для APK
- Возможность деплоя на любой cloud provider

**Alternatives considered**:
- Native Python deployment (сложнее управление зависимостями)
- Kubernetes (избыточно для MVP)
- Serverless functions (не подходит для долгие ML операции)