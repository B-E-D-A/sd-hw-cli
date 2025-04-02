package org.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;

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
            validateParameters(params, input);
            String content = getContent(params, input);
            Pattern pattern = compilePattern(params);

            return searchMatches(content, pattern, params.getAfterContext());
        } catch (IOException e) {
            return "grep: " + e.getMessage();
        } catch (PatternSyntaxException e) {
            return "grep: invalid pattern: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "grep: " + e.getMessage();
        }
    }

    /** Проверяет обязательные параметры */
    private void validateParameters(GrepParameters params, String input) {
        if (params.getPattern() == null) {
            throw new IllegalArgumentException("missing pattern");
        }
        if (input == null && params.getFileName() == null) {
            throw new IllegalArgumentException("missing file parameter");
        }
    }

    /** Получает содержимое для поиска (из input или файла) */
    private String getContent(GrepParameters params, String input) throws IOException {
        if (input != null) {
            return input;
        }
        return new String(Files.readAllBytes(Paths.get(params.getFileName())));
    }

    /** Компилирует regex-шаблон с учетом флагов */
    private Pattern compilePattern(GrepParameters params) {
        String patternStr = preparePatternString(params);
        int flags = preparePatternFlags(params);
        return Pattern.compile(patternStr, flags);
    }

    /** Подготавливает строку паттерна (добавляет границы слов при необходимости) */
    private String preparePatternString(GrepParameters params) {
        String patternStr = params.getPattern();
        if (params.isWholeWord()) {
            patternStr = "\\b" + patternStr + "\\b";
        }
        return patternStr;
    }

    /** Определяет флаги для regex на основе параметров */
    private int preparePatternFlags(GrepParameters params) {
        int flags = Pattern.UNICODE_CASE;
        if (params.isIgnoreCase()) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        return flags;
    }

    /** Ищет совпадения и формирует результат с учетом контекста */
    private String searchMatches(String content, Pattern pattern, int afterContext) {
        String[] lines = content.split("\n");
        StringBuilder result = new StringBuilder();
        int linesAfterToPrint = 0;
        int lastPrintedLine = -1;

        for (int i = 0; i < lines.length; i++) {
            if (isMatchFound(pattern, lines[i])) {

                if (shouldPrintLine(i, lastPrintedLine)) {
                    appendLine(result, lines[i]);
                    lastPrintedLine = i;
                }
                linesAfterToPrint = afterContext;
            } else if (shouldPrintContextLine(i, lastPrintedLine, linesAfterToPrint)) {
                appendLine(result, lines[i]);
                lastPrintedLine = i;
                linesAfterToPrint--;
            }
        }

        return result.toString();
    }

    private boolean isMatchFound(Pattern pattern, String line) {
        return pattern.matcher(line).find();
    }

    private boolean shouldPrintLine(int currentLine, int lastPrintedLine) {
        return currentLine > lastPrintedLine;
    }

    private boolean shouldPrintContextLine(int currentLine, int lastPrintedLine, int linesAfterToPrint) {
        return linesAfterToPrint > 0 && shouldPrintLine(currentLine, lastPrintedLine);
    }

    private void appendLine(StringBuilder result, String line) {
        result.append(line).append("\n");
    }
}