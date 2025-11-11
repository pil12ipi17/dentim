# Data Model: Android Karaoke MVP

**Date**: 2025-11-11  
**Feature**: 001-android-karaoke-mvp

## Core Entities

### Song
Представляет аудиофайл с метаданными на Android устройстве

**Attributes**:
- `id`: Long (Primary Key) - Уникальный идентификатор
- `fileName`: String - Имя файла (без пути)
- `title`: String? - Название песни (опционально, извлекается из метаданных)
- `artist`: String? - Исполнитель (опционально, из метаданных)
- `duration`: Long - Длительность в миллисекундах
- `fileSize`: Long - Размер файла в байтах
- `filePath`: String - Локальный путь к файлу
- `createdAt`: LocalDateTime - Дата добавления
- `lastPlayedAt`: LocalDateTime? - Последнее воспроизведение

**Validation Rules**:
- fileName не пустое
- fileSize > 0 и <= 100MB (104,857,600 bytes)
- filePath должен существовать и быть читаемым
- duration > 0

**State Transitions**: N/A (простая сущность)

### Instrumental
Обработанная минусовка, связанная с оригинальной песней

**Attributes**:
- `id`: Long (Primary Key)
- `songId`: Long (Foreign Key → Song.id) - Связь с оригинальной песней
- `quality`: AudioQuality (Enum) - Качество обработки
- `filePath`: String - Путь к обработанной минусовке
- `fileSize`: Long - Размер обработанного файла
- `processedAt`: LocalDateTime - Дата завершения обработки
- `processingDuration`: Long - Время обработки в секундах

**Relationships**:
- Принадлежит одной Song (Many-to-One)
- У одной Song может быть несколько Instrumental с разным качеством

**Validation Rules**:
- songId должен существовать
- filePath должен существовать и быть читаемым
- quality должен быть валидным enum значением
- fileSize > 0

### ProcessingTask
Представляет операцию обработки аудиофайла с отслеживанием статуса

**Attributes**:
- `id`: Long (Primary Key)
- `songId`: Long (Foreign Key → Song.id)
- `quality`: AudioQuality (Enum) - Запрошенное качество
- `status`: ProcessingStatus (Enum) - Текущий статус
- `progress`: Int - Прогресс от 0 до 100
- `errorMessage`: String? - Сообщение об ошибке если статус ERROR
- `startedAt`: LocalDateTime - Время начала обработки
- `completedAt`: LocalDateTime? - Время завершения (успешного или с ошибкой)
- `serverId`: String? - ID задачи на сервере для отслеживания

**State Transitions**:
```
QUEUED → UPLOADING → PROCESSING → COMPLETED
  ↓         ↓           ↓            ↓
ERROR ← ERROR ← ERROR ← ERROR
```

**Validation Rules**:
- progress от 0 до 100
- completedAt устанавливается только при COMPLETED или ERROR
- errorMessage обязательно при статусе ERROR

## Enums

### AudioQuality
```kotlin
enum class AudioQuality(val displayName: String, val sampleRate: Int, val bitDepth: Int) {
    HIGH("Высокое (44.1kHz/16-bit)", 44100, 16),
    MEDIUM("Среднее (22kHz/16-bit)", 22050, 16),
    ECONOMY("Экономное (22kHz/8-bit)", 22050, 8)
}
```

### ProcessingStatus
```kotlin
enum class ProcessingStatus {
    QUEUED,      // В очереди на обработку
    UPLOADING,   // Загружается на сервер
    PROCESSING,  // Обрабатывается ИИ-моделью
    COMPLETED,   // Успешно завершено
    ERROR        // Ошибка в процессе
}
```

## Database Schema (Room)

### Relationships
- One Song → Many Instrumental (1:N)
- One Song → Many ProcessingTask (1:N)
- ProcessingTask.quality связан с потенциальным Instrumental.quality

### Indexes
- Song: index на fileName для быстрого поиска
- ProcessingTask: composite index на (status, startedAt) для мониторинга активных задач
- Instrumental: index на songId для быстрого получения минусовок песни

### Database Constraints
- ON DELETE CASCADE от Song к Instrumental и ProcessingTask
- UNIQUE constraint на (songId, quality) для Instrumental (одна минусовка на качество)

## API Data Transfer Objects

### UploadRequest
```json
{
  "fileName": "string",
  "fileSize": "number",
  "quality": "HIGH | MEDIUM | ECONOMY"
}
```

### TaskStatusResponse
```json
{
  "taskId": "string",
  "status": "QUEUED | UPLOADING | PROCESSING | COMPLETED | ERROR",
  "progress": "number (0-100)",
  "errorMessage": "string?",
  "estimatedTimeRemaining": "number?" // секунды
}
```

### ProcessedFileResponse
```json
{
  "taskId": "string",
  "downloadUrl": "string",
  "expiresAt": "ISO 8601 datetime",
  "fileSize": "number"
}
```