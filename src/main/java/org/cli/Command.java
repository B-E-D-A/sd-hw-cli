package org.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Представляет команду, вводимую пользователем в CLI.
 */
public class Command {
    private final String name;
    private final List<String> arguments;

    /**Флаг, указывающий, что аргумент команды должен трактоваться буквально (не как regex)*/
    private boolean literalMatch = false;

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
     * Возвращает флаг, должны ли аргументы трактоваться буквально.
     */
    public boolean isLiteralMatch() {
        return literalMatch;
    }

    /**
     * Устанавливает флаг, должны ли аргументы трактоваться буквально.
     */
    public void setLiteralMatch(boolean literalMatch) {
        this.literalMatch = literalMatch;
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
