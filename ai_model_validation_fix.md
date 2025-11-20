# 🔧 Исправление: AI Model Validation Error

## ❌ **Проблема обнаружена:**
Backend логи показывают:
```
AI model: "demucs"
ERROR: Invalid AI model: "demucs"
```

## 🔍 **Причина:**
Android отправляет значение `ai_model` **в кавычках**: `"demucs"`  
Backend проверяет точное соответствие: `demucs` (без кавычек)

## ✅ **Исправление:**
Обновлена валидация в `backend/src/main.py`:

**Было:**
```python
if ai_model.lower() not in {"demucs", "spleeter"}:
```

**Стало:**
```python
ai_model_clean = ai_model.strip('"').lower()
if ai_model_clean not in {"demucs", "spleeter"}:
```

## 🧪 **Тест исправления:**
```bash
curl -X POST "http://localhost:8000/api/v1/upload" \
  -F "file=@-;type=audio/mpeg" \
  -F 'ai_model="demucs"'

# ✅ Результат: {"taskId":"...","status":"QUEUED","estimatedDuration":180}
```

## 🎯 **Статус:**
- ✅ Backend обновлен и перезапущен
- ✅ API принимает значения в кавычках  
- ✅ Готов для Android тестирования

**Попробуйте загрузить файл через Android приложение еще раз!**