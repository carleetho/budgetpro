package com.budgetpro.blastradius.validator;

import com.budgetpro.blastradius.classifier.ClassifiedFiles;
import com.budgetpro.blastradius.classifier.Zone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Resultado de la validación de blast radius.
 * Contiene información sobre si la validación pasó o falló, y los detalles de las violaciones.
 */
public class ValidationResult {
    
    private final boolean success;
    private final boolean overrideDetected;
    private final ClassifiedFiles classifiedFiles;
    private final List<Violation> violations;
    private final String errorMessage;
    
    private ValidationResult(boolean success, boolean overrideDetected, 
                           ClassifiedFiles classifiedFiles, 
                           List<Violation> violations, 
                           String errorMessage) {
        this.success = success;
        this.overrideDetected = overrideDetected;
        this.classifiedFiles = classifiedFiles;
        this.violations = violations != null 
            ? Collections.unmodifiableList(new ArrayList<>(violations))
            : Collections.emptyList();
        this.errorMessage = errorMessage;
    }
    
    /**
     * Crea un resultado de validación exitoso.
     */
    public static ValidationResult success(ClassifiedFiles classifiedFiles, boolean overrideDetected) {
        return new ValidationResult(true, overrideDetected, classifiedFiles, null, null);
    }
    
    /**
     * Crea un resultado de validación fallido con violaciones.
     */
    public static ValidationResult failure(ClassifiedFiles classifiedFiles, 
                                          List<Violation> violations, 
                                          boolean overrideDetected) {
        return new ValidationResult(false, overrideDetected, classifiedFiles, violations, null);
    }
    
    /**
     * Crea un resultado de error (configuración o Git).
     */
    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, false, null, null, errorMessage);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isOverrideDetected() {
        return overrideDetected;
    }
    
    public ClassifiedFiles getClassifiedFiles() {
        return classifiedFiles;
    }
    
    public List<Violation> getViolations() {
        return violations;
    }
    
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public boolean isError() {
        return errorMessage != null;
    }
    
    /**
     * Obtiene el código de salida apropiado según el resultado.
     * 
     * @return 0 si éxito, 1 si fallo de validación, 2 si error de configuración/Git
     */
    public int getExitCode() {
        if (isError()) {
            return 2; // Error de configuración o Git
        }
        if (isSuccess()) {
            return 0; // Validación pasada
        }
        return 1; // Validación fallida
    }
    
    @Override
    public String toString() {
        if (isError()) {
            return "ValidationResult{error='" + errorMessage + "'}";
        }
        return "ValidationResult{" +
                "success=" + success +
                ", overrideDetected=" + overrideDetected +
                ", violations=" + violations.size() +
                ", totalFiles=" + (classifiedFiles != null ? classifiedFiles.getTotalCount() : 0) +
                '}';
    }
}
