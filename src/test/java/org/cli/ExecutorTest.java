package org.cli;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
class ExecutorTest {

    private final Environment environment = new Environment();
    private final Executor executor = new Executor(environment);
    @Test
    void testLsCurrentDirectory() throws IOException {
        File testFile = new File("ls_test_file.txt");
        testFile.createNewFile();
        Command lsCommand = new Command("ls", List.of());
        String output = executor.execute(lsCommand, null);
        assertTrue(output.contains("ls_test_file.txt"));
        testFile.delete();
    }

    @Test
    void testLsSpecificDirectory() throws IOException {
        File testDir = new File("ls_test_dir");
        testDir.mkdir();
        File fileInDir = new File("ls_test_dir/file.txt");
        fileInDir.createNewFile();

        Command lsCommand = new Command("ls", List.of("ls_test_dir"));
        String output = executor.execute(lsCommand, null);
        assertTrue(output.contains("file.txt"));

        fileInDir.delete();
        testDir.delete();
    }

    @Test
    void testLsNonexistentDirectory() {
        Command lsCommand = new Command("ls", List.of("nonexistent"));
        String output = executor.execute(lsCommand, null);
        assertTrue(output.contains("ls: cannot access"));
    }

    @Test
    void testLsWithFileInsteadOfDirectory() throws IOException {
        File file = new File("ls_file.txt");
        file.createNewFile();
        Command lsCommand = new Command("ls", List.of("ls_file.txt"));
        String output = executor.execute(lsCommand, null);
        assertTrue(output.contains("ls:"));
        assertTrue(output.contains("Not a directory"));
        file.delete();
    }

    @Test
    void testLsTooManyArguments() {
        Command lsCommand = new Command("ls", List.of("dir1", "dir2"));
        String output = executor.execute(lsCommand, null);
        assertEquals("ls: too many arguments", output);
    }

    @Test
    void testCdToHome() {
        Command cdCommand = new Command("cd", List.of());
        String output = executor.execute(cdCommand, null);
        assertEquals("", output);
        assertEquals(System.getProperty("user.home"), environment.getCurrentDirectory());
    }

    @Test
    void testCdToExistingDirectory() throws IOException {
        File testDir = new File("testdir");
        testDir.mkdir();
        Command cdCommand = new Command("cd", List.of("testdir"));
        String output = executor.execute(cdCommand, null);
        assertEquals("", output);
        assertTrue(environment.getCurrentDirectory().endsWith("testdir"));
        testDir.delete();
    }

    @Test
    void testCdToNonexistentDirectory() {
        Command cdCommand = new Command("cd", List.of("nonexistent"));
        String output = executor.execute(cdCommand, null);
        assertTrue(output.contains("cd: nonexistent: No such file or directory"));
    }

    @Test
    void testCdToFileInsteadOfDirectory() throws IOException {
        File file = new File("notadir.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Just a file");
        }
        Command cdCommand = new Command("cd", List.of("notadir.txt"));
        String output = executor.execute(cdCommand, null);
        assertTrue(output.contains("cd: notadir.txt: Not a directory"));
        file.delete();
    }

    @Test
    void testCdTooManyArguments() {
        Command cdCommand = new Command("cd", List.of("dir1", "dir2"));
        String output = executor.execute(cdCommand, null);
        assertEquals("cd: too many arguments", output);
    }

    @Test
    void testCdThenPwd() throws IOException {
        File testDir = new File("cd_then_pwd_dir");
        testDir.mkdir();

        // Run `cd cd_then_pwd_dir`
        Command cdCommand = new Command("cd", List.of("cd_then_pwd_dir"));
        String cdOutput = executor.execute(cdCommand, null);
        assertEquals("", cdOutput);

        // Run `pwd` in the new directory
        Command pwdCommand = new Command("pwd", List.of());
        String pwdOutput = executor.execute(pwdCommand, null);
        assertEquals(testDir.getCanonicalPath(), pwdOutput.trim());

        testDir.delete();
    }

    @Test
    void testCdThenLs() throws IOException {
        // Setup new directory and a file inside it
        File testDir = new File("cd_then_ls_dir");
        testDir.mkdir();
        File file = new File(testDir, "testfile.txt");
        file.createNewFile();

        // Run `cd cd_then_ls_dir`
        Command cdCommand = new Command("cd", List.of("cd_then_ls_dir"));
        executor.execute(cdCommand, null);

        // Run `ls` — should list the file in new directory
        Command lsCommand = new Command("ls", List.of());
        String output = executor.execute(lsCommand, null);
        assertTrue(output.contains("testfile.txt"));

        file.delete();
        testDir.delete();
    }



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
        assertTrue(output.contains("Error while executing command"));
    }

    @Test
    void testLsCommand() {
        Command lsCommand = new Command("ls", List.of());
        String output = executor.execute(lsCommand, null);
        assertNotNull(output);
        assertFalse(output.isEmpty());
    }
}