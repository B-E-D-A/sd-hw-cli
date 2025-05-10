package org.cli;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.util.ArrayList;
import java.util.List;

/**
 * Параметры команды grep для парсинга JCommander.
 */
public class GrepParameters {
    private boolean literalMatch = false;

    @Parameter(names = {"-w", "--word-regexp"}, description = "Search for whole words only")
    private boolean wholeWord = false;

    @Parameter(names = {"-i", "--ignore-case"}, description = "Case insensitive search")
    private boolean ignoreCase = false;

    @Parameter(names = {"-A", "--after-context"},
            description = "Print NUM lines after match",
            validateWith = PositiveIntegerValidator.class)
    private int afterContext = 0;

    @Parameter(description = "pattern [file...]", required = true)
    private List<String> parameters = new ArrayList<>();

    /** Валидатор для положительных чисел */
    public static class PositiveIntegerValidator implements IParameterValidator {
        @Override
        public void validate(String name, String value) throws ParameterException {
            try {
                int num = Integer.parseInt(value);
                if (num < 0) {
                    throw new ParameterException("Parameter " + name +
                            " should be positive (found " + value + ")");
                }
            } catch (NumberFormatException e) {
                throw new ParameterException("Parameter " + name +
                        " must be an integer (found " + value + ")");
            }
        }
    }

    /** Setters **/
    public void setLiteralMatch(boolean literalMatch) {
        this.literalMatch = literalMatch;
    }

    /** Getters **/
    public boolean isWholeWord() { return wholeWord; }
    public boolean isIgnoreCase() { return ignoreCase; }
    public int getAfterContext() { return afterContext; }
    public String getPattern() { return parameters.get(0); }
    public String getFileName() { return parameters.size() > 1 ? parameters.get(1) : null; }
    public List<String> getRawParameters() {
        return parameters;
    }
    public boolean isLiteralMatch() {
        return literalMatch;
    }

}
