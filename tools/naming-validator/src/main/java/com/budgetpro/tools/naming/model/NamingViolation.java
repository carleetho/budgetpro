package com.budgetpro.tools.naming.model;

import java.nio.file.Path;

/**
 * Representa una violación de convención de nombres detectada.
 * 
 * @param filePath     Ruta del archivo que contiene la violación.
 * @param className    Nombre de la clase analizada.
 * @param expectedName Nombre esperado según las reglas.
 * @param severity     Severidad de la violación.
 * @param message      Mensaje descriptivo del error en español.
 * @param suggestion   Sugerencia de corrección en español.
 */
public record NamingViolation(Path filePath, String className, String expectedName, ViolationSeverity severity,
        String message, String suggestion) {
}
