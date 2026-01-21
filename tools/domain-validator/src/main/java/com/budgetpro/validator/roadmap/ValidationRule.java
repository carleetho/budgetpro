package com.budgetpro.validator.roadmap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Regla de validación que especifica qué verificar en el código.
 */
public class ValidationRule {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("target")
    private String target;
    
    @JsonProperty("required")
    private Boolean required;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("field")
    private String field;
    
    @JsonProperty("cardinality")
    private String cardinality;
    
    @JsonProperty("required_states")
    private List<String> requiredStates;
    
    @JsonProperty("required_values")
    private List<String> requiredValues;
    
    @JsonProperty("required_methods")
    private List<String> requiredMethods;

    public ValidationRule() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Boolean getRequired() {
        return required != null ? required : true;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getCardinality() {
        return cardinality;
    }

    public void setCardinality(String cardinality) {
        this.cardinality = cardinality;
    }

    public List<String> getRequiredStates() {
        return requiredStates;
    }

    public void setRequiredStates(List<String> requiredStates) {
        this.requiredStates = requiredStates;
    }

    public List<String> getRequiredValues() {
        return requiredValues;
    }

    public void setRequiredValues(List<String> requiredValues) {
        this.requiredValues = requiredValues;
    }

    public List<String> getRequiredMethods() {
        return requiredMethods;
    }

    public void setRequiredMethods(List<String> requiredMethods) {
        this.requiredMethods = requiredMethods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationRule that = (ValidationRule) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target);
    }

    @Override
    public String toString() {
        return "ValidationRule{" +
                "type='" + type + '\'' +
                ", target='" + target + '\'' +
                ", required=" + getRequired() +
                '}';
    }
}
