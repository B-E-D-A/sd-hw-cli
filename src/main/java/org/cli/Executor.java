package org.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


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
            default -> executeExternal(command, input);
        };
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
                output.append(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)))).append("\n");
            } catch (IOException e) {
                output.append("cat: ").append(fileName).append(": No such file\n");
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
        return System.getProperty("user.dir");
    }

    /**
     * Запускает внешнюю команду через `ProcessBuilder`.
     */
    private String executeExternal(Command command, String input) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.getFullCommand());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            if (input != null) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(input.getBytes());
                    os.flush();
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            process.waitFor();
            return outputStream.toString();
        } catch (IOException | InterruptedException e) {
            return "Error while executing command: " + e.getMessage();
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
}
