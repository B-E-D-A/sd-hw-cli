package org.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления переменными окружения CLI.
 */
public class Environment {
    private final Map<String, String> variables;

    public Environment() {
        this.variables = new HashMap<>(System.getenv());
    }

    /**
     * Получает значение переменной окружения.
     *
     * @param name имя переменной
     * @return значение переменной или пустая строка, если переменная отсутствует
     */
    public String getVariable(String name) {
        return variables.getOrDefault(name, "\"\"");
    }

    /**
     * Устанавливает новую переменную окружения или изменяет существующую.
     *
     * @param name  имя переменной
     * @param value значение переменной
     */
    public void setVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Возвращает копию всех переменных окружения.
     */
    public Map<String, String> getVariables() {
        return new HashMap<>(variables);
    }
}
