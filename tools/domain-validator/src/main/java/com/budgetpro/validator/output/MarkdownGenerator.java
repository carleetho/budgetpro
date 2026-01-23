package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.DependencyConstraint;
import com.budgetpro.validator.roadmap.ModuleDefinition;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera el documento Markdown GSOT (Golden Source of Truth) del roadmap canónico.
 */
public class MarkdownGenerator {
    
    private final ModuleDocumenter moduleDocumenter;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public MarkdownGenerator() {
        this.moduleDocumenter = new ModuleDocumenter();
    }

    /**
     * Genera el documento Markdown completo del roadmap canónico.
     */
    public String generate(CanonicalRoadmap roadmap) {
        StringBuilder document = new StringBuilder();
        
        // Encabezado del documento
        generateHeader(document, roadmap);
        
        // Introducción
        generateIntroduction(document);
        
        // Principio de Baseline
        generateBaselinePrinciple(document, roadmap);
        
        // Agrupar módulos por fase
        Map<String, List<ModuleDefinition>> modulesByPhase = groupModulesByPhase(roadmap.getModules());
        
        // Crear mapa de nombres de módulos para referencias
        Map<String, String> moduleNameMap = roadmap.getModules().stream()
                .collect(Collectors.toMap(
                    ModuleDefinition::getId,
                    ModuleDefinition::getName,
                    (a, b) -> a
                ));
        
        // Generar secciones por fase
        generatePhaseSection(document, "Foundation", modulesByPhase.getOrDefault("foundation", Collections.emptyList()), moduleNameMap);
        generatePhaseSection(document, "Execution", modulesByPhase.getOrDefault("execution", Collections.emptyList()), moduleNameMap);
        generatePhaseSection(document, "Analysis", modulesByPhase.getOrDefault("analysis", Collections.emptyList()), moduleNameMap);
        
        // Apéndices
        generateAppendices(document, roadmap);
        
        return document.toString();
    }

    /**
     * Genera el encabezado del documento.
     */
    private void generateHeader(StringBuilder document, CanonicalRoadmap roadmap) {
        document.append("# BudgetPro Canonical Development Roadmap\n\n");
        document.append("**Version**: ").append(roadmap.getVersion()).append("\n\n");
        document.append("**Generated**: ").append(LocalDateTime.now().format(TIMESTAMP_FORMATTER)).append("\n\n");
        document.append("**Description**: ").append(roadmap.getDescription()).append("\n\n");
        document.append("---\n\n");
    }

    /**
     * Genera la introducción del documento.
     */
    private void generateIntroduction(StringBuilder document) {
        document.append("## Overview\n\n");
        document.append("Este documento define el orden canónico de desarrollo de módulos BudgetPro ")
               .append("basado en la causalidad del dominio de construcción, principios de establecimiento ")
               .append("de baseline y dependencias inter-módulo obligatorias.\n\n");
        
        document.append("### Principios del Dominio de Construcción\n\n");
        document.append("1. **No ejecución sin presupuesto aprobado**: Los módulos de ejecución ")
               .append("(Compras, Inventarios, RRHH) requieren un presupuesto congelado.\n\n");
        document.append("2. **Baseline simultáneo**: Presupuesto y Cronograma deben congelarse juntos ")
               .append("para establecer el baseline del proyecto.\n\n");
        document.append("3. **Compromiso en aprobación**: El presupuesto se compromete en el momento ")
               .append("de aprobación de la compra, no en el pago.\n\n");
        document.append("4. **Integridad de baseline**: Una vez congelado, el baseline solo puede ")
               .append("modificarse mediante procesos de cambio controlados.\n\n");
    }

