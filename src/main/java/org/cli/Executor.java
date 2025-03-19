package org.cli;

import java.io.IOException;

/**
 * Класс, выполняющий команды CLI.
 */
public class Executor {
    private final Environment environment;

    /**
     * Конструктор инициализирует Executor с доступом к переменным окружения.
     *
     * @param environment объект, управляющий переменными окружения
     */
    public Executor(Environment environment) {
        this.environment = environment;
    }

    /**
     * Выполняет переданную команду.
     *
     * @param command команда для выполнения
     */
    public void execute(Command command) {
        switch (command.getName()) {
            case "echo":
                executeEcho(command);
                break;
            case "cat":
                executeCat(command);
                break;
            case "wc":
                executeWc(command);
                break;
            case "pwd":
                executePwd();
                break;
            case "exit":
                System.exit(0);
                break;
            default:
                executeExternal(command);
                break;
        }
    }

    /**
     * Реализация команды `echo`.
     * Выводит аргументы команды в стандартный вывод.
     */
    private void executeEcho(Command command) {
        System.out.println(String.join(" ", command.getArguments()));
    }

    /**
     * Реализация команды `cat`.
     * Выводит содержимое указанного файла (или файлов).
     */
    private void executeCat(Command command) {
        if (command.getArguments().isEmpty()) {
            System.err.println("cat: missing file parameter");
            return;
        }
        for (String fileName : command.getArguments()) {
            try {
                System.out.println(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName))));
            } catch (IOException e) {
                System.err.println("cat: " + fileName + ": No such file");
            }
        }
    }

    /**
     * Реализация команды `wc`.
     * Выводит количество строк, слов, байтов в файле и название самого файла.
     */
    private void executeWc(Command command) {
        if (command.getArguments().isEmpty()) {
            System.err.println("wc: missing file parameter");
            return;
        }
        for (String fileName : command.getArguments()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)));
                String[] lines = content.split("\n");
                String[] words = content.split("\\s+");
                System.out.println(lines.length + " " + words.length + " " + content.length() + " " + fileName);
            } catch (IOException e) {
                System.err.println("wc: " + fileName + ": No such file");
            }
        }
    }

    /**
     * Реализация команды `pwd`.
     * Выводит текущую директорию.
     */
    private void executePwd() {
        System.out.println(System.getProperty("user.dir"));
    }

    /**
     * Запускает внешнюю команду через `ProcessBuilder`.
     */
    private void executeExternal(Command command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.getFullCommand());
            processBuilder.inheritIO();  // Наследует ввод/вывод от родительского процесса
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while executing command: " + e.getMessage());
        }
    }
}
