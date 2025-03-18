package org.cli;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentTest {

    @Test
    void testSetAndGetVariable() {
        Environment env = new Environment();
        env.setVariable("TEST_VAR", "Hello");
        assertEquals("Hello", env.getVariable("TEST_VAR"));
    }

    @Test
    void testGetNonExistingVariable() {
        Environment env = new Environment();
        assertEquals("", env.getVariable("UNKNOWN_VAR"));
    }

    @Test
    void testEnvironmentVariables() {
        Environment env = new Environment();
        env.setVariable("A", "1");
        env.setVariable("B", "2");
        Map<String, String> vars = env.getVariables();
        assertEquals("1", vars.get("A"));
        assertEquals("2", vars.get("B"));
    }
}
