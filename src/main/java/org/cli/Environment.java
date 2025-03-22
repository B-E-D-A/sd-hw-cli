package org.cli;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, String> variables;

    public Environment() {
        this.variables = new HashMap<>(System.getenv());
    }

    public String getVariable(String name) {
        return variables.getOrDefault(name, "\"\"");
    }

    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }
}
