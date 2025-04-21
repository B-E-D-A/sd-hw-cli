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
     * @param environment окружение с переменными
     * @return результат выполнения последней команды
     */
    public String execute(Environment environment) {
        String input = null;
        Executor executor = new Executor(environment);

        for (Command command : commands) {
            input = executor.execute(command, input);
        }

        return input;
    }
}