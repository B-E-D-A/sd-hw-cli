package org.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationTest {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final String testFileName = "integration_test_file.txt";

    @BeforeEach
    void setUp() throws IOException {
        System.setOut(new PrintStream(outputStream));
        String testFileContent = "Line 1\nLine 2\nLine 3\n";
        Files.write(Path.of(testFileName), testFileContent.getBytes());
    }

    @AfterEach
    void tearDown() throws IOException {
        System.setOut(originalOut);
        Files.deleteIfExists(Path.of(testFileName));
    }

    @Test
    void testEchoAfterFailedCat() {
        String input = "cat non_existent_file.txt\necho Hello\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("ERROR: cat: non_existent_file.txt: No such file"));
        assertTrue(output.contains("Hello"));
    }

    @Test
    void testSetAndEchoVariable() {
        String input = "set VAR=test_value\necho $VAR\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("test_value"));
    }

    @Test
    void testMixedSuccessAndErrorCommands() {
        String input = "echo First\nwc no_such_file.txt\nset VAR=value\necho $VAR\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("First"));
        assertTrue(output.contains("ERROR: wc: no_such_file.txt: No such file"));
        assertTrue(output.contains("value"));
    }

    @Test
    void testExitSkipsFollowingCommands() {
        String input = "echo Before\nexit\necho After\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Before"));
        assertFalse(output.contains("After"));
    }

    @Test
    void testSequentialExecutionWithErrors() {
        String input = String.join("\n",
                "set VAR=hello",
                "echo $VAR",
                "cat no_such_file.txt",
                "echo world",
                "set NUM=123",
                "echo $NUM",
                "wc",
                "echo end"
        ) + "\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("hello"));
        assertTrue(output.contains("ERROR: cat: no_such_file.txt: No such file"));
        assertTrue(output.contains("world"));
        assertTrue(output.contains("123"));
        assertTrue(output.contains("ERROR: wc: missing file parameter"));
        assertTrue(output.contains("end"));
    }

    @Test
    void testVariableAvailableAfterError() {
        String input = String.join("\n",
                "set FOO=bar",
                "cat missing.txt",
                "echo $FOO"
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});
        String output = outputStream.toString();

        assertTrue(output.contains("ERROR: cat: missing.txt: No such file"));
        assertTrue(output.contains("bar"));
    }

    @Test
    void testPwdAndEchoInSequence() {
        String input = String.join("\n",
                "pwd",
                "echo Hello"
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});
        String output = outputStream.toString();

        assertTrue(output.contains(System.getProperty("user.dir")));
        assertTrue(output.contains("Hello"));
    }

    @Test
    void testIgnoresMultipleEmptyInputs() {
        String input = "\n\n\n" +
                "echo Done\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Done"));
    }

    @Test
    void testPipelineStopsOnFirstError() {
        String input = "cat no_such_file.txt | wc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("ERROR: cat: no_such_file.txt: No such file"));
        assertFalse(output.contains("0 0"));
    }

    @Test
    void testPipelineSkipsAfterMiddleError() {
        String input = "echo Hello | grep -invalid-flag | wc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("ERR: grep: Unknown option:"));
        assertFalse(output.contains("1 1"));
    }

    @Test
    void testGrepInPipeline() {
        String input = "cat " + testFileName + " | grep -i line | wc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("3 6"));
    }

    @Test
    void testComplexPipelineWithErrorAndVariable() {
        String input = "cat non_existent.txt | wc\nset FILE=" + testFileName + "\ncat $FILE | grep 2 | wc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("ERROR: cat: non_existent.txt: No such file"));
        assertTrue(output.contains("1 2"));
    }

    @Test
    void testGrepAfterEcho() {
        String input = "echo Hello | grep Hello\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Hello"));
    }

    @Test
    void testWcCountsOutputFromCat() {
        String input = "cat " + testFileName + " | wc\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("3 6"));
    }

    @Test
    void testPipelineUsesVariable() throws IOException {
        String file = "temp_input.txt";
        Path path = Path.of(file);
        Files.write(path, "data here".getBytes());

        String input = String.join("\n",
                "set FILE=" + file,
                "cat $FILE | wc"
        ) + "\n";

        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});
        String output = outputStream.toString();

        assertTrue(output.contains("1 2"));
        Files.deleteIfExists(path);
    }

    @Test
    void testGrepMatchesWholeWordInPipeline() {
        String input = "echo apple pineapple grape\n | grep -w apple\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("apple"));
    }

}