package com.budgetpro.validator.roadmap;

import com.budgetpro.validator.model.DependencyType;
import com.budgetpro.validator.model.ViolationSeverity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Constraint de dependencia entre módulos.
 * Define reglas que deben cumplirse para que un módulo pueda desarrollarse.
 */
public class DependencyConstraint {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("rule")
    private String rule;
    
    @JsonProperty("severity")
    private String severity;
    
    @JsonProperty("coupled_with")
    private String coupledWith;

    public DependencyConstraint() {
    }

    public DependencyConstraint(String type, String rule, String severity) {
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.rule = Objects.requireNonNull(rule, "rule cannot be null");
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCoupledWith() {
        return coupledWith;
    }

    public void setCoupledWith(String coupledWith) {
        this.coupledWith = coupledWith;
    }

    /**
     * Convierte el tipo de constraint a DependencyType enum.
     */
    public DependencyType getDependencyType() {
        if (type == null) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "state", "state_dependency" -> DependencyType.STATE_DEPENDENCY;
            case "data", "data_dependency" -> DependencyType.DATA_DEPENDENCY;
            case "temporal", "temporal_coupling", "temporal_dependency" -> DependencyType.TEMPORAL_DEPENDENCY;
            case "business_logic", "business_logic_dependency" -> DependencyType.BUSINESS_LOGIC;
            default -> null;
        };
    }

    /**
     * Convierte la severidad a ViolationSeverity enum.
     */
    public ViolationSeverity getViolationSeverity() {
        if (severity == null) {
            return ViolationSeverity.WARNING;
        }
        return switch (severity.toLowerCase()) {
            case "critical" -> ViolationSeverity.CRITICAL;
            case "warning" -> ViolationSeverity.WARNING;
            case "info" -> ViolationSeverity.INFO;
            default -> ViolationSeverity.WARNING;
        };
    }

    /**
     * Verifica si este constraint es de acoplamiento temporal.
     */
    public boolean isTemporalCoupling() {
        return "temporal_coupling".equalsIgnoreCase(type) || 
               "temporal".equalsIgnoreCase(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DependencyConstraint that = (DependencyConstraint) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(rule, that.rule) &&
                Objects.equals(coupledWith, that.coupledWith);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rule, coupledWith);
    }

    @Override
    public String toString() {
        return "DependencyConstraint{" +
                "type='" + type + '\'' +
                ", rule='" + rule + '\'' +
                ", severity='" + severity + '\'' +
                (coupledWith != null ? ", coupledWith='" + coupledWith + '\'' : "") +
                '}';
    }
}
