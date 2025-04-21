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

- **Дополнительно поддерживает:**
  - `grep` -  ищет строки, соответствующие заданному шаблону. Поддерживаются флаги `-i` или `--ignore-case`, `-w` или `--word-regexp`, `-A NUM` или `--after-context NUM`

Для реализации парсинга аргументов команды grep была выбрана библиотека `JCommander`. Альтернативно рассматривались `Apache Commons CLI`, `Argparse4j`. 

Почему вырана именно `JCommander`?
- Лаконичный синтаксис на аннотациях. При использовании `Apache Commons CLI` много шаблонного кода, без аннотаций, а в `Argparse4j` синтаксис сложнее, чем в выбранной библиотеке.
- Автоматическая конвертация типов, а в `Apache Commons CLI` ручная обработка типов
- Поддержка Unicode
- Гибкость — поддерживает короткие (`-i`), длинные (`--ignore-case`) и комбинированные (`-iwA 5`) флаги.

`Argparse4j` избыточна для данного проекта, поэтому `JCommander` оказался оптимальным выбором по соотношению простоты и функциональности.

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
-  Поиск строк по шаблону: `grep -A 5 "jar" README.md`

## Лицензия

Этот проект лицензирован под [MIT License](LICENSE).
