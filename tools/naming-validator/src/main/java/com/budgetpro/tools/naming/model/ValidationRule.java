package com.budgetpro.tools.naming.model;

import java.nio.file.Path;
import java.util.List;

/**
 * Interfaz para reglas de validación de nombres.
 */
public interface ValidationRule {
    /**
     * Valida una clase contra una regla específica.
     * 
     * @param filePath  Ruta del archivo Java.
     * @param className Nombre de la clase.
     * @return Lista de violaciones encontradas.
     */
    List<NamingViolation> validate(Path filePath, String className);
}
