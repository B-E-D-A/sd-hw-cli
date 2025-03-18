package org.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Представляет команду, вводимую пользователем в CLI.
 */
public class Command {
    private final String name;
    private final List<String> arguments;

    /**
     * Создаёт команду с указанным именем и аргументами.
     *
     * @param name      имя команды
     * @param arguments список аргументов команды
     */
    public Command(String name, List<String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Возвращает имя команды.
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает список аргументов команды.
     */
    public List<String> getArguments() {
        return arguments;
    }

    /**
     * Возвращает полную команду в виде списка.
     *
     * @return список строк с полным вызовом команды
     */
    public List<String> getFullCommand() {
        List<String> fullCommand = new ArrayList<>();
        fullCommand.add(name);
        fullCommand.addAll(arguments);
        return fullCommand;
    }
}
