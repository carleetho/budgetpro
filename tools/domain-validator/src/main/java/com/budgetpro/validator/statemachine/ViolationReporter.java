package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reporta las violaciones de transiciones de estado en un formato legible y
 * accionable.
 */
public class ViolationReporter {

    /**
     * Genera un reporte detallado de las violaciones encontradas.
     * 
     * @param violations Lista de violaciones detectadas
     */
    public void report(List<TransitionViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            System.out.println("\n✅ State machine validation passed. No violations found.");
            return;
        }

        // Agrupar por archivo
        Map<String, List<TransitionViolation>> groupedViolations = violations.stream()
                .collect(Collectors.groupingBy(TransitionViolation::getFilePath));

        // Ordenar archivos alfabéticamente
        List<String> sortedFiles = groupedViolations.keySet().stream().sorted().collect(Collectors.toList());

        System.out.println("\n🔍 State Machine Validation Report:");

        for (String filePath : sortedFiles) {
            System.out.println("\nFile: " + filePath);
            List<TransitionViolation> fileViolations = groupedViolations.get(filePath).stream()
                    .sorted(Comparator.comparingInt(TransitionViolation::getLineNumber)).collect(Collectors.toList());

            for (TransitionViolation v : fileViolations) {
                if (v.getSeverity() == ViolationSeverity.CRITICAL) {
                    reportError(v);
                } else {
                    reportWarning(v);
                }
            }
        }

        reportSummary(violations);
    }

    private void reportError(TransitionViolation v) {
        System.out.println(String.format("  ❌ ERROR: Invalid state transition in %s:%d",
                getSimpleFileName(v.getFilePath()), v.getLineNumber()));
        System.out.println(String.format("    Attempted: %s → %s",
                v.getFromState() != null ? v.getFromState() : "UNKNOWN", v.getToState()));

        String validTransitions = (v.getValidTransitions() == null || v.getValidTransitions().isEmpty())
                ? "(none - final state)"
                : String.join(", ", v.getValidTransitions());

        System.out.println(String.format("    Valid transitions from %s: %s",
                v.getFromState() != null ? v.getFromState() : "UNKNOWN", validTransitions));
    }

    private void reportWarning(TransitionViolation v) {
        System.out.println(String.format("  ⚠️ WARNING: State change without validation in %s:%d",
                getSimpleFileName(v.getFilePath()), v.getLineNumber()));
        System.out.println(String.format("    Method '%s' changes state without checking current state",
                v.getMethodName() != null ? v.getMethodName() : "unknown"));
        System.out.println("    Suggestion: Add validation to ensure current state allows this transition");
    }

    private void reportSummary(List<TransitionViolation> violations) {
        long errorCount = violations.stream().filter(v -> v.getSeverity() == ViolationSeverity.CRITICAL).count();
        long warningCount = violations.size() - errorCount;

        System.out.println(String.format("\nFound %d violations (%d errors, %d warnings)", violations.size(),
                errorCount, warningCount));
    }

    private String getSimpleFileName(String filePath) {
        if (filePath == null)
            return "unknown";
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash == -1 ? filePath : filePath.substring(lastSlash + 1);
    }
}
