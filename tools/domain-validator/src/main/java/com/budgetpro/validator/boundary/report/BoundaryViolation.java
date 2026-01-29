package com.budgetpro.validator.boundary.report;

import java.nio.file.Path;

/**
 * Representa una violación de las reglas de frontera hexagonal.
 */
public record BoundaryViolation(Path file, String forbiddenImport, String message) {
}
