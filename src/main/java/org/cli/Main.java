package org.cli;

import java.util.List;
import java.util.Scanner;

/**
 * Главный класс CLI-интерпретатора, принимает ввод пользователя
 */
public class Main {
    public static void main(String[] args) {
        Environment environment = new Environment();
        Parser parser = new Parser(environment);
        Executor executor = new Executor(environment);

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                try {
                    List<Command> commands = parser.parse(input);
                    for (Command command : commands) {
                        executor.execute(command);
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
