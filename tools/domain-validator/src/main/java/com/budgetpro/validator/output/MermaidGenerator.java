package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.DependencyConstraint;
import com.budgetpro.validator.roadmap.ModuleDefinition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Genera diagramas Mermaid que visualizan el roadmap canónico.
 */
public class MermaidGenerator {
    
    private final DiagramStyler styler;
    
    public MermaidGenerator() {
        this.styler = new DiagramStyler();
    }

    /**
     * Genera un diagrama Mermaid completo del roadmap canónico.
     * 
     * @param roadmap Roadmap canónico
     * @return Código Mermaid como string
     */
    public String generate(CanonicalRoadmap roadmap) {
        StringBuilder diagram = new StringBuilder();
        
        // Encabezado del diagrama
        diagram.append("graph TD\n");
        
        // Generar nodos agrupados por fase
        Map<String, List<ModuleDefinition>> modulesByPhase = groupModulesByPhase(roadmap.getModules());
        
        // Generar subgrafos por fase
        generatePhaseSubgraphs(diagram, modulesByPhase);
        
        // Generar dependencias normales
        generateDependencies(diagram, roadmap.getModules());
        
        // Generar acoplamientos temporales
        generateTemporalCouplings(diagram, roadmap.getModules());
        
        // Generar estilos
        generateStyles(diagram, roadmap.getModules());
        
        return diagram.toString();
    }

    /**
     * Agrupa módulos por fase.
     */
    private Map<String, List<ModuleDefinition>> groupModulesByPhase(List<ModuleDefinition> modules) {
        return modules.stream()
                .collect(Collectors.groupingBy(ModuleDefinition::getPhase));
    }

    /**
     * Genera subgrafos por fase para agrupación visual.
     */
    private void generatePhaseSubgraphs(StringBuilder diagram, Map<String, List<ModuleDefinition>> modulesByPhase) {
        int subgraphIndex = 0;
        
        for (Map.Entry<String, List<ModuleDefinition>> entry : modulesByPhase.entrySet()) {
            String phase = entry.getKey();
            List<ModuleDefinition> phaseModules = entry.getValue();
            
            if (phaseModules.isEmpty()) {
                continue;
            }
            
            String phaseLabel = capitalizeFirst(phase) + " Phase";
            String subgraphId = "subgraph" + subgraphIndex++;
            
            diagram.append(String.format("    subgraph %s[\"%s\"]\n", subgraphId, phaseLabel));
            
            for (ModuleDefinition module : phaseModules) {
                String nodeId = styler.sanitizeForMermaid(module.getId());
                String nodeLabel = module.getName();
                diagram.append(String.format("        %s[\"%s\"]\n", nodeId, nodeLabel));
            }
            
            diagram.append("    end\n\n");
        }
    }


    /**
     * Genera flechas de dependencias normales (sólidas).
     */
    private void generateDependencies(StringBuilder diagram, List<ModuleDefinition> modules) {
        diagram.append("\n    %% Dependencies\n");
        
        for (ModuleDefinition module : modules) {
            String fromId = styler.sanitizeForMermaid(module.getId());
            
            for (String depId : module.getDependencies()) {
                // Verificar que no es acoplamiento temporal (se maneja separadamente)
                boolean isTemporalCoupling = module.getConstraints().stream()
                        .anyMatch(c -> c.isTemporalCoupling() && depId.equals(c.getCoupledWith()));
                
                if (!isTemporalCoupling) {
                    String toId = styler.sanitizeForMermaid(depId);
                    diagram.append(String.format("    %s --> %s\n", fromId, toId));
                }
            }
        }
    }

    /**
     * Genera flechas de acoplamiento temporal (punteadas con etiquetas).
     */
    private void generateTemporalCouplings(StringBuilder diagram, List<ModuleDefinition> modules) {
        diagram.append("\n    %% Temporal Couplings\n");
        
        Set<String> processedCouplings = new HashSet<>();
        
        for (ModuleDefinition module : modules) {
            DependencyConstraint temporalCoupling = module.getTemporalCouplingConstraint();
            
            if (temporalCoupling != null && temporalCoupling.getCoupledWith() != null) {
                String fromId = styler.sanitizeForMermaid(module.getId());
                String toId = styler.sanitizeForMermaid(temporalCoupling.getCoupledWith());
                
                // Crear clave única para evitar duplicados
                String couplingKey = fromId.compareTo(toId) < 0 ? fromId + "-" + toId : toId + "-" + fromId;
                
                if (!processedCouplings.contains(couplingKey)) {
                    // Extraer etiqueta del constraint
                    String label = extractCouplingLabel(temporalCoupling);
                    
                    diagram.append(String.format("    %s -.->|\"%s\"| %s\n", fromId, label, toId));
                    processedCouplings.add(couplingKey);
                }
            }
        }
    }

