package org.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GrepHandlerTest {

    private final Environment environment = new Environment();
    private final Executor executor = new Executor(environment);

    @Test
    void testGrepBasicSearch(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Минимальный синтаксис grep\nДругая строка");
        }

        Command grepCommand = new Command("grep", List.of("\"Минимальный\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("Минимальный синтаксис grep\n", output);
    }

    @Test
    void testGrepEndOfLine(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Минимальный синтаксис grep\nДругая строка");
        }

        Command grepCommand = new Command("grep", List.of("\"синтаксис$\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("", output);
    }

    @Test
    void testGrepStartOfLine(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Минимальный синтаксис grep\nДругая строка");
        }

        Command grepCommand = new Command("grep", List.of("\"^Минимальный\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("Минимальный синтаксис grep\n", output);
    }

    @Test
    void testGrepCaseInsensitive(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Минимальный синтаксис grep\nДругая строка");
        }

        Command grepCommand = new Command("grep", List.of("-i", "\"минимальный\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("Минимальный синтаксис grep\n", output);
    }

    @Test
    void testGrepWholeWord(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Минимальный синтаксис grep\nДругая строка");
        }

        Command grepCommand = new Command("grep", List.of("-w", "\"Минимал\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("", output);
    }

    @Test
    void testGrepAfterContext(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Строка 1\nСтрока II\nСтрока 3\nСтрока 4");
        }

        Command grepCommand = new Command("grep", List.of("-A", "1", "\"II\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("Строка II\nСтрока 3\n", output);
    }

    @Test
    void testGrepOverlappingContexts(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Строка A\nСтрока B\nСтрока A\nСтрока C\nСтрока D");
        }

        Command grepCommand = new Command("grep", List.of("-A", "2", "\"A\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertEquals("Строка A\nСтрока B\nСтрока A\nСтрока C\nСтрока D\n", output);
    }

    @Test
    void testGrepPipelineInput() {
        String input = "Строка 1\nСтрока 2\nСтрока 3";
        Command grepCommand = new Command("grep", List.of("\"2\""));
        String output = executor.execute(grepCommand, input);
        assertEquals("Строка 2\n", output);
    }

    @Test
    void testGrepInvalidPattern(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("test.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("Тестовая строка");
        }

        Command grepCommand = new Command("grep", List.of("\"[invalid\"", testFile.getPath()));
        String output = executor.execute(grepCommand, null);
        assertTrue(output.startsWith("grep: invalid pattern"));
    }

    @Test
    void testGrepMissingFile() {
        Command grepCommand = new Command("grep", List.of("\"pattern\"", "nonexistent.txt"));
        String output = executor.execute(grepCommand, null);
        assertTrue(output.startsWith("grep: nonexistent.txt"));
    }

    @Test
    void testGrepMissingPattern() {
        Command grepCommand = new Command("grep", List.of());
        String output = executor.execute(grepCommand, null);
        assertEquals("grep: Main parameters are required (\"pattern [file...]\")", output);
    }

    @Test
    void testGrepMixedCase() {
        String input = "МИНИМАЛЬНЫЙ Синтаксис\nДругая строка";
        Command cmd = new Command("grep", List.of("-i", "синтаксис"));
        String result = executor.execute(cmd, input);
        assertEquals("МИНИМАЛЬНЫЙ Синтаксис\n", result);
    }

    @Test
    void testGrepSpecialChars() {
        String input = "Line with (special) chars\nAnother line";
        Command cmd = new Command("grep", List.of("(special)"));
        String result = executor.execute(cmd, input);
        assertEquals("Line with (special) chars\n", result);
    }

    @Test
    void testGrepWithRussianINN(@TempDir Path tempDir) throws IOException {
        File testFile = tempDir.resolve("inn.txt").toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write(
                    """
                            ИНН организаций и физлиц:
                            ООО Компания: 1234567890
                            ИП Иванов: 123456789012
                            Невалидный номер: 12-34-567890"""
            );
        }

        List<Command> commands = List.of(
                new Command("cat", List.of(testFile.getAbsolutePath())),
                new Command("grep", List.of("\\b\\d{10}\\b|\\b\\d{12}\\b"))
        );
        Pipeline pipeline = new Pipeline(commands);
        String result = pipeline.execute(environment).trim();

        assertTrue(result.contains("1234567890"));
        assertTrue(result.contains("123456789012"));
        assertFalse(result.contains("12-34-567890"));
    }
}