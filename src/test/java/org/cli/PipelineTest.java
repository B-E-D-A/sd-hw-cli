package org.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void testGrepAfterEcho() {
        List<Command> commands = List.of(
                new Command("echo", List.of("Line1\nLine2\nLine3")),
                new Command("grep", List.of("Line2"))
        );
        Pipeline pipeline = new Pipeline(commands);

        String result = pipeline.execute(environment);

        assertEquals("Line2", result.trim());
    }

    @Test
    void testComplexPipelineWithGrep(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("data.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Error: something went wrong\nWarning: minor issue\nInfo: everything ok");
        }

        List<Command> commands = List.of(
                new Command("cat", List.of(testFile.getAbsolutePath())),
                new Command("grep", List.of("-i", "error|warning")),
                new Command("wc", List.of())
        );
        Pipeline pipeline = new Pipeline(commands);

        String result = pipeline.execute(environment);

        assertTrue(result.contains("2 7"));
    }
}