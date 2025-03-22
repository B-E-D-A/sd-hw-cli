package org.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PipelineTest {

    private Environment environment;
    private Executor executor;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        executor = new Executor(environment);
    }

    @Test
    void testSingleCommandPipeline() {
        List<Command> commands = List.of(new Command("echo", List.of("Hello, World!")));
        Pipeline pipeline = new Pipeline(commands);

        String result = pipeline.execute(environment);

        assertEquals("Hello, World!", result.trim());
    }

    @Test
    void testPipelineWithTwoCommands() {
        List<Command> commands = List.of(
                new Command("echo", List.of("Hello, World!")),
                new Command("wc", List.of())
        );
        Pipeline pipeline = new Pipeline(commands);

        String result = pipeline.execute(environment);

        assertEquals("1 2 13", result.trim());
    }
}