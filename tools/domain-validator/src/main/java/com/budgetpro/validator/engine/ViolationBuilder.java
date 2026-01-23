package com.budgetpro.validator.engine;

import com.budgetpro.validator.model.DependencyType;
import com.budgetpro.validator.model.Violation;
import com.budgetpro.validator.model.ViolationSeverity;

import java.util.HashMap;
import java.util.Map;

/**
 * Constructor de violaciones con sugerencias y contexto estructurado.
 */
public class ViolationBuilder {
    
    private String moduleId;
    private ViolationSeverity severity;
    private DependencyType type;
    private String message;
    private String suggestion;
    private boolean blocking;
    private Map<String, Object> context;

    public ViolationBuilder() {
        this.context = new HashMap<>();
        this.blocking = false;
    }

    public ViolationBuilder module(String moduleId) {
        this.moduleId = moduleId;
        return this;
    }

    public ViolationBuilder severity(ViolationSeverity severity) {
        this.severity = severity;
        this.blocking = (severity == ViolationSeverity.CRITICAL);
        return this;
    }

    public ViolationBuilder type(DependencyType type) {
        this.type = type;
        return this;
    }

    public ViolationBuilder message(String message) {
        this.message = message;
        return this;
    }

    public ViolationBuilder suggestion(String suggestion) {
        this.suggestion = suggestion;
        return this;
    }

    public ViolationBuilder blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }

    public ViolationBuilder context(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Agrega una cadena de dependencias al contexto.
     */
    public ViolationBuilder dependencyChain(String chain) {
        this.context.put("dependency_chain", chain);
        return this;
    }

    /**
     * Agrega elementos detectados vs esperados al contexto.
     */
    public ViolationBuilder detectedVsExpected(Object detected, Object expected) {
        this.context.put("detected", detected);
        this.context.put("expected", expected);
        return this;
    }

    /**
     * Agrega ubicación de archivo al contexto.
     */
    public ViolationBuilder fileLocation(String filePath) {
        this.context.put("file_location", filePath);
        return this;
    }

    /**
     * Construye la violación final.
     */
    public Violation build() {
        if (moduleId == null || severity == null || type == null || message == null) {
            throw new IllegalStateException("ViolationBuilder missing required fields: moduleId, severity, type, message");
        }

        Violation violation = new Violation(moduleId, severity, type, message, suggestion, blocking);
        if (!context.isEmpty()) {
            violation.setContext(context);
        }

        return violation;
    }

    /**
     * Crea un builder para una violación crítica de dependencia faltante.
     */
    public static ViolationBuilder missingDependency(String moduleId, String missingDep, DependencyType depType) {
        return new ViolationBuilder()
                .module(moduleId)
                .severity(ViolationSeverity.CRITICAL)
                .type(depType)
                .message(String.format("Módulo '%s' requiere que el módulo '%s' esté implementado", moduleId, missingDep))
                .suggestion(String.format("Implementar primero el módulo '%s' antes de continuar con '%s'", missingDep, moduleId))
                .blocking(true)
                .context("missing_module", missingDep);
    }

    /**
     * Crea un builder para una violación de acoplamiento temporal.
     */
    public static ViolationBuilder temporalCouplingViolation(String moduleId, String coupledModule, String rule) {
        return new ViolationBuilder()
                .module(moduleId)
                .severity(ViolationSeverity.CRITICAL)
                .type(DependencyType.TEMPORAL_DEPENDENCY)
                .message(String.format("Módulo '%s' viola acoplamiento temporal con '%s': %s", moduleId, coupledModule, rule))
                .suggestion(String.format("Implementar mecanismo de acoplamiento temporal: cuando '%s' se congela, '%s' debe congelarse automáticamente", coupledModule, moduleId))
                .blocking(true)
                .context("coupled_module", coupledModule)
                .context("rule", rule);
    }

    /**
     * Crea un builder para una violación de regla de validación.
     */
    public static ViolationBuilder validationRuleViolation(String moduleId, String ruleType, String target, boolean required) {
        ViolationSeverity severity = required ? ViolationSeverity.CRITICAL : ViolationSeverity.WARNING;
        
        return new ViolationBuilder()
                .module(moduleId)
                .severity(severity)
                .type(DependencyType.BUSINESS_LOGIC)
                .message(String.format("Regla de validación fallida: %s '%s' no encontrado en módulo '%s'", ruleType, target, moduleId))
                .suggestion(String.format("Implementar %s '%s' según especificación del roadmap canónico", ruleType, target))
                .blocking(required)
                .context("rule_type", ruleType)
                .context("target", target)
                .context("required", required);
    }

    /**
     * Crea un builder para una violación de estado requerido.
     */
    public static ViolationBuilder stateDependencyViolation(String moduleId, String requiredState, String entityName) {
        return new ViolationBuilder()
                .module(moduleId)
                .severity(ViolationSeverity.CRITICAL)
                .type(DependencyType.STATE_DEPENDENCY)
                .message(String.format("Módulo '%s' requiere que %s esté en estado '%s'", moduleId, entityName, requiredState))
                .suggestion(String.format("Asegurar que %s alcance el estado '%s' antes de usar '%s'", entityName, requiredState, moduleId))
                .blocking(true)
                .context("required_state", requiredState)
                .context("entity", entityName);
    }
}
