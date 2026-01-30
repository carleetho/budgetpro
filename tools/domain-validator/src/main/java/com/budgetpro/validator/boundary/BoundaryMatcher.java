package com.budgetpro.validator.boundary;

import java.util.List;

/**
 * Lógica para verificar si un import coincide con patrones prohibidos.
 */
public class BoundaryMatcher {
    
    private final List<String> forbiddenPatterns;
    private final List<String> allowedPatterns;

    public BoundaryMatcher(List<String> forbiddenPatterns, List<String> allowedPatterns) {
        this.forbiddenPatterns = forbiddenPatterns != null ? forbiddenPatterns : List.of();
        this.allowedPatterns = allowedPatterns != null ? allowedPatterns : List.of();
    }

    /**
     * Verifica si un import específico viola las reglas de frontera.
     * 
     * @param importPath El path completo del import (ej: org.springframework.stereotype.Service)
     * @return true si el import está prohibido y no está en la lista de permitidos.
     */
    public boolean isForbidden(String importPath) {
        if (importPath == null || importPath.isBlank()) {
            return false;
        }

        // Primero verificamos si está explícitamente permitido (ej: java.*)
        for (String pattern : allowedPatterns) {
            if (matches(importPath, pattern)) {
                return false;
            }
        }

        // Luego verificamos si coincide con alguno prohibido
        for (String pattern : forbiddenPatterns) {
            if (matches(importPath, pattern)) {
                return true;
            }
        }

        return false;
    }

    private boolean matches(String importPath, String pattern) {
        if (pattern.endsWith(".*")) {
            String basePackage = pattern.substring(0, pattern.length() - 2);
            return importPath.startsWith(basePackage + ".") || importPath.equals(basePackage);
        }
        return importPath.equals(pattern);
    }
}
