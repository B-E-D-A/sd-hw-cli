package org.cli;

import java.util.List;
import java.util.Scanner;

/**
 * Главный класс CLI-интерпретатора, принимает ввод пользователя
 */
public class Main {
    public static void main(String[] args) {
        Environment environment = new Environment();
        Executor executor = new Executor(environment);
        Parser parser = new Parser(environment, executor);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break; // Если нет ввода, выходим из цикла
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue; // Если ввод пустой, пропускаем его
                }
                if ("exit".equalsIgnoreCase(input)) {
                    break; // Если введено "exit", выходим из программы
                }
                try {
                    List<Command> commands = parser.parse(input); // Парсим команду, введенную пользователем
                    Pipeline pipeline = new Pipeline(commands); // Создаем пайплайн для последовательного выполнения команд
                    String output = pipeline.execute(environment); // Выполняем пайплайн и получаем вывод

                    // Если есть вывод, печатаем его
                    if (output != null) {
                        System.out.print(output);
                        if (!output.isEmpty()) {
                            System.out.print("\n"); // Добавляем новую строку, если вывод не пустой
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
