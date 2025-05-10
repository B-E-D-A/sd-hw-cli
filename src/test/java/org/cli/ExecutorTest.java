package org.cli;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorTest {

    private final Environment environment = new Environment();
    private final Executor executor = new Executor(environment);

    @Test
    void testExecuteCat() throws IOException {
        String fileName = "testfile.txt";
        String content = "Hello, World!\nThis is a test file.";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
        Command catCommand = new Command("cat", List.of(fileName));
        String output = executor.execute(catCommand, null);
        assertTrue(output.contains("Hello, World!"));
        assertTrue(output.contains("This is a test file."));

        new File(fileName).delete();
    }

    @Test
    void testExecuteCatWithMissingFile() {
        Command catCommand = new Command("cat", List.of("nonexistentfile.txt"));
        String output = executor.execute(catCommand, null);
        assertTrue(output.contains("cat: nonexistentfile.txt: No such file"));
    }

    @Test
    void testExecuteWc() throws IOException {
        String fileName = "testfile.txt";
        String content = "Hello, World!\nThis is a test file.";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }

        Command wcCommand = new Command("wc", List.of(fileName));
        String output = executor.execute(wcCommand, null);
        assertTrue(output.contains("2 7"));
        assertTrue(output.contains("testfile.txt"));

        new File(fileName).delete();
    }

    @Test
    void testExecuteWcWithMissingFile() {
        Command wcCommand = new Command("wc", List.of("nonexistentfile.txt"));
        String output = executor.execute(wcCommand, null);
        assertTrue(output.contains("wc: nonexistentfile.txt: No such file"));
    }

    @Test
    void testEchoCommand() {
        Command echoCommand = new Command("echo", Arrays.asList("Hello", "World"));
        String output = executor.execute(echoCommand, null);
        assertEquals("Hello World", output.trim());
    }

    @Test
    void testPwdCommand() {
        Command pwdCommand = new Command("pwd", List.of());
        String output = executor.execute(pwdCommand, null);
        assertEquals(System.getProperty("user.dir"), output.trim());
    }

    @Test
    void testUnknownCommand() {
        Command unknownCommand = new Command("unknown_command", List.of());
        String output = executor.execute(unknownCommand, null);
        assertTrue(output.contains("ERROR: unknown_command:"));
    }

    @Test
    void testLsCommand() {
        Command lsCommand = new Command("ls", List.of());
        String output = executor.execute(lsCommand, null);
        assertNotNull(output);
        assertFalse(output.isEmpty());
    }
}