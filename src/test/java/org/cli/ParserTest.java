package org.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Parser parser;
    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.setVariable("USER", "Katya");
        Executor executor = new Executor(environment);
        parser = new Parser(environment, executor);
    }

    @Test
    void testParseSimpleCommand() {
        List<Command> commands = parser.parse("echo Hello World");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Hello", "World"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithDoubleQuotes() {
        List<Command> commands = parser.parse("echo \"Hello, World!\"");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Hello, World!"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithSingleQuotes() {
        List<Command> commands = parser.parse("echo 'Hello, World!'");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Hello, World!"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithMixedQuotes() {
        List<Command> commands = parser.parse("echo 'Hello' \"World!\"");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Hello", "World!"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithEnvironmentVariable() {
        List<Command> commands = parser.parse("echo $USER");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Katya"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithMultipleEnvironmentVariables() {
        environment.setVariable("HOST", "localhost");
        List<Command> commands = parser.parse("echo $USER@$HOST");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Katya@localhost"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithUnknownVariable() {
        List<Command> commands = parser.parse("echo $UNKNOWN");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
    }

    @Test
    void testParseEmptyInput() {
        List<Command> commands = parser.parse("");
        assertTrue(commands.isEmpty(), "Parser should return an empty list for empty input.");
    }

    @Test
    void testParseWhitespaceInput() {
        List<Command> commands = parser.parse("   ");
        assertTrue(commands.isEmpty(), "Parser should return an empty list for whitespace input.");
    }

    @Test
    void testParseCommandWithPipes() {
        List<Command> commands = parser.parse("cat file.txt | wc");
        assertEquals(2, commands.size());
        assertEquals("cat", commands.get(0).getName());
        assertEquals(List.of("file.txt"), commands.get(0).getArguments());
        assertEquals("wc", commands.get(1).getName());
        assertTrue(commands.get(1).getArguments().isEmpty());
    }
}