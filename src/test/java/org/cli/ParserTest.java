package org.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {

    private Parser parser;
    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.setVariable("USER", "Alice");
        parser = new Parser(environment);
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
        assertEquals(List.of("Alice"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithMultipleEnvironmentVariables() {
        environment.setVariable("HOST", "localhost");
        List<Command> commands = parser.parse("echo $USER@$HOST");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of("Alice@localhost"), commands.get(0).getArguments());
    }

    @Test
    void testParseCommandWithUnknownVariable() {
        List<Command> commands = parser.parse("echo $UNKNOWN");
        assertEquals(1, commands.size());
        assertEquals("echo", commands.get(0).getName());
        assertEquals(List.of(""), commands.get(0).getArguments());
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
        assertEquals(1, commands.size()); // Пока `Pipeline` не реализован, команды в `Parser` не разделяются по `|`
        assertEquals("cat", commands.get(0).getName());
        assertEquals(List.of("file.txt", "|", "wc"), commands.get(0).getArguments());
    }
}
