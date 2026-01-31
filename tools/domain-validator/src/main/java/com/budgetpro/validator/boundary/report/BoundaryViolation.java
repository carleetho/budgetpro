package com.budgetpro.validator.boundary.report;

import java.nio.file.Path;

/**
 * Representa una violación de las reglas de frontera hexagonal.
 */
public record BoundaryViolation(
        /** Archivo donde se encontró la violación */
        Path file,
        /** Importación o dependencia prohibida detectada */
        String forbiddenImport,
        /** Mensaje descriptivo del error */
        String message,
        /** Severidad de la violación (CRITICAL, WARNING, etc.) */
        String severity) {
}
