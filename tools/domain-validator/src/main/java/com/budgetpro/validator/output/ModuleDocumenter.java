package com.budgetpro.validator.output;

import com.budgetpro.validator.roadmap.DependencyConstraint;
import com.budgetpro.validator.roadmap.ModuleDefinition;
import com.budgetpro.validator.roadmap.ValidationRule;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Genera la documentación Markdown para un módulo individual.
 */
public class ModuleDocumenter {
    
    // Mapa de justificaciones por módulo (basado en principios del dominio de construcción)
    private static final Map<String, String> MODULE_JUSTIFICATIONS = Map.ofEntries(
        Map.entry("proyecto", "Establece el contexto del proyecto y la billetera financiera. Sin proyecto activo, no puede existir presupuesto ni ejecución."),
        Map.entry("presupuesto", "Define el baseline financiero del proyecto. Establece las partidas presupuestarias y su estructura WBS. **Principio de construcción**: No se puede ejecutar sin presupuesto aprobado y congelado."),
        Map.entry("tiempo", "Define el cronograma de obra y establece el baseline temporal. **Principio de construcción**: El cronograma debe congelarse junto con el presupuesto para establecer el baseline completo del proyecto."),
        Map.entry("compras", "Registra la adquisición real de recursos. **Principio de construcción**: No se puede comprar sin presupuesto aprobado y congelado. El compromiso presupuestario ocurre en el momento de aprobación de la compra, no en el pago."),
        Map.entry("inventarios", "Gestiona el almacén y el consumo de materiales. Requiere compras para recibir materiales y presupuesto para validar disponibilidad."),
        Map.entry("rrhh", "Gestiona el personal asignado al proyecto. Requiere cronograma para asignar personal a actividades y presupuesto para validar disponibilidad de mano de obra."),
        Map.entry("estimacion", "Calcula estimaciones a la conclusión (EAC) y proyecciones. Requiere datos históricos de compras, inventarios y RRHH."),
        Map.entry("evm", "Aplica Earned Value Management para medir desempeño. Requiere baseline congelado (presupuesto + tiempo) y datos de ejecución."),
        Map.entry("cambios", "Gestiona cambios al proyecto y sus impactos en baseline. Requiere baseline establecido y mecanismos de control de cambios."),
        Map.entry("alertas", "Genera alertas y notificaciones sobre desviaciones. Requiere módulos de análisis (EVM, Cambios) para detectar problemas."),
        Map.entry("billetera", "Gestiona los flujos financieros del proyecto. Requerido por Proyecto para establecer el contexto financiero."),
        Map.entry("catalogo", "Mantiene el catálogo de APUs (Análisis de Precios Unitarios). Requerido por Presupuesto para calcular costos de partidas.")
    );

    /**
     * Genera la sección Markdown completa para un módulo.
     */
    public String generateModuleSection(ModuleDefinition module, Map<String, String> moduleNameMap) {
        StringBuilder section = new StringBuilder();
        
        // H3: Nombre del módulo
        section.append("### ").append(module.getName()).append("\n\n");
        
        // Priority
        section.append("**Priority**: ").append(module.getPriority()).append("\n\n");
        
        // Justification
        String justification = MODULE_JUSTIFICATIONS.getOrDefault(
            module.getId().toLowerCase(),
            "Módulo requerido para el funcionamiento completo del sistema BudgetPro."
        );
        section.append("**Justification**: ").append(justification).append("\n\n");
        
        // Dependencies
        if (!module.getDependencies().isEmpty()) {
            section.append("**Dependencies**:\n");
            for (String depId : module.getDependencies()) {
                String depName = moduleNameMap.getOrDefault(depId, depId);
                section.append("- ").append(depName).append("\n");
            }
            section.append("\n");
        } else {
            section.append("**Dependencies**: None (foundation module)\n\n");
        }
        
        // Enables
        if (!module.getEnables().isEmpty()) {
            section.append("**Enables**:\n");
            for (String enableId : module.getEnables()) {
                String enableName = moduleNameMap.getOrDefault(enableId, enableId);
                section.append("- ").append(enableName).append("\n");
            }
            section.append("\n");
        }
        
        // Critical Constraints
        List<DependencyConstraint> criticalConstraints = module.getConstraints().stream()
                .filter(c -> "critical".equalsIgnoreCase(c.getSeverity()))
                .collect(Collectors.toList());
        
        if (!criticalConstraints.isEmpty()) {
            section.append("**Critical Constraints**:\n\n");
            for (DependencyConstraint constraint : criticalConstraints) {
                section.append("1. **").append(constraint.getType().toUpperCase()).append("**: ");
                section.append(constraint.getRule());
                
                if (constraint.isTemporalCoupling() && constraint.getCoupledWith() != null) {
                    String coupledName = moduleNameMap.getOrDefault(constraint.getCoupledWith(), constraint.getCoupledWith());
                    section.append(" (coupled with ").append(coupledName).append(")");
                }
                
                section.append("\n");
            }
            section.append("\n");
        }
        
        // Baseline Principle (especial para Presupuesto y Tiempo)
        if (module.hasTemporalCouplingWith("tiempo") || module.hasTemporalCouplingWith("presupuesto")) {
            section.append("**Baseline Principle**:\n\n");
            section.append("> **CRITICAL**: Este módulo está acoplado temporalmente con otro módulo del baseline. ")
                   .append("Ambos deben congelarse simultáneamente para establecer el baseline del proyecto. ")
                   .append("No se puede proceder con módulos de ejecución hasta que ambos estén congelados.\n\n");
        }
        
        // Must Implement
        List<ValidationRule> requiredRules = module.getValidationRules().stream()
                .filter(ValidationRule::getRequired)
                .collect(Collectors.toList());
        
        if (!requiredRules.isEmpty()) {
            section.append("**Must Implement**:\n\n");
            for (ValidationRule rule : requiredRules) {
                section.append("- **").append(formatRuleType(rule.getType())).append("**: ");
                section.append(formatRuleTarget(rule));
                section.append("\n");
            }
            section.append("\n");
        }
        
        return section.toString();
    }

    /**
     * Formatea el tipo de regla para legibilidad.
     */
    private String formatRuleType(String type) {
        if (type == null) {
            return "Unknown";
        }
        return switch (type.toLowerCase()) {
            case "entity_exists" -> "Entity";
            case "service_exists" -> "Service";
            case "state_machine_exists" -> "State Machine";
            case "enum_exists" -> "Enum";
            case "port_exists" -> "Port";
            case "relationship_exists" -> "Relationship";
            case "reference_exists" -> "Reference";
            default -> type;
        };
    }

    /**
     * Formatea el target de una regla para legibilidad.
     */
    private String formatRuleTarget(ValidationRule rule) {
        StringBuilder target = new StringBuilder();
        
        if (rule.getTarget() != null) {
            // Simplificar nombres de clase completos
            String targetStr = rule.getTarget();
            if (targetStr.contains(".")) {
                String[] parts = targetStr.split("\\.");
                target.append(parts[parts.length - 1]);
            } else {
                target.append(targetStr);
            }
        }
        
        // Agregar información adicional según el tipo
        if (rule.getRequiredStates() != null && !rule.getRequiredStates().isEmpty()) {
            target.append(" (states: ").append(String.join(", ", rule.getRequiredStates())).append(")");
        }
        
        if (rule.getRequiredMethods() != null && !rule.getRequiredMethods().isEmpty()) {
            target.append(" (methods: ").append(String.join(", ", rule.getRequiredMethods())).append(")");
        }
        
        return target.toString();
    }
}