    /**
     * Genera la sección del principio de baseline.
     */
    private void generateBaselinePrinciple(StringBuilder document, CanonicalRoadmap roadmap) {
        document.append("## Baseline Principle\n\n");
        document.append("### Presupuesto + Tiempo Freeze Together\n\n");
        
        // Buscar módulos con acoplamiento temporal
        List<ModuleDefinition> baselineModules = roadmap.getModules().stream()
                .filter(m -> m.hasTemporalCouplingWith("tiempo") || m.hasTemporalCouplingWith("presupuesto"))
                .collect(Collectors.toList());
        
        if (!baselineModules.isEmpty()) {
            document.append("Los siguientes módulos están acoplados temporalmente y **DEBEN** congelarse simultáneamente:\n\n");
            for (ModuleDefinition module : baselineModules) {
                document.append("- **").append(module.getName()).append("**");
                DependencyConstraint temporalCoupling = module.getTemporalCouplingConstraint();
                if (temporalCoupling != null && temporalCoupling.getCoupledWith() != null) {
                    String coupledName = roadmap.getModuleById(temporalCoupling.getCoupledWith())
                            .map(ModuleDefinition::getName)
                            .orElse(temporalCoupling.getCoupledWith());
                    document.append(" ↔ **").append(coupledName).append("**");
                }
                document.append("\n");
            }
            document.append("\n");
        }
        
        document.append("**Regla crítica**: No se puede proceder con módulos de ejecución ")
               .append("(Compras, Inventarios, RRHH) hasta que ambos módulos del baseline ")
               .append("estén completamente implementados y congelados.\n\n");
    }

    /**
     * Agrupa módulos por fase.
     */
    private Map<String, List<ModuleDefinition>> groupModulesByPhase(List<ModuleDefinition> modules) {
        return modules.stream()
                .collect(Collectors.groupingBy(ModuleDefinition::getPhase));
    }

    /**
     * Genera una sección completa para una fase.
     */
    private void generatePhaseSection(StringBuilder document, String phaseName, 
                                     List<ModuleDefinition> modules, 
                                     Map<String, String> moduleNameMap) {
        if (modules.isEmpty()) {
            return;
        }
        
        document.append("## Phase: ").append(phaseName).append("\n\n");
        
        // Descripción de la fase
        String phaseDescription = getPhaseDescription(phaseName);
        document.append(phaseDescription).append("\n\n");
        
        // Ordenar módulos por prioridad (CRITICAL primero)
        List<ModuleDefinition> sortedModules = modules.stream()
                .sorted(Comparator.comparing((ModuleDefinition m) -> 
                    "CRITICAL".equals(m.getPriority()) ? 0 : 1)
                    .thenComparing(ModuleDefinition::getName))
                .collect(Collectors.toList());
        
        // Generar sección para cada módulo
        for (ModuleDefinition module : sortedModules) {
            document.append(moduleDocumenter.generateModuleSection(module, moduleNameMap));
        }
    }

    /**
     * Obtiene la descripción de una fase.
     */
    private String getPhaseDescription(String phaseName) {
        return switch (phaseName.toLowerCase()) {
            case "foundation" -> 
                "Establece el baseline del proyecto. Incluye Proyecto, Presupuesto y Tiempo. " +
                "Estos módulos deben estar completamente implementados antes de proceder con módulos de ejecución.";
            case "execution" -> 
                "Módulos que consumen el baseline establecido. Incluye Compras, Inventarios, RRHH y Estimación. " +
                "Requieren presupuesto congelado y cronograma establecido.";
            case "analysis" -> 
                "Módulos de análisis y medición de desempeño. Incluye EVM, Cambios y Alertas. " +
                "Requieren baseline congelado y datos de ejecución.";
            default -> "Fase de desarrollo del roadmap canónico.";
        };
    }

    /**
     * Genera los apéndices del documento.
     */
    private void generateAppendices(StringBuilder document, CanonicalRoadmap roadmap) {
        document.append("## Appendices\n\n");
        
        document.append("### Module Dependency Graph\n\n");
        document.append("Para visualización del roadmap, ejecuta:\n\n");
        document.append("```bash\n");
        document.append("java -jar domain-validator.jar generate-roadmap --format mermaid\n");
        document.append("```\n\n");
        
        document.append("### Validation\n\n");
        document.append("Para validar el orden de desarrollo actual contra este roadmap:\n\n");
        document.append("```bash\n");
        document.append("java -jar domain-validator.jar validate --repo-path ./backend\n");
        document.append("```\n\n");
        
        document.append("### Exit Codes\n\n");
        document.append("- `0`: Validación pasada sin violaciones\n");
        document.append("- `1`: Violaciones críticas detectadas (bloquea desarrollo)\n");
        document.append("- `2`: Advertencias detectadas (requiere revisión)\n");
        document.append("- `3`: Error durante el análisis\n\n");
    }
}
