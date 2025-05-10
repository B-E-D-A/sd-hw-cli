package org.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбирает строку ввода в команды и аргументы.
 */
public class Parser {
    private final Environment environment;

    public Parser(Environment environment, Executor executor) {
        this.environment = environment;
    }

    /**
     * Преобразует строку в список команд.
     * Строка разбивается на команды, разделенные символом |.
     */
    public List<Command> parse(String input) {
        List<Command> commands = new ArrayList<>();
        if (input.isBlank()) {
            return commands;
        }

        // Подставляем переменные окружения в строку
        input = resolveVariables(input);

        // Разбиваем строку на команды по символу "|"
        String[] commandStrings = input.split("\\|");
        for (String commandString : commandStrings) {
            List<Token> tokens = tokenizeWithQuotes(commandString.trim());
            if (tokens.isEmpty()) {
                continue; // Если токены пусты, переходим к следующей команде
            }
            String commandName = tokens.get(0).value(); // Первая часть — это имя команды

            // Остальные части — это аргументы команды
            List<String> args = tokens.subList(1, tokens.size()).stream()
                    .map(Token::value)
                    .toList();

            Command command = new Command(commandName, args);

            // Проверка для команды 'grep', чтобы установить флаг literalMatch, если используется одинарная кавычка
            if ("grep".equals(commandName) && !tokens.isEmpty() && tokens.size() > 1) {
                if (tokens.get(1).quoteType() == QuoteType.SINGLE) {
                    command.setLiteralMatch(true);
                }
            }
            commands.add(command);

        }
        return commands;
    }

    /**
     * Разбивает строку на аргументы-токены и помечает тип кавычек.
     */
    private List<Token> tokenizeWithQuotes(String input) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|'([^']*)'|(\\S+)").matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new Token(matcher.group(1), QuoteType.DOUBLE)); // Строка в двойных кавычках
            } else if (matcher.group(2) != null) {
                tokens.add(new Token(matcher.group(2), QuoteType.SINGLE)); // Строка в одинарных кавычках
            } else {
                tokens.add(new Token(matcher.group(3), QuoteType.NONE)); // Обычное слово без кавычек
            }
        }

        return tokens;
    }

    /**
     * Заменяет переменные окружения вида $VAR на их значения.
     */
    private String resolveVariables(String arg) {
        Pattern pattern = Pattern.compile("\\$(\\w+)"); // Регулярное выражение для поиска переменных
        Matcher matcher = pattern.matcher(arg);
        StringBuilder result = new StringBuilder();

        // Применяем замену для каждой найденной переменной
        while (matcher.find()) {
            String varName = matcher.group(1);  // Имя переменной
            String value = environment.getVariable(varName); // Получаем значение переменной из окружения
            matcher.appendReplacement(result, value); // Заменяем переменную на её значение
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Тип кавычек вокруг аргумента
     */
    private enum QuoteType {
        NONE, SINGLE, DOUBLE
    }

    /**
     * Аргумент с типом кавычек
     */
    private record Token(String value, QuoteType quoteType) {
    }
}
