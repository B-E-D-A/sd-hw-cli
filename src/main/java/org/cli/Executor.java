package org.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


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
     * @param input   входные данные для команды (может быть null)
     * @return вывод команды
     */
    public String execute(Command command, String input) {
        return switch (command.getName()) {
            case "echo" -> executeEcho(command, input);
            case "cat" -> executeCat(command, input);
            case "wc" -> executeWc(command, input);
            case "pwd" -> executePwd();
            case "exit" -> {
                System.exit(0);  // Завершаем выполнение приложения с кодом 0
                yield "";
            }
            case "set" -> executeSet(command, input);
            case "grep" -> executeGrep(command, input);
            default -> executeExternal(command, input);  // Для внешних команд используется ProcessBuilder
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
                args.set(i, environment.getVariable(varName));  // Заменяем переменную на её значение
            }
        }
        return String.join(" ", args);  // Объединяем аргументы в одну строку, разделённую пробелами
    }

    /**
     * Реализация команды `cat`.
     * Выводит содержимое указанного файла (или файлов).
     * Если файл не найден, выводится ошибка.
     */
    private String executeCat(Command command, String input) {
        if (input != null) {
            return input;
        }

        if (command.getArguments().isEmpty()) {
            return "ERROR: cat: missing file parameter"; // Ошибка при отсутствии аргумента для файла
        }

        StringBuilder output = new StringBuilder();
        for (String fileName : command.getArguments()) {
            try {
                output.append(new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)))).append("\n");
            } catch (IOException e) {
                output.append("ERROR: cat: ").append(fileName).append(": No such file");  // Если файл не найден
            }
        }
        return output.toString();
    }

    /**
     * Реализация команды wc.
     * Выводит количество строк, слов, байтов в файле и название самого файла.
     * Если файл не найден, выводится ошибка.
     */
    private String executeWc(Command command, String input) {
        if (input != null) {
            String[] lines = input.split("\n");
            String[] words = input.split("\\s+");
            return lines.length + " " + words.length + " " + input.length();  // Подсчитываем строки, слова и байты
        }

        if (command.getArguments().isEmpty()) {
            return "ERROR: wc: missing file parameter";  // Ошибка при отсутствии аргумента для файла
        }

        StringBuilder output = new StringBuilder();
        for (String fileName : command.getArguments()) {
            try {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(fileName)));
                String[] lines = content.split("\n");
                String[] words = content.split("\\s+");
                output.append(lines.length).append(" ").append(words.length).append(" ").append(content.length()).append(" ").append(fileName).append("\n");
            } catch (IOException e) {
                output.append("ERROR: wc: ").append(fileName).append(": No such file\n");   // Ошибка при отсутствии файла
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
            ProcessBuilder processBuilder = new ProcessBuilder(command.getFullCommand()); // Создаем процесс для выполнения внешней команды
            processBuilder.redirectErrorStream(true);  // Объединяем стандартный и поток ошибок
            Process process = processBuilder.start();

            // Если есть входные данные — передаем их в стандартный ввод процесса
            if (input != null) {
                try (OutputStream os = process.getOutputStream()) {
                    os.write(input.getBytes()); // Записываем вход в поток
                    os.flush();
                }
            }

            // Читаем вывод из стандартного вывода процесса
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (InputStream is = process.getInputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                // Читаем поток до конца
                while ((length = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            process.waitFor();  // Ожидаем завершения процесса
            return outputStream.toString(); // Возвращаем вывод команды
        } catch (IOException | InterruptedException e) {
            return "ERROR: " + command.getName() + ": " + e.getMessage(); // Возвращаем ошибку в случае исключения
        }
    }

    /**
     * Реализация команды `set`.
     * Устанавливает переменные окружения в CLI.
     * Если синтаксис неправильный (например, отсутствует знак "="), выводится ошибка.
     */
    private String executeSet(Command command, String input) {
        if (command.getArguments().isEmpty()) {
            return "ERROR: set: missing variable name or value"; // Ошибка при отсутствии аргумента
        }
        String arg = command.getArguments().get(0);

        String[] parts = arg.split("=", 2);
        if (parts.length < 2) {
            return "ERROR: set: invalid syntax. Use: set VAR_NAME=value"; // Ошибка при неправильном синтаксисе
        }

        String varName = parts[0];
        String varValue = parts[1];
        environment.setVariable(varName, varValue); // Устанавливаем переменную окружения
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
                .acceptUnknownOptions(false)
                .build();

        try {
            jc.parse(command.getArguments().toArray(new String[0]));
            for (String arg : params.getRawParameters()) {
                if (arg.startsWith("-")) {
                    throw new ParameterException("Unknown option: " + arg); //исключение, если пользователь ввел флаг -unknown, который не поддерживается в нашем CLI
                }
            }
            if (command.isLiteralMatch()) {
                params.setLiteralMatch(true); // Устанавливаем режим буквального совпадения
            }
            return grepHandler.execute(params, input); // Выполняем поиск с параметрами
        } catch (ParameterException e) {
            return "ERR: grep: " + e.getMessage(); // Возвращаем ошибку при неправильных параметрах
        }
    }
}