    /**
     * Extrae una etiqueta legible del constraint de acoplamiento temporal.
     */
    private String extractCouplingLabel(DependencyConstraint constraint) {
        String rule = constraint.getRule();
        
        // Simplificar la regla para la etiqueta
        if (rule.toLowerCase().contains("freeze")) {
            return "freeze together";
        } else if (rule.toLowerCase().contains("temporal")) {
            return "temporal coupling";
        } else {
            // Usar primeros 20 caracteres de la regla
            return rule.length() > 20 ? rule.substring(0, 20) + "..." : rule;
        }
    }

    /**
     * Genera estilos CSS para los módulos.
     */
    private void generateStyles(StringBuilder diagram, List<ModuleDefinition> modules) {
        diagram.append("\n    %% Styles\n");
        
        // Generar definiciones de clase por prioridad
        Set<String> priorities = modules.stream()
                .map(ModuleDefinition::getPriority)
                .collect(Collectors.toSet());
        
        for (String priority : priorities) {
            String className = "class" + priority.toLowerCase();
            String color = getPriorityColor(priority);
            String borderColor = getPriorityBorderColor(priority);
            diagram.append(String.format("    classDef %s fill:%s,stroke:%s,stroke-width:2px,color:#000\n",
                    className, color, borderColor));
        }
        
        // Aplicar clases a módulos según prioridad
        for (ModuleDefinition module : modules) {
            String moduleId = styler.sanitizeForMermaid(module.getId());
            String className = "class" + module.getPriority().toLowerCase();
            diagram.append(String.format("    class %s %s\n", moduleId, className));
        }
        
        // Estilos inline para módulos críticos (más visible)
        for (ModuleDefinition module : modules) {
            if ("CRITICAL".equals(module.getPriority())) {
                String moduleId = styler.sanitizeForMermaid(module.getId());
                diagram.append(String.format("    style %s fill:%s,stroke:%s,stroke-width:3px\n",
                        moduleId,
                        "#ff6b6b",
                        "#c92a2a"));
            }
        }
    }

    /**
     * Obtiene el color para una prioridad.
     */
    private String getPriorityColor(String priority) {
        return switch (priority) {
            case "CRITICAL" -> "#ff6b6b";
            case "HIGH" -> "#ffa94d";
            case "MEDIUM" -> "#ffd43b";
            case "LOW" -> "#a8e6cf";
            default -> "#e9ecef";
        };
    }

    /**
     * Obtiene el color de borde para una prioridad.
     */
    private String getPriorityBorderColor(String priority) {
        return switch (priority) {
            case "CRITICAL" -> "#c92a2a";
            case "HIGH" -> "#d9480f";
            case "MEDIUM" -> "#f59f00";
            case "LOW" -> "#51cf66";
            default -> "#495057";
        };
    }

    /**
     * Capitaliza la primera letra de un string.
     */
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Genera un diagrama simplificado sin subgrafos (útil para diagramas más pequeños).
     */
    public String generateSimplified(CanonicalRoadmap roadmap) {
        StringBuilder diagram = new StringBuilder();
        
        diagram.append("graph TD\n");
        
        // Generar nodos
        for (ModuleDefinition module : roadmap.getModules()) {
            String nodeId = styler.sanitizeForMermaid(module.getId());
            String nodeLabel = module.getName();
            String phase = module.getPhase();
            diagram.append(String.format("    %s[\"%s<br/><small>%s</small>\"]\n", 
                    nodeId, nodeLabel, capitalizeFirst(phase)));
        }
        
        // Generar dependencias
        generateDependencies(diagram, roadmap.getModules());
        
        // Generar acoplamientos temporales
        generateTemporalCouplings(diagram, roadmap.getModules());
        
        // Generar estilos
        generateStyles(diagram, roadmap.getModules());
        
        return diagram.toString();
    }
}
