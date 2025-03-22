package org.cli;

import java.util.List;

public class Pipeline {
    private final List<Command> commands;

    public Pipeline(List<Command> commands) {
        this.commands = commands;
    }

    public String execute(Environment environment) {
        String input = null;
        Executor executor = new Executor(environment);

        for (Command command : commands) {
            input = executor.execute(command, input);
        }

        return input;
    }
}