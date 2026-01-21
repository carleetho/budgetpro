package com.budgetpro.validator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado completo de una ejecución de validación del roadmap canónico.
 */
public class ValidationResult {
    
    @JsonProperty("validation_id")
    private String validationId;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("repository_path")
    private String repositoryPath;
    
    @JsonProperty("canonical_version")
    private String canonicalVersion;
    
    @JsonProperty("status")
    private ValidationStatus status;
    
    @JsonProperty("violations")
    private List<Violation> violations;
    
    @JsonProperty("module_statuses")
    private List<ModuleStatus> moduleStatuses;

    public ValidationResult() {
        this.validationId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.violations = new ArrayList<>();
        this.moduleStatuses = new ArrayList<>();
        this.canonicalVersion = "1.0.0";
    }

    public ValidationResult(String repositoryPath, ValidationStatus status) {
        this();
        this.repositoryPath = Objects.requireNonNull(repositoryPath, "repositoryPath cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public String getValidationId() {
        return validationId;
    }

    public void setValidationId(String validationId) {
        this.validationId = validationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public String getCanonicalVersion() {
        return canonicalVersion;
    }

    public void setCanonicalVersion(String canonicalVersion) {
        this.canonicalVersion = canonicalVersion;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations != null ? violations : new ArrayList<>();
    }

    public List<ModuleStatus> getModuleStatuses() {
        return moduleStatuses;
    }

    public void setModuleStatuses(List<ModuleStatus> moduleStatuses) {
        this.moduleStatuses = moduleStatuses != null ? moduleStatuses : new ArrayList<>();
    }

    /**
     * Agrega una violación al resultado.
     */
    public void addViolation(Violation violation) {
        if (violation != null) {
            this.violations.add(violation);
        }
    }

    /**
     * Agrega un estado de módulo al resultado.
     */
    public void addModuleStatus(ModuleStatus moduleStatus) {
        if (moduleStatus != null) {
            this.moduleStatuses.add(moduleStatus);
        }
    }

    /**
     * Calcula el código de salida apropiado según el estado de validación.
     * 
     * @return 0 si PASSED, 1 si CRITICAL_VIOLATIONS, 2 si WARNINGS, 3 si ERROR
     */
    public int getExitCode() {
        return switch (status) {
            case PASSED -> 0;
            case CRITICAL_VIOLATIONS -> 1;
            case WARNINGS -> 2;
            case ERROR -> 3;
        };
    }

    /**
     * Verifica si hay violaciones críticas que bloquean el desarrollo.
     */
    public boolean hasCriticalViolations() {
        return violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL && v.isBlocking());
    }

    /**
     * Verifica si hay advertencias que requieren revisión.
     */
    public boolean hasWarnings() {
        return violations.stream()
                .anyMatch(v -> v.getSeverity() == ViolationSeverity.WARNING);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "validationId='" + validationId + '\'' +
                ", status=" + status +
                ", violations=" + violations.size() +
                ", moduleStatuses=" + moduleStatuses.size() +
                '}';
    }
}
