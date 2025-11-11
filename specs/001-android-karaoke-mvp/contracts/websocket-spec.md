# WebSocket Specification: Real-time Task Updates

**Endpoint**: `ws://localhost:8000/ws/tasks/{taskId}`

## Connection Protocol

### Authentication
- Пока не требуется для MVP
- В будущем: токен авторизации в query параметрах

### Connection Lifecycle
1. Клиент подключается к `ws://server/ws/tasks/{taskId}`
2. Сервер подтверждает подключение отправкой текущего статуса
3. Сервер отправляет обновления при изменении прогресса/статуса
4. Соединение закрывается автоматически при завершении задачи или ошибке

## Message Types

### Server → Client Messages

#### Progress Update
```json
{
  "type": "progress_update",
  "data": {
    "taskId": "task_550e8400-e29b-41d4-a716-446655440000",
    "status": "PROCESSING",
    "progress": 45,
    "estimatedTimeRemaining": 120,
    "timestamp": "2025-11-11T12:30:45Z"
  }
}
```

#### Status Change
```json
{
  "type": "status_change",
  "data": {
    "taskId": "task_550e8400-e29b-41d4-a716-446655440000",
    "oldStatus": "PROCESSING",
    "newStatus": "COMPLETED",
    "progress": 100,
    "completedAt": "2025-11-11T12:35:15Z",
    "downloadUrl": "/api/v1/tasks/task_550e8400.../download"
  }
}
```

#### Error
```json
{
  "type": "error",
  "data": {
    "taskId": "task_550e8400-e29b-41d4-a716-446655440000",
    "status": "ERROR", 
    "errorCode": "PROCESSING_FAILED",
    "errorMessage": "AI model failed to separate vocals",
    "timestamp": "2025-11-11T12:32:10Z"
  }
}
```

### Client → Server Messages

#### Ping (Keep-Alive)
```json
{
  "type": "ping",
  "timestamp": "2025-11-11T12:30:00Z"
}
```

#### Request Status
```json
{
  "type": "request_status",
  "data": {
    "taskId": "task_550e8400-e29b-41d4-a716-446655440000"
  }
}
```

## Error Handling

### Connection Errors
- **1000**: Normal closure (task completed)
- **1001**: Server going away
- **4000**: Task not found
- **4001**: Task already completed
- **5000**: Internal server error

### Reconnection Strategy
- Клиент должен переподключаться с exponential backoff
- Максимум 3 попытки переподключения
- После неудачных попыток fallback на HTTP polling

## Android Implementation Notes

### Dependencies
```kotlin
// build.gradle.kts
implementation("org.java-websocket:Java-WebSocket:1.5.3")
// или OkHttp WebSocket
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

### Usage Pattern
```kotlin
class TaskProgressWebSocket(private val taskId: String) {
    private var webSocket: WebSocket? = null
    
    fun connect(onProgressUpdate: (TaskStatus) -> Unit) {
        val request = Request.Builder()
            .url("ws://localhost:8000/ws/tasks/$taskId")
            .build()
            
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Parse JSON and call onProgressUpdate
            }
        })
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Task monitoring complete")
    }
}
```

### Fallback Strategy
Если WebSocket недоступен, использовать HTTP polling каждые 2 секунды:
```kotlin
class TaskStatusPoller(private val apiService: ApiService) {
    fun startPolling(taskId: String, callback: (TaskStatus) -> Unit) {
        // Implementation with coroutines and delay
    }
}
```