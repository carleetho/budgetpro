package com.budgetpro.validator.roadmap;

import com.budgetpro.validator.model.ModulePhase;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Definición completa de un módulo en el roadmap canónico.
 */
public class ModuleDefinition {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("phase")
    private String phase;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("dependencies")
    private List<String> dependencies;
    
    @JsonProperty("enables")
    private List<String> enables;
    
    @JsonProperty("constraints")
    private List<DependencyConstraint> constraints;
    
    @JsonProperty("validation_rules")
    private List<ValidationRule> validationRules;

    public ModuleDefinition() {
        this.dependencies = new ArrayList<>();
        this.enables = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.validationRules = new ArrayList<>();
    }

    public ModuleDefinition(String id, String name, String phase, String priority) {
        this();
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.phase = Objects.requireNonNull(phase, "phase cannot be null");
        this.priority = Objects.requireNonNull(priority, "priority cannot be null");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Obtiene la fase como enum ModulePhase.
     */
    public ModulePhase getPhaseEnum() {
        if (phase == null) {
            return null;
        }
        return switch (phase.toLowerCase()) {
            case "foundation" -> ModulePhase.FOUNDATION;
            case "execution" -> ModulePhase.EXECUTION;
            case "analysis" -> ModulePhase.ANALYSIS;
            default -> null;
        };
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public List<String> getEnables() {
        return enables;
    }

    public void setEnables(List<String> enables) {
        this.enables = enables != null ? enables : new ArrayList<>();
    }

    public List<DependencyConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<DependencyConstraint> constraints) {
        this.constraints = constraints != null ? constraints : new ArrayList<>();
    }

    public List<ValidationRule> getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(List<ValidationRule> validationRules) {
        this.validationRules = validationRules != null ? validationRules : new ArrayList<>();
    }

    /**
     * Obtiene el constraint de acoplamiento temporal si existe.
     */
    public DependencyConstraint getTemporalCouplingConstraint() {
        return constraints.stream()
                .filter(DependencyConstraint::isTemporalCoupling)
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si este módulo tiene acoplamiento temporal con otro módulo.
     */
    public boolean hasTemporalCouplingWith(String moduleId) {
        return constraints.stream()
                .anyMatch(c -> c.isTemporalCoupling() && 
                              moduleId.equals(c.getCoupledWith()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleDefinition that = (ModuleDefinition) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ModuleDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phase='" + phase + '\'' +
                ", priority='" + priority + '\'' +
                ", dependencies=" + dependencies.size() +
                ", enables=" + enables.size() +
                ", constraints=" + constraints.size() +
                ", validationRules=" + validationRules.size() +
                '}';
    }
}
