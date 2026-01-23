package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.ModuleDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Aplica estilos visuales a los módulos en el diagrama Mermaid.
 */
public class DiagramStyler {
    
    // Colores para diferentes tipos de módulos
    private static final String CRITICAL_COLOR = "#ff6b6b"; // Rojo
    private static final String HIGH_PRIORITY_COLOR = "#ffa94d"; // Naranja
    private static final String MEDIUM_PRIORITY_COLOR = "#ffd43b"; // Amarillo
    private static final String LOW_PRIORITY_COLOR = "#a8e6cf"; // Verde claro
    
    // Colores para fases
    private static final String FOUNDATION_COLOR = "#e3f2fd"; // Azul claro
    private static final String EXECUTION_COLOR = "#fff3e0"; // Naranja claro
    private static final String ANALYSIS_COLOR = "#f3e5f5"; // Púrpura claro

    /**
     * Obtiene el color de relleno para un módulo basado en su prioridad.
     */
    public String getModuleFillColor(ModuleDefinition module) {
        String priority = module.getPriority();
        
        return switch (priority) {
            case "CRITICAL" -> CRITICAL_COLOR;
            case "HIGH" -> HIGH_PRIORITY_COLOR;
            case "MEDIUM" -> MEDIUM_PRIORITY_COLOR;
            case "LOW" -> LOW_PRIORITY_COLOR;
            default -> "#e9ecef"; // Gris claro por defecto
        };
    }

    /**
     * Obtiene el color de borde para un módulo.
     */
    public String getModuleBorderColor(ModuleDefinition module) {
        String priority = module.getPriority();
        
        return switch (priority) {
            case "CRITICAL" -> "#c92a2a"; // Rojo oscuro
            case "HIGH" -> "#d9480f"; // Naranja oscuro
            default -> "#495057"; // Gris oscuro
        };
    }

    /**
     * Obtiene el color de fondo para una fase.
     */
    public String getPhaseBackgroundColor(String phase) {
        return switch (phase.toLowerCase()) {
            case "foundation" -> FOUNDATION_COLOR;
            case "execution" -> EXECUTION_COLOR;
            case "analysis" -> ANALYSIS_COLOR;
            default -> "#ffffff"; // Blanco
        };
    }

    /**
     * Genera la definición de clase CSS para un módulo.
     */
    public String generateClassDef(String moduleId, ModuleDefinition module) {
        String fillColor = getModuleFillColor(module);
        String borderColor = getModuleBorderColor(module);
        
        return String.format("classDef %s fill:%s,stroke:%s,stroke-width:2px,color:#000",
                sanitizeForMermaid(moduleId),
                fillColor,
                borderColor);
    }

    /**
     * Genera el estilo inline para un módulo crítico.
     */
    public String generateCriticalStyle(String moduleId) {
        return String.format("style %s fill:%s,stroke:%s,stroke-width:3px",
                sanitizeForMermaid(moduleId),
                CRITICAL_COLOR,
                "#c92a2a");
    }

    /**
     * Genera el estilo para una flecha de acoplamiento temporal.
     */
    public String generateTemporalCouplingStyle(String fromId, String toId) {
        return String.format("linkStyle %d stroke:#ff6b6b,stroke-width:2px,stroke-dasharray: 5 5",
                getLinkIndex(fromId, toId));
    }

    /**
     * Sanitiza un ID para uso en Mermaid (sin espacios ni caracteres especiales).
     */
    public String sanitizeForMermaid(String id) {
        if (id == null) {
            return "unknown";
        }
        // Reemplazar espacios y caracteres especiales
        return id.replaceAll("[^a-zA-Z0-9]", "_");
    }

    /**
     * Calcula un índice único para un link (simplificado).
     */
    private int getLinkIndex(String fromId, String toId) {
        // Hash simple para generar índice
        return (fromId.hashCode() + toId.hashCode()) % 1000;
    }

    /**
     * Genera todas las definiciones de clase CSS para los módulos.
     */
    public Map<String, String> generateAllClassDefs(java.util.List<ModuleDefinition> modules) {
        Map<String, String> classDefs = new HashMap<>();
        
        for (ModuleDefinition module : modules) {
            String moduleId = module.getId();
            classDefs.put(moduleId, generateClassDef(moduleId, module));
        }
        
        return classDefs;
    }
}
