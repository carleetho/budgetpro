package com.budgetpro.validator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * Representa una violación detectada durante la validación del roadmap canónico.
 */
public class Violation {
    
    @JsonProperty("module_id")
    private String moduleId;
    
    @JsonProperty("severity")
    private ViolationSeverity severity;
    
    @JsonProperty("type")
    private DependencyType type;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("suggestion")
    private String suggestion;
    
    @JsonProperty("blocking")
    private boolean blocking;
    
    @JsonProperty("context")
    private Map<String, Object> context;

    public Violation() {
    }

    public Violation(String moduleId, ViolationSeverity severity, DependencyType type, 
                    String message, String suggestion, boolean blocking) {
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId cannot be null");
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.suggestion = suggestion;
        this.blocking = blocking;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ViolationSeverity severity) {
        this.severity = severity;
    }

    public DependencyType getType() {
        return type;
    }

    public void setType(DependencyType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Violation violation = (Violation) o;
        return blocking == violation.blocking &&
                Objects.equals(moduleId, violation.moduleId) &&
                severity == violation.severity &&
                type == violation.type &&
                Objects.equals(message, violation.message) &&
                Objects.equals(suggestion, violation.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleId, severity, type, message, suggestion, blocking);
    }

    @Override
    public String toString() {
        return "Violation{" +
                "moduleId='" + moduleId + '\'' +
                ", severity=" + severity +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", blocking=" + blocking +
                '}';
    }
}
