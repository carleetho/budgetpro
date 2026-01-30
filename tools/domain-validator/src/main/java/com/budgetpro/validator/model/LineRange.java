package com.budgetpro.validator.model;

/**
 * Representa un rango de líneas en un archivo.
 */
public record LineRange(int startLine, int endLine) {

    public boolean contains(int lineNumber) {
        return lineNumber >= startLine && lineNumber <= endLine;
    }
}
