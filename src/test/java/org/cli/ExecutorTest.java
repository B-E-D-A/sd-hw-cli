package org.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutorTest {

    private final Environment environment = new Environment();
    private final Executor executor = new Executor(environment);
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    void testExecuteCat() throws IOException {
        String fileName = "testfile.txt";
        String content = "Hello, World!\nThis is a test file.";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
        Command catCommand = new Command("cat", List.of(fileName));
        executor.execute(catCommand);
        assertTrue(outContent.toString().contains("Hello, World!"));
        assertTrue(outContent.toString().contains("This is a test file."));
        new File(fileName).delete();
    }

    @Test
    void testExecuteCatWithMissingFile() {
        Command catCommand = new Command("cat", List.of("nonexistentfile.txt"));
        executor.execute(catCommand);
        assertTrue(errContent.toString().contains("cat: nonexistentfile.txt: No such file"));
    }

    @Test
    void testExecuteWc() throws IOException {
        String fileName = "testfile.txt";
        String content = "Hello, World!\nThis is a test file.";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }

        Command wcCommand = new Command("wc", List.of(fileName));
        executor.execute(wcCommand);
        String output = outContent.toString();
        assertTrue(output.contains("2 7"));
        assertTrue(output.contains("testfile.txt"));

        new File(fileName).delete();
    }

    @Test
    void testExecuteWcWithMissingFile() {
        Command wcCommand = new Command("wc", List.of("nonexistentfile.txt"));
        executor.execute(wcCommand);
        assertTrue(errContent.toString().contains("wc: nonexistentfile.txt: No such file"));
    }

    @Test
    void testEchoCommand() {
        Environment env = new Environment();
        Executor executor = new Executor(env);

        Command echoCommand = new Command("echo", Arrays.asList("Hello", "World"));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        executor.execute(echoCommand);

        assertEquals("Hello World" + System.lineSeparator(), outContent.toString());
    }

    @Test
    void testPwdCommand() {
        Environment env = new Environment();
        Executor executor = new Executor(env);

        Command pwdCommand = new Command("pwd", List.of());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        executor.execute(pwdCommand);

        assertTrue(outContent.toString().trim().endsWith(System.getProperty("user.dir")));
    }

    @Test
    void testUnknownCommand() {
        Environment env = new Environment();
        Executor executor = new Executor(env);

        Command unknownCommand = new Command("unknown_command", List.of());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));

        executor.execute(unknownCommand);

        assertTrue(errContent.toString().contains("Error while executing command"));
    }
}
