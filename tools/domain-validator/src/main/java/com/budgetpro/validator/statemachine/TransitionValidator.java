package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.config.StateMachineConfig;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Valida transiciones de estado contra reglas configuradas.
 */
public class TransitionValidator {

    /**
     * Valida una lista de asignaciones de estado contra las reglas configuradas.
     */
    public List<TransitionViolation> validate(List<StateAssignment> assignments, StateMachineConfig config) {
        List<TransitionViolation> violations = new ArrayList<>();

        if (assignments == null || config == null) {
            return violations;
        }

        for (StateAssignment assignment : assignments) {
            String className = assignment.getClassName();
            String fromState = assignment.getFromState();
            String toState = assignment.getToState();

            // Buscar definición para la clase
            StateMachineConfig.StateMachineDefinition def = config.findDefinitionForClass(className);

            if (def == null) {
                // Si no hay configuración para esta clase, no podemos validar transiciones
                // específicas
                // pero podríamos validar que el estado existe si tuviéramos acceso a los enums
                // detectados
                continue;
            }

            // 1. Caso especial: estados terminales no permiten transiciones
            if (fromState != null && def.isTerminalState(fromState)) {
                violations.add(createFinalStateViolation(assignment, fromState));
                continue;
            }

            // 2. Si tenemos estado origen, validar transición
            if (fromState != null) {
                if (!def.isValidTransition(fromState, toState)) {
                    List<String> validTransitions = def.getTransitions().getOrDefault(fromState, List.of());
                    violations.add(createInvalidTransitionViolation(assignment, fromState, toState, validTransitions));
                }
            } else {
                // 3. Estado origen no determinable - falta validación semántica
                violations.add(createMissingValidationViolation(assignment));
            }
        }

        return violations;
    }

    private TransitionViolation createFinalStateViolation(StateAssignment assignment, String finalState) {
        String message = String.format("Transición inválida: '%s' es un estado terminal y no permite transiciones. "
                + "Intento de transición a '%s'.", finalState, assignment.getToState());

        return new TransitionViolation(ViolationSeverity.CRITICAL, assignment.getFilePath(), assignment.getLineNumber(),
                finalState, assignment.getToState(), List.of(), TransitionViolation.ViolationType.INVALID_TRANSITION,
                message, assignment.getClassName(), assignment.getMethodName());
    }

    private TransitionViolation createInvalidTransitionViolation(StateAssignment assignment, String fromState,
            String toState, List<String> validTransitions) {
        String validTransitionsStr = validTransitions.isEmpty() ? "(ninguna)" : String.join(", ", validTransitions);

        String message = String.format("Transición inválida detectada: '%s' → '%s'. " + "Se esperaba una de: [%s]",
                fromState, toState, validTransitionsStr);

        return new TransitionViolation(ViolationSeverity.CRITICAL, assignment.getFilePath(), assignment.getLineNumber(),
                fromState, toState, validTransitions, TransitionViolation.ViolationType.INVALID_TRANSITION, message,
                assignment.getClassName(), assignment.getMethodName());
    }

    private TransitionViolation createMissingValidationViolation(StateAssignment assignment) {
        String message = String.format(
                "Falta validación de estado: se detectó una transición a '%s' sin verificar el estado previo. "
                        + "Debe envolver la transición en un bloque 'if' o 'switch' que valide el estado actual.",
                assignment.getToState());

        return new TransitionViolation(ViolationSeverity.WARNING, assignment.getFilePath(), assignment.getLineNumber(),
                null, assignment.getToState(), null, TransitionViolation.ViolationType.MISSING_VALIDATION, message,
                assignment.getClassName(), assignment.getMethodName());
    }
}
