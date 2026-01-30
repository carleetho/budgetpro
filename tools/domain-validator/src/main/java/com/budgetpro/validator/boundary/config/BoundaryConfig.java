package com.budgetpro.validator.boundary.config;

import java.util.List;

/**
 * Representa la configuración de las fronteras arquitectónicas (hexagonal).
 * Define qué paquetes están prohibidos dentro de la capa de dominio.
 */
public record BoundaryConfig(List<String> forbiddenImports, List<String> allowedStandardLibs) {
    /**
     * Valida que la configuración sea consistente.
     * 
     * @return Lista de errores encontrados, o lista vacía si es válida.
     */
    public List<String> validate() {
        if (forbiddenImports == null || forbiddenImports.isEmpty()) {
            return List.of("Boundary configuration must define 'forbidden_imports'");
        }
        return List.of();
    }
}
