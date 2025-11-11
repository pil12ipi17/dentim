# Java 17 Installation Required

## Проблема
Android Gradle Plugin требует Java 17, а у вас установлена Java 16.

## Решение: Установка Java 17

### Способ 1: Через Homebrew (Рекомендуется)
```bash
# Установите Java 17
brew install openjdk@17

# Добавьте в PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Проверьте версию
java --version
```

### Способ 2: Через Amazon Corretto
```bash
# Скачайте и установите Amazon Corretto 17
# https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html
# Выберите macOS installer (.pkg)

# После установки добавьте в PATH
echo 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc
```

### Способ 3: Временное решение для тестирования
```bash
# Если вы хотите просто протестировать Backend API
# без сборки Android проекта:

./quick-test.sh
# Выберите опцию 6 - Test Backend API

# Или напрямую:
source backend/venv/bin/activate
python test_api.py
```

## Проверка установки
```bash
java --version
# Должно показать: openjdk 17.x.x

# Если показывает Java 16, перезапустите терминал или выполните:
source ~/.zshrc
```

## После установки Java 17
```bash
# Теперь можно собирать Android проект:
cd android
./gradlew assembleDebug

# Или используйте quick-test.sh:
./quick-test.sh
# Выберите опцию 1 - Build Android Project
```

## Альтернатива: Android Studio
Android Studio автоматически установит и настроит нужную версию Java:
1. Установите Android Studio
2. Откройте проект: `/Users/denis/AndroidKaraoke/dentim/android/`
3. Android Studio предложит установить нужный JDK
4. Согласитесь на установку

Тогда сборка будет работать внутри Android Studio без проблем.