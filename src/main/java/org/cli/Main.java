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
                    String output = null;
                    for (Command command : commands) {
                        output = executor.execute(command, output);
                    }
                    if (output != null) {
                        System.out.print(output);
                        if (output != "") {
                            System.out.print("\n");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
