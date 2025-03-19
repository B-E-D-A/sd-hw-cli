# CLI Interpreter

[![CI](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/ci.yml/badge.svg?branch=cli-arch)](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/ci.yml)
[![CodeQL Advanced](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/codeql.yml/badge.svg?branch=cli-arch)](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/codeql.yml)
[![Workflow: Dependabot](https://img.shields.io/badge/Dependabot-enabled-33dd44?logo=github)](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/dependabot/dependabot-updates)

## Описание проекта

Этот проект представляет собой интерпретатор командной строки, который поддерживает следующие функции:

- **Базовые команды**:
  - `cat [FILE]` — выводит содержимое файла.
  - `echo` — выводит аргументы на экран.
  - `wc [FILE]` — выводит количество строк, слов и байт в файле.
  - `pwd` — выводит текущую директорию.
  - `exit` — завершает работу интерпретатора.

- **Поддержка кавычек**:
  - Одинарные и двойные кавычки для обработки аргументов.

- **Подстановка переменных окружения**:
  - Поддержка переменных окружения (например, `FILE=example.txt`) и их подстановка в командах (например, `cat $FILE`).

- **Пайплайны**:
  - Поддержка оператора `|` для передачи вывода одной команды на вход другой (например, `cat example.txt | wc`).

## Установка и запуск

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/B-E-D-A/sd-hw-cli.git
   cd sd-hw-cli
   ```
2. Соберите проект с помощью Gradle:
   ```bash
   ./gradlew clean build
   ```
3. Запустите интерпретатор
   ```bash
   ./cli
   ```
   или с помощью jar файла:
   ```bash 
   java -jar build/libs/cli-1.0.jar
   ```

## Тестирование

Чтобы запустить тесты, выполните команду:

```bash
./gradlew clean test
```

## Примеры использования

-  Вывод содержимого файла: `cat example.txt`
-  Подсчет строк, слов и байт в файле: `wc example.txt`
-  Использование переменных окружения: `FILE=example.txt` `cat $FILE`
-  Пайплайны: `cat example.txt | wc`
-  Вывод текущей директории: `pwd`
-  Завершение работы: `exit`


## Лицензия

Этот проект лицензирован под [MIT License](LICENSE).
