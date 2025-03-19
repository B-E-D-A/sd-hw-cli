package org.cli;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandTest {

    @Test
    void testCommandCreation() {
        List<String> args = Arrays.asList("file1.txt", "file2.txt");
        Command command = new Command("cat", args);
        assertEquals("cat", command.getName());
        assertEquals(args, command.getArguments());
    }

    @Test
    void testGetFullCommand() {
        List<String> args = Arrays.asList("file1.txt", "file2.txt");
        Command command = new Command("cat", args);
        List<String> fullCommand = command.getFullCommand();
        assertEquals(Arrays.asList("cat", "file1.txt", "file2.txt"), fullCommand);
    }
}
