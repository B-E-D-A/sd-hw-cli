package org.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Класс, выполняющий команды CLI.
 */
public class Executor {
    private final Environment environment;
    private final GrepHandler grepHandler;

    public Executor(Environment environment) {
        this.environment = environment;
        this.grepHandler = new GrepHandler(environment);
    }

    /**
     * Выполняет переданную команду.
     *
     * @param command команда для выполнения
     * @param input входные данные для команды (может быть null)
     * @return вывод команды
     */
    public String execute(Command command, String input) {
        return switch (command.getName()) {
            case "echo" -> executeEcho(command, input);
            case "cat" -> executeCat(command, input);
            case "wc" -> executeWc(command, input);
            case "pwd" -> executePwd();
            case "exit" -> {
                System.exit(0);
                yield "";
            }
            case "set" -> executeSet(command, input);
            case "grep" -> executeGrep(command, input);
            // Добавлены новые комманды
            case "ls" -> executeLs(command, input);
            case "cd" -> executeCd(command, input);
            default -> executeExternal(command, input);
        };
    }

    /**
     * Реализация команды `cd`.
     * используется для изменения текущей директории
     */
    private String executeCd(Command command, String input) {
        if (command.getArguments().size() > 1) {
            return "cd: too many arguments";
        }

        String target = command.getArguments().isEmpty()
                ? System.getProperty("user.home")
                : command.getArguments().get(0);

        try {
            // Меняем пути в соответствии с рабочей директорией
            Path newPath = resolvePath(target);

            if (!Files.exists(newPath)) {
                return "cd: " + target + ": No such file or directory";
            }
            if (!Files.isDirectory(newPath)) {
                return "cd: " + target + ": Not a directory";
            }

            environment.setCurrentDirectory(newPath.toString());
            return "";
        } catch (InvalidPathException e) {
            return "cd: " + target + ": Invalid path";
        } catch (Exception e) {
            return "cd: " + e.getMessage();
        }
    }

    /**
     * Реализация команды `ls`.
     * используется для вывода списка файлов и каталогов в текущем каталоге или указанном каталоге.  
     */
    private String executeLs(Command command, String input) {
        if (command.getArguments().size() > 1) {
            return "ls: too many arguments";
        }

        // Меняем пути в соответствии с рабочей директорией
        Path dir = command.getArguments().isEmpty()
                ? Paths.get(environment.getCurrentDirectory())
                : resolvePath(command.getArguments().get(0));

        try {
            if (!Files.exists(dir)) {
                return "ls: cannot access '" + dir + "': No such file or directory";
            }
            if (!Files.isDirectory(dir)) {
                return "ls: '" + dir + "': Not a directory";
            }

            return Files.list(dir)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.joining(" "));
        } catch (IOException e) {
            return "ls: " + e.getMessage();
        }
    }

    /**
     * Реализация команды `echo`.
     * Выводит аргументы команды в стандартный вывод.
     */
    private String executeEcho(Command command, String input) {
        List<String> args = command.getArguments();
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i).startsWith("$")) {
                String varName = args.get(i).substring(1);
                args.set(i, environment.getVariable(varName));
            }
        }
        return String.join(" ", args);
    }

    /**
     * Реализация команды `cat`.
     * Выводит содержимое указанного файла (или файлов).
     */
    private String executeCat(Command command, String input) {
        if (input != null) {
            return input;
        }

        if (command.getArguments().isEmpty()) {
            return "cat: missing file parameter";
        }

        StringBuilder output = new StringBuilder();
        for (String fileName : command.getArguments()) {
            try {
                // Меняем пути в соответствии с рабочей директорией
                Path filePath = resolvePath(fileName);
                output.append(Files.readString(filePath)).append("\n");
            } catch (NoSuchFileException e) {
                output.append("cat: ").append(fileName).append(": No such file\n");
            } catch (IOException e) {
                output.append("cat: ").append(fileName).append(": ").append(e.getMessage()).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * Реализация команды wc.
     * Выводит количество строк, слов, байтов в файле и название самого файла.
     */
    private String executeWc(Command command, String input) {
        if (input != null) {
            String[] lines = input.split("\n");
            String[] words = input.split("\\s+");
            return lines.length + " " + words.length + " " + input.length();
        }

        if (command.getArguments().isEmpty()) {
            return "wc: missing file parameter";
        }

        StringBuilder output = new StringBuilder();
        for (String fileName : command.getArguments()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)));
                String[] lines = content.split("\n");
                String[] words = content.split("\\s+");
                output.append(lines.length).append(" ").append(words.length).append(" ").append(content.length()).append(" ").append(fileName).append("\n");
            } catch (IOException e) {
                output.append("wc: ").append(fileName).append(": No such file\n");
            }
        }
        return output.toString();
    }

    /**
     * Реализация команды `pwd`.
     * Выводит текущую директорию.
     */
    private String executePwd() {
        // Получаем дирректорию без обращения к системи используя Environment
        return environment.getCurrentDirectory();
    }

    /**
     * Запускает внешнюю команду через `ProcessBuilder`.
     */
    private String executeExternal(Command command, String input) {
        try {
            // меняем пути в соответствии с текущей рабочей директорией
            Process process = new ProcessBuilder(command.getFullCommand())
                    .directory(Paths.get(environment.getCurrentDirectory()).toFile())
                    .start();

            if (input != null) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(input.getBytes());
                }
            }

            return new String(process.getInputStream().readAllBytes());
        } catch (IOException e) {
            return "Error while executing command";
        }
    }

    private String executeSet(Command command, String input) {
        if (command.getArguments().isEmpty()) {
            return "set: missing variable name or value";
        }

        String arg = command.getArguments().get(0);

        String[] parts = arg.split("=", 2);

        if (parts.length < 2) {
            return "set: invalid syntax. Use: set VAR_NAME=value";
        }

        String varName = parts[0];
        String varValue = parts[1];

        environment.setVariable(varName, varValue);
        return "";
    }


    /**
     * Реализация команды `grep`.
     * Ищет строки, соответствующие заданному шаблону.
     */
    private String executeGrep(Command command, String input) {
        GrepParameters params = new GrepParameters();
        JCommander jc = JCommander.newBuilder()
                .addObject(params)
                .build();

        try {
            jc.parse(command.getArguments().toArray(new String[0]));
            return grepHandler.execute(params, input);
        } catch (ParameterException e) {
            return "grep: " + e.getMessage();
        }
    }


    /**
     * Преобразует путь с учетом текущей директории.
     */
    private Path resolvePath(String path) {
        Path p = Paths.get(path);
        if (p.isAbsolute()) {
            return p;
        }
        if (path.startsWith("~")) {
            return Paths.get(System.getProperty("user.home"), path.substring(1));
        }
        return Paths.get(environment.getCurrentDirectory(), path);
    }
}
