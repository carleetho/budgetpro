package com.budgetpro.validator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneOffset;
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

    private static final DateTimeFormatter ISO_8601_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    public ValidationResult() {
        this.validationId = UUID.randomUUID().toString();
        this.timestamp = ISO_8601_FORMATTER.format(Instant.now());
        this.violations = new ArrayList<>();
        this.moduleStatuses = new ArrayList<>();
        this.canonicalVersion = "1.0.0";
    }

    public ValidationResult(String repositoryPath, ValidationStatus status) {
        this();
        this.repositoryPath = Objects.requireNonNull(repositoryPath, "repositoryPath cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    /**
     * Creates an error result with a specific message.
     * 
     * @param message Error description
     * @return ValidationResult with ERROR status
     */
    public static ValidationResult error(String message) {
        ValidationResult result = new ValidationResult();
        result.setStatus(ValidationStatus.ERROR);
        result.setRepositoryPath("unknown");

        Violation violation = new Violation();
        violation.setMessage(message);
        violation.setSeverity(ViolationSeverity.CRITICAL);
        violation.setModuleId("SYSTEM");
        result.addViolation(violation);

        return result;
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
     * Calcula el código de salida considerando modo estricto. En modo estricto, las
     * advertencias también bloquean (exit code 1).
     * 
     * @param strict Si true, las advertencias bloquean el merge
     * @return 0 si PASSED, 1 si CRITICAL_VIOLATIONS o (strict && WARNINGS), 2 si
     *         WARNINGS, 3 si ERROR
     */
    public int getExitCode(boolean strict) {
        if (strict && status == ValidationStatus.WARNINGS) {
            return 1; // Bloquear en modo estricto
        }
        return getExitCode();
    }

    /**
     * Verifica si hay violaciones críticas que bloquean el desarrollo.
     */
    public boolean hasCriticalViolations() {
        return violations.stream().anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL && v.isBlocking());
    }

    /**
     * Verifica si hay advertencias que requieren revisión.
     */
    public boolean hasWarnings() {
        return violations.stream().anyMatch(v -> v.getSeverity() == ViolationSeverity.WARNING);
    }

    @Override
    public String toString() {
        return "ValidationResult{" + "validationId='" + validationId + '\'' + ", status=" + status + ", violations="
                + violations.size() + ", moduleStatuses=" + moduleStatuses.size() + '}';
    }
}
