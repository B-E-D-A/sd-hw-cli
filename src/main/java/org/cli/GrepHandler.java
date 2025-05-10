package org.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Обработчик команды grep с поддержкой regex, регистронезависимости и контекста.
 */
public class GrepHandler {
    private final Environment environment;

    public GrepHandler(Environment environment) {
        this.environment = environment;
    }

    /**
     * Выполняет поиск по шаблону во входных данных или файле.
     */
    public String execute(GrepParameters params, String input) {
        try {
            validateParameters(params, input);   // Проверяем параметры поиска
            String content = getContent(params, input);  // Получаем содержимое для поиска
            Pattern pattern = compilePattern(params);  // Компилируем паттерн с флагами

            return searchMatches(content, pattern, params.getAfterContext());  // Ищем совпадения в контексте
        } catch (IOException e) {
            return "ERROR: grep: " + e.getMessage();  // Обработка ошибок ввода/вывода
        } catch (PatternSyntaxException e) {
            return "ERROR: grep: invalid pattern: " + e.getMessage();  // Ошибка синтаксиса регулярного выражения
        } catch (IllegalArgumentException e) {
            return "ERROR: grep: " + e.getMessage();  // Ошибка валидации параметров
        }
    }

    /**
     * Проверяет обязательные параметры
     */
    private void validateParameters(GrepParameters params, String input) {
        if (params.getPattern() == null) {
            throw new IllegalArgumentException("missing pattern");  // Паттерн обязателен
        }
        if (input == null && params.getFileName() == null) {
            throw new IllegalArgumentException("missing file parameter"); // Должен быть либо входной текст, либо файл
        }
    }

    /**
     * Получает содержимое для поиска (из input или файла)
     */
    private String getContent(GrepParameters params, String input) throws IOException {
        if (input != null) {
            return input; // Если передан input, используем его
        }
        return new String(Files.readAllBytes(Paths.get(params.getFileName())));  // Иначе читаем файл
    }

    /**
     * Компилирует regex-шаблон с учетом флагов
     */
    private Pattern compilePattern(GrepParameters params) {
        String patternStr = preparePatternString(params);  // Подготовка строки паттерна
        int flags = preparePatternFlags(params); // Определяем флаги для паттерна
        return Pattern.compile(patternStr, flags);
    }

    /**
     * Подготавливает строку паттерна (добавляет границы слов при необходимости)
     */
    private String preparePatternString(GrepParameters params) {
        String patternStr = params.getPattern();
        if (params.isLiteralMatch()) {
            patternStr = Pattern.quote(patternStr); // Если задано буквальное совпадение, экранируем паттерн
        } else if (params.isWholeWord()) {
            patternStr = "\\b" + patternStr + "\\b"; // добавляем границы слов
        }
        return patternStr;
    }

    /**
     * Определяет флаги для regex на основе параметров
     */
    private int preparePatternFlags(GrepParameters params) {
        int flags = Pattern.UNICODE_CASE; // Поддержка Unicode
        if (params.isIgnoreCase()) {
            flags |= Pattern.CASE_INSENSITIVE; // Учитываем регистронезависимость
        }
        return flags;
    }

    /**
     * Ищет совпадения и формирует результат с учетом контекста
     */
    private String searchMatches(String content, Pattern pattern, int afterContext) {
        String[] lines = content.split("\n"); // Разбиваем содержимое на строки
        StringBuilder result = new StringBuilder();
        int linesAfterToPrint = 0;
        int lastPrintedLine = -1;

        for (int i = 0; i < lines.length; i++) {
            if (isMatchFound(pattern, lines[i])) { // Если совпадение найдено

                if (shouldPrintLine(i, lastPrintedLine)) { // Если нужно напечатать строку
                    appendLine(result, lines[i]);
                    lastPrintedLine = i;
                }
                linesAfterToPrint = afterContext; // Сколько строк после совпадения нужно напечатать
            } else if (shouldPrintContextLine(i, lastPrintedLine, linesAfterToPrint)) {
                appendLine(result, lines[i]); // Печатаем строки контекста
                lastPrintedLine = i;
                linesAfterToPrint--; // Уменьшаем количество строк после совпадения
            }
        }

        return result.toString();
    }

    private boolean isMatchFound(Pattern pattern, String line) {
        return pattern.matcher(line).find(); // Ищем совпадение в строке
    }

    private boolean shouldPrintLine(int currentLine, int lastPrintedLine) {
        return currentLine > lastPrintedLine;  // Проверяем, не была ли эта строка уже выведена
    }

    private boolean shouldPrintContextLine(int currentLine, int lastPrintedLine, int linesAfterToPrint) {
        return linesAfterToPrint > 0 && shouldPrintLine(currentLine, lastPrintedLine); // Проверка для строк контекста
    }

    private void appendLine(StringBuilder result, String line) {
        result.append(line).append("\n");  // Добавляем строку в результат
    }
}