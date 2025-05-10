package org.cli;

import java.util.List;

/**
 * Класс для обработки пайплайнов команд.
 */
public class Pipeline {
    private final List<Command> commands;

    public Pipeline(List<Command> commands) {
        this.commands = commands;
    }

    /**
     * Выполняет пайплайн команд, передавая вывод каждой команды на вход следующей.
     *
     * @param environment окружение с переменными
     * @return результат выполнения последней команды
     */
    public String execute(Environment environment) {
        String result = null;
        Executor executor = new Executor(environment);

        // Последовательно выполняем каждую команду
        for (Command command : commands) {
            result = executor.execute(command, result); // Передаём результат предыдущей команде как input
            if (isError(result)) {
                break;  // Прерываем выполнение пайплайна, если произошла ошибка
            }
        }
        return result;
    }

    /**
     * Проверяет, является ли результат выполнения команды ошибкой
     *
     * @param result результат выполнения команды
     * @return true если результат содержит маркер ошибки, false в противном случае
     */
    private boolean isError(String result) {
        return result != null && (result.startsWith("ERROR: ") || result.startsWith("ERR: "));
    }
}