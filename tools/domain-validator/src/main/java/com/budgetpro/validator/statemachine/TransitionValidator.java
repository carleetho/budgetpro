package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.analyzer.StateMachineConfig;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Valida transiciones de estado contra reglas configuradas.
 * 
 * Detecta:
 * - Transiciones inválidas (ERROR): estados destino no permitidos desde el estado origen
 * - Falta de validación (WARNING): estado origen no determinable, sugiere falta de lógica de validación
 * - Estados no existentes (ERROR): estado destino no definido en el enum
 * - Estados finales: transiciones desde estados finales (ej: CONGELADO) siempre son ERROR
 */
public class TransitionValidator {
    
    /**
     * Valida una lista de asignaciones de estado contra las reglas configuradas.
     * 
     * @param assignments Lista de asignaciones de estado detectadas
     * @param config Configuración de máquinas de estado con transiciones válidas
     * @return Lista de violaciones detectadas
     */
    public List<TransitionViolation> validate(List<StateAssignment> assignments, StateMachineConfig config) {
        List<TransitionViolation> violations = new ArrayList<>();
        
        if (assignments == null || config == null) {
            return violations;
        }
        
        for (StateAssignment assignment : assignments) {
            // Verificar si el estado destino existe en el enum
            if (!assignment.isValid()) {
                violations.add(createNonExistentStateViolation(assignment));
                continue;
            }
            
            String className = assignment.getClassName();
            String fromState = assignment.getFromState();
            String toState = assignment.getToState();
            
            // Caso especial: estados finales no permiten transiciones
            if (fromState != null && config.isFinalState(className, fromState)) {
                violations.add(createFinalStateViolation(assignment, fromState));
                continue;
            }
            
            // Si tenemos estado origen, validar transición
            if (fromState != null) {
                if (!config.isValidTransition(className, fromState, toState)) {
                    List<String> validTransitions = config.getAllowedTransitions(className, fromState);
                    violations.add(createInvalidTransitionViolation(assignment, fromState, toState, validTransitions));
                }
                // Si la transición es válida, no hay violación
            } else {
                // Estado origen no determinable - falta validación
                violations.add(createMissingValidationViolation(assignment));
            }
        }
        
        return violations;
    }
    
    /**
     * Crea una violación para un estado no existente en el enum.
     */
    private TransitionViolation createNonExistentStateViolation(StateAssignment assignment) {
        String message = String.format(
            "Estado '%s' no existe en el enum. Verifique que el estado esté definido correctamente.",
            assignment.getToState()
        );
        
        return new TransitionViolation(
            ViolationSeverity.CRITICAL,
            assignment.getFilePath(),
            assignment.getLineNumber(),
            assignment.getFromState(),
            assignment.getToState(),
            null,
            TransitionViolation.ViolationType.NON_EXISTENT_STATE,
            message,
            assignment.getClassName(),
            assignment.getMethodName()
        );
    }
    
    /**
     * Crea una violación para transición desde estado final.
     */
    private TransitionViolation createFinalStateViolation(StateAssignment assignment, String finalState) {
        String message = String.format(
            "Transición inválida: '%s' es un estado final y no permite transiciones. " +
            "Intento de transición a '%s'.",
            finalState,
            assignment.getToState()
        );
        
        return new TransitionViolation(
            ViolationSeverity.CRITICAL,
            assignment.getFilePath(),
            assignment.getLineNumber(),
            finalState,
            assignment.getToState(),
            List.of(), // No hay transiciones válidas desde estados finales
            TransitionViolation.ViolationType.INVALID_TRANSITION,
            message,
            assignment.getClassName(),
            assignment.getMethodName()
        );
    }
    
    /**
     * Crea una violación para transición inválida.
     */
    private TransitionViolation createInvalidTransitionViolation(StateAssignment assignment,
                                                                  String fromState,
                                                                  String toState,
                                                                  List<String> validTransitions) {
        String validTransitionsStr = validTransitions.isEmpty() 
            ? "(ninguna)" 
            : validTransitions.stream().collect(Collectors.joining(", "));
        
        String message = String.format(
            "Transición inválida: '%s' → '%s' no está permitida. " +
            "Transiciones válidas desde '%s': %s",
            fromState,
            toState,
            fromState,
            validTransitionsStr
        );
        
        return new TransitionViolation(
            ViolationSeverity.CRITICAL,
            assignment.getFilePath(),
            assignment.getLineNumber(),
            fromState,
            toState,
            validTransitions,
            TransitionViolation.ViolationType.INVALID_TRANSITION,
            message,
            assignment.getClassName(),
            assignment.getMethodName()
        );
    }
    
    /**
     * Crea una violación para falta de validación (estado origen no determinable).
     */
    private TransitionViolation createMissingValidationViolation(StateAssignment assignment) {
        String message = String.format(
            "Falta lógica de validación: no se pudo determinar el estado origen antes de " +
            "transicionar a '%s'. Se recomienda agregar validación condicional (if/switch) " +
            "que verifique el estado actual antes de cambiar el estado.",
            assignment.getToState()
        );
        
        return new TransitionViolation(
            ViolationSeverity.WARNING,
            assignment.getFilePath(),
            assignment.getLineNumber(),
            null,
            assignment.getToState(),
            null,
            TransitionViolation.ViolationType.MISSING_VALIDATION,
            message,
            assignment.getClassName(),
            assignment.getMethodName()
        );
    }
}
