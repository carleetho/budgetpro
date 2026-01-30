package com.budgetpro.tools.naming.engine;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contenedor de los resultados de una ejecución de validación.
 */
public class ValidationResult {

    private final List<NamingViolation> allViolations;

    public ValidationResult(List<NamingViolation> allViolations) {
        this.allViolations = List.copyOf(allViolations);
    }

    public List<NamingViolation> getAllViolations() {
        return allViolations;
    }

    public List<NamingViolation> getBlockingViolations() {
        return allViolations.stream().filter(v -> v.severity() == ViolationSeverity.BLOCKING)
                .collect(Collectors.toList());
    }

    public List<NamingViolation> getWarningViolations() {
        return allViolations.stream().filter(v -> v.severity() == ViolationSeverity.WARNING)
                .collect(Collectors.toList());
    }

    public boolean hasBlockingViolations() {
        return allViolations.stream().anyMatch(v -> v.severity() == ViolationSeverity.BLOCKING);
    }

    public int getTotalViolationCount() {
        return allViolations.size();
    }
}
