package com.budgetpro.validator.boundary.config;

import java.util.List;

/**
 * Representa la configuración de las fronteras arquitectónicas (hexagonal).
 * Define qué paquetes están prohibidos dentro de la capa de dominio y el nivel
 * de severidad.
 */
public record BoundaryConfig(boolean enabled, String severity, boolean structuralAnalysis,
        List<String> forbiddenImports, List<String> allowedStandardLibs) {
    /**
     * Constructor compacto para valores por defecto.
     */
    public BoundaryConfig {
        if (severity == null)
            severity = "CRITICAL";
        if (forbiddenImports == null)
            forbiddenImports = List.of();
        if (allowedStandardLibs == null)
            allowedStandardLibs = List.of();
    }

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

    public static BoundaryConfig defaultConfig() {
        return new BoundaryConfig(true, "CRITICAL", true, List.of(), List.of());
    }
}
