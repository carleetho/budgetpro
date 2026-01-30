package com.budgetpro.blastradius.git;

import java.util.Objects;

/**
 * Representa un archivo staged en el índice de Git.
 * El path está normalizado para usar forward slashes.
 */
public record StagedFile(String path) {
    
    public StagedFile {
        Objects.requireNonNull(path, "path cannot be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("path cannot be empty");
        }
    }
    
    /**
     * Normaliza el path para usar forward slashes.
     * 
     * @param rawPath Path original (puede tener backslashes en Windows)
     * @return Path normalizado con forward slashes
     */
    public static String normalizePath(String rawPath) {
        if (rawPath == null) {
            return null;
        }
        return rawPath.replace('\\', '/');
    }
}
