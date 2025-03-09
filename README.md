# CLI Interpreter

[![CI](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/ci.yml/badge.svg)](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/ci.yml)
[![CodeQL Advanced](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/codeql.yml/badge.svg)](https://github.com/B-E-D-A/sd-hw-cli/actions/workflows/codeql.yml)
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
   ./gradlew build
   ```
4. Запустите интерпретатор

## Лицензия

Этот проект лицензирован под [MIT License](LICENSE).
